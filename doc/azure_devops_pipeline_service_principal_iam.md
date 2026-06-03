# Grant pipeline access to Container Apps (step-by-step)

Your Azure DevOps pipeline uses a **service connection** named **`HAGS-ACR-hagsreg`**.  
Behind that is an **Azure AD service principal** (an app identity, not your personal login).

- **You** in the Portal → can see `hags-customer-api` in `rg-hags-prod`.
- **The pipeline** → uses the service principal → may get **403 Forbidden** until you grant a role on the resource group.

This guide adds **Contributor** on **`rg-hags-prod`** so deploy can run `az containerapp update`.

---

## Part A — Find the service principal (Azure DevOps)

1. Open **Azure DevOps**: `https://dev.azure.com/hags-pinki` (or your org URL).
2. Open the project that runs **HAG_CUST_APIS** (e.g. **HAGS**).
3. Bottom-left **Project settings** (gear icon).
4. Under **Pipelines**, click **Service connections**.
5. Click **`HAGS-ACR-hagsreg`**.
6. Top-right, click **Manage Service Principal** (or **View service principal**).  
   - A new browser tab opens in **Microsoft Entra ID** (Azure AD) showing the app registration / enterprise application.

**Write down these (you need them in Part B):**

| Field | Example | Where |
|-------|---------|--------|
| **Display name** | `HAGS-ACR-hagsreg` or similar | Overview |
| **Application (client) ID** | GUID | Overview |
| **Object ID** | GUID (different from client ID) | Overview — use this for IAM |

If **Manage Service Principal** is missing:

- On the service connection page, open **Manage Azure Active Directory app** / **App registration**.
- Or: **Azure Portal** → **Microsoft Entra ID** → **Enterprise applications** → search **`HAGS`** or the connection name.

---

## Part B — Grant Contributor on `rg-hags-prod` (Azure Portal)

You must be **Owner** or **User Access Administrator** on the subscription or resource group (your personal account).

### B1. Open the resource group

1. [https://portal.azure.com](https://portal.azure.com)
2. Search bar: **`rg-hags-prod`**
3. Click the resource group **rg-hags-prod** (subscription: **Azure subscription 1**).

### B2. Add role assignment

1. Left menu → **Access control (IAM)**.
2. Tab **Role assignments** → confirm your pipeline SP is **not** already **Contributor** (optional).
3. Click **+ Add** → **Add role assignment**.

### B3. Role tab

1. **Role** tab.
2. Search: **`Contributor`** (recommended — covers Container Apps, ACR push context, etc.).  
   - Narrower alternative: **`Container Apps Contributor`** (only Container Apps; may be enough for deploy only).
3. Select **Contributor** → **Next**.

### B4. Members tab

1. **Members** tab.
2. **Assign access to:** **User, group, or service principal** (NOT “Managed identity” unless you know the MI name).
3. Click **+ Select members**.
4. Search box: paste the **display name** from Part A, or the **Application (client) ID**.
   - Pick the entry type **Application** / service principal (not a user with a similar name).
5. **Select** → **Next**.

### B5. Review + assign

1. **Review + assign**.
2. Wait until **Assignment added successfully** (can take 1–5 minutes to apply).

### B6. Verify

1. **Access control (IAM)** → **Role assignments**.
2. Filter **Role** = `Contributor` (or search the SP name).
3. You should see your service principal scoped to **rg-hags-prod**.

---

## Part C — Re-run the pipeline

1. Azure DevOps → **Pipelines** → your pipeline → **Run pipeline** (branch **master**).
2. Open the **Deploy** stage → **Deploy image to Container App**.
3. Success looks like: `Deployed. URL: https://...`
4. Failure with **403** → wait 5–10 minutes for IAM propagation and retry, or confirm you assigned on **rg-hags-prod** (not only on Key Vault or ACR).

---

## Part D — Push latest `azure-pipelines.yml` (if you still see old errors)

If the log still says **“was not found”** and mentions **“does NOT run az containerapp create”**, Azure DevOps is using **old YAML**.

1. Commit and push `azure-pipelines.yml` from your repo to **master** (GitHub and/or Azure DevOps repo).
2. Run the pipeline again on the commit that contains the new deploy script.

---

## Troubleshooting

| Symptom | What to check |
|---------|----------------|
| Cannot find SP in “Select members” | Use **client ID** in search; check **Enterprise applications** in Entra ID. |
| “You do not have authorization to assign roles” | Ask subscription admin to assign Contributor for you. |
| Portal shows app, pipeline still fails | Wrong RG in YAML (`resourceGroup: rg-hags-prod`); wrong subscription on service connection. |
| Deploy OK but app **crashing** | Not IAM — fix **env vars** and **Azure SQL** (revision logs). |
| Only Key Vault worked before | Expected — KV uses different RBAC; Container Apps needs RG-level role. |

---

## What each identity can do (summary)

| Identity | Typical access today | Needs for deploy |
|--------|----------------------|------------------|
| You (apinki@gmail.com) | See/edit in Portal | — |
| Pipeline SP `HAGS-ACR-hagsreg` | ACR push, Key Vault read | **Contributor** on `rg-hags-prod` |
| `hags-ca-mi` | Pull images from ACR | Separate — runtime of the app, not the pipeline |

---

## Optional: assign at subscription level (not recommended)

Some teams assign **Contributor** on the whole subscription. That works but is broader than needed. Prefer **resource group `rg-hags-prod` only**.
