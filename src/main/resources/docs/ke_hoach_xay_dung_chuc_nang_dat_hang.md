# Kế hoạch xây dựng chức năng đặt hàng

Tài liệu này mô tả kế hoạch triển khai chức năng **xác nhận đặt hàng** theo sơ đồ `SD20_1_XacNhanDatHang.puml`. Phạm vi của tài liệu tập trung vào bước đặt hàng trước, sau đó mới chuyển sang thanh toán trực tuyến ở `SD20_2_ThanhToanTrucTuyen.puml`.

## 1. Mục tiêu

Xây dựng API xác nhận đặt hàng cho khách hàng đã đăng nhập:

```text
POST /api/checkouts
```

Luồng chính:

```text
Khách hàng nhấn "Xác nhận đặt hàng"
-> Frontend gọi POST /api/checkouts
-> CheckoutController.confirmCheckout(...)
-> CheckoutService.confirmCheckout(...)
-> Sinh checkoutCode
-> Tạo checkout session trạng thái CREATING
-> Giữ tồn kho
-> Giữ voucher nếu có
-> Lưu checkout session items và chuyển checkout session sang RESERVED
-> Nếu COD: tạo Order ngay
-> Nếu online: tạo payment attempt và trả URL thanh toán
```

## 2. Phạm vi chức năng

Chức năng đặt hàng xử lý các phần sau:

- Lấy dữ liệu checkout từ giỏ hàng và địa chỉ giao hàng.
- Kiểm tra tồn kho lần cuối.
- Giữ tồn kho tạm thời bằng Reservation Pattern trên PostgreSQL.
- Kiểm tra và giữ voucher tạm thời trên PostgreSQL nếu khách hàng nhập mã.
- Tính tổng tiền đơn hàng.
- Tạo checkout session trước để lấy `checkout_session_id` làm khóa ngoại cho reservation và payment attempt.
- Lưu checkout session items và chuyển checkout session sang `RESERVED`.
- Nếu phương thức là COD, tạo đơn hàng ngay.
- Nếu phương thức là VNPay hoặc MoMo, tạo payment attempt và trả thông tin cho bước thanh toán.
- Gửi sự kiện sau khi đơn COD được tạo thành công.

Chức năng này chưa xử lý callback thanh toán trực tuyến. Phần callback, ghi nhận kết quả thanh toán và tạo đơn sau khi thanh toán online thành công thuộc `SD20_2`.

## 3. Pattern áp dụng

| Pattern | Thành phần áp dụng | Vai trò |
|---|---|---|
| Facade | `CheckoutService.confirmCheckout(...)` | Điều phối toàn bộ quy trình xác nhận đặt hàng |
| Reservation | `InventoryReservationService`, `VoucherService` | Giữ tồn kho và voucher tạm thời bằng bảng phụ PostgreSQL |
| Adapter | `PaymentGatewayAdapter`, các adapter VNPay/MoMo | Chuẩn hóa cách tạo URL thanh toán |
| Observer | `OrderCreatedEvent`, `OrderNotificationListener` | Gửi email/thông báo sau khi đơn hàng được tạo |

## 4. Luồng tổng quát theo SD20_1

```text
Customer
-> Frontend: Nhấn "Xác nhận đặt hàng"
-> CheckoutController: POST /api/checkouts(addressId, voucherCode, paymentMethod)
-> CheckoutService: confirmCheckout(requestDTO, customerId)
-> Repository: findCheckoutData(customerId, addressId)
-> CheckoutService: generateCheckoutCode()
-> Repository: createCheckoutSession(checkoutCode, status=CREATING, expiresAt)
-> InventoryReservationService: reserveStock(checkoutSessionId, items, expiresAt)
-> VoucherService: reserveVoucher(checkoutSessionId, code, subtotal, expiresAt) nếu có voucher
-> CheckoutService: calculateTotal(subtotal, shippingFee, discountAmount)
-> Repository: saveCheckoutSessionItemsAndMarkReserved(checkoutSessionId, snapshot)
```

Sau khi checkout session được giữ thành công, hệ thống rẽ nhánh theo phương thức thanh toán:

```text
Nếu COD:
-> OrderService.createOrder(checkoutCode, COD)
-> Tạo Order, OrderItem, Payment COD
-> Consume reservation
-> Xóa item đã mua khỏi giỏ
-> Publish OrderCreatedEvent
-> Trả orderCode

Nếu thanh toán online:
-> PaymentService.createPaymentAttempt(checkoutSessionId, amount)
-> PaymentGatewayAdapterFactory.getAdapter(paymentMethod)
-> PaymentGatewayAdapter.createPaymentUrl(paymentReference, amount, expiresAt)
-> Trả paymentUrl
-> Chuyển tiếp sang SD20_2
```

## 5. API đề xuất

### 5.1. Endpoint

```http
POST /api/checkouts
Authorization: Bearer <access_token>
Content-Type: application/json
```

### 5.2. Request DTO

```java
public record ConfirmCheckoutRequestDTO(
        Long addressId,
        String voucherCode,
        PaymentMethod paymentMethod
) {}
```

Ràng buộc:

- `addressId` bắt buộc.
- `voucherCode` có thể null hoặc rỗng.
- `paymentMethod` bắt buộc, nhận một trong các giá trị giai đoạn đầu: `cod`, `vnpay`, `momo`.

`zalopay` để mở rộng sau, chưa đưa vào phạm vi triển khai đầu tiên để tránh dàn trải.

### 5.3. Response cho COD

```java
public record OrderResponseDTO(
        String orderCode,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        OrderStatus status
) {}
```

### 5.4. Response cho thanh toán online

```java
public record CheckoutResponseDTO(
        String checkoutCode,
        String paymentReference,
        String paymentUrl,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        OffsetDateTime expiresAt
) {}
```

## 6. Cấu trúc package đề xuất

Nguyên tắc tổ chức:

- `controller`, `dto`, `entity`, `repository`, `service` vẫn giữ vai trò theo layer chuẩn của Spring Boot.
- Các class thể hiện pattern sẽ đặt trong thư mục `pattern`.
- `service` nên ưu tiên chứa interface hoặc service hợp đồng nghiệp vụ.
- Implementation áp dụng pattern sẽ nằm trong `pattern/...` để dễ trình bày trong báo cáo và dễ đối chiếu với sơ đồ thiết kế.

```text
vn.hcmute.edu.dp.nhom10.backend
├── controller
│   └── CheckoutController.java
├── dto
│   ├── request
│   │   └── ConfirmCheckoutRequestDTO.java
│   └── response
│       ├── CheckoutResponseDTO.java
│       └── OrderResponseDTO.java
├── entity
│   ├── CheckoutSession.java
│   ├── InventoryReservation.java
│   ├── VoucherReservation.java
│   └── PaymentAttempt.java
├── repository
│   ├── CheckoutSessionRepository.java
│   ├── InventoryReservationRepository.java
│   ├── VoucherReservationRepository.java
│   ├── PaymentAttemptRepository.java
│   └── PaymentRepository.java
├── service
│   ├── CheckoutService.java
│   ├── InventoryReservationService.java
│   ├── VoucherService.java
│   ├── OrderService.java
│   └── PaymentService.java
├── pattern
│   ├── facade
│   │   └── checkout
│   │       └── CheckoutServiceFacade.java
│   ├── reservation
│   │   ├── inventory
│   │   │   └── InventoryReservationServiceImpl.java
│   │   └── voucher
│   │       └── VoucherReservationServiceImpl.java
│   ├── adapter
│   │   └── payment
│   │       ├── PaymentGatewayAdapter.java
│   │       ├── PaymentGatewayAdapterFactory.java
│   │       ├── VnPayAdapter.java
│   │       └── MomoAdapter.java
│   └── observer
│       └── order
│           ├── event
│           │   └── OrderCreatedEvent.java
│           └── listener
│               └── OrderNotificationListener.java
```

Mapping pattern:

| Pattern | Package đề xuất | Class chính |
|---|---|---|
| Facade | `pattern.facade.checkout` | `CheckoutServiceFacade` |
| Reservation | `pattern.reservation.inventory` | `InventoryReservationServiceImpl` |
| Reservation | `pattern.reservation.voucher` | `VoucherReservationServiceImpl` |
| Adapter | `pattern.adapter.payment` | `PaymentGatewayAdapter`, `VnPayAdapter`, `MomoAdapter` |
| Observer | `pattern.observer.order` | `OrderCreatedEvent`, `OrderNotificationListener` |

`CheckoutServiceFacade` vẫn triển khai hàm theo SD20_1 là `confirmCheckout(requestDTO, customerId)`. Tên class có hậu tố `Facade` để thể hiện pattern, còn tên hàm vẫn giữ đúng sequence diagram.

## 7. Database cần bổ sung

Hiện hệ thống đã có các bảng chính:

- `orders`
- `order_items`
- `payments`
- `vouchers`
- `cart_items`
- `product_variants`

Để đúng luồng SD20_1, cần bổ sung các bảng phụ phục vụ reservation, checkout session và trạng thái thanh toán online.

### 7.1. Bảng `checkout_sessions`

Mục đích: lưu snapshot checkout sau khi đã giữ tồn kho/voucher.

Các cột đề xuất:

```text
id
checkout_code
user_id
shipping_name
shipping_phone
shipping_province
shipping_district
shipping_ward
shipping_address
subtotal
shipping_fee
discount_amount
total_amount
voucher_id
payment_method
status
expires_at
created_at
updated_at
```

`checkout_code` vẫn là mã nghiệp vụ public/unique để frontend và callback tham chiếu. Các bảng nội bộ nên liên kết bằng `checkout_session_id` để đảm bảo toàn vẹn dữ liệu bằng khóa ngoại.

Trạng thái đề xuất:

```text
CREATING
RESERVED
COMPLETED
FAILED
EXPIRED
RELEASED
```

### 7.2. Bảng `checkout_session_items`

Mục đích: lưu snapshot sản phẩm tại thời điểm checkout.

Các cột đề xuất:

```text
id
checkout_session_id
product_variant_id
product_name
variant_info
quantity
unit_price
subtotal
```

Lý do cần bảng này: nếu giá, tên sản phẩm hoặc thông tin biến thể thay đổi sau đó, checkout vẫn giữ đúng dữ liệu tại thời điểm khách xác nhận.

### 7.3. Bảng `inventory_reservations`

Mục đích: giữ quyền mua tạm thời cho từng biến thể sản phẩm.

Các cột đề xuất:

```text
id
checkout_session_id
product_variant_id
quantity
status
expires_at
created_at
updated_at
```

Trạng thái đề xuất:

```text
ACTIVE
CONSUMED
RELEASED
EXPIRED
```

Nguyên tắc:

- Khi `reserveStock(...)`, chưa trừ `stock_quantity`.
- Khi COD tạo đơn thành công hoặc online thanh toán thành công, gọi `consumeStockReservation(...)` để trừ tồn kho.
- Khi checkout lỗi, thanh toán lỗi hoặc hết hạn, gọi `releaseStockReservation(...)`.

### 7.4. Bảng `voucher_reservations`

Mục đích: giữ lượt dùng voucher tạm thời.

Các cột đề xuất:

```text
id
checkout_session_id
voucher_id
discount_amount
status
expires_at
created_at
updated_at
```

Nguyên tắc:

- Khi `reserveVoucher(...)`, chưa tăng `times_used`.
- Khi đơn được tạo thành công, gọi `consumeVoucherReservation(...)`.
- Khi checkout lỗi, thanh toán lỗi hoặc hết hạn, gọi `releaseVoucherReservation(...)`.

### 7.5. Bảng `payment_attempts`

Mục đích: lưu lần thử thanh toán online trước khi có `Order`.

Hiện bảng `payments` đang bắt buộc có `order_id`. Theo SD20_1, nhánh online chưa tạo `Order`, nên cần `payment_attempts`.

Các cột đề xuất:

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
expires_at
created_at
updated_at
```

Trạng thái đề xuất:

```text
PENDING
COMPLETED
FAILED
EXPIRED
```

## 8. Controller

### 8.1. `CheckoutController`

Nhiệm vụ:

- Nhận request từ frontend.
- Lấy thông tin khách hàng đang đăng nhập.
- Gọi `CheckoutService.confirmCheckout(requestDTO, customerId)`.
- Trả response phù hợp với COD hoặc online.

Tên hàm theo SD20_1:

```java
public ResponseEntity<ApiResponse<?>> confirmCheckout(
        ConfirmCheckoutRequestDTO requestDTO,
        Authentication authentication
)
```

Pseudo flow:

```text
confirmCheckout(requestDTO, authentication)
-> customerId = resolveCustomerId(authentication)
-> result = checkoutService.confirmCheckout(requestDTO, customerId)
-> return ApiResponse.success(result)
```

## 9. CheckoutService - Facade Pattern

`CheckoutService` là facade chính của chức năng đặt hàng. Controller không gọi trực tiếp `InventoryReservationService`, `VoucherService`, `OrderService` hay `PaymentService`.

Implementation áp dụng Facade Pattern nên đặt tại:

```text
pattern.facade.checkout.CheckoutServiceFacade
```

### 9.1. Interface đề xuất

```java
public interface CheckoutService {
    Object confirmCheckout(ConfirmCheckoutRequestDTO requestDTO, Long customerId);
    CheckoutData findCheckoutData(Long customerId, Long addressId);
    BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal shippingFee, BigDecimal discountAmount);
    void releaseFailedCheckout(String checkoutCode);
}
```

### 9.2. Hàm `confirmCheckout(requestDTO, customerId)`

Tên hàm theo SD20_1:

```java
confirmCheckout(requestDTO, customerId)
```

Luồng chi tiết:

```text
1. findCheckoutData(customerId, addressId)
2. validate cartSnapshot không rỗng
3. checkoutCode = generateCheckoutCode()
4. expiresAt = now + checkoutReservationTtl
5. createCheckoutSession(checkoutCode, status=CREATING, expiresAt) để lấy checkoutSessionId
6. reserveStock(checkoutSessionId, items, expiresAt)
7. Nếu có voucherCode:
   7.1. reserveVoucher(checkoutSessionId, code, subtotal, expiresAt)
   7.2. nhận discountAmount
8. calculateTotal(subtotal, shippingFee, discountAmount)
9. saveCheckoutSessionItemsAndMarkReserved(checkoutSessionId, snapshot)
10. Nếu paymentMethod == cod:
   10.1. createOrder(checkoutCode, COD)
   10.2. trả OrderResponseDTO
11. Nếu paymentMethod là online:
   11.1. createPaymentAttempt(checkoutSessionId, amount)
   11.2. trả CheckoutResponseDTO(paymentUrl)
```

### 9.3. Transaction boundary

Nên chia thành các transaction rõ ràng:

- Transaction 1 (`@Transactional`): sinh `checkoutCode`, tạo `checkout_sessions(status=CREATING)`, giữ tồn kho, giữ voucher, lưu `checkout_session_items` và chuyển checkout session sang `RESERVED`.
- Transaction 2 COD: tạo order, consume reservation, xóa giỏ hàng.
- Transaction online: tạo payment attempt.

Nếu reserve stock thành công nhưng reserve voucher hoặc save checkout session lỗi, toàn bộ Transaction 1 phải rollback. Không gọi gateway thanh toán khi đang giữ transaction database.

## 10. Repository layer

### 10.1. `findCheckoutData(customerId, addressId)`

Tên theo SD20_1:

```java
findCheckoutData(customerId, addressId)
```

Nhiệm vụ:

- Lấy cart items của khách hàng.
- Fetch `ProductVariant` và `Product`.
- Lấy địa chỉ giao hàng.
- Tạo snapshot để tính tiền và tạo đơn.

### 10.2. `selectCartWithVariantsAndAddress(customerId, addressId)`

Tên theo SD20_1:

```java
selectCartWithVariantsAndAddress(customerId, addressId)
```

Có thể triển khai bằng:

- Query riêng trong repository.
- Hoặc service gọi nhiều repository rồi gom thành `CheckoutData`.

## 11. InventoryReservationService - Reservation Pattern

### 11.1. Interface đề xuất

Implementation áp dụng Reservation Pattern cho tồn kho nên đặt tại:

```text
pattern.reservation.inventory.InventoryReservationServiceImpl
```

```java
public interface InventoryReservationService {
    void reserveStock(Long checkoutSessionId, List<CheckoutItemSnapshot> items, OffsetDateTime expiresAt);
    void consumeStockReservation(String checkoutCode);
    void releaseStockReservation(String checkoutCode);
}
```

### 11.2. Hàm `reserveStock(checkoutSessionId, items, expiresAt)`

Tên hàm theo SD20_1:

```java
reserveStock(checkoutSessionId, items, expiresAt)
```

Luồng:

```text
reserveStock(checkoutSessionId, items, expiresAt)
-> lockVariantsAndCheckAvailability(items)
-> saveInventoryReservations(checkoutSessionId, items, expiresAt)
```

### 11.3. Hàm `lockVariantsAndCheckAvailability(items)`

Tên hàm theo SD20_1:

```java
lockVariantsAndCheckAvailability(items)
```

Nhiệm vụ:

- Lock các dòng `product_variants` liên quan bằng `FOR UPDATE`.
- Kiểm tra `stock_quantity`.
- Tính cả các reservation còn `ACTIVE` nếu không trừ tồn kho ngay.

Công thức kiểm tra:

```text
availableQuantity = stock_quantity - sum(active_reservation_quantity)
availableQuantity >= requestedQuantity
```

Nếu không đủ hàng:

```text
throw StockNotAvailableException
```

### 11.4. Hàm `saveInventoryReservations(checkoutSessionId, items, expiresAt)`

Tên hàm theo SD20_1:

```java
saveInventoryReservations(checkoutSessionId, items, expiresAt)
```

Nhiệm vụ:

- Tạo reservation cho từng sản phẩm trong checkout.
- Gán `checkoutSessionId` cho từng reservation.
- Gán `status = ACTIVE`.
- Gán `expiresAt`.

### 11.5. Hàm `consumeStockReservation(checkoutCode)`

Tên hàm theo SD20_1:

```java
consumeStockReservation(checkoutCode)
```

Luồng:

```text
consumeStockReservation(checkoutCode)
-> deductStockAndConsumeReservations(checkoutCode)
```

### 11.6. Hàm `deductStockAndConsumeReservations(checkoutCode)`

Tên hàm theo SD20_1:

```java
deductStockAndConsumeReservations(checkoutCode)
```

Nhiệm vụ:

- Lock reservation và variant.
- Trừ `product_variants.stock_quantity`.
- Chuyển reservation sang `CONSUMED`.

### 11.7. Hàm `releaseStockReservation(checkoutCode)`

Tên hàm theo SD20_1:

```java
releaseStockReservation(checkoutCode)
```

Luồng:

```text
releaseStockReservation(checkoutCode)
-> releaseInventoryReservations(checkoutCode)
```

## 12. VoucherService - Reservation Pattern

### 12.1. Interface đề xuất

Implementation áp dụng Reservation Pattern cho voucher nên đặt tại:

```text
pattern.reservation.voucher.VoucherReservationServiceImpl
```

```java
public interface VoucherService {
    BigDecimal reserveVoucher(Long checkoutSessionId, String code, BigDecimal subtotal, OffsetDateTime expiresAt);
    void consumeVoucherReservation(String checkoutCode);
    void releaseVoucherReservation(String checkoutCode);
}
```

### 12.2. Hàm `reserveVoucher(checkoutSessionId, code, subtotal, expiresAt)`

Tên hàm theo SD20_1:

```java
reserveVoucher(checkoutSessionId, code, subtotal, expiresAt)
```

Luồng:

```text
reserveVoucher(checkoutSessionId, code, subtotal, expiresAt)
-> lockVoucherAndCheckAvailability(code)
-> validate minOrderAmount, date range, isActive
-> calculateDiscountAmount(...)
-> saveVoucherReservation(checkoutSessionId, code, expiresAt)
-> return discountAmount
```

### 12.3. Hàm `lockVoucherAndCheckAvailability(code)`

Tên hàm theo SD20_1:

```java
lockVoucherAndCheckAvailability(code)
```

Nhiệm vụ:

- Lock voucher bằng `FOR UPDATE`.
- Kiểm tra `isActive`.
- Kiểm tra `startDate`, `endDate`.
- Kiểm tra số lượt còn lại.

Công thức kiểm tra:

```text
availableUses = usage_limit - times_used - active_reservations
availableUses > 0
```

Nếu không khả dụng:

```text
throw VoucherUnavailableException
```

### 12.4. Hàm `saveVoucherReservation(checkoutSessionId, code, expiresAt)`

Tên hàm theo SD20_1:

```java
saveVoucherReservation(checkoutSessionId, code, expiresAt)
```

Nhiệm vụ:

- Tạo `voucher_reservations`.
- Gán `checkoutSessionId` để liên kết với checkout session và inventory reservations.
- Gán `status = ACTIVE`.
- Lưu `discountAmount`.

### 12.5. Hàm `consumeVoucherReservation(checkoutCode)`

Tên hàm theo SD20_1:

```java
consumeVoucherReservation(checkoutCode)
```

Luồng:

```text
consumeVoucherReservation(checkoutCode)
-> consumeVoucher(checkoutCode)
```

### 12.6. Hàm `consumeVoucher(checkoutCode)`

Tên hàm theo SD20_1:

```java
consumeVoucher(checkoutCode)
```

Nhiệm vụ:

- Tăng `vouchers.times_used`.
- Chuyển voucher reservation sang `CONSUMED`.

### 12.7. Hàm `releaseVoucherReservationByCheckout(checkoutCode)`

Tên hàm theo SD20_2, dùng lại cho luồng release:

```java
releaseVoucherReservationByCheckout(checkoutCode)
```

Nhiệm vụ:

- Chuyển voucher reservation đang `ACTIVE` sang `RELEASED`.

## 13. OrderService - Tạo đơn hàng COD

### 13.1. Interface đề xuất

```java
public interface OrderService {
    OrderResponseDTO createOrder(String checkoutCode, PaymentMethod paymentMethod);
    Order createOrderFromSnapshot(String checkoutCode);
}
```

### 13.2. Hàm `createOrder(checkoutCode, COD)`

Tên hàm theo SD20_1:

```java
createOrder(checkoutCode, COD)
```

Luồng:

```text
createOrder(checkoutCode, COD)
-> lockCheckoutAndReservations(checkoutCode)
-> saveOrderWithItemsAndCodPayment(checkoutCode)
-> consumeStockReservation(checkoutCode)
-> consumeVoucherReservation(checkoutCode) nếu có voucher
-> deleteCartItemsAndCompleteCheckout(checkoutCode)
-> publish OrderCreatedEvent
-> return orderResponse
```

### 13.3. Hàm `lockCheckoutAndReservations(checkoutCode)`

Tên hàm theo SD20_1:

```java
lockCheckoutAndReservations(checkoutCode)
```

Nhiệm vụ:

- Lock checkout session.
- Lock inventory reservations.
- Lock voucher reservation nếu có.
- Đảm bảo session đang ở trạng thái `RESERVED`.
- Đảm bảo chưa hết hạn.

### 13.4. Hàm `saveOrderWithItemsAndCodPayment(checkoutCode)`

Tên hàm theo SD20_1:

```java
saveOrderWithItemsAndCodPayment(checkoutCode)
```

Nhiệm vụ:

- Tạo `Order`.
- Tạo `OrderItem` từ checkout item snapshot.
- Tạo `Payment` với:

```text
method = cod
status = pending
amount = totalAmount
```

Trạng thái Order ban đầu:

```text
status = pending
```

### 13.5. Hàm `deleteCartItemsAndCompleteCheckout(checkoutCode)`

Tên hàm theo SD20_1:

```java
deleteCartItemsAndCompleteCheckout(checkoutCode)
```

Nhiệm vụ:

- Xóa các item đã mua khỏi giỏ hàng.
- Chuyển `checkout_sessions.status = COMPLETED`.
- Commit transaction.

## 14. PaymentService - Chuẩn bị thanh toán online

### 14.1. Interface đề xuất

```java
public interface PaymentService {
    CheckoutResponseDTO createPaymentAttempt(Long checkoutSessionId, BigDecimal amount);
}
```

### 14.2. Hàm `createPaymentAttempt(checkoutSessionId, amount)`

Tên hàm theo SD20_1:

```java
createPaymentAttempt(checkoutSessionId, amount)
```

Luồng:

```text
createPaymentAttempt(checkoutSessionId, amount)
-> savePaymentAttempt(checkoutSessionId, amount, status=PENDING)
-> PaymentGatewayAdapterFactory.getAdapter(paymentMethod)
-> createPaymentUrl(paymentReference, amount, expiresAt)
-> return paymentUrl
```

### 14.3. Hàm `savePaymentAttempt(checkoutSessionId, amount, status=PENDING)`

Tên hàm theo SD20_1:

```java
savePaymentAttempt(checkoutSessionId, amount, status=PENDING)
```

Nhiệm vụ:

- Sinh `paymentReference`.
- Lưu payment attempt.
- Gán `checkout_session_id` thay vì chỉ lưu `checkout_code`.
- Chưa tạo `Payment` vì chưa có `Order`.

### 14.4. Hàm `createPaymentUrl(paymentReference, amount, expiresAt)`

Tên hàm theo SD20_1:

```java
createPaymentUrl(paymentReference, amount, expiresAt)
```

Nhiệm vụ:

- Chọn `PaymentGatewayAdapter` theo `paymentMethod`.
- Gọi cổng thanh toán tương ứng.
- Trả URL thanh toán.

Nếu gateway lỗi hoặc timeout:

```text
PaymentGatewayUnavailable
-> PaymentInitializationFailed
-> releaseFailedCheckout(checkoutCode)
-> releaseReservationsAndFailPayment(checkoutCode)
```

## 15. PaymentGatewayAdapter - Adapter Pattern

Các class Adapter thanh toán nên đặt tại:

```text
pattern.adapter.payment
```

### 15.1. Interface

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

### 15.2. Adapter triển khai

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

Tương tự:

- `MomoAdapter`

`ZaloPayAdapter` sẽ được bổ sung ở giai đoạn sau khi hệ thống đã ổn định với VNPay và MoMo.

### 15.3. PaymentGatewayAdapterFactory

```java
public class PaymentGatewayAdapterFactory {
    public PaymentGatewayAdapter getAdapter(PaymentMethod method) {
        // return adapter theo method
    }
}
```

## 16. Observer sau khi tạo đơn hàng

Các class Observer cho đơn hàng nên đặt tại:

```text
pattern.observer.order
```

### 16.1. Event

```java
public class OrderCreatedEvent {
    private final Long orderId;
    private final String orderCode;
    private final Long userId;
    private final String customerEmail;
}
```

### 16.2. Listener

```java
@Component
public class OrderNotificationListener {
    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // send order confirmation email
    }
}
```

Nhiệm vụ:

- Gửi email xác nhận đơn hàng.
- Gửi thông báo trong hệ thống nếu có notification module.
- Ghi log hoạt động nếu cần.

Không nên gửi email trực tiếp trong `OrderService`, vì sẽ làm luồng tạo đơn bị phụ thuộc vào dịch vụ email.

## 17. Luồng lỗi

### 17.1. Không đủ tồn kho

```text
lockVariantsAndCheckAvailability(items)
-> StockNotAvailableException
-> rollbackReservationTransaction()
-> HTTP 409
```

Thông báo:

```text
Sản phẩm không đủ số lượng tồn kho. Vui lòng cập nhật giỏ hàng.
```

### 17.2. Voucher không khả dụng

```text
lockVoucherAndCheckAvailability(code)
-> VoucherUnavailableException
-> rollbackReservationTransaction()
-> HTTP 400
```

Thông báo:

```text
Voucher không khả dụng hoặc đã hết lượt sử dụng.
```

### 17.3. Giỏ hàng trống

```text
findCheckoutData(customerId, addressId)
-> cartSnapshot empty
-> HTTP 400
```

Thông báo:

```text
Giỏ hàng đang trống.
```

### 17.4. Địa chỉ không tồn tại

```text
findCheckoutData(customerId, addressId)
-> AddressNotFoundException
-> HTTP 404
```

Thông báo:

```text
Không tìm thấy địa chỉ giao hàng.
```

### 17.5. Không thể khởi tạo thanh toán online

```text
PaymentGatewayAdapter.createPaymentUrl(...)
-> PaymentGatewayUnavailable
-> releaseFailedCheckout(checkoutCode)
-> releaseReservationsAndFailPayment(checkoutCode)
-> HTTP 502
```

Thông báo:

```text
Không thể khởi tạo thanh toán. Vui lòng thử lại.
```

## 18. Cleanup reservation hết hạn

Cần job định kỳ để dọn checkout session, inventory reservation, voucher reservation và payment attempt hết hạn. Cleanup xử lý theo từng checkout trong một transaction riêng để tránh trạng thái nửa vời.

Tên hàm đề xuất:

```java
releaseExpiredCheckouts()
releaseExpiredInventoryReservations()
releaseExpiredVoucherReservations()
failExpiredPaymentAttempts()
```

Luồng:

```text
Mỗi 1-5 phút
-> tìm danh sách checkout_sessions status=RESERVED và expires_at < now
-> với từng checkout:
   BEGIN
   lock checkout_session
   lock payment_attempt nếu có
   lock inventory_reservations
   lock voucher_reservation nếu có
   mark inventory_reservations EXPIRED hoặc RELEASED
   mark voucher_reservation EXPIRED hoặc RELEASED nếu có
   mark checkout EXPIRED
   mark payment_attempt EXPIRED nếu còn PENDING
   COMMIT
```

## 19. Mapping trạng thái

### 19.1. COD thành công

```text
checkout_sessions.status = COMPLETED
inventory_reservations.status = CONSUMED
voucher_reservations.status = CONSUMED nếu có
orders.status = pending
payments.method = cod
payments.status = pending
```

### 19.2. Online khởi tạo thanh toán thành công

```text
checkout_sessions.status = RESERVED
inventory_reservations.status = ACTIVE
voucher_reservations.status = ACTIVE nếu có
payment_attempts.status = PENDING
```

Chưa tạo `orders` và `payments`. Việc tạo đơn thật sẽ diễn ra ở SD20_2 sau khi callback thanh toán thành công.

### 19.3. Online khởi tạo thanh toán thất bại

```text
checkout_sessions.status = FAILED hoặc RELEASED
inventory_reservations.status = RELEASED
voucher_reservations.status = RELEASED nếu có
payment_attempts.status = FAILED
```

## 20. Thứ tự triển khai đề xuất

1. Bổ sung enum trạng thái cho checkout, inventory reservation, voucher reservation, payment attempt.
2. Tạo entity và repository cho `CheckoutSession`, `CheckoutSessionItem`, `InventoryReservation`, `VoucherReservation`, `PaymentAttempt`.
3. Tạo DTO cho request và response.
4. Tạo `CheckoutController.confirmCheckout(...)`.
5. Tạo `CheckoutServiceFacade` trong `pattern.facade.checkout`, triển khai `CheckoutService.confirmCheckout(...)` theo Facade Pattern.
6. Tạo `InventoryReservationServiceImpl` trong `pattern.reservation.inventory`, triển khai `reserveStock(...)`, `consumeStockReservation(...)`, `releaseStockReservation(...)`.
7. Tạo `VoucherReservationServiceImpl` trong `pattern.reservation.voucher`, triển khai `reserveVoucher(...)`, `consumeVoucherReservation(...)`, `releaseVoucherReservation(...)`.
8. Tạo `OrderService.createOrder(checkoutCode, COD)`.
9. Tạo `PaymentService.createPaymentAttempt(checkoutSessionId, amount)`.
10. Tạo `PaymentGatewayAdapter` và adapter khung cho VNPay/MoMo trong `pattern.adapter.payment`.
11. Tạo `OrderCreatedEvent` và `OrderNotificationListener` trong `pattern.observer.order`.
12. Tạo job cleanup reservation hết hạn.
13. Viết unit test và integration test.

## 21. Test case cần có

### 21.1. Test thành công

- COD thành công với giỏ hàng hợp lệ.
- COD thành công có voucher.
- Online thành công tạo được `paymentUrl`.
- Online không tạo `Order` ở SD20_1.

### 21.2. Test lỗi nghiệp vụ

- Giỏ hàng trống.
- Không tìm thấy địa chỉ.
- Sản phẩm không đủ tồn kho.
- Sản phẩm bị inactive.
- Voucher sai mã.
- Voucher hết hạn.
- Voucher chưa tới ngày dùng.
- Voucher hết lượt.
- Tổng tiền không đạt `minOrderAmount`.

### 21.3. Test transaction

- Nếu reserve stock thành công nhưng reserve voucher lỗi, toàn bộ reservation phải rollback.
- Nếu tạo Order COD lỗi, reservation không được consume sai.
- Nếu khởi tạo payment online lỗi, checkout phải release reservation.
- Nếu gọi lại cùng checkout đã completed, không tạo đơn trùng.

### 21.4. Test concurrent

- Hai khách cùng đặt sản phẩm còn 1 item, chỉ một checkout được reserve thành công.
- Hai khách cùng dùng voucher còn 1 lượt, chỉ một checkout được reserve thành công.

## 22. Quy tắc kỹ thuật quan trọng

- Không trừ tồn kho tại bước thêm vào giỏ hàng.
- Không tăng `voucher.times_used` khi mới reserve.
- Không tạo `Order` cho thanh toán online ở SD20_1.
- Không gọi gateway thanh toán khi đang giữ lock database lâu.
- Mọi bước consume/release reservation phải idempotent để tránh lỗi khi callback hoặc job chạy lại.
- Snapshot sản phẩm, giá và địa chỉ phải được lưu tại thời điểm checkout.
- `checkoutCode` và `paymentReference` phải unique, nhưng các bảng nội bộ liên kết bằng `checkout_session_id`.
- Mọi luồng consume/release/finalize/cleanup phải lock theo cùng thứ tự: `checkout_sessions` -> `payment_attempts` -> `inventory_reservations` -> `voucher_reservations` -> `product_variants` -> `vouchers`.
- Các repository lock nên dùng transaction rõ ràng và kiểm soát phạm vi lock ngắn.

## 23. Kết luận

Chức năng đặt hàng nên được triển khai với `CheckoutService.confirmCheckout(...)` làm Facade chính. Bên trong Facade, hệ thống dùng Reservation Pattern trên PostgreSQL để giữ tồn kho và voucher, dùng Observer Pattern để xử lý email/thông báo sau khi tạo đơn, và chuẩn bị Adapter Pattern cho bước thanh toán online với VNPay/MoMo.

Luồng đúng theo SD20_1 là:

```text
Đặt hàng trước
-> COD thì tạo Order ngay
-> Online thì tạo checkout + payment attempt
-> Thanh toán online thật sự xử lý tiếp ở SD20_2
```
