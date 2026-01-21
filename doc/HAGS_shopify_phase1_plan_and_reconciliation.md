# Phase 1 (Low-cost MVP) Shopify plan for a UK grading company

## What you’re building in Phase 1
A low-cost launch setup using mostly “off-the-shelf” Shopify functionality + apps:

- **Online store** on Shopify
- **Membership** sold as a **monthly subscription**
- **Submissions** sold as **products** at **3 price points**
- **Photo uploads** (front/back, multiple cards) handled by a **file upload app** (or post-purchase upload flow the app supports)
- **Basic customer account area** (Shopify customer accounts) where customers can see orders/history
- **Basic tracking** using order status / fulfillment status / tags (manual at first)

This avoids custom portal/app development until revenue justifies it.

---

## Estimated monthly running cost (UK)

### 1) Shopify plan
- Typical starting point: **Shopify Basic**  
- Expect: **~£25/month** on monthly billing (annual billing often reduces the effective monthly price)

### 2) Membership / subscriptions app
- If Shopify’s own subscriptions solution fits your store: **often £0 additional app cost** (varies by eligibility/region/features)
- Otherwise, many third‑party subscription apps start around **£0–£15/month** for basic tiers (premium can be higher)

### 3) File upload app (card photos)
- Typical: **~£10–£30/month**

### 4) Payment processing fees (per transaction)
- You’ll pay card processing fees per order (percentage + fixed fee).
- Example mental model: **~2% + ~£0.25** per order on entry plans (exact rate depends on plan and card type).

---

## Realistic Phase 1 total (UK)
- **Lean MVP:** **~£35–£70/month** (Shopify + low-cost subscriptions + upload app)  
- **Plus** per-order card processing fees.

---

# Reconciling Shopify data with your internal database (Azure)

You can reconcile Shopify’s “commerce truth” (customers, orders, subscription contracts) with your internal “grading truth” (submissions, card items, grading status) using a **customer mapping process** + **event-driven sync**.

## Recommended approach: Shopify as system-of-record for commerce
Use Shopify as the source of truth for:
- Customer accounts (identity)
- Orders + line items (what was purchased)
- Subscription contracts (membership status)

Use your internal DB as the source of truth for:
- Submission records
- Card-level details (photos, attributes, grading steps)
- Tracking/status history
- Any operational/admin workflow

### The key design principle
**Never copy everything.** Store *references* to Shopify objects (IDs) and keep your internal domain model focused on grading/submissions.

---

## The mapping identifiers you’ll use

### Shopify identifiers to persist internally
Store these in your internal DB exactly as provided by Shopify:

- **shopify_customer_id** (unique customer)
- **shopify_order_id** (unique order)
- **shopify_order_name** (human-friendly like #1042)
- **shopify_line_item_id** (the purchased submission tier line item)
- **shopify_product_id / variant_id** (which tier)
- **subscription_contract_id** (if using Shopify subscriptions/contracts)
- **shopify_fulfillment_id** (if you use fulfillment states for tracking)

### Your internal identifiers
- **internal_customer_id**
- **submission_id**
- **card_item_id** (each card within a submission)

---

## Customer mapping process (recommended)

### Step 1 — Create/Link customer in your DB
When a customer first interacts (purchase, account creation, or first submission), create a record:

**InternalCustomers**
- internal_customer_id (UUID)
- shopify_customer_id
- email (copied for convenience; treat Shopify as truth)
- created_at / updated_at

**Mapping rule**
- Primary key for mapping is **shopify_customer_id**
- Email is helpful for recovery, but don’t rely on it (emails can change).

### Step 2 — Keep the mapping updated
Subscribe to Shopify events (webhooks) for:
- customer create/update
- order created/paid
- subscription contract created/updated/cancelled

When an event arrives:
- Upsert the customer mapping by **shopify_customer_id**
- Update your cached fields (email, name) if you keep them

---

## Order → Submission creation (the important reconciliation)

### Core idea
Each “Submission” purchase creates an **allowance/entitlement** in your internal DB, which the customer later consumes by uploading multiple card photos.

### A simple internal schema pattern

**PurchaseEntitlements**
- entitlement_id (UUID)
- shopify_order_id
- shopify_line_item_id
- shopify_customer_id
- tier_code (T1/T2/T3)
- cards_allowed (e.g., 10)
- cards_used (starts at 0)
- status (active/consumed/refunded/cancelled)
- created_at

**Submissions**
- submission_id (UUID)
- entitlement_id (FK)
- shopify_customer_id
- submission_status (draft/received/in_grading/completed/shipped)
- created_at / updated_at

**Cards**
- card_item_id (UUID)
- submission_id (FK)
- front_image_url (Azure)
- back_image_url (Azure)
- metadata fields (set, year, notes, etc.)
- status fields (optional)

### When to create the entitlement
Trigger from Shopify via webhook:
- **Order paid** (recommended) OR **payment captured**
Then:
1) Validate the order is paid
2) For each relevant line item (submission tier), create a PurchaseEntitlement
3) Return success

### Idempotency (critical)
Webhooks can be delivered more than once.
Make the write idempotent by using a unique constraint such as:
- (shopify_order_id, shopify_line_item_id) must be unique

If your handler sees it already exists, it should be a no-op.

---

## Photo uploads: keeping Shopify light
For Phase 1, an upload app may store file links with the order.
For Phase 2, or if you want cleaner ops now:
- Upload photos directly to **Azure Blob Storage** via your API
- Store only the Azure URLs/IDs internally
- Optionally write back a reference into Shopify (metafield or order note) for convenience

**Recommended**: Keep the “real” assets and submission logic in your Azure stack.

---

## Handling membership state (Shopify subscriptions)
If membership is a subscription product:
- Use Shopify subscription contract updates to set internal flags like:
  - is_member_active
  - member_since
  - membership_tier

You can keep this as a cached view in your internal DB so your portal/API can respond quickly without constantly querying Shopify.

---

## Reconciliation strategies (pick one)
You can reconcile Shopify ↔ internal DB using one or both of these:

### A) Event-driven (best)
- Webhooks from Shopify are processed by your Azure backend
- Your DB stays current in near real-time

### B) Periodic backfill (safety net)
- A scheduled job (daily/hourly) pulls recent orders/subscription changes and ensures nothing was missed

Most teams do **A + B** for reliability.

---

## Where to store “links back” in Shopify (optional)
If you want Shopify admin to show your internal references:
- Use **order metafields** (submission_id, entitlement_id)
- Use **customer metafields** (internal_customer_id)
- Use **order tags** (Received, In Grading, Completed)

This helps your ops team quickly see the internal state from Shopify admin.

---

## Security & compliance notes (quick but important)
- Don’t expose Shopify secrets in the browser
- Verify webhook signatures in Azure before trusting events
- Keep PII minimal in your internal DB (store only what you need)
- Ensure GDPR-compliant retention and deletion flows

---

## “Day 1” implementation checklist (low cost)
1) Shopify Basic store set up
2) Membership subscription product created
3) 3 submission products/variants created
4) File upload app installed and tested with multiple photos
5) Order tags/status conventions decided (“Received”, “In grading”, “Shipped”)
6) Minimal Azure webhook receiver built for:
   - order paid → create entitlement
   - (optional) subscription state changes → update membership cache
7) A simple internal admin screen (even a spreadsheet view) to track submissions

---

If you want, share your tier definitions (prices + cards allowed + turnaround),
and I can draft a “Phase 1 product setup” that maps tiers cleanly to entitlements.
