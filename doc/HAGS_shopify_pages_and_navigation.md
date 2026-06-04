# HAGS Shopify — Pages, Menus & Navigation Setup

Step-by-step instructions to implement [HAGS_website_trust_and_content_plan.md](./HAGS_website_trust_and_content_plan.md) in **Shopify Admin**, using your live theme: **Horizon**.

**Theme:** Horizon (Shopify Online Store 2.0)  
**Editor:** **Online Store → Themes → Customize** (homepage = `templates/index.json`)

**Admin paths used below**

| Task | Path |
| ---- | ---- |
| Create pages | **Online Store → Pages** |
| Menus | **Online Store → Navigation** |
| Homepage | **Online Store → Themes → Customize** (Homepage) |
| Legal policies | **Settings → Policies** (and/or Pages) |
| Theme language | **Online Store → Themes → … → Edit default theme content** |
| SEO per page | Each page → **Search engine listing** |

---

## 0. Your store today (homepage snapshot)

Based on your **current live homepage** (charcoal + gold hero, cauldron brand mark, card pile image below):

| Area | What you have now | Gap vs trust plan | Priority fix |
| ---- | ----------------- | ----------------- | ------------ |
| **Announcement bar** | “Welcome to our store” (generic) | No trust or inclusion message | Replace with inclusive welcome or service note (§6.1) |
| **Header menu** | Home · Submissions · Memberships · Contact · About us | Missing How It Works, Standards, FAQ, Gallery, trust pages | Phase menu (§3) — keep **Submissions** & **Memberships** |
| **Hero headline** | Large gold **HAGS** + illustration | Good brand anchor; plan also wants clear “what we do” for new visitors | Keep logo; add/adjust sub-headline (§6.2) |
| **Hero sub-copy** | “Professional Card Grading by Collectors, for Collectors” + family-run / certificate line | Aligns well — **card grading** is clearer than generic “collectors” in plan | Minor SEO tweak only |
| **Hero CTAs** | **None visible** | Plan needs **Submit** + **Learn How Grading Works** | Add 2 buttons in hero section (§6.2) — highest impact |
| **Below hero** | Full-width **trading card** photo (Pokémon etc.) | Strong visual; needs trust section + steps on top or below | Add sections under hero (§6.3) |
| **Accessibility** | Gold text on charcoal | Check contrast (§8); don’t rely on gold alone for critical info | Verify WCAG; add white/cream body text if needed |
| **Inclusion** | Not visible on homepage yet | Footer trust line + `/pages/inclusion` | After first pages exist |

**Brand to preserve:** charcoal background, gold typography, cauldron/hag line art — implement trust content **around** this look, don’t replace it with a generic white hero unless you choose a rebrand.

**Product focus:** copy should say **trading cards** (TCG/sports as you accept) — your hero already does; trust pages should match (“cards” not vague “items” unless you grade more categories).

---

## 1. Before you start

1. Note your **primary domain** — URLs below use `/pages/...`.
2. **Duplicate the theme** before code edits: **Themes → … → Duplicate** (Horizon updates can overwrite `sections/*.liquid` if you edit vendor files — prefer Theme Editor + **Pages** for trust content).
3. Use the default **`page`** template for trust pages (**Customize → Pages → Default page** in Horizon shows which sections appear — usually title + page content).
4. **Primary CTA** = your existing **Submissions** collection URL (same everywhere: hero buttons, menus, end of trust pages).
5. Horizon docs (navigation, header): [Horizon — Navigation](https://shopify-horizon.mintlify.app/features/navigation), [Horizon — Header](https://shopify-horizon.mintlify.app/features/header).

---

## 2. Page inventory (create these in Shopify)

Create each row in **Online Store → Pages → Add page**.

| # | Page title (Shopify) | URL handle | Visibility | Template suggestion |
| --- | -------------------- | ---------- | ---------- | ------------------- |
| — | *(Homepage — not a Page)* | `/` | — | Theme: **Homepage** |
| 1 | How Grading Works | `how-grading-works` | Visible | `page` (default) |
| 2 | Grading Standards | `grading-standards` | Visible | `page` |
| 3 | Submission Guidelines | `submission-guidelines` | Visible | `page` |
| 4 | Shipping & Turnaround Times | `shipping-turnaround` | Visible | `page` |
| 5 | Secure Handling & Customer Protection | `secure-handling` | Visible | `page` |
| 6 | Grade Review & Customer Support | `grade-review-support` | Visible | `page` |
| 7 | Why Trust HAGS? | `why-trust-hags` | Visible | `page` |
| 8 | About Us | `about-us` | Visible | `page` |
| 9 | FAQ | `faq` | Visible | `page` or `page.faq` if theme supports accordions |
| 10 | Grading Gallery | `grading-gallery` | Visible | `page` (+ image sections in theme editor if available) |
| 11 | Accessibility Statement | `accessibility` | Visible | `page` |
| 12 | Inclusion & Equal Treatment | `inclusion` | Visible | `page` |
| — | Contact | `contact` | Visible | `page.contact` if theme has it, else `page` |
| — | Submit Your Item | `submit` | Visible | `page` or link to **Catalog → product/collection** |

**Shopify legal policies** (footer — see §5): use **Settings → Policies** for Privacy, Terms, Refund. Add **Submission Agreement** as a **Page** if it is longer than a standard policy template.

**Per-page settings (every page)**

- **Search engine listing**: unique meta title + description (include “HAGS”, topic, UK spelling if applicable).
- **Visibility**: Visible (unless draft).
- One **H1** = page title in Shopify (do not add a second H1 in the body).
- Body headings: use **Heading 2** / **Heading 3** only (no skipped levels).

---

## 3. Main navigation menu

**Online Store → Navigation** — edit the menu your header already uses (likely **Main menu**).

### 3a. Current menu (keep what works)

You already have:

| Label | Typical link | Action |
| ----- | ------------ | ------ |
| Home | `/` | Keep |
| Submissions | Collection or `/collections/submissions` | **Keep label** — this is your primary CTA path; same as plan’s “Submit Your Item” |
| Memberships | Membership collection/page | Keep if you sell memberships |
| Contact | `/pages/contact` or Shopify contact | Keep |
| About us | `/pages/about-us` | Keep — expand page content per §7 |

### 3b. Phase 1 menu (minimal change — fits ~5–7 items)

Add trust pages **without** crowding the header. Suggested order:

```
Home                 →  /
Submissions          →  <your existing submissions URL>   ← primary CTA (unchanged label)
How Grading Works    →  /pages/how-grading-works         ← NEW
Memberships          →  <existing>
About us             →  /pages/about-us
FAQ                  →  /pages/faq                        ← NEW
Contact              →  /pages/contact
```

Put **Grading Standards**, **Submission Guidelines**, **Shipping**, **Gallery**, and **Why Trust HAGS** in the **footer first** (§4) until you want dropdowns.

### 3c. Phase 2 menu (full trust structure — optional dropdowns)

When pages exist and you want fewer footer-only links:

```
Home
Submissions          →  <submissions collection>
Memberships          →  <memberships>
How It Works         →  /pages/how-grading-works
  └ Grading Standards      →  /pages/grading-standards
  └ Submission Guidelines  →  /pages/submission-guidelines
  └ Shipping & Turnaround    →  /pages/shipping-turnaround
Why Trust HAGS       →  /pages/why-trust-hags
  └ Secure Handling        →  /pages/secure-handling
  └ Grade Review & Support →  /pages/grade-review-support
Gallery              →  /pages/grading-gallery
About us             →  /pages/about-us
FAQ                  →  /pages/faq
Contact              →  /pages/contact
```

**Notes**

- **Submissions** = your shop wording; no need to rename to “Submit Your Item” in the menu if customers already understand it.
- **How It Works** / **Why Trust HAGS** parents: if Shopify forces a URL, link parent to the first child page.
- Search, account, cart icons: leave as theme defaults (right side of header).

**Assign menu to header (Horizon)**

**Customize → Header** section:

| Setting | Recommendation for HAGS |
| ------- | ------------------------ |
| **Menu** | Your main menu (Phase 1 or 2 above) |
| **Menu style** | **Text** for trust links (simple dropdowns). Use **Collection images** only on a parent that points at **Submissions** if you want product imagery in the mega menu — not required. |
| **Logo position** | **Left** (matches your current layout) |
| **Menu position** | **Center** (matches current nav) |
| **Sticky header** | **Always** or **Scroll up** — keeps Submissions reachable |
| **Transparent header** | **Off** on homepage if hero is already full-width charcoal (avoids white flash over hero) |
| **Mobile drawer** | Enable; turn **accordion** on if you add Phase 2 nested links |

Horizon renders nested menu items as **mega menu / submenu** panels — test on mobile after adding children under “How It Works”.

---

## 4. Footer navigation menus

Create **two** menus in **Online Store → Navigation** (easier to maintain than one huge list).

### Menu A: `Footer — Grading & support`

```
How Grading Works       →  /pages/how-grading-works
Grading Standards       →  /pages/grading-standards
Submission Guidelines   →  /pages/submission-guidelines
Shipping & Turnaround   →  /pages/shipping-turnaround
Secure Handling         →  /pages/secure-handling
Grade Review & Support  →  /pages/grade-review-support
FAQ                     →  /pages/faq
Contact                 →  /pages/contact
```

### Menu B: `Footer — Company & legal`

```
About Us                →  /pages/about-us
Why Trust HAGS?         →  /pages/why-trust-hags
Grading Gallery         →  /pages/grading-gallery
Accessibility           →  /pages/accessibility
Inclusion & Equal Treatment → /pages/inclusion
Privacy policy          →  /policies/privacy-policy
Terms of service        →  /policies/terms-of-service
Refund policy           →  /policies/refund-policy
Submission Agreement    →  /pages/submission-agreement  (create page; see §5)
```

**Theme setup (Horizon footer)**

**Customize → Footer** — Horizon uses block-based footer columns:

- Add a **Menu** block → `Footer — Grading & support`
- Add a **Menu** block → `Footer — Company & legal`
- Add **Text** or **Rich text** block below menus for the inclusion line

If the footer only shows one menu today, **Add block → Menu** for the second list.

Add the **inclusion trust line** in a **Text** / **Rich text** block:

> HAGS is committed to an accessible, welcoming experience for every collector — including LGBTQ+ people and disabled users. If you need a reasonable adjustment or have feedback on inclusion, [contact us](/pages/contact).

---

## 5. Policies vs pages (legal)

| Content | Where in Shopify | URL |
| ------- | ---------------- | --- |
| Privacy Policy | **Settings → Policies → Privacy policy** | `/policies/privacy-policy` |
| Terms & Conditions | **Settings → Policies → Terms of service** | `/policies/terms-of-service` |
| Refund Policy | **Settings → Policies → Refund policy** | `/policies/refund-policy` |
| Submission Agreement | **Pages** (long custom terms) | `/pages/submission-agreement` |

After saving policies, enable **“Show policy links in footer”** in theme footer settings if available, **or** add manual links in `Footer — Company & legal` (recommended — you control order and labels).

Paste lawyer-reviewed text into policies; use the trust plan only as a **content outline**, not legal final copy.

---

## 6. Homepage — Horizon theme editor

**Customize → Home page** (template `index.json`). Work **inside your existing hero** (charcoal + gold + cauldron) — don’t replace the brand look.

### 6.1 Announcement bar (top strip)

**Customize → Header** (or **Announcement bar** if shown as its own section in your Horizon install):

| Current | Change to (example) |
| ------- | ------------------- |
| Welcome to our store | **Professional card grading — secure handling & digital certificate verification** |

Optional second line (link to `/pages/inclusion`):

> Welcoming all collectors · [Accessibility & inclusion](/pages/inclusion)

Keep text **short**; dark bar + readable contrast (black on white is fine on your screenshot).

### 6.2 Hero section (your existing block — add CTAs)

Select the **first homepage section** (hero / image with text — the charcoal block with **HAGS**, cauldron art, and gold copy).

**Keep**

- Gold **HAGS** wordmark + illustration
- Sub-headline: *Professional Card Grading by Collectors, for Collectors*
- Body: *Family-run grading services… certificate verification*

**Add or edit (Horizon blocks inside hero)**

Use **Add block → Button** (or **Button group**) twice if the section supports multiple buttons:

| Button label | Link |
| ------------ | ---- |
| **Start a submission** (or keep brand tone: **Submit cards**) | Your **Submissions** collection URL (same as nav **Submissions**) |
| **How grading works** | `/pages/how-grading-works` |

Button style in Horizon: use **primary** (gold/filled) for Submissions, **secondary** (outline) for How grading works — check **Theme settings → Colors** so gold buttons meet contrast on charcoal.

**Do not** add a second giant H1 in the hero if “HAGS” is already the visual title — for SEO, set homepage meta title in **Preferences** (§6.5).

### 6.3 Section below hero (card pile image)

Your full-width **trading card** image is a strong visual — keep it.

- Set **Alt text** on the image: e.g. “Assorted graded and raw trading cards including Pokémon TCG”.
- Either:
  - **A)** Add a **Rich text** or **Multicolumn** section **above** that image with “Why submit to HAGS?” bullets, **or**
  - **B)** Add **Image with text** overlay on the next section with short trust copy (if Horizon section supports text on image).

Recommended **new sections** after the card image ( **Add section** — Horizon groups: **Content**, **Media**, **Products**):

| Order | Horizon section (pick closest name) | Content |
| ----- | ----------------------------------- | ------- |
| 3 | **Rich text** or **Multicolumn** | **Why submit to HAGS?** — 8 bullets from trust plan (include accessible site + LGBTQ+ welcome) |
| 4 | **Multicolumn** or **Icon with text** | **How it works** — 5 steps; link **Learn more** → `/pages/how-grading-works` |
| 5 | **Featured collection** | **Submissions** collection (service tiers) |
| 6 | **Collage** / **Image with text** / **Slideshow** | **Graded examples** — alt text per image |
| 7 | **Rich text** | Inclusion line + links to `/pages/accessibility` and `/pages/inclusion` |

Reorder by dragging sections in the left sidebar; **duplicate** a section in Horizon if you want two similar promos without rebuilding.

### 6.4 Horizon global colors (charcoal + gold)

**Customize → Theme settings → Colors** (color schemes):

- Keep **scheme** used by hero (dark background + gold accent).
- Add a **secondary scheme** with **cream/white body text** for long trust sections if gold italic body fails contrast checks ([WebAIM Contrast Checker](https://webaim.org/resources/contrastchecker/)).
- Document any gold-on-charcoal body text that fails **4.5:1** in `/pages/accessibility`.

### 6.5 Homepage SEO

**Settings → Preferences** (store meta) or **Customize → Home page → SEO**:

- **Title:** `HAGS | Professional Trading Card Grading UK` (adjust region)
- **Description:** Family-run card grading, secure handling, digital certificates — welcoming all collectors.

---

## 6H. Horizon — trust pages layout (optional)

For long pages (FAQ, How Grading Works), after creating the **Page** in Admin:

**Customize → Pages → [page] → Change template** if Horizon offers `page` with extra sections.

- Default: **page title + rich text** from Admin is enough for v1.
- v2: **Customize → Pages → Default page** → add **Collapsible content** / **Accordion** blocks for FAQ (Horizon **Content** sections).
- Assign **same color scheme** as site (dark footer / light content — pick one readable pattern).

**Product / collection pages (Submissions, Memberships)**

**Customize → Products → Default product** / **Collections → Default collection**:

- Add a **Rich text** block in the template: links to Submission Guidelines, Shipping, Agreement.
- Horizon allows sections on product/collection templates — use without editing `product.json` in code until needed.

---

## 7. Page-by-page body content (paste into each Page)

Use the headings below in the Shopify page body. Copy full paragraphs from [HAGS_website_trust_and_content_plan.md](./HAGS_website_trust_and_content_plan.md).

### Page: How Grading Works (`how-grading-works`)

- H1 = page title only.
- H2: Step 1 – Submission Received … through Step 7 – Packaging & Return (seven H2 blocks).
- Bottom CTA button (rich text link): **Submit Your Item**.

### Page: Grading Standards (`grading-standards`)

- H2: Our Approach (bullet list of evaluation criteria).
- H2: Consistency Matters.
- H2: Understanding Grades (numbered scale 10 → 1 with descriptions; use HTML table in code view if needed).

### Page: Submission Guidelines (`submission-guidelines`)

- H2: Before You Submit.
- H2: Packaging Recommendations.
- H2: Accepted Items (list categories).
- H2: Non-Accepted Items.

### Page: Shipping & Turnaround Times (`shipping-turnaround`)

- H2: Processing Times — **table** (Service Level | Estimated Turnaround) — replace `XX` with real days from your products.
- H2: Shipping.
- H2: Tracking.
- H2: Important Notes.

**Align table with product variants** (Standard / Priority / Express) in **Products** so site copy matches checkout.

### Page: Secure Handling (`secure-handling`)

- H2: Secure Handling (bullet chain of custody).
- H2: Quality Assurance.

### Page: Grade Review & Customer Support (`grade-review-support`)

- Single H2 or intro + paragraphs from plan (grade review policy tone).

### Page: Why Trust HAGS? (`why-trust-hags`)

- H2: Our Commitment.
- H2: What Sets Us Apart — use H3 subsections: Transparency, Consistency, Security, Customer Support, Continuous Improvement, Accessibility & inclusion, Open to all.

### Page: About Us (`about-us`)

- H2: Our Story.
- H2: Our Mission.
- H2: Our Team (bios; inclusive photos — alt text on each image).
- H2: Diversity, equity & inclusion (LGBTQ+ welcome, accessibility, representation).

### Page: FAQ (`faq`)

**Option A — Horizon (recommended when ready)**  
**Customize → Pages → Create template** (duplicate `page`) → add **Collapsible row** / accordion blocks (Horizon **Content** category), one block per question.

**Option B — Single Page (fastest)**  
**Online Store → Pages → FAQ** — **Heading 2** per question, answer below. Include all questions from the trust plan, especially:

- Accessibility / screen readers → link `/pages/accessibility`
- Reasonable adjustments → contact link
- LGBTQ+ welcome → link `/pages/inclusion`
- Chosen name / pronouns → explain Shopify account name + order notes / contact support (see §9)

### Page: Grading Gallery (`grading-gallery`)

- **v1:** Images + captions in page body.
- **v2 (Horizon):** Custom **page** template with **Collage** or **Slideshow** sections — one block per example, alt text on each media block.
- Each image: descriptive **Alt text**; caption under image: grade, what affected it, what to look for.
- Do not use grade colour alone — add grade number in text.

### Page: Accessibility Statement (`accessibility`)

Include:

- Target: WCAG 2.2 Level AA.
- Measures taken (theme choice, alt text, keyboard, contrast checks).
- Known limitations (third-party apps, PDFs).
- Contact for barriers: email + link to Contact page.
- Last reviewed date (update manually).

### Page: Inclusion & Equal Treatment (`inclusion`)

Include:

- Equal treatment in grading and support.
- LGBTQ+ welcome statement.
- Anti-harassment summary.
- Privacy re sensitive data (link Privacy policy).
- Link Accessibility statement.

### Page: Contact (`contact`)

- Support email, hours, response time.
- Optional: **Contact form** app or theme contact form section pointing to this template.
- Link to FAQ and Grade Review page.

### Page: Submit Your Item (`submit`)

- Short intro + buttons to **grading products** or **cart**.
- Link Submission Guidelines and Submission Agreement.

### Page: Submission Agreement (`submission-agreement`)

- Full terms for submitting items; link from product pages and checkout if possible (**Settings → Checkout → Order status** / product description links).

---

## 8. Shopify accessibility checklist

Do these in Admin and Theme Editor — not only in page copy.

| Item | Action in Shopify |
| ---- | ------------------- |
| Image alt text | **Content → Files** and each **theme image block** — write descriptive alt for gallery and team photos |
| Heading order | Page title = only H1; body uses H2/H3 in order |
| Link text | Menus and buttons: descriptive labels (“Grading standards” not “Learn more” alone on every link) |
| Contrast | **Horizon → Theme settings → Colors** — gold on charcoal hero + announcement bar; fix failing text |
| Focus | Horizon header/menu includes focus states — don’t add custom CSS that removes outlines |
| Motion | **Slideshow** sections: disable autoplay; Horizon respects reduced motion where theme supports it |
| Mega menu | Keyboard: Tab through **Header → menu**; ensure new nested links appear in mobile **drawer** |
| Tables | Shipping page: use `<table>` with `<th>` in HTML mode for screen readers |
| Language | **Settings → Store languages** — set default; `lang` comes from theme |
| Skip link | May require theme code edit or app — note in Accessibility Statement if not yet implemented |
| Video | **Files** or YouTube embed — add transcript link in page body |
| Policies | Readable HTML in policy editor; avoid image-only policy PDFs |

**Apps:** Prefer apps with stated accessibility compliance; each app can affect Lighthouse score — document in Accessibility Statement.

---

## 9. Customers, forms & LGBTQ+ inclusion (Shopify limits)

| Need | Shopify approach |
| ---- | ---------------- |
| Optional pronouns / title | Not native on default customer accounts. Options: (1) **Customer note** at checkout, (2) **metafields** on customer (Shopify Plus or app), (3) custom question via **checkout extensibility** / form app |
| Chosen name | Customer can update **name** in account; state on FAQ that legal name for shipping may be required on label |
| Gender-neutral copy | Edit **theme language** strings (“customer”, “you”) and all **Pages** |
| Checkout fields | **Settings → Checkout** — only collect necessary fields; avoid optional gender fields unless required |
| Harassment / inclusion policy | `/pages/inclusion` + staff training; not a Shopify setting |

Document honest FAQ answers: e.g. “Add pronouns in order notes or email support after purchase” until metafields are configured.

---

## 10. Products & collections (tie-in to menus)

| Shopify object | Purpose | Menu link |
| -------------- | ------- | --------- |
| Collection **Submissions** (your live handle) | Submission SKUs / tiers | Main menu **Submissions** |
| Collection **Memberships** | Membership products | Main menu **Memberships** |
| Products: Standard / Priority / Express | Match Shipping page table | Linked from **Submit** page and homepage |
| Product description | Short trust bullets + links to Guidelines + Agreement |

On each product page, add links:

- `/pages/submission-guidelines`
- `/pages/shipping-turnaround`
- `/pages/submission-agreement`

---

## 11. SEO quick reference

| Page handle | Suggested meta title pattern |
| ----------- | ---------------------------- |
| `how-grading-works` | How Grading Works \| HAGS |
| `grading-standards` | Grading Standards & Scale \| HAGS |
| `submission-guidelines` | Submission Guidelines \| HAGS |
| `shipping-turnaround` | Shipping & Turnaround Times \| HAGS |
| `why-trust-hags` | Why Trust HAGS \| Professional Grading |
| `faq` | FAQ \| HAGS Grading |
| `accessibility` | Accessibility Statement \| HAGS |
| `inclusion` | Inclusion & Equal Treatment \| HAGS |

---

## 12. Launch checklist

- [ ] All pages created with correct **handles** and **Visible**
- [ ] **Main menu** and **Footer** menus created and assigned in theme
- [ ] Homepage sections published with both CTAs working
- [ ] **Policies** filled in Settings → Policies; footer links work
- [ ] **Submission Agreement** page live
- [ ] Shipping table **XX** replaced with real turnaround days
- [ ] Product pages link to guidelines + agreement
- [ ] Alt text on homepage + gallery + About team images
- [ ] FAQ includes accessibility and LGBTQ+ questions
- [ ] `/pages/accessibility` and `/pages/inclusion` linked from footer trust line
- [ ] Horizon **mobile drawer** — nested links expand; Submissions easy to tap
- [ ] Hero **two buttons** live (Submissions + How grading works)
- [ ] Announcement bar updated from generic “Welcome to our store”
- [ ] Test keyboard: Tab through header, main content, footer
- [ ] 60-second test: new visitor can answer success criteria from trust plan

---

## 13. Optional later (Shopify)

| Feature | Shopify path |
| ------- | ------------ |
| Customer testimonials | **Sections** on homepage or **Metaobjects** |
| Blog / grading guides | **Online Store → Blog posts** — add **Blog** to footer menu |
| Submission tracking portal | External app or custom storefront (link from account page) |
| Population / verification DB | App or separate site — footer link when ready |

---

## 14. Quick map: trust plan → Shopify

| Trust plan section | Shopify implementation |
| ------------------ | ------------------------ |
| Homepage | **Horizon** — Customize → Home page (§6) |
| Header / nav | **Horizon** — Header section + Navigation menus (§3, §6H) |
| Pages 1–10 | **Pages** (handles in §2) |
| Footer links | **Navigation** menus + **Policies** |
| Accessibility & inclusion | Pages `accessibility`, `inclusion` + theme/settings §8 |
| Future trust builders | Theme sections, Blog, Apps (§13) |

Source content for wording: [HAGS_website_trust_and_content_plan.md](./HAGS_website_trust_and_content_plan.md).
