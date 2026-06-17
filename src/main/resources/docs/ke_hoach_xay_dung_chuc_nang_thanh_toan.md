# Kế hoạch xây dựng chức năng thanh toán

Tài liệu này mô tả kế hoạch triển khai chức năng **thanh toán trực tuyến và ghi nhận kết quả** theo sơ đồ `SD20_2_ThanhToanTrucTuyen.puml`. Chức năng này tiếp nối luồng đặt hàng ở `SD20_1_XacNhanDatHang.puml`.

Phạm vi triển khai đầu tiên chỉ gồm:

- VNPay.
- MoMo.

ZaloPay để mở rộng sau, chưa đưa vào phạm vi triển khai đầu tiên.

## 1. Mục tiêu

Xây dựng luồng thanh toán online an toàn, idempotent và đúng với SD20_2:

```text
Khách hàng thanh toán tại cổng VNPay/MoMo
-> Cổng thanh toán redirect người dùng về frontend
-> Frontend chỉ gọi API kiểm tra trạng thái
-> Cổng thanh toán gọi callback/IPN server-to-server
-> Backend xác thực chữ ký callback
-> Backend ghi nhận kết quả thanh toán
-> Nếu thành công: tạo Order từ checkout snapshot
-> Nếu thất bại/hủy: giải phóng reservation
-> Nếu đã thanh toán nhưng reservation hết hạn: đánh dấu cần hoàn tiền
```

Nguyên tắc quan trọng:

- Callback/IPN là nguồn kết quả đáng tin cậy.
- Return URL chỉ dùng để hiển thị trạng thái cho khách hàng.
- Không tạo Order trực tiếp từ Return URL.
- Chỉ ACK thành công cho cổng thanh toán sau khi transaction database đã commit.
- Mọi xử lý callback phải idempotent để tránh tạo đơn trùng.

## 2. Phạm vi chức năng

Chức năng thanh toán xử lý các phần sau:

- Tra cứu trạng thái payment attempt theo `paymentReference`.
- Nhận callback/IPN từ VNPay/MoMo.
- Xác thực chữ ký callback bằng Adapter Pattern.
- Lock payment attempt để xử lý nguyên tử.
- Đối chiếu callback với payment attempt.
- Xử lý callback trùng lặp theo idempotency.
- Nếu thanh toán thành công và reservation còn hiệu lực:
  - finalize checkout đã thanh toán.
  - tạo Order từ checkout snapshot.
  - consume stock reservation.
  - consume voucher reservation nếu có.
  - tạo Payment trạng thái `completed`.
  - xóa item đã mua khỏi giỏ.
  - publish event sau khi thanh toán thành công.
- Nếu thanh toán thất bại hoặc khách hủy:
  - release stock reservation.
  - release voucher reservation nếu có.
  - mark payment attempt và checkout failed.
- Nếu đã thanh toán nhưng reservation hết hạn:
  - mark payment attempt `REQUIRES_REFUND`.
  - gọi refund async qua adapter.

## 3. Pattern áp dụng

| Pattern | Thành phần áp dụng | Vai trò |
|---|---|---|
| Facade | `PaymentCallbackFacade.handlePaymentCallback(...)` | Điều phối toàn bộ callback thanh toán |
| Adapter | `PaymentGatewayAdapter`, `VnPayAdapter`, `MomoAdapter` | Xác thực callback, chuẩn hóa phản hồi gateway, gửi refund |
| Reservation | `CheckoutService.finalizePaidCheckout(...)`, `releaseFailedCheckout(...)` | Consume hoặc release reservation sau kết quả thanh toán |
| Observer | `PaymentCompletedEvent`, `PaymentFailedEvent`, `OrderPaidEvent` | Gửi email/thông báo sau khi thanh toán thành công/thất bại |

Ghi chú: `SD20_1_XacNhanDatHang.puml`, `SD20_2_ThanhToanTrucTuyen.puml` và code đều thống nhất dùng `PaymentGatewayAdapterFactory` và `PaymentGatewayAdapter` để thể hiện Adapter Pattern, tránh nhầm với Strategy Pattern.

## 4. Luồng tổng quát theo SD20_2

### 4.1. Return URL về trình duyệt

```text
Gateway -> Frontend: redirectToReturnUrl(paymentReference)
Frontend -> PaymentController: GET /api/payments/{paymentReference}/status
PaymentController -> PaymentService: getPaymentStatus(paymentReference)
PaymentService -> Repository: findPaymentAttempt(paymentReference)
Repository -> Database: selectPaymentAttemptStatus(paymentReference)
PaymentService -> PaymentController: PaymentStatusResponseDTO(paymentStatus)
PaymentController -> Frontend: paymentStatus
Frontend -> Customer: displayPaymentStatus(paymentStatus)
```

Return URL chỉ hiển thị trạng thái. Nếu callback/IPN chưa về kịp, frontend có thể polling API status.

### 4.2. Callback/IPN server-to-server

```text
Gateway -> PaymentController:
POST /api/payments/callback
(paymentReference, transactionId, amount, result, signature)

PaymentController -> PaymentService:
handlePaymentCallback(callbackDTO)

PaymentService -> PaymentGatewayAdapterFactory:
getAdapter(callbackDTO.method)

PaymentService -> PaymentGatewayAdapter:
verifyCallbackSignature(callbackDTO)
```

Sau khi chữ ký hợp lệ:

```text
findPaymentAttempt(paymentReference)
-> lockCheckoutSession(paymentAttempt.checkoutSessionId)
-> lockPaymentAttempt(paymentReference)
-> validateCallbackAgainstPaymentAttempt(callbackDTO, paymentAttempt)
-> nếu callback trùng: ACK idempotent
-> nếu thành công: finalizePaidCheckout(checkoutCode)
-> nếu thất bại/hủy: releaseFailedCheckout(checkoutCode)
-> nếu đã thanh toán nhưng reservation hết hạn: markPaymentRequiresRefund(paymentReference)
```

## 5. API đề xuất

### 5.1. Kiểm tra trạng thái thanh toán

```http
GET /api/payments/{paymentReference}/status
Authorization: Bearer <access_token>
```

Response:

```java
public record PaymentStatusResponseDTO(
        String paymentReference,
        String checkoutCode,
        PaymentAttemptStatus status,
        PaymentMethod method,
        BigDecimal amount,
        String orderCode,
        String message,
        OffsetDateTime updatedAt
) {}
```

### 5.2. Callback/IPN thanh toán

```http
POST /api/payments/callback
Content-Type: application/json
```

Request DTO chung:

```java
public record PaymentCallbackDTO(
        PaymentMethod method,
        String paymentReference,
        String transactionId,
        BigDecimal amount,
        String resultCode,
        String resultMessage,
        String signature,
        Map<String, String> rawParams
) {}
```

Với VNPay/MoMo, có thể dùng DTO riêng nếu payload thực tế khác nhau:

```text
VnPayCallbackDTO
MomoCallbackDTO
```

Sau đó map về `PaymentCallbackDTO` nội bộ để `PaymentService` xử lý thống nhất.

### 5.3. Response ACK cho gateway

Mỗi cổng có format ACK riêng, nên response nên đi qua adapter:

```java
public record GatewayCallbackResponse(
        boolean accepted,
        String responseCode,
        String message
) {}
```

## 6. Cấu trúc package đề xuất

Nguyên tắc tổ chức:

- `controller`, `dto`, `entity`, `repository`, `service` giữ vai trò theo layer chuẩn của Spring Boot.
- Các class thể hiện pattern đặt trong thư mục `pattern`.
- `service` ưu tiên chứa interface hoặc hợp đồng nghiệp vụ.
- Implementation áp dụng pattern đặt trong `pattern/...` để dễ trình bày trong báo cáo.

```text
vn.hcmute.edu.dp.nhom10.backend
├── controller
│   └── PaymentController.java
├── dto
│   ├── request
│   │   ├── PaymentCallbackDTO.java
│   │   ├── VnPayCallbackDTO.java
│   │   └── MomoCallbackDTO.java
│   └── response
│       ├── PaymentStatusResponseDTO.java
│       └── GatewayCallbackResponse.java
├── entity
│   ├── PaymentAttempt.java
│   ├── CheckoutSession.java
│   ├── InventoryReservation.java
│   ├── VoucherReservation.java
│   ├── Order.java
│   └── Payment.java
├── enums
│   ├── PaymentAttemptStatus.java
│   ├── CheckoutSessionStatus.java
│   ├── ReservationStatus.java
│   ├── PaymentMethod.java
│   └── PaymentStatus.java
├── repository
│   ├── PaymentAttemptRepository.java
│   ├── CheckoutSessionRepository.java
│   ├── InventoryReservationRepository.java
│   ├── VoucherReservationRepository.java
│   ├── OrderRepository.java
│   └── PaymentRepository.java
├── service
│   ├── PaymentService.java
│   ├── CheckoutService.java
│   ├── OrderService.java
│   ├── InventoryReservationService.java
│   └── VoucherService.java
├── pattern
│   ├── facade
│   │   └── payment
│   │       └── PaymentCallbackFacade.java
│   ├── adapter
│   │   └── payment
│   │       ├── PaymentGatewayAdapter.java
│   │       ├── PaymentGatewayAdapterFactory.java
│   │       ├── VnPayAdapter.java
│   │       └── MomoAdapter.java
│   ├── reservation
│   │   ├── inventory
│   │   │   └── InventoryReservationServiceImpl.java
│   │   └── voucher
│   │       └── VoucherReservationServiceImpl.java
│   └── observer
│       └── payment
│           ├── event
│           │   ├── PaymentCompletedEvent.java
│           │   ├── PaymentFailedEvent.java
│           │   └── OrderPaidEvent.java
│           └── listener
│               └── PaymentNotificationListener.java
```

Mapping pattern:

| Pattern | Package đề xuất | Class chính |
|---|---|---|
| Facade | `pattern.facade.payment` | `PaymentCallbackFacade` |
| Adapter | `pattern.adapter.payment` | `PaymentGatewayAdapter`, `VnPayAdapter`, `MomoAdapter` |
| Reservation | `pattern.reservation.inventory` | `InventoryReservationServiceImpl` |
| Reservation | `pattern.reservation.voucher` | `VoucherReservationServiceImpl` |
| Observer | `pattern.observer.payment` | `PaymentCompletedEvent`, `PaymentNotificationListener` |

## 7. Database cần bổ sung hoặc dùng lại

Chức năng thanh toán dùng lại các bảng từ chức năng đặt hàng:

- `checkout_sessions`
- `checkout_session_items`
- `inventory_reservations`
- `voucher_reservations`
- `payment_attempts`
- `orders`
- `order_items`
- `payments`

### 7.1. Bảng `payment_attempts`

Đây là bảng trung tâm của SD20_2.

Các cột cần có:

```text
id
payment_reference
checkout_session_id
method
amount
status
payment_url
gateway_transaction_id
gateway_payload
failure_reason
requires_refund_reason
expires_at
completed_at
failed_at
created_at
updated_at
```

Trạng thái đề xuất:

```text
PENDING
COMPLETED
FAILED
EXPIRED
REQUIRES_REFUND
REFUND_REQUESTED
REFUNDED
```

### 7.2. Bảng `payments`

Sau khi callback thành công và `Order` được tạo, insert bản ghi `payments` thật:

```text
order_id
method
amount
status = completed
transaction_id
payment_data
paid_at
```

### 7.3. Idempotency

Cần unique constraint hoặc cơ chế kiểm tra để tránh xử lý trùng:

```text
payment_attempts.payment_reference unique
payments.transaction_id unique nếu gateway transaction id ổn định
orders.order_code unique
```

`payment_attempts.checkout_session_id` là khóa ngoại tới `checkout_sessions.id`. `checkout_code` vẫn được trả ra DTO bằng cách join sang `checkout_sessions`, không dùng làm khóa liên kết nội bộ chính.

Với callback lặp lại, hệ thống không tạo lại `Order`, không trừ tồn kho lần hai và vẫn ACK hợp lệ cho gateway nếu callback đã được xử lý.

## 8. PaymentController

### 8.1. Hàm `getPaymentStatus(paymentReference)`

Tên hàm theo SD20_2:

```java
getPaymentStatus(paymentReference)
```

Endpoint:

```java
@GetMapping("/api/payments/{paymentReference}/status")
public ResponseEntity<ApiResponse<PaymentStatusResponseDTO>> getPaymentStatus(
        @PathVariable String paymentReference
)
```

Luồng:

```text
getPaymentStatus(paymentReference)
-> paymentService.getPaymentStatus(paymentReference)
-> return PaymentStatusResponseDTO
```

### 8.2. Hàm `handlePaymentCallback(callbackDTO)`

Tên hàm theo SD20_2:

```java
handlePaymentCallback(callbackDTO)
```

Endpoint:

```java
@PostMapping("/api/payments/callback")
public ResponseEntity<GatewayCallbackResponse> handlePaymentCallback(
        @RequestBody PaymentCallbackDTO callbackDTO
)
```

Luồng:

```text
handlePaymentCallback(callbackDTO)
-> paymentService.handlePaymentCallback(callbackDTO)
-> return gateway-specific ACK
```

## 9. PaymentService và PaymentCallbackFacade

`PaymentService` là interface nghiệp vụ. `PaymentCallbackFacade` là implementation điều phối callback theo Facade Pattern.

Implementation áp dụng Facade Pattern nên đặt tại:

```text
pattern.facade.payment.PaymentCallbackFacade
```

### 9.1. Interface đề xuất

```java
public interface PaymentService {
    PaymentStatusResponseDTO getPaymentStatus(String paymentReference);
    GatewayCallbackResponse handlePaymentCallback(PaymentCallbackDTO callbackDTO);
    void requestRefundAsync(String transactionId);
}
```

### 9.2. Hàm `getPaymentStatus(paymentReference)`

Tên hàm theo SD20_2:

```java
getPaymentStatus(paymentReference)
```

Luồng:

```text
getPaymentStatus(paymentReference)
-> findPaymentAttempt(paymentReference)
-> selectPaymentAttemptStatus(paymentReference)
-> return PaymentStatusResponseDTO(paymentStatus)
```

Không tạo Order, không consume reservation và không release reservation ở API này.

### 9.3. Hàm `handlePaymentCallback(callbackDTO)`

Tên hàm theo SD20_2:

```java
handlePaymentCallback(callbackDTO)
```

Luồng chi tiết:

```text
1. adapter = PaymentGatewayAdapterFactory.getAdapter(callbackDTO.method)
2. adapter.verifyCallbackSignature(callbackDTO)
3. Nếu chữ ký không hợp lệ:
   3.1. return PaymentCallbackRejected
4. paymentCallbackResult = adapter.parseCallback(callbackDTO)
5. findPaymentAttempt(paymentReference) để lấy checkoutSessionId
6. lockCheckoutSession(checkoutSessionId)
7. lockPaymentAttempt(paymentReference)
8. validateCallbackAgainstPaymentAttempt(paymentCallbackResult, paymentAttempt)
9. Nếu sai merchant/reference/amount:
   9.1. return PaymentCallbackMismatch
10. Nếu callback đã xử lý trước:
   10.1. return idempotentAcknowledgement
11. Nếu thanh toán thành công và reservation còn hiệu lực:
   11.1. finalizePaidCheckout(checkoutCode)
   11.2. return paymentCompletedAcknowledgement
12. Nếu thanh toán thất bại hoặc bị hủy:
   12.1. releaseFailedCheckout(checkoutCode)
   12.2. return paymentFailureAcknowledgement
13. Nếu đã thanh toán nhưng reservation hết hạn:
   13.1. markPaymentRequiresRefund(paymentReference)
   13.2. requestRefundAsync(transactionId)
   13.3. return paymentRecordedAcknowledgement
```

### 9.4. Hàm `verifyCallbackSignature(callbackDTO)`

Tên hàm theo SD20_2:

```java
verifyCallbackSignature(callbackDTO)
```

Hàm này nằm trong `PaymentGatewayAdapter`, không đặt trực tiếp trong `PaymentService`.

```java
public interface PaymentGatewayAdapter {
    PaymentMethod supportMethod();
    String createPaymentUrl(String paymentReference, BigDecimal amount, OffsetDateTime expiresAt);
    boolean verifyCallbackSignature(PaymentCallbackDTO callbackDTO);
    PaymentCallbackResult parseCallback(PaymentCallbackDTO callbackDTO);
    GatewayCallbackResponse buildAcknowledgement(PaymentCallbackResult result);
    GatewayCallbackResponse buildErrorResponse(PaymentCallbackResult result);
    void requestRefund(String transactionId);
}
```

### 9.5. Hàm `lockPaymentAttempt(paymentReference)`

Tên hàm theo SD20_2:

```java
lockPaymentAttempt(paymentReference)
```

Repository cần có method lock:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<PaymentAttempt> findByPaymentReferenceForUpdate(String paymentReference);
```

Hoặc dùng native query:

```sql
SELECT *
FROM payment_attempts
WHERE payment_reference = :paymentReference
FOR UPDATE;
```

Trước khi gọi hàm này, service đọc `PaymentAttempt` theo `paymentReference` để lấy `checkoutSessionId`, sau đó lock `checkout_sessions` trước. Thứ tự này phải thống nhất với finalize/release/cleanup để giảm rủi ro deadlock.

### 9.6. Hàm `validateCallbackAgainstPaymentAttempt(paymentCallbackResult, paymentAttempt)`

Tên hàm theo SD20_2:

```java
validateCallbackAgainstPaymentAttempt(paymentCallbackResult, paymentAttempt)
```

Các điều kiện cần kiểm tra:

- `paymentReference` khớp.
- `method` khớp.
- `amount` khớp.
- `merchantId` hoặc terminal id khớp nếu có.
- `paymentAttempt.status` còn hợp lệ để xử lý.
- `checkoutSessionId` tồn tại và join được sang `checkout_sessions.checkout_code`.

Nếu mismatch, trả lỗi cho gateway và không thay đổi trạng thái checkout.

## 10. PaymentGatewayAdapter - Adapter Pattern

Các adapter thanh toán đặt tại:

```text
pattern.adapter.payment
```

### 10.1. Interface

```java
public interface PaymentGatewayAdapter {
    PaymentMethod supportMethod();
    String createPaymentUrl(String paymentReference, BigDecimal amount, OffsetDateTime expiresAt);
    boolean verifyCallbackSignature(PaymentCallbackDTO callbackDTO);
    PaymentCallbackResult parseCallback(PaymentCallbackDTO callbackDTO);
    GatewayCallbackResponse buildAcknowledgement(PaymentCallbackResult result);
    GatewayCallbackResponse buildErrorResponse(PaymentCallbackResult result);
    void requestRefund(String transactionId);
}
```

### 10.2. `VnPayAdapter`

```java
@Component
public class VnPayAdapter implements PaymentGatewayAdapter {
    @Override
    public PaymentMethod supportMethod() {
        return PaymentMethod.vnpay;
    }

    @Override
    public String createPaymentUrl(String paymentReference, BigDecimal amount, OffsetDateTime expiresAt) {
        // Build VNPay signed URL
    }

    @Override
    public boolean verifyCallbackSignature(PaymentCallbackDTO callbackDTO) {
        // Verify VNPay secure hash
    }

    @Override
    public PaymentCallbackResult parseCallback(PaymentCallbackDTO callbackDTO) {
        // Normalize VNPay callback result
    }

    @Override
    public void requestRefund(String transactionId) {
        // Call VNPay refund API if supported in current scope
    }
}
```

### 10.3. `MomoAdapter`

```java
@Component
public class MomoAdapter implements PaymentGatewayAdapter {
    @Override
    public PaymentMethod supportMethod() {
        return PaymentMethod.momo;
    }

    @Override
    public String createPaymentUrl(String paymentReference, BigDecimal amount, OffsetDateTime expiresAt) {
        // Build MoMo payment URL
    }

    @Override
    public boolean verifyCallbackSignature(PaymentCallbackDTO callbackDTO) {
        // Verify MoMo signature
    }

    @Override
    public PaymentCallbackResult parseCallback(PaymentCallbackDTO callbackDTO) {
        // Normalize MoMo callback result
    }

    @Override
    public void requestRefund(String transactionId) {
        // Call MoMo refund API if supported in current scope
    }
}
```

### 10.4. `PaymentGatewayAdapterFactory`

```java
@Component
public class PaymentGatewayAdapterFactory {
    public PaymentGatewayAdapter getAdapter(PaymentMethod method) {
        // return VnPayAdapter hoặc MomoAdapter
    }
}
```

Nếu `method` không phải `vnpay` hoặc `momo`, trả lỗi `UnsupportedPaymentMethodException`.

## 11. CheckoutService - finalize/release reservation

`CheckoutService` trong SD20_2 không nhận request trực tiếp từ frontend. Nó được `PaymentService` gọi khi callback hợp lệ.

### 11.1. Hàm `finalizePaidCheckout(checkoutCode)`

Tên hàm theo SD20_2:

```java
finalizePaidCheckout(checkoutCode)
```

Luồng:

```text
finalizePaidCheckout(checkoutCode)
-> lockCheckoutAndReservations(checkoutCode)
-> createOrderFromSnapshot(checkoutCode)
-> consumeStockReservation(checkoutCode)
-> consumeVoucherReservation(checkoutCode) nếu có voucher
-> completePaidCheckout(checkoutCode, transactionId)
-> return orderCode
```

Điều kiện:

- Checkout session đang `RESERVED`.
- Checkout chưa hết hạn.
- Payment attempt đang `PENDING`.
- Reservation còn `ACTIVE`.

### 11.2. Hàm `lockCheckoutAndReservations(checkoutCode)`

Tên hàm theo SD20_2:

```java
lockCheckoutAndReservations(checkoutCode)
```

Nhiệm vụ:

- Lock checkout session.
- Lock inventory reservations.
- Lock voucher reservation nếu có.
- Đảm bảo dữ liệu chưa được xử lý trước đó.

Nếu checkout session đã được lock trước đó bởi callback transaction, hàm này tái sử dụng lock hiện có và tiếp tục lock các bảng còn lại theo thứ tự chuẩn.

### 11.3. Hàm `completePaidCheckout(checkoutCode, transactionId)`

Tên hàm theo SD20_2:

```java
completePaidCheckout(checkoutCode, transactionId)
```

Nhiệm vụ:

- Insert `Payment(status=COMPLETED)`.
- Mark payment attempt `COMPLETED`.
- Xóa item đã mua khỏi giỏ.
- Mark checkout session `COMPLETED`.
- Commit transaction.

### 11.4. Hàm `releaseFailedCheckout(checkoutCode)`

Tên hàm theo SD20_2:

```java
releaseFailedCheckout(checkoutCode)
```

Luồng:

```text
releaseFailedCheckout(checkoutCode)
-> releaseStockReservation(checkoutCode)
-> releaseVoucherReservation(checkoutCode) nếu có
-> failPaymentAttemptAndCheckout(checkoutCode)
```

## 12. OrderService - tạo đơn sau thanh toán online

### 12.1. Hàm `createOrderFromSnapshot(checkoutCode)`

Tên hàm theo SD20_2:

```java
createOrderFromSnapshot(checkoutCode)
```

Nhiệm vụ:

- Tạo `Order` từ `checkout_sessions`.
- Tạo `OrderItem` từ `checkout_session_items`.
- Gán `Order.status = pending`.
- Không dùng lại dữ liệu giỏ hàng trực tiếp vì giỏ có thể đã thay đổi sau khi khách chuyển sang cổng thanh toán.

### 12.2. Hàm `saveOrderWithItems(checkoutCode)`

Tên hàm theo SD20_2:

```java
saveOrderWithItems(checkoutCode)
```

Nhiệm vụ:

- Insert `orders`.
- Insert `order_items`.
- Trả `orderCode`.

## 13. InventoryReservationService

### 13.1. Hàm `consumeStockReservation(checkoutCode)`

Tên hàm theo SD20_2:

```java
consumeStockReservation(checkoutCode)
```

Luồng:

```text
consumeStockReservation(checkoutCode)
-> deductStockAndConsumeReservations(checkoutCode)
```

Nhiệm vụ:

- Trừ `product_variants.stock_quantity`.
- Mark inventory reservations `CONSUMED`.
- Không được consume nếu reservation đã `RELEASED`, `EXPIRED` hoặc `CONSUMED`.

### 13.2. Hàm `releaseStockReservation(checkoutCode)`

Tên hàm theo SD20_2:

```java
releaseStockReservation(checkoutCode)
```

Luồng:

```text
releaseStockReservation(checkoutCode)
-> releaseInventoryReservations(checkoutCode)
-> markInventoryReservationsReleased()
```

Không cộng lại tồn kho vì khi reserve chưa trừ tồn kho thật.

## 14. VoucherService

### 14.1. Hàm `consumeVoucherReservation(checkoutCode)`

Tên hàm theo SD20_2:

```java
consumeVoucherReservation(checkoutCode)
```

Luồng:

```text
consumeVoucherReservation(checkoutCode)
-> consumeVoucher(checkoutCode)
-> incrementVoucherUsageAndMarkConsumed()
```

Nhiệm vụ:

- Tăng `vouchers.times_used`.
- Mark voucher reservation `CONSUMED`.

### 14.2. Hàm `releaseVoucherReservation(checkoutCode)`

Tên hàm theo SD20_2:

```java
releaseVoucherReservation(checkoutCode)
```

Luồng:

```text
releaseVoucherReservation(checkoutCode)
-> releaseVoucherReservationByCheckout(checkoutCode)
-> markVoucherReservationReleased()
```

Không giảm `times_used` vì khi reserve chưa tăng `times_used`.

## 15. Observer sau thanh toán

Các class Observer cho thanh toán đặt tại:

```text
pattern.observer.payment
```

### 15.1. Event đề xuất

```java
public class PaymentCompletedEvent {
    private final String paymentReference;
    private final String transactionId;
    private final String orderCode;
    private final Long userId;
}

public class PaymentFailedEvent {
    private final String paymentReference;
    private final String reason;
    private final Long userId;
}

public class OrderPaidEvent {
    private final Long orderId;
    private final String orderCode;
    private final BigDecimal amount;
}
```

### 15.2. Listener đề xuất

```java
@Component
public class PaymentNotificationListener {
    @Async
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        // send payment success email/notification
    }

    @Async
    @EventListener
    public void handlePaymentFailed(PaymentFailedEvent event) {
        // send payment failed notification if needed
    }
}
```

Không gửi email trực tiếp trong transaction callback. Sau khi transaction commit thành công mới nên publish event hoặc dùng `@TransactionalEventListener(phase = AFTER_COMMIT)`.

## 16. Luồng lỗi

### 16.1. Chữ ký callback không hợp lệ

```text
verifyCallbackSignature(callbackDTO)
-> false
-> PaymentCallbackRejected
-> sendGatewayErrorResponse()
```

Không lock checkout, không thay đổi payment attempt.

### 16.2. Sai merchant, reference hoặc số tiền

```text
validateCallbackAgainstPaymentAttempt(callbackDTO, paymentAttempt)
-> PaymentCallbackMismatch
-> sendGatewayErrorResponse()
```

Nên ghi log bảo mật để điều tra.

### 16.3. Callback đã xử lý trước đó

```text
paymentAttempt.status in [COMPLETED, FAILED, REQUIRES_REFUND, REFUNDED]
-> idempotentAcknowledgement
-> sendGatewayAcknowledgement()
```

Không tạo lại Order, không consume/release reservation lần hai.

### 16.4. Thanh toán thành công và reservation còn hiệu lực

```text
finalizePaidCheckout(checkoutCode)
-> createOrderFromSnapshot(checkoutCode)
-> consume reservations
-> insert Payment COMPLETED
-> complete checkout
-> ACK gateway
```

### 16.5. Thanh toán thất bại hoặc bị hủy

```text
releaseFailedCheckout(checkoutCode)
-> release stock reservation
-> release voucher reservation nếu có
-> mark payment attempt FAILED
-> mark checkout FAILED
-> ACK gateway
```

### 16.6. Đã thanh toán nhưng reservation hết hạn

```text
payment success callback
-> checkout expired hoặc reservation expired
-> markPaymentRequiresRefund(paymentReference)
-> requestRefundAsync(transactionId)
-> ACK gateway
```

Trạng thái:

```text
payment_attempts.status = REQUIRES_REFUND
checkout_sessions.status = EXPIRED hoặc FAILED
```

## 17. Refund

### 17.1. Hàm `markPaymentRequiresRefund(paymentReference)`

Tên hàm theo SD20_2:

```java
markPaymentRequiresRefund(paymentReference)
```

Nhiệm vụ:

- Cập nhật payment attempt sang `REQUIRES_REFUND`.
- Lưu `transactionId`.
- Lưu raw callback payload.
- Không tạo Order.

### 17.2. Hàm `requestRefundAsync(transactionId)`

Tên hàm theo SD20_2:

```java
requestRefundAsync(transactionId)
```

Luồng:

```text
requestRefundAsync(transactionId)
-> PaymentGatewayAdapterFactory.getAdapter(method)
-> PaymentGatewayAdapter.requestRefund(transactionId)
-> Gateway.requestRefund(transactionId)
-> update status REFUND_REQUESTED hoặc REFUNDED tùy response
```

Nếu giai đoạn đầu chưa làm refund tự động, vẫn cần lưu trạng thái `REQUIRES_REFUND` để admin xử lý thủ công.

## 18. Mapping trạng thái

### 18.1. Callback thành công

```text
payment_attempts.status = COMPLETED
checkout_sessions.status = COMPLETED
inventory_reservations.status = CONSUMED
voucher_reservations.status = CONSUMED nếu có
orders.status = pending
payments.status = completed
```

### 18.2. Callback thất bại/hủy

```text
payment_attempts.status = FAILED
checkout_sessions.status = FAILED hoặc RELEASED
inventory_reservations.status = RELEASED
voucher_reservations.status = RELEASED nếu có
orders không được tạo
payments không được tạo
```

### 18.3. Callback trùng

```text
Không thay đổi dữ liệu
Trả ACK cho gateway nếu callback hợp lệ và đã xử lý trước đó
```

### 18.4. Reservation hết hạn nhưng gateway báo đã thanh toán

```text
payment_attempts.status = REQUIRES_REFUND
orders không được tạo
payments có thể chưa tạo, hoặc tạo bản ghi audit riêng nếu cần
```

## 19. Transaction boundary

Callback thành công cần xử lý trong transaction ngắn và rõ ràng:

```text
lock checkout session
-> lock payment attempt
-> validate
-> lock inventory reservations
-> lock voucher reservation nếu có
-> create order
-> consume reservations
-> insert payment
-> complete checkout/payment attempt
-> commit
-> ACK gateway
-> publish event after commit
```

Không gọi API refund hoặc gửi email trong transaction chính.

Thứ tự lock thống nhất cho mọi luồng finalize/release/cleanup:

```text
checkout_sessions
-> payment_attempts
-> inventory_reservations
-> voucher_reservations
-> product_variants
-> vouchers
```

Cleanup cũng xử lý theo từng checkout trong một transaction riêng với đúng thứ tự lock này.

Nếu transaction lỗi:

- Không ACK thành công.
- Trả response để gateway retry theo giao thức của VNPay/MoMo.
- Đảm bảo lần retry sau xử lý được nhờ idempotency.

## 20. Thứ tự triển khai đề xuất

1. Bổ sung enum `PaymentAttemptStatus` với `PENDING`, `COMPLETED`, `FAILED`, `EXPIRED`, `REQUIRES_REFUND`, `REFUND_REQUESTED`, `REFUNDED`.
2. Bổ sung hoặc hoàn thiện entity/repository `PaymentAttempt`.
3. Tạo DTO `PaymentCallbackDTO`, `VnPayCallbackDTO`, `MomoCallbackDTO`, `PaymentStatusResponseDTO`, `GatewayCallbackResponse`.
4. Tạo `PaymentController.getPaymentStatus(...)`.
5. Tạo `PaymentController.handlePaymentCallback(...)`.
6. Tạo `PaymentGatewayAdapter`, `VnPayAdapter`, `MomoAdapter`, `PaymentGatewayAdapterFactory` trong `pattern.adapter.payment`.
7. Tạo `PaymentCallbackFacade` trong `pattern.facade.payment`, triển khai `PaymentService.handlePaymentCallback(...)`.
8. Bổ sung `CheckoutService.finalizePaidCheckout(checkoutCode)`.
9. Bổ sung `CheckoutService.releaseFailedCheckout(checkoutCode)`.
10. Bổ sung `OrderService.createOrderFromSnapshot(checkoutCode)`.
11. Bổ sung consume/release reservation nếu chức năng đặt hàng chưa hoàn tất.
12. Tạo event/listener trong `pattern.observer.payment`.
13. Viết test cho callback thành công, thất bại, sai chữ ký, sai amount, callback trùng và reservation hết hạn.
14. Sau khi VNPay/MoMo ổn định, mới mở rộng `ZaloPayAdapter`.

## 21. Test case cần có

### 21.1. Test Return URL/status

- Lấy trạng thái payment attempt `PENDING`.
- Lấy trạng thái payment attempt `COMPLETED`.
- Không tìm thấy `paymentReference`.
- Người dùng không sở hữu payment attempt thì không xem được status.

### 21.2. Test callback hợp lệ

- VNPay callback thành công, tạo Order và Payment.
- MoMo callback thành công, tạo Order và Payment.
- Callback thất bại/hủy, release reservation.
- Callback thành công có voucher, consume voucher đúng.
- Callback thành công không voucher, bỏ qua consume voucher.

### 21.3. Test bảo mật

- Sai chữ ký.
- Sai amount.
- Sai paymentReference.
- Sai method.
- Callback gửi lại với transaction id lạ.

### 21.4. Test idempotency

- Callback thành công gửi 2 lần, chỉ tạo 1 Order.
- Callback thất bại gửi 2 lần, chỉ release reservation 1 lần.
- Callback thành công sau khi đã failed cần xử lý theo rule rõ ràng, ưu tiên audit và không tự tạo đơn nếu trạng thái không hợp lệ.

### 21.5. Test reservation hết hạn

- Payment success nhưng checkout expired, mark `REQUIRES_REFUND`.
- Payment success nhưng inventory reservation expired, mark `REQUIRES_REFUND`.
- Payment success nhưng voucher reservation expired, mark `REQUIRES_REFUND`.

### 21.6. Test transaction

- Lỗi khi tạo Order thì không consume reservation.
- Lỗi khi insert Payment thì không complete checkout.
- Lỗi khi consume stock thì không tạo payment completed.
- Retry callback sau lỗi transaction có thể xử lý lại đúng.

## 22. Quy tắc kỹ thuật quan trọng

- Không tạo Order từ Return URL.
- Không tin dữ liệu từ frontend để xác nhận thanh toán.
- Chỉ tin callback/IPN sau khi verify chữ ký.
- Callback phải idempotent.
- ACK gateway chỉ sau khi database commit thành công.
- Không gọi refund/email/API ngoài trong transaction chính.
- Không cộng lại tồn kho khi payment failed vì reservation chưa trừ tồn kho thật.
- Nếu payment success nhưng reservation hết hạn, không tạo Order và phải mark `REQUIRES_REFUND`.
- Các bảng nội bộ liên kết checkout bằng `checkout_session_id`; `checkout_code` là mã nghiệp vụ public.
- Mọi luồng finalize/release/cleanup phải dùng cùng thứ tự lock để giảm rủi ro deadlock.
- Chỉ hỗ trợ VNPay/MoMo ở giai đoạn đầu; ZaloPay mở rộng sau.

## 23. Kết luận

Chức năng thanh toán nên được triển khai với `PaymentCallbackFacade.handlePaymentCallback(...)` làm Facade xử lý callback. Bên trong Facade, hệ thống dùng `PaymentGatewayAdapter` để xác thực và chuẩn hóa từng cổng VNPay/MoMo, dùng Reservation Pattern để consume hoặc release tài nguyên đã giữ từ bước đặt hàng, và dùng Observer Pattern để gửi email/thông báo sau khi thanh toán được ghi nhận.

Luồng đúng theo SD20_2 là:

```text
Return URL chỉ hiển thị trạng thái
-> Callback/IPN mới là nguồn xác nhận
-> Callback thành công thì finalize checkout và tạo Order
-> Callback thất bại thì release reservation
-> Callback success nhưng reservation hết hạn thì đánh dấu cần hoàn tiền
```
