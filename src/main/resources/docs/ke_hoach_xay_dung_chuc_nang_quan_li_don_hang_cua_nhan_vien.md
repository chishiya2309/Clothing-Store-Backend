# Kế hoạch xây dựng chức năng quản lí đơn hàng của nhân viên

Tài liệu này tổng hợp các yêu cầu hiện có trong hệ thống và chuyển thành kế hoạch cài đặt chi tiết cho phân hệ **quản lí đơn hàng của role Staff**.

Mục tiêu là triển khai chức năng nhân viên xem danh sách đơn hàng, xem chi tiết đơn hàng, xác nhận đơn, chuyển sang giao hàng, xác nhận hoàn thành và hủy đơn hàng mà không xung đột với luồng đặt hàng, thanh toán, voucher, tồn kho và điểm tích lũy hiện có.

## 1. Căn cứ hiện có

### 1.1. Use case

Trong tài liệu use case, chức năng quản lí đơn hàng của Staff gồm:

- Xem danh sách đơn hàng.
- Xem chi tiết đơn hàng.
- Xác nhận đơn hàng.
- Cập nhật trạng thái đơn hàng theo từng nghiệp vụ cụ thể.
- Hủy đơn hàng.

Các trạng thái chính của đơn hàng:

```text
pending -> processing -> shipping -> completed
pending -> cancelled
processing -> cancelled
```

Sau khi đơn đã ở `shipping` hoặc `completed`, Staff không được hủy bằng nghiệp vụ cancel thông thường. Nếu sau này cần trả hàng hoặc hoàn hàng thì phải thiết kế use case riêng, không trộn vào cancel.

### 1.2. Nền tảng code hiện có

Hệ thống hiện đã có:

- `Order`, `OrderItem`, `Payment`.
- `OrderStatus`: `pending`, `processing`, `shipping`, `completed`, `cancelled`.
- `PaymentStatus`: `pending`, `completed`, `failed`, `refunded`.
- `UserRole`: `admin`, `customer`, `staff`.
- Luồng đặt hàng và thanh toán đã tạo đơn với trạng thái ban đầu `pending`.
- Voucher, tồn kho và điểm tích lũy/membership đã có nền tảng riêng trong hệ thống.
- Chuẩn phân trang `PageResponseAbstract` và `PageResponse<T>` trong package `dto.response`.

Không tạo lại các phần đã có. Khi triển khai cần tái sử dụng entity, repository, service và strategy hiện có nếu phù hợp.

## 2. Phạm vi chức năng

Base path đề xuất:

```text
/api/staff/orders
```

Tất cả endpoint trong controller cần bảo vệ bằng:

```java
@PreAuthorize("hasRole('STAFF')")
```

Danh sách API:

| Chức năng | Method | Endpoint | Mô tả |
| --- | --- | --- | --- |
| Xem danh sách đơn hàng | `GET` | `/api/staff/orders` | Phân trang, lọc theo trạng thái, ngày, khách hàng |
| Xem chi tiết đơn hàng | `GET` | `/api/staff/orders/{orderCode}` | Lấy đầy đủ thông tin đơn, item, payment và timeline |
| Xác nhận đơn hàng | `PATCH` | `/api/staff/orders/{orderCode}/confirm` | Chuyển `pending` sang `processing` |
| Bắt đầu giao hàng | `PATCH` | `/api/staff/orders/{orderCode}/ship` | Chuyển `processing` sang `shipping` |
| Hoàn thành đơn hàng | `PATCH` | `/api/staff/orders/{orderCode}/complete` | Chuyển `shipping` sang `completed` sau khi có căn cứ giao thành công |
| Hủy đơn hàng | `PATCH` | `/api/staff/orders/{orderCode}/cancel` | Chuyển đơn hợp lệ sang `cancelled`, ghi lý do hủy |

Không public endpoint tổng quát kiểu `PATCH /api/staff/orders/{orderCode}/status` cho Staff, vì mỗi trạng thái có điều kiện nghiệp vụ và side effect khác nhau.

## 3. Cấu trúc file cần bổ sung

```text
src/main/java/vn/hcmute/edu/dp/nhom10/backend/
  controller/staff/
    StaffOrderController.java

  dto/request/
    StaffCancelOrderRequest.java
    StaffCompleteOrderRequest.java

  dto/response/
    StaffOrderListItemResponse.java
    StaffOrderDetailResponse.java
    StaffOrderItemResponse.java
    StaffPaymentSummaryResponse.java
    StaffOrderStatusTimelineResponse.java

  service/
    StaffOrderService.java
    OrderInventoryAdjustmentService.java
    OrderVoucherAdjustmentService.java
    LoyaltyPointService.java

  service/impl/
    StaffOrderServiceImpl.java
    OrderInventoryAdjustmentServiceImpl.java
    OrderVoucherAdjustmentServiceImpl.java
    LoyaltyPointServiceImpl.java

  repository/
    OrderStatusHistoryRepository.java

  entity/
    OrderStatusHistory.java

  policy/
    OrderStatusTransitionPolicy.java

  event/
    OrderStatusChangedEvent.java

  listener/
    OrderStatusChangedEventListener.java

  exception/
    OrderStateConflictException.java
```

`LoyaltyPointService` chỉ tạo mới nếu code hiện tại chưa có service tương đương. Nếu đã có service loyalty/membership sẵn, dùng lại service đó thay vì tạo lớp mới.

## 4. Mở rộng database bắt buộc

Cần tạo bảng phụ `order_status_histories` để lưu lịch sử trạng thái của đơn hàng. Đây là yêu cầu bắt buộc vì một đơn hàng có nhiều lần thay đổi trạng thái và hệ thống cần biết ai là người thực hiện thay đổi.

```sql
CREATE TABLE order_status_histories (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status order_status,
    to_status order_status NOT NULL,
    changed_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    changed_by_role user_role,
    reason TEXT,
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_status_history_order_time_id
    ON order_status_histories(order_id, created_at, id);
```

Ý nghĩa các cột:

| Cột | Ý nghĩa |
| --- | --- |
| `order_id` | Đơn hàng được thay đổi trạng thái |
| `from_status` | Trạng thái trước khi đổi, `NULL` khi tạo history đầu tiên |
| `to_status` | Trạng thái mới |
| `changed_by` | User thực hiện thay đổi, có thể là Staff, Customer hoặc Admin |
| `changed_by_role` | Role tại thời điểm thao tác |
| `reason` | Lý do hủy, ghi chú giao hàng hoặc nội dung nghiệp vụ ngắn |
| `metadata` | Dữ liệu bổ sung như `confirmationSource`, mã vận đơn, ghi chú đối soát |
| `created_at` | Thời điểm thay đổi |

Khi tạo đơn thành công, insert history đầu tiên:

```text
from_status = null
to_status = pending
changed_by = null
changed_by_role = null
```

Khi map timeline, có thể hiển thị actor là `SYSTEM`. Không cần tạo tài khoản hệ thống giả chỉ để ghi history.

Timeline cần sắp xếp ổn định theo:

```text
created_at ASC, id ASC
```

## 5. DTO và chuẩn phân trang

Những API có phân trang bắt buộc dùng chuẩn `PageResponseAbstract`/`PageResponse<T>` hiện có trong hệ thống.

Response danh sách đơn hàng nên dùng:

```java
PageResponse<StaffOrderListItemResponse>
```

Không trả trực tiếp `org.springframework.data.domain.Page<T>` ra client.

Thông tin danh sách cần có:

- Mã đơn hàng.
- Tên/email/số điện thoại khách hàng.
- Ngày tạo đơn.
- Tổng tiền.
- Trạng thái đơn hàng.
- Phương thức thanh toán.
- Trạng thái thanh toán đại diện.

Bộ lọc danh sách:

- `status`.
- `fromDate`, `toDate`.
- `keyword`: tìm theo mã đơn, tên khách hàng, email hoặc số điện thoại.
- `page`, `size`, `sort`.

Nếu `fromDate` sau `toDate`, trả lỗi validation `400 Bad Request`.

## 6. Query danh sách đơn hàng

Không join fetch nhiều collection trong query phân trang vì dễ làm sai `totalElements`, duplicate row hoặc gặp `MultipleBagFetchException`.

Quy trình query danh sách:

1. Query page `Order` theo filter.
2. Lấy danh sách `orderIds` trong page.
3. Nếu `orderIds` rỗng, không query payment.
4. Nếu có `orderIds`, query payment theo danh sách order id.
5. Chọn payment đại diện.
6. Map sang `StaffOrderListItemResponse`.

Ví dụ:

```java
Page<Order> orderPage = orderRepository.searchStaffOrders(
        status,
        fromDate,
        toDate,
        normalizedKeyword,
        pageRequest
);

List<Long> orderIds = orderPage.getContent().stream()
        .map(Order::getId)
        .toList();

Map<Long, Payment> representativePaymentByOrderId;

if (orderIds.isEmpty()) {
    representativePaymentByOrderId = Map.of();
} else {
    representativePaymentByOrderId = chooseRepresentativePayments(
            paymentRepository.findAllByOrder_IdInOrderByCreatedAtDesc(orderIds)
    );
}
```

Quy tắc chọn payment đại diện:

1. Ưu tiên payment có `status = completed`.
2. Nếu có nhiều payment completed, chọn payment mới nhất theo `createdAt DESC`.
3. Nếu chưa có payment completed, chọn payment mới nhất theo `createdAt DESC`.
4. Nếu đơn không có payment, trả `paymentMethod = null`, `paymentStatus = null`.

Không lấy `order.getPayments().get(0)` vì collection không đảm bảo thứ tự.

## 7. Xem chi tiết đơn hàng

API chi tiết cần trả:

- Thông tin đơn hàng.
- Thông tin khách hàng.
- Địa chỉ giao hàng snapshot.
- Danh sách item theo snapshot sản phẩm tại thời điểm đặt hàng.
- Thông tin payment hiện tại.
- Timeline trạng thái từ `order_status_histories`.

Không suy timeline từ `orders.updatedAt` vì chỉ có một mốc cập nhật cuối cùng và không biết ai đã thao tác.

## 8. Quy tắc chuyển trạng thái

Tạo `OrderStatusTransitionPolicy` để gom rule chuyển trạng thái:

```java
@Component
public class OrderStatusTransitionPolicy {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.pending, Set.of(OrderStatus.processing, OrderStatus.cancelled),
            OrderStatus.processing, Set.of(OrderStatus.shipping, OrderStatus.cancelled),
            OrderStatus.shipping, Set.of(OrderStatus.completed),
            OrderStatus.completed, Set.of(),
            OrderStatus.cancelled, Set.of()
    );

    public void validate(OrderStatus currentStatus, OrderStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            throw new InvalidDataException("Order status is required");
        }
        if (!ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(targetStatus)) {
            throw new OrderStateConflictException(
                    "Không thể chuyển từ trạng thái " + currentStatus + " sang " + targetStatus
            );
        }
    }
}
```

`OrderStatusTransitionPolicy` là transition policy/rule table, không phải State Pattern GoF đầy đủ. Không nên trình bày class này là State Pattern trong báo cáo học thuật nếu không tách thành các state class riêng.

`OrderStateConflictException` phải được map thành HTTP `409 Conflict` trong global exception handler. Các lỗi validation như thiếu lý do hủy vẫn trả HTTP `400 Bad Request`.

## 9. Chức năng xác nhận đơn hàng

Endpoint:

```text
PATCH /api/staff/orders/{orderCode}/confirm
```

Rule:

```text
pending -> processing
```

Luồng xử lý:

1. Lock order bằng `PESSIMISTIC_WRITE`.
2. Validate transition `pending -> processing`.
3. Set `Order.status = processing`.
4. Insert `OrderStatusHistory`.
5. Publish `OrderStatusChangedEvent`.
6. Trả chi tiết đơn hàng mới.

Nếu đơn không ở `pending`, trả `409 Conflict`.

## 10. Chức năng bắt đầu giao hàng

Endpoint:

```text
PATCH /api/staff/orders/{orderCode}/ship
```

Rule:

```text
processing -> shipping
```

Luồng xử lý tương tự xác nhận đơn:

1. Lock order.
2. Validate transition.
3. Set `Order.status = shipping`.
4. Insert history `processing -> shipping`.
5. Publish event.
6. Trả response mới.

Nếu đơn không ở `processing`, trả `409 Conflict`.

## 11. Chức năng hoàn thành đơn hàng

Endpoint:

```text
PATCH /api/staff/orders/{orderCode}/complete
```

Rule:

```text
shipping -> completed
```

Staff chỉ được chuyển từ `shipping` sang `completed` khi có căn cứ giao hàng thành công. Request cần có:

```json
{
  "confirmationSource": "shipping_partner",
  "note": "GHN báo giao thành công lúc 14:30 ngày 21/06/2026"
}
```

`confirmationSource` nên giới hạn bằng enum hoặc danh sách giá trị hợp lệ:

| Giá trị | Ý nghĩa |
| --- | --- |
| `shipping_partner` | Đối tác vận chuyển xác nhận giao thành công |
| `internal_shipper` | Shipper nội bộ xác nhận giao thành công |
| `customer_confirmation` | Khách hàng xác nhận đã nhận hàng |
| `admin_instruction` | Admin/quản lý yêu cầu cập nhật sau đối soát |

Khi complete:

1. Lock order.
2. Validate transition `shipping -> completed`.
3. Validate `confirmationSource` và `note`.
4. Cập nhật payment COD nếu nghiệp vụ hiện tại yêu cầu.
5. Cộng điểm tích lũy/membership qua service hiện có.
6. Set `Order.status = completed`.
7. Insert history kèm `confirmationSource` và `note`.
8. Publish event.

Điểm tích lũy đã có trong hệ thống nên không tạo bảng điểm mới trong chức năng này. Nếu chưa có service riêng, tạo `LoyaltyPointService` nhưng phải tái sử dụng entity/repository/strategy hiện có.

## 12. Chức năng hủy đơn hàng của Staff

Endpoint:

```text
PATCH /api/staff/orders/{orderCode}/cancel
```

Request:

```json
{
  "reason": "Khách yêu cầu hủy trước khi giao hàng"
}
```

Rule hủy:

| Trạng thái hiện tại | Staff được hủy? | Ghi chú |
| --- | --- | --- |
| `pending` | Có | Hủy trước khi xử lý |
| `processing` | Có | Hủy trước khi giao |
| `shipping` | Không | Đơn đang giao, không dùng cancel thông thường |
| `completed` | Không | Đơn đã hoàn thành, không có trả hàng trong phạm vi này |
| `cancelled` | Không | Trả `409 Conflict`, không side effect |

Staff cancel không thực hiện hoàn tiền. Nếu đơn thanh toán online đã paid, Admin sẽ xử lý hoàn tiền hoặc nghiệp vụ liên quan trong chức năng Admin riêng.

Khi Staff hủy đơn:

1. Lock order.
2. Validate đơn được phép hủy.
3. Restore tồn kho qua `OrderInventoryAdjustmentService`.
4. Restore số lần dùng voucher qua `OrderVoucherAdjustmentService`.
5. Set `Order.status = cancelled`.
6. Insert `OrderStatusHistory` kèm lý do hủy, actor là Staff.
7. Publish `OrderStatusChangedEvent`.
8. Commit transaction.
9. Listener gửi email sau commit.

Nếu bất kỳ bước restore tồn kho, restore voucher, cập nhật order hoặc insert history thất bại, toàn bộ transaction cancel phải rollback.

### 12.1. Restore tồn kho

Chỉ hoàn lại tồn kho nếu đơn đã từng trừ tồn kho khi đặt hàng. Không được cộng lại tồn kho nhiều lần.

Service đề xuất:

```java
public interface OrderInventoryAdjustmentService {
    void restoreInventoryForCancelledOrder(Order order);
}
```

Yêu cầu:

- Gom quantity theo `productVariantId`.
- Lock product variant trước khi cộng lại tồn kho.
- Không restore nếu order đã `cancelled` từ trước.
- Chạy trong cùng transaction với cancel.

### 12.2. Restore voucher

Voucher đã được consume khi order được tạo thành công. Khi hủy đơn hợp lệ, cần điều chỉnh lại `Voucher.timesUsed`.

Service đề xuất:

```java
public interface OrderVoucherAdjustmentService {
    void restoreVoucherUsageForCancelledOrder(Order order);
}
```

Yêu cầu:

- Chỉ gọi khi order có voucher.
- Giảm `timesUsed` đúng một lần.
- Không cho `timesUsed` âm.
- Chạy trong cùng transaction với cancel.

Không cập nhật theo reservation lúc này vì order đã được tạo và reservation đã consumed.

## 13. Email thông báo

Tạo event chung:

```java
public record OrderStatusChangedEvent(
        Long orderId,
        String orderCode,
        Long customerId,
        String customerEmail,
        Long changedByStaffId,
        String changedByStaffEmail,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String reason,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal paidAmount,
        OffsetDateTime changedAt
) {
}
```

Listener bắt buộc dùng:

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```

Không gửi email trực tiếp trong service cập nhật đơn. Nếu transaction rollback thì không gửi email; nếu email lỗi thì không rollback trạng thái đơn đã commit.

Khi Staff hủy đơn:

- Customer nhận email thông báo đơn đã bị hủy và lý do hủy.
- Admin nhận email nội bộ để xem đơn, kiểm tra payment và thực hiện nghiệp vụ riêng như hoàn tiền nếu cần.
- Nội dung email Admin không được ghi là đã hoàn tiền.

## 14. Trình tự transaction cố định

Đối với mọi thao tác đổi trạng thái:

1. Lock `Order`.
2. Validate transition.
3. Thực hiện side effect nghiệp vụ.
4. Cập nhật `Order.status`.
5. Insert `OrderStatusHistory`.
6. Publish `OrderStatusChangedEvent`.
7. Commit transaction.
8. Listener gửi email sau commit bằng `AFTER_COMMIT`.

Riêng cancel:

1. Lock `Order`.
2. Validate order có được cancel hay không.
3. Restore inventory.
4. Restore voucher.
5. Set `Order.status = cancelled`.
6. Insert history.
7. Publish event.
8. Commit.
9. Listener gửi email sau commit bằng `AFTER_COMMIT`.

Không được publish event trước khi các side effect bắt buộc hoàn tất.

## 15. Lỗi và response

| Trường hợp | Exception/HTTP |
| --- | --- |
| Không tìm thấy đơn | `ResourceNotFoundException`, HTTP 404 |
| Sai trạng thái | `OrderStateConflictException`, HTTP 409 |
| Hủy đơn đã `cancelled` | `OrderStateConflictException`, HTTP 409 |
| Thiếu lý do hủy | Validation `@NotBlank`, HTTP 400 |
| Thiếu căn cứ complete | Validation, HTTP 400 |
| Page/size sai | `InvalidDataException` hoặc `IllegalArgumentException`, HTTP 400 |
| Role không phải Staff | Spring Security trả HTTP 403 |

Thông điệp lỗi trạng thái nên thống nhất:

```text
Không thể chuyển từ trạng thái {A} sang {B}
```

## 16. Test cần bổ sung

### 16.1. Test danh sách đơn hàng

- Trả response dạng `PageResponse<StaffOrderListItemResponse>`.
- Có đủ metadata: `pageNumber`, `pageSize`, `totalPages`, `totalElements`, `content`.
- Lọc theo status.
- Lọc theo ngày.
- Tìm theo keyword.
- Page rỗng không query payment với `IN ()`.

### 16.2. Test chi tiết đơn hàng

- Trả đủ thông tin order, customer, item, payment.
- Timeline đọc từ `order_status_histories`.
- Timeline sắp xếp theo `createdAt`, sau đó `id`.
- History ban đầu `null -> pending` hiển thị actor là `SYSTEM`.

### 16.3. Test chuyển trạng thái

- `pending -> processing` thành công.
- `processing -> shipping` thành công.
- `shipping -> completed` thành công khi có `confirmationSource` và `note`.
- Sai trạng thái trả `409 Conflict`.
- Thiếu dữ liệu request trả `400 Bad Request`.

### 16.4. Test cancel

- Staff hủy được đơn `pending`.
- Staff hủy được đơn `processing`.
- Không hủy được đơn `shipping`.
- Không hủy được đơn `completed`.
- Hủy đơn đã `cancelled` trả `409 Conflict` và không restore lần hai.
- Cancel rollback nếu restore inventory hoặc voucher thất bại.
- Staff cancel gửi email cho Customer và Admin sau commit.
- Staff cancel không gọi nghiệp vụ hoàn tiền.

### 16.5. Test concurrency

- Hai Staff cùng confirm một đơn: chỉ một request thành công.
- Hủy và complete cùng lúc: lock order đảm bảo chỉ một kết quả hợp lệ.
- Cancel không hoàn tồn kho/voucher nhiều lần.

## 17. Checklist triển khai

1. Tạo migration `order_status_histories`.
2. Tạo entity `OrderStatusHistory`.
3. Tạo `OrderStatusHistoryRepository`.
4. Bổ sung DTO request/response cho Staff order.
5. Tạo `OrderStatusTransitionPolicy`.
6. Tạo `OrderStateConflictException` và map HTTP 409.
7. Tạo `StaffOrderController`.
8. Tạo `StaffOrderService` và `StaffOrderServiceImpl`.
9. Cài đặt API danh sách bằng `PageResponse<T>`.
10. Cài đặt API chi tiết kèm timeline.
11. Cài đặt confirm.
12. Cài đặt ship.
13. Cài đặt complete kèm căn cứ giao hàng thành công và điểm tích lũy.
14. Cài đặt cancel kèm restore tồn kho, restore voucher và history.
15. Tạo `OrderStatusChangedEvent`.
16. Tạo listener email với `@TransactionalEventListener(phase = AFTER_COMMIT)`.
17. Bổ sung unit test, controller test và concurrency test.

## 18. Kết luận thiết kế

Thiết kế này không conflict với code hiện có nếu triển khai theo hướng mở rộng:

- Không thay đổi ý nghĩa enum `OrderStatus` hiện tại.
- Không thay đổi luồng đặt hàng/thanh toán hiện tại.
- Không đưa hoàn tiền online vào chức năng Staff cancel.
- Không tạo lại module voucher, tồn kho hoặc điểm tích lũy nếu hệ thống đã có.
- Chỉ bổ sung bảng history để đáp ứng yêu cầu timeline và audit.
- Dùng event sau commit để tách email khỏi transaction nghiệp vụ chính.
