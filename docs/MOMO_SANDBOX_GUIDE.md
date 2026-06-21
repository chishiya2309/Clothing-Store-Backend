# MoMo Sandbox Guide

This guide runs MoMo one-time wallet payment with `requestType=captureWallet` and `autoCapture=true`.

## 1. Start ngrok

```powershell
ngrok http 8080
```

Open the inspector when you need to inspect callbacks:

```text
http://127.0.0.1:4040
```

## 2. Set Environment Variables

Run these commands in the same terminal that starts the backend:

```powershell
$env:MOMO_ENABLED="true"
$env:MOMO_PARTNER_CODE="<sandbox-partner-code>"
$env:MOMO_ACCESS_KEY="<sandbox-access-key>"
$env:MOMO_SECRET_KEY="<sandbox-secret-key>"
$env:MOMO_CREATE_URL="https://test-payment.momo.vn/v2/gateway/api/create"
$env:MOMO_REDIRECT_URL="https://<ngrok-domain>/api/payments/momo/return"
$env:MOMO_IPN_URL="https://<ngrok-domain>/api/payments/momo/ipn"
$env:MOMO_REQUEST_TYPE="captureWallet"
$env:MOMO_LANG="vi"
$env:MOMO_AUTO_CAPTURE="true"
```

Check non-secret values only:

```powershell
echo $env:MOMO_ENABLED
echo $env:MOMO_PARTNER_CODE
echo $env:MOMO_REDIRECT_URL
echo $env:MOMO_IPN_URL
```

Do not print `MOMO_SECRET_KEY`.

## 3. Manual Flow

1. Log in.
2. Add at least one cart item.
3. Ensure the account has a default address.
4. `POST /api/checkouts/confirm` with `paymentMethod=momo`.
5. Read `onlinePayment.paymentUrl` from the response.
6. Open the payment URL in a browser.
7. Pay in the MoMo test environment.
8. Check `GET /api/payments/momo/return`.
9. Check `POST /api/payments/momo/ipn` in the ngrok inspector.
10. Confirm IPN returns HTTP `204 No Content`.
11. Verify the database state.

## 4. Expected Database State After Success

- `PaymentAttempt.status = completed`
- `CheckoutSession.status = completed`
- exactly one `Order`
- exactly one `Payment` with `method=momo` and `status=completed`
- `Payment.transactionId = transId`
- inventory reservation consumed and stock decreased once
- voucher reservation consumed when a voucher is used
- purchased cart item removed

## 5. Current Scope

Implemented:

- MoMo sandbox create payment
- HMAC-SHA256 request/response/IPN verification
- redirect status endpoint
- IPN finalization
- pending/failure/requires-refund handling
- duplicate callback idempotency

Not implemented:

- production refund API
- recurring/tokenized payment
- Pay Later
- ATM/credit card flows
- frontend QR generator
- production deployment hardening

Manual Sandbox status: not verified in this repository run. Automated tests use mocked MoMo HTTP and PostgreSQL integration tests.
