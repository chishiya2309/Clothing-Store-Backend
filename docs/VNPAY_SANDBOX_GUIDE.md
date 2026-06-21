# VNPay Sandbox Guide

## 1. Scope

This guide describes how to run and verify the VNPay sandbox flow for the backend on branch `feat/place-order`.

Covered flow:

- Create a checkout with `paymentMethod=vnpay`.
- Generate a signed VNPay sandbox payment URL.
- Receive browser return at `/api/payments/vnpay/return`.
- Receive VNPay IPN at `/api/payments/vnpay/ipn`.
- Finalize a successful online payment exactly once.

Out of scope:

- Refund API.
- QueryDR/reconciliation API.
- MoMo.
- Frontend implementation.
- Production VNPay credentials.

## 2. Required Environment Variables

Do not commit real credentials. Configure them through environment variables or a local-only runtime profile.

```powershell
$env:VNPAY_ENABLED="true"
$env:VNPAY_TMN_CODE="<sandbox terminal code>"
$env:VNPAY_HASH_SECRET="<sandbox hash secret>"
$env:VNPAY_PAY_URL="https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"
$env:VNPAY_RETURN_URL="https://<public-host>/api/payments/vnpay/return"
```

Optional values normally keep their defaults:

```powershell
$env:VNPAY_VERSION="2.1.0"
$env:VNPAY_COMMAND="pay"
$env:VNPAY_ORDER_TYPE="other"
$env:VNPAY_LOCALE="vn"
$env:VNPAY_CURRENCY="VND"
$env:VNPAY_EXPIRE_MINUTES="15"
```

## 3. Public URL With ngrok

VNPay must reach the callback endpoints from the internet. For local demo, expose the backend port with ngrok:

```powershell
ngrok http 8080
```

Use the HTTPS forwarding URL as `<public-host>`, for example:

```powershell
$env:VNPAY_RETURN_URL="https://example.ngrok-free.app/api/payments/vnpay/return"
```

The IPN endpoint is:

```text
https://example.ngrok-free.app/api/payments/vnpay/ipn
```

If the VNPay sandbox portal asks for an IPN URL, configure the URL above. The backend currently uses `VNPAY_RETURN_URL` when generating the browser return parameter; the IPN URL is registered/configured on the VNPay sandbox side.

## 4. Application Profile

Start the backend with the dev profile and the environment variables above:

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Keep local secrets outside source control. Do not add sandbox credentials to `application-dev.yml`, `application-test.yml`, test classes, docs, or commit messages.

## 5. Checkout TTL And Payment Expiry

Checkout and payment expiration are intentionally aligned:

- Checkout reservation TTL is controlled by `checkout.reservation-ttl-minutes`.
- VNPay URL expiry is controlled by `payment.vnpay.expire-minutes` / `VNPAY_EXPIRE_MINUTES`.
- The generated VNPay expiry is capped by the checkout expiration time.
- The cleanup scheduler is controlled by `checkout.cleanup.enabled` and `checkout.cleanup.fixed-delay-ms`.

For sandbox demos, keep the TTL long enough to complete bank simulator steps, but short enough to test expired-payment behavior.

## 6. Generate A Payment URL

Create or reuse a cart, then call checkout confirmation with `paymentMethod=vnpay`.

Expected backend result:

- A `paymentReference` is created.
- A signed `paymentUrl` points to `sandbox.vnpayment.vn`.
- `vnp_TxnRef` equals the backend payment reference.
- `vnp_Amount` equals order amount multiplied by 100.
- `vnp_SecureHash` exists in the outbound payment URL.

The backend logs the checkout code and payment reference, but does not log the full payment URL or hash secret.

## 7. Browser Return

After the sandbox payment page redirects the user, VNPay calls:

```text
GET /api/payments/vnpay/return
```

The return endpoint verifies signature, terminal code, known payment reference, amount, gateway status, and current attempt status. It returns a DTO suitable for the frontend to show success, failure, pending, expired, or refund-required states.

The return endpoint does not finalize the order. Order finalization is handled by IPN.

## 8. IPN

VNPay server-to-server confirmation calls:

```text
GET /api/payments/vnpay/ipn
```

Expected response shape is VNPay-compatible JSON:

```json
{
  "RspCode": "00",
  "Message": "Confirm Success"
}
```

Common response codes:

| RspCode | Meaning |
| --- | --- |
| `00` | Confirm success |
| `01` | Payment attempt not found |
| `02` | Transaction already processed |
| `04` | Invalid amount |
| `97` | Invalid signature |
| `99` | Unknown error |

## 9. Successful Payment Acceptance Criteria

A valid first successful IPN must:

- Return `RspCode=00`.
- Create exactly one order.
- Create exactly one completed VNPay payment.
- Mark the payment attempt `completed`.
- Mark the checkout session `completed`.
- Consume active inventory reservations.
- Consume active voucher reservation when a voucher was used.
- Remove purchased cart items.
- Publish `OrderCreatedEvent`.

## 10. Duplicate IPN Acceptance Criteria

A duplicate successful IPN for the same `vnp_TxnRef` must be idempotent:

- Sequential duplicate: first call returns `00`, later call returns `02`.
- Concurrent duplicate: only one request creates the order/payment; the other observes terminal state and returns `02`.
- The database must still contain one order and one payment for the checkout.

The transaction service locks the checkout session before locking the payment attempt to make the duplicate path deterministic under concurrent IPN delivery.

## 11. Failed Payment Acceptance Criteria

A valid failed VNPay IPN must:

- Return `RspCode=00` to acknowledge receipt.
- Mark the payment attempt `failed`.
- Store sanitized gateway payload.
- Release active inventory and voucher reservations.
- Mark the checkout `failed` unless it was already completed.
- Not create an order.

## 12. Expired Or Late Paid Callback

If VNPay reports a successful payment after the checkout or attempt has expired or already failed, the backend must:

- Return `RspCode=00` to avoid repeated VNPay retries.
- Mark the attempt `requires_refund`.
- Store a safe refund reason.
- Not create an order.
- Not consume inventory or voucher reservations.

This project does not implement the refund API yet; `requires_refund` is an operational queue state.

## 13. Negative Test Checklist

Verify these cases before demo acceptance:

- Missing required VNPay parameter returns `99` or invalid request handling without crashing.
- Invalid signature returns `97`.
- Wrong `vnp_TmnCode` returns `97`.
- Unknown `vnp_TxnRef` returns `01`.
- Amount mismatch returns `04`.
- Duplicate completed payment returns `02`.
- Gateway failure status is recorded as failed and releases reservations.
- Successful callback after expiry becomes `requires_refund`.

## 14. Safe Logging Checklist

Logs may include:

- `checkoutCode`.
- `paymentReference`.
- VNPay gateway transaction number.
- Response code and transaction status.
- Attempt status.
- Expected amount and VNPay amount for mismatch diagnostics.

Logs must not include:

- `VNPAY_HASH_SECRET`.
- Full `vnp_SecureHash`.
- Full generated payment URL.
- Customer credentials or tokens.

## 15. Automated Test Commands

Run unit tests first:

```powershell
mvn clean test
```

Then run integration tests:

```powershell
mvn clean verify -Pintegration
```

Integration tests use Testcontainers/PostgreSQL, so Docker must be running and accessible.

## 16. Troubleshooting

| Symptom | Likely cause | Check |
| --- | --- | --- |
| VNPay page rejects request | Wrong terminal code, hash secret, amount, or signed params | Recheck env vars and signed URL tests |
| Return works but IPN never arrives | Public IPN URL not configured or ngrok changed | Update VNPay sandbox portal URL |
| `RspCode=97` | Invalid signature or terminal code | Compare `VNPAY_TMN_CODE` and hash secret |
| `RspCode=04` | Amount mismatch | Compare checkout total with `vnp_Amount / 100` |
| `RspCode=02` on retry | Duplicate callback | Expected after a prior successful/terminal callback |
| Build fails before tests | Maven dependency or Docker issue | Verify network/cache and Docker/Testcontainers |

## 17. Manual Sandbox Evidence Template

Use this template when recording a demo run:

```text
Date:
Branch:
Backend URL:
ngrok URL:
VNPay sandbox terminal:
Checkout code:
Payment reference:
Order code:
First IPN RspCode:
Duplicate IPN RspCode:
Return status:
Notes:
```

Do not paste secrets or full secure hashes into the evidence.
