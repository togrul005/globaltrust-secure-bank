# GlobalTrust Secure Bank — Security Prototype

A runnable **Spring Boot 3 + Spring Security** prototype for the *Designing a Secure
Information System Using Formal Security Models* project (case study: **International
Banking System**). It enforces two formal models inside a realistic banking workflow:

| Model | What it secures here |
|-------|----------------------|
| **Clark-Wilson** (1987) | Money movement via **well-formed transactions**, a **maker-checker** approval flow with **separation of duty**, and **mandatory audit logging**. |
| **Chinese Wall / Brewer-Nash** (1989) | A **dynamic conflict-of-interest barrier** on the advisory desk: once an advisor opens one client in a conflict class, competitors in that class become unreachable. |

It satisfies every item in the assignment's **+5 bonus** checklist:

- ✅ **Authentication** — HTTP Basic over a JPA user store, passwords hashed with **BCrypt**
- ✅ **Authorization** — method-level **RBAC** with `@PreAuthorize`
- ✅ **RBAC *and* ABAC** — roles *plus* attributes (account ownership, CoI class, amount)
- ✅ **Audit logging** — append-only `AuditEvent` table + an AOP console trace
- ✅ **A formal model in a realistic workflow** — Clark-Wilson maker-checker (and Chinese Wall)

---

## 1. Tech stack

Java 17 · Spring Boot 3.3.5 · Spring Security 6 · Spring Data JPA · H2 (in-memory) ·
Bean Validation · Spring AOP · springdoc-openapi (Swagger UI).

No database or external service to install — H2 runs in memory and seeds itself on start.

## 2. Run it

```bash
# Prerequisites: JDK 17+ and Maven 3.6+ (or just open the folder in IntelliJ / Eclipse).
mvn spring-boot:run
```

Then open:

- **Swagger UI** (click "Authorize", enter any user below): http://localhost:8080/swagger-ui.html
- **H2 console** (JDBC URL `jdbc:h2:mem:bankdb`, user `sa`, empty password): http://localhost:8080/h2-console

Run the tests (they prove the two key rules):

```bash
mvn test
```

## 3. Seeded users (all share the password `password`)

| Username | Role | Notes |
|----------|------|-------|
| `alice` | CUSTOMER | owns `ACC-1001` (€5,000) |
| `bob` | CUSTOMER | owns `ACC-1002` (€8,000) |
| `tara` | TELLER | can initiate transfers on behalf of customers |
| `victor` | APPROVER | can initiate **and** approve (but never their own) |
| `wendy` | APPROVER | second approver — used to execute victor's transfers |
| `aria` | AUDITOR | read-only access to the audit trail |
| `adam` | ADMIN | manages users; **cannot move money** (separation of duty) |
| `diana` | ADVISOR | investment-advisory desk (Chinese Wall) |
| `trent` | TRADER | trading desk (walled off from advisory endpoints) |

Accounts: `ACC-1001` (alice), `ACC-1002` (bob), `ACC-9000` (operations, €1,000,000).
Companies: **Acme**/**Globex** (CoI class `TECH`), **Initech**/**Umbrella** (CoI class `PHARMA`).

**Threshold:** transfers **≤ €1,000** auto-execute (single control); **> €1,000** require a second approver.

## 4. Where each model lives in the code

**Clark-Wilson**

| Concept | In the code |
|---------|-------------|
| Constrained Data Item (CDI) | `Account.balance`, `Transfer` records |
| Integrity Verification Procedure (IVP) | the validation checks inside `TransferService.createTransfer` / `approveTransfer` (positive amount, account exists, sufficient funds, ownership) |
| Transformation Procedure (TP) | `createTransfer`, `approveTransfer`, `rejectTransfer`, `applyFunds` — the only places a balance changes |
| Access triple *(user, TP, CDI)* | `@PreAuthorize` on each TP + ownership/ABAC checks |
| **Separation of duty** | `initiatedBy != approvedBy` check in `approveTransfer` / `rejectTransfer` → `SoDViolationException` |
| Logging | `AuditService` (append-only `AuditEvent`) + `AuditAspect` (console trace) |

**Chinese Wall (Brewer-Nash)**

| Concept | In the code |
|---------|-------------|
| Objects / company datasets | `Company` |
| Conflict-of-interest class | `Company.coiClass` |
| The "wall" (access history) | `AdvisorWallAccess` (one lock per advisor per class) |
| Enforcement | `AdvisoryService.viewCompany` → `WallViolationException` on a competitor |

## 5. Demo walkthrough (the video script)

Run `./demo.sh` for the whole thing automatically, or follow these steps in Swagger.
Every step below is also in `requests.http` (IntelliJ HTTP client).

**A. Authentication + RBAC**
1. `GET /api/me` as **alice** → shows `ROLE_CUSTOMER`.
2. `GET /api/audit` as **alice** → **403** (customers can't read the audit log).
3. `GET /api/audit` as **aria** → **200** (auditor can).

**B. Clark-Wilson — small transfer auto-executes**
4. As **alice**: `POST /api/transfers` `{ "fromAccount":"ACC-1001","toAccount":"ACC-1002","amount":500,"reason":"rent" }`
   → status `EXECUTED` immediately (≤ €1,000). Balances change.

**C. Clark-Wilson — maker-checker + separation of duty**
5. As **victor**: `POST /api/transfers` `{ "fromAccount":"ACC-9000","toAccount":"ACC-1001","amount":2500,"reason":"ops" }`
   → status `PENDING_APPROVAL` (note the returned `id`).
6. As **victor**: `POST /api/transfers/{id}/approve` → **403 "Separation of Duty Violation"** (maker ≠ checker).
7. As **wendy**: `POST /api/transfers/{id}/approve` → status `EXECUTED`, `approvedBy = wendy`.

**D. Chinese Wall**
8. As **diana**: `GET /api/advisory/companies/1` (Acme, TECH) → **granted**, diana is now walled to Acme.
9. As **diana**: `GET /api/advisory/companies/2` (Globex, TECH competitor) → **403 "Chinese Wall Violation"**.
10. As **diana**: `GET /api/advisory/companies/3` (Initech, PHARMA) → **granted** (different class).

**E. Admin separation of duty**
11. As **adam**: `POST /api/admin/users` → creates a user (admin power).
12. As **adam**: `POST /api/transfers` → **403** (admin has no authority over money).

**F. Audit trail**
13. As **aria**: `GET /api/audit` → the full trail, including the **DENIED** SoD and Chinese-Wall attempts.

## 6. From prototype to production

This prototype keeps the moving parts minimal so the security logic is easy to read.
In production the same design maps onto: **OIDC/JWT via Keycloak or Microsoft Entra ID**
instead of Basic auth; a real encrypted datastore with keys in an **HSM**; an immutable,
externalised **audit sink / SIEM**; and **mTLS + network segmentation** between the trust
zones shown in the architecture diagram. The access model (RBAC + ABAC), the Clark-Wilson
TPs/SoD and the Chinese Wall logic are unchanged.

## 7. Project layout

```
src/main/java/com/globaltrust/bank
├── domain      JPA entities + enums (AppUser, Account, Transfer, Company, AuditEvent, ...)
├── repo        Spring Data repositories
├── security    JpaUserDetailsService, SecurityUtils
├── config      SecurityConfig, OpenApiConfig, DataSeeder
├── audit       AuditService (persistent) + AuditAspect (AOP trace)
├── service     AccountService, TransferService (Clark-Wilson), AdvisoryService (Chinese Wall), AdminService
├── web         REST controllers + ApiExceptionHandler
└── dto         request/response records
```
