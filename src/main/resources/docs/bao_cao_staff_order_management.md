# Báo cáo chức năng quản lí đơn hàng của nhân viên

## 1. Thông tin nhanh về nhánh

- Nhánh hiện tại: `feat/staff-order-management`
- Nhánh dùng để đối chiếu: `main`
- Cách đối chiếu: `git diff main...HEAD`
- Số commit trên nhánh: 5 commit
- Tổng thay đổi: 62 file, 6545 dòng thêm, 7 dòng xóa
- Lưu ý: file báo cáo này chỉ tổng hợp thay đổi của nhánh, không sửa code và không cần commit.

## 2. Tóm tắt chức năng đã thực hiện

Nhánh này đã xây dựng phân hệ quản lí đơn hàng cho nhân viên Staff với các nhóm chức năng chính:

1. Thêm API dành cho Staff tại base path `/api/staff/orders`, tất cả được bảo vệ bằng role `STAFF`.
2. Cho phép Staff xem danh sách đơn hàng có phân trang, lọc theo trạng thái, khoảng ngày, từ khóa và sắp xếp.
3. Cho phép Staff xem chi tiết đơn hàng, gồm thông tin khách hàng, địa chỉ giao hàng, voucher, sản phẩm, thanh toán và timeline trạng thái.
4. Cho phép Staff xác nhận đơn hàng: `pending -> processing`.
5. Cho phép Staff chuyển đơn sang đang giao: `processing -> shipping`.
6. Cho phép Staff hoàn thành đơn hàng: `shipping -> completed`.
   - Đơn COD pending sẽ được chuyển payment sang `completed`.
   - Khách hàng được cộng điểm tích lũy theo tổng tiền đơn hàng.
   - Có cập nhật hạng thành viên nếu đủ điểm.
7. Cho phép Staff hủy đơn hàng hợp lệ: `pending -> cancelled` hoặc `processing -> cancelled`.
   - Hoàn trả tồn kho cho các biến thể sản phẩm trong đơn.
   - Hoàn lại lượt sử dụng voucher nếu đơn có voucher.
   - Không tự động hoàn tiền online; đánh dấu cần kiểm tra thủ công khi payment online đã completed.
   - Gửi email thông báo cho khách hàng và admin sau khi transaction commit thành công.
8. Thêm bảng `order_status_histories` để lưu timeline/audit trạng thái đơn hàng.
9. Tích hợp ghi lịch sử trạng thái ban đầu khi tạo đơn từ COD, Momo IPN và VNPAY IPN.
10. Bổ sung test unit, controller test, security test và integration test cho luồng Staff order management.

## 3. API đã thêm cho Staff

| Chức năng | Method | Endpoint | Mô tả |
| --- | --- | --- | --- |
| Xem danh sách đơn hàng | `GET` | `/api/staff/orders` | Lọc theo `status`, `fromDate`, `toDate`, `keyword`; hỗ trợ `page`, `size`, `sortBy`, `sortDir`. |
| Xem chi tiết đơn hàng | `GET` | `/api/staff/orders/{orderCode}` | Lấy chi tiết đơn, item, payment đại diện và timeline trạng thái. |
| Xác nhận đơn | `PATCH` | `/api/staff/orders/{orderCode}/confirm` | Chuyển đơn từ `pending` sang `processing`. |
| Bắt đầu giao hàng | `PATCH` | `/api/staff/orders/{orderCode}/ship` | Chuyển đơn từ `processing` sang `shipping`. |
| Hoàn thành đơn | `PATCH` | `/api/staff/orders/{orderCode}/complete` | Chuyển `shipping` sang `completed`, xử lí COD payment và điểm tích lũy. |
| Hủy đơn | `PATCH` | `/api/staff/orders/{orderCode}/cancel` | Hủy đơn hợp lệ, hoàn kho, hoàn voucher usage, ghi lí do và gửi thông báo. |

## 4. Trạng thái đơn hàng được hỗ trợ

Luồng hợp lệ:

```text
pending -> processing
pending -> cancelled
processing -> shipping
processing -> cancelled
shipping -> completed
```

Các luồng bị chặn:

- `shipping -> cancelled`
- `completed -> *`
- `cancelled -> *`
- Chuyển cùng trạng thái.
- Chuyển tắt bước như `pending -> shipping`, `processing -> completed`.

## 5. File mới được sinh ra

### 5.1. Main source

- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/controller/staff/StaffOrderController.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/request/StaffCancelOrderRequest.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/request/StaffCompleteOrderRequest.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/response/StaffOrderDetailResponse.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/response/StaffOrderItemResponse.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/response/StaffOrderListItemResponse.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/response/StaffOrderStatusTimelineResponse.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/dto/response/StaffPaymentSummaryResponse.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/entity/OrderStatusHistory.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/enums/OrderCompletionSource.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/event/OrderStatusChangedEvent.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/exception/OrderStateConflictException.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/listener/OrderStatusChangedEventListener.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/pattern/specification/OrderSpecification.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/policy/OrderStatusTransitionPolicy.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/repository/OrderStatusHistoryRepository.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/LoyaltyPointAwardResult.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/LoyaltyPointService.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/OrderInventoryAdjustmentService.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/OrderStatusHistoryService.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/OrderVoucherAdjustmentService.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/StaffOrderService.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/LoyaltyPointServiceImpl.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/OrderInventoryAdjustmentServiceImpl.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/OrderStatusHistoryServiceImpl.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/OrderVoucherAdjustmentServiceImpl.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/StaffOrderServiceImpl.java`

### 5.2. Database và tài liệu

- `src/main/resources/db/phase3_order_status_history_schema_patch.sql`
- `src/main/resources/docs/ke_hoach_xay_dung_chuc_nang_quan_li_don_hang_cua_nhan_vien.md`

### 5.3. Test mới

- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/controller/staff/StaffOrderControllerSecurityTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/controller/staff/StaffOrderControllerTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/StaffOrderCancelConcurrencyIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/StaffOrderCancelIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/StaffOrderCompleteConcurrencyIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/StaffOrderCompleteIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/StaffOrderConfirmConcurrencyIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/StaffOrderTransitionIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/listener/OrderStatusChangedEventListenerTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/policy/OrderStatusTransitionPolicyTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/service/LoyaltyPointServiceImplTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/service/OrderInventoryAdjustmentServiceImplTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/service/OrderStatusHistoryServiceImplTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/service/OrderVoucherAdjustmentServiceImplTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/service/StaffOrderServiceImplTest.java`

## 6. File cũ được chỉnh sửa

- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/exception/GlobalExceptionHandling.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/repository/OrderItemRepository.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/repository/OrderRepository.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/repository/PaymentRepository.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/repository/UserRepository.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/EmailService.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/BrevoEmailServiceImpl.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/impl/OrderServiceImpl.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/internal/MomoIpnTransactionService.java`
- `src/main/java/vn/hcmute/edu/dp/nhom10/backend/service/internal/VnPayIpnTransactionService.java`
- `src/main/resources/db/database_schema.sql`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/AbstractPostgresIntegrationTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/MomoIpnIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/PlaceOrderCodIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/integration/VnPayIpnIT.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/service/OrderServiceImplTest.java`
- `src/test/java/vn/hcmute/edu/dp/nhom10/backend/service/VnPayIpnTransactionServiceTest.java`
- `src/test/resources/db/cleanup_place_order.sql`

## 7. Chi tiết file và hàm chức năng

### 7.1. Controller và API

#### `StaffOrderController.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `getOrders(...)` | Nhận query filter/sort/page từ request Staff, gọi service lấy danh sách đơn hàng và trả `ApiResponse`. |
| `getOrderDetail(String orderCode)` | Lấy chi tiết đơn hàng theo mã đơn. |
| `confirmOrder(String orderCode, Authentication authentication)` | Lấy Staff hiện tại từ authentication và xác nhận đơn `pending -> processing`. |
| `shipOrder(String orderCode, Authentication authentication)` | Lấy Staff hiện tại và chuyển đơn `processing -> shipping`. |
| `completeOrder(String orderCode, StaffCompleteOrderRequest request, Authentication authentication)` | Hoàn thành đơn `shipping -> completed`, yêu cầu có nguồn xác nhận và ghi chú. |
| `cancelOrder(String orderCode, StaffCancelOrderRequest request, Authentication authentication)` | Hủy đơn hợp lệ, yêu cầu có lí do hủy. |

### 7.2. DTO request/response

#### `StaffCancelOrderRequest.java` - file mới

| Thành phần | Chức năng |
| --- | --- |
| `StaffCancelOrderRequest(String reason)` | Request hủy đơn; bắt buộc `reason` không rỗng và tối đa 500 kí tự. |

#### `StaffCompleteOrderRequest.java` - file mới

| Thành phần | Chức năng |
| --- | --- |
| `StaffCompleteOrderRequest(OrderCompletionSource confirmationSource, String note)` | Request hoàn thành đơn; bắt buộc có nguồn xác nhận và ghi chú tối đa 500 kí tự. |

#### `StaffOrderListItemResponse.java` - file mới

Response item cho danh sách đơn hàng, gồm: `orderCode`, thông tin khách hàng, `createdAt`, `totalAmount`, `status`, `paymentMethod`, `paymentStatus`.

#### `StaffOrderDetailResponse.java` - file mới

Response chi tiết đơn hàng, gồm: thông tin tiền, khách hàng, địa chỉ giao hàng, voucher, danh sách item, payment đại diện và timeline trạng thái.

#### `StaffOrderItemResponse.java` - file mới

Response từng sản phẩm trong đơn, gồm: `productVariantId`, `sku`, tên sản phẩm, thông tin biến thể, đơn giá, số lượng và thành tiền.

#### `StaffPaymentSummaryResponse.java` - file mới

Response tóm tắt payment, gồm: `id`, `method`, `status`, `amount`, `transactionId`, `paidAt`, `createdAt`.

#### `StaffOrderStatusTimelineResponse.java` - file mới

Response timeline trạng thái, gồm: trạng thái cũ/mới, người thao tác, role, nhãn hiển thị, lí do, metadata và thời điểm tạo.

### 7.3. Service quản lí đơn hàng Staff

#### `StaffOrderService.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `getOrders(...)` | Khai báo nghiệp vụ lấy danh sách đơn hàng cho Staff. |
| `getOrderDetail(String orderCode)` | Khai báo nghiệp vụ lấy chi tiết đơn hàng. |
| `confirmOrder(String orderCode, Long staffUserId)` | Khai báo nghiệp vụ xác nhận đơn. |
| `shipOrder(String orderCode, Long staffUserId)` | Khai báo nghiệp vụ chuyển sang giao hàng. |
| `completeOrder(String orderCode, Long staffUserId, StaffCompleteOrderRequest request)` | Khai báo nghiệp vụ hoàn thành đơn. |
| `cancelOrder(String orderCode, Long staffUserId, StaffCancelOrderRequest request)` | Khai báo nghiệp vụ hủy đơn. |

#### `StaffOrderServiceImpl.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `getOrders(...)` | Validate page/date, tạo `Specification`, phân trang/sắp xếp, lấy payment đại diện và map sang `PageResponse<StaffOrderListItemResponse>`. |
| `getOrderDetail(String orderCode)` | Chuẩn hóa mã đơn, lấy order, item, payment và lịch sử trạng thái để trả chi tiết. |
| `confirmOrder(...)` | Chuyển đơn sang `processing` thông qua hàm chung `transitionOrder`. |
| `shipOrder(...)` | Chuyển đơn sang `shipping` thông qua hàm chung `transitionOrder`. |
| `completeOrder(...)` | Validate request, khóa order, kiểm tra transition, xử lí COD payment, cộng điểm tích lũy, set `completed`, ghi timeline metadata và publish event. |
| `cancelOrder(...)` | Validate lí do, khóa order, kiểm tra transition, chụp thông tin payment, hoàn kho, hoàn voucher usage, set `cancelled`, ghi timeline và publish event thông báo hủy. |
| `transitionOrder(...)` | Xử lí các transition đơn giản `confirm` và `ship`: khóa order, validate trạng thái, cập nhật status, ghi history và publish event. |
| `validateCancelRequest(...)` | Kiểm tra request hủy đơn không null, lí do không rỗng, không quá 500 kí tự. |
| `validateCompleteRequest(...)` | Kiểm tra request hoàn thành không null, có `confirmationSource`, có `note`, note không quá 500 kí tự. |
| `completeCodPaymentIfPresent(...)` | Nếu đơn COD có payment `pending` thì chuyển sang `completed` và set `paidAt`; nếu đã completed thì không ghi đè; nếu failed/refunded thì báo conflict. |
| `completionMetadata(...)` | Tạo metadata cho timeline hoàn thành đơn: nguồn xác nhận, kết quả COD payment, điểm được cộng, tier trước/sau. |
| `cancellationMetadata(...)` | Tạo metadata cho timeline hủy đơn: đã hoàn kho, có hoàn voucher usage, không refund tự động, có cần review refund thủ công hay không. |
| `findStaffActor(...)` | Kiểm tra và lấy user Staff thao tác theo `staffUserId`. |
| `publishStatusChangedEvent(...)` | Phát `OrderStatusChangedEvent` sau khi đổi trạng thái, có overload cho trường hợp kèm thông tin payment/refund review. |
| `representativePaymentSnapshot(...)` | Lấy payment đại diện của đơn để xác định thông tin thông báo và refund review khi hủy. |
| `requiresManualRefundReview(...)` | Xác định đơn online đã thanh toán completed có cần review hoàn tiền thủ công hay không. |
| `representativePayments(...)` | Lấy payment đại diện cho nhiều order trong màn danh sách, tránh truy vấn lặp lại từng đơn. |
| `chooseRepresentativePayment(...)` | Ưu tiên payment `completed`, nếu không có thì lấy payment mới nhất. |
| `paymentCreatedAtDesc()` | Comparator sắp xếp payment mới nhất trước. |
| `toListItemResponse(...)` | Map order và payment sang item danh sách. |
| `toDetailResponse(...)` | Map order, item, payment, history sang response chi tiết. |
| `toOrderItemResponse(...)` | Map `OrderItem` sang response item. |
| `toPaymentSummaryResponse(...)` | Map `Payment` sang response tóm tắt payment. |
| `toTimelineResponse(...)` | Map `OrderStatusHistory` sang timeline response. |
| `actorLabel(...)` | Tạo tên hiển thị người thao tác; nếu system thì hiện `SYSTEM`. |
| `validatePage(...)` | Chặn page âm, size <= 0, size > 100. |
| `validateDateRange(...)` | Chặn `fromDate` sau `toDate`. |
| `resolveSort(...)` | Ghép field sort và direction thành `Sort`. |
| `normalizeSortBy(...)` | Chỉ cho phép sort theo `createdAt`, `updatedAt`, `totalAmount`, `status`, `orderCode`. |
| `resolveDirection(...)` | Chỉ cho phép `asc` hoặc `desc`. |
| `normalizeKeyword(...)` | Trim và lower-case từ khóa tìm kiếm. |
| `normalizeOrderCode(...)` | Trim mã đơn, chặn null/blank. |
| `toStartOfDay(...)` | Đổi `LocalDate` thành đầu ngày theo timezone hệ thống. |
| `toStartOfNextDay(...)` | Đổi `toDate` thành mốc đầu ngày kế tiếp để lọc exclusive. |
| `CodPaymentCompletionResult` | Record nội bộ mô tả kết quả xử lí COD payment. |
| `PaymentSnapshot` | Record nội bộ lưu method/status/amount của payment đại diện. |

### 7.4. Policy và exception trạng thái

#### `OrderStatusTransitionPolicy.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `validate(OrderStatus currentStatus, OrderStatus targetStatus)` | Kiểm tra transition có nằm trong danh sách cho phép hay không; nếu sai thì ném `OrderStateConflictException`. |

#### `OrderStateConflictException.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `OrderStateConflictException(String message)` | Exception riêng cho lỗi xung đột trạng thái đơn hàng. |

#### `GlobalExceptionHandling.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `handleOrderStateConflictException(...)` | Bắt `OrderStateConflictException` và trả HTTP `409 CONFLICT` với message rõ ràng. |

### 7.5. Lịch sử trạng thái đơn hàng

#### `OrderStatusHistory.java` - file mới

Entity mới map bảng `order_status_histories`, lưu:

- `order`
- `fromStatus`
- `toStatus`
- `changedBy`
- `changedByRole`
- `reason`
- `metadata` dạng JSONB
- `createdAt`

#### `OrderStatusHistoryRepository.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `findAllByOrder_IdOrderByCreatedAtAscIdAsc(Long orderId)` | Lấy timeline của một order theo thời gian tăng dần và id tăng dần. |

#### `OrderStatusHistoryService.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `recordInitialStatus(Order order)` | Khai báo ghi trạng thái ban đầu của đơn. |
| `recordTransition(...)` | Khai báo ghi một lần chuyển trạng thái của đơn. |

#### `OrderStatusHistoryServiceImpl.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `recordInitialStatus(Order order)` | Ghi history đầu tiên với `fromStatus = null`, `toStatus = order.status`, actor là system. |
| `recordTransition(...)` | Ghi history khi Staff đổi trạng thái, lưu actor, role, lí do và metadata. |
| `validateOrder(...)` | Kiểm tra order đã persisted và có status trước khi ghi initial history. |
| `validateTransition(...)` | Kiểm tra order, from/to status và actor trước khi ghi transition. |
| `validateOrderIdentity(...)` | Kiểm tra order tồn tại và đã có id. |

### 7.6. Lọc và truy vấn dữ liệu

#### `OrderSpecification.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `hasStatus(OrderStatus status)` | Tạo điều kiện lọc theo trạng thái nếu có. |
| `createdAtGreaterThanOrEqualTo(OffsetDateTime fromDateTime)` | Tạo điều kiện lọc từ ngày. |
| `createdAtLessThan(OffsetDateTime toDateTimeExclusive)` | Tạo điều kiện lọc đến trước mốc ngày kế tiếp. |
| `hasKeyword(String keyword)` | Tìm theo mã đơn, số điện thoại giao hàng, tên/email/phone khách hàng. |
| `escapeLike(String value)` | Escape kí tự đặc biệt khi dùng `LIKE`. |

#### `OrderRepository.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| Kế thừa thêm `JpaSpecificationExecutor<Order>` | Hỗ trợ truy vấn danh sách đơn bằng `Specification`. |
| `findByOrderCodeForUpdate(String orderCode)` | Khóa pessimistic order theo mã đơn khi Staff cập nhật trạng thái, giảm race condition. |

#### `OrderItemRepository.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `findAllByOrderIdWithVariantOrderById(Long orderId)` | Lấy item của đơn kèm `productVariant` bằng join fetch, phục vụ chi tiết đơn và hoàn kho. |

#### `PaymentRepository.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `findAllByOrderId(Long orderId)` | Lấy payment của một đơn. |
| `findAllByOrder_IdInOrderByCreatedAtDesc(Collection<Long> orderIds)` | Lấy payment cho nhiều đơn khi hiện danh sách Staff. |
| `findAllByOrderIdForUpdate(Long orderId)` | Khóa payment của đơn khi hoàn thành COD. |

#### `UserRepository.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `findAllByRoleAndIsActiveTrue(UserRole role)` | Lấy admin active để gửi email thông báo hủy đơn. |
| `findByIdForUpdate(Long id)` | Khóa user khi cộng điểm tích lũy để tránh cập nhật đồng thời. |

### 7.7. Hoàn kho, voucher và điểm tích lũy

#### `OrderInventoryAdjustmentService.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `restoreInventoryForCancelledOrder(Order order)` | Khai báo nghiệp vụ hoàn tồn kho khi hủy đơn. |

#### `OrderInventoryAdjustmentServiceImpl.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `restoreInventoryForCancelledOrder(Order order)` | Lấy item của đơn, gom số lượng theo variant, khóa variant theo id tăng dần và cộng lại tồn kho. |
| `validateOrder(Order order)` | Kiểm tra order hợp lệ và đã persisted. |
| `aggregateQuantityByVariantId(List<OrderItem> orderItems)` | Gom số lượng theo `productVariantId`, xử lí trường hợp một variant xuất hiện nhiều dòng. |
| `variantId(OrderItem orderItem)` | Lấy id variant và chặn item thiếu variant. |
| `quantity(OrderItem orderItem)` | Lấy số lượng và chặn số lượng null/không dương. |

#### `OrderVoucherAdjustmentService.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `restoreVoucherUsageForCancelledOrder(Order order)` | Khai báo nghiệp vụ hoàn lại lượt sử dụng voucher khi hủy đơn. |

#### `OrderVoucherAdjustmentServiceImpl.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `restoreVoucherUsageForCancelledOrder(Order order)` | Nếu đơn có voucher, khóa voucher và giảm `timesUsed` đi 1; nếu không có voucher thì bỏ qua. |

#### `LoyaltyPointService.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `awardForCompletedOrder(Order order)` | Khai báo nghiệp vụ cộng điểm khi đơn hoàn thành. |

#### `LoyaltyPointAwardResult.java` - file mới

Record trả về kết quả cộng điểm: điểm được cộng, điểm trước/sau, tier trước/sau, có đổi tier hay không.

#### `LoyaltyPointServiceImpl.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `awardForCompletedOrder(Order order)` | Khóa customer, cộng điểm theo `totalAmount / 1000`, tính tier mới và trả kết quả. |
| `calculateAwardedPoints(BigDecimal totalAmount)` | Làm tròn xuống số điểm từ tổng tiền. |
| `determineResultingTier(User customer, MembershipTier previousTier)` | Tìm tier phù hợp theo điểm sau khi cộng, không hạ cấp tier. |
| `hasTierChanged(MembershipTier previousTier, MembershipTier resultingTier)` | Xác định tier có thay đổi hay không. |
| `tierName(MembershipTier tier)` | Lấy tên tier để đưa vào metadata. |

### 7.8. Event và email thông báo hủy đơn

#### `OrderStatusChangedEvent.java` - file mới

Record sự kiện đổi trạng thái đơn hàng, gồm: order, customer, Staff, trạng thái cũ/mới, lí do, thông tin payment, có cần review refund thủ công và thời điểm đổi trạng thái.

#### `OrderStatusChangedEventListener.java` - file mới

| Hàm | Chức năng |
| --- | --- |
| `handleOrderStatusChanged(OrderStatusChangedEvent event)` | Lắng nghe sau commit; chỉ xử lí khi trạng thái mới là `cancelled`. |
| `sendCustomerEmail(OrderStatusChangedEvent event)` | Gửi email thông báo hủy đơn cho khách hàng, có bắt lỗi để không chặn admin email. |
| `sendAdminEmails(OrderStatusChangedEvent event)` | Lấy admin active, loại trùng email và gửi thông báo hủy đơn cho admin. |

#### `EmailService.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `sendOrderCancellationEmailToCustomer(...)` | Khai báo gửi email hủy đơn cho khách hàng. |
| `sendOrderCancellationEmailToAdmin(...)` | Khai báo gửi email thông báo Staff hủy đơn cho admin. |

#### `BrevoEmailServiceImpl.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `sendOrderCancellationEmailToCustomer(...)` | Tạo nội dung email hủy đơn cho khách hàng và gửi qua Brevo. |
| `sendOrderCancellationEmailToAdmin(...)` | Tạo nội dung email cho admin, kèm staff, customer, payment và refund review. |
| `sendTransactionalEmail(...)` | Hàm dùng chung để gửi transactional email qua Brevo API. |

### 7.9. Enum và request context

#### `OrderCompletionSource.java` - file mới

Enum nguồn xác nhận hoàn thành đơn:

- `shipping_partner`
- `internal_shipper`
- `customer_confirmation`
- `admin_instruction`

### 7.10. Tích hợp vào luồng tạo đơn có sẵn

#### `OrderServiceImpl.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `createCodOrder(...)` | Sau khi save order COD, gọi `orderStatusHistoryService.recordInitialStatus(savedOrder)` để tạo timeline ban đầu. |

#### `MomoIpnTransactionService.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `process(...)` | Khi IPN Momo thành công và tạo order, ghi initial status history cho order vừa sinh. |

#### `VnPayIpnTransactionService.java` - file cũ được chỉnh sửa

| Hàm/phần thay đổi | Chức năng |
| --- | --- |
| `process(...)` | Khi IPN VNPAY thành công và tạo order, ghi initial status history cho order vừa sinh. |

### 7.11. Database

#### `database_schema.sql` - file cũ được chỉnh sửa

Thêm bảng:

- `order_status_histories`

Cột chính:

- `id`
- `order_id`
- `from_status`
- `to_status`
- `changed_by`
- `changed_by_role`
- `reason`
- `metadata`
- `created_at`

Thêm index:

- `idx_order_status_history_order_time_id` trên `(order_id, created_at, id)`

#### `phase3_order_status_history_schema_patch.sql` - file mới

Patch SQL chạy trên database PostgreSQL hiện có:

- `CREATE TABLE IF NOT EXISTS order_status_histories`
- Tạo comment bảng.
- Tạo index `idx_order_status_history_order_time_id`.
- Bao trong transaction `BEGIN`/`COMMIT`.

### 7.12. Test đã thêm/chỉnh sửa

#### Test controller và security

| File | Nội dung kiểm thử |
| --- | --- |
| `StaffOrderControllerSecurityTest.java` | Kiểm tra các endpoint Staff yêu cầu authentication/role `STAFF`, chặn user không đúng quyền. |
| `StaffOrderControllerTest.java` | Kiểm tra mapping API, request param, request body, validate input, response message/data cho list/detail/confirm/ship/complete/cancel. |

#### Test service và policy mới

| File | Nội dung kiểm thử |
| --- | --- |
| `StaffOrderServiceImplTest.java` | Kiểm tra list/detail, confirm, ship, complete, cancel, conflict, rollback side effect, publish event, metadata. |
| `OrderStatusTransitionPolicyTest.java` | Kiểm tra các transition hợp lệ và các transition bị từ chối. |
| `OrderStatusHistoryServiceImplTest.java` | Kiểm tra ghi initial history, ghi transition và validate input. |
| `OrderInventoryAdjustmentServiceImplTest.java` | Kiểm tra hoàn tồn kho, gom duplicate variant, khóa variant và các lỗi dữ liệu. |
| `OrderVoucherAdjustmentServiceImplTest.java` | Kiểm tra hoàn usage voucher, trường hợp không có voucher và dữ liệu bất thường. |
| `LoyaltyPointServiceImplTest.java` | Kiểm tra cộng điểm, nâng tier, không hạ tier, lỗi khi thiếu customer. |
| `OrderStatusChangedEventListenerTest.java` | Kiểm tra email hủy đơn cho customer/admin, admin email bị trùng, lỗi email không chặn luồng còn lại, bỏ qua event không phải cancel. |

#### Test integration mới

| File | Nội dung kiểm thử |
| --- | --- |
| `StaffOrderTransitionIT.java` | Kiểm tra confirm/ship thành công, timeline đúng và conflict không làm đổi dữ liệu. |
| `StaffOrderConfirmConcurrencyIT.java` | Kiểm tra đồng thời xác nhận một đơn chỉ một transaction thành công. |
| `StaffOrderCancelIT.java` | Kiểm tra hủy đơn pending/processing, hoàn kho/voucher, event, refund review, rollback khi lỗi. |
| `StaffOrderCancelConcurrencyIT.java` | Kiểm tra hủy đơn đồng thời, tránh hoàn kho/voucher hai lần. |
| `StaffOrderCompleteIT.java` | Kiểm tra hoàn thành đơn COD/online, payment, loyalty, history, event, rollback khi lỗi. |
| `StaffOrderCompleteConcurrencyIT.java` | Kiểm tra hoàn thành đồng thời, tránh cộng điểm/payment hai lần. |

#### Test cũ được chỉnh sửa

| File | Nội dung thay đổi |
| --- | --- |
| `OrderServiceImplTest.java` | Bổ sung kiểm tra `createCodOrder_success_recordsInitialHistory` và thứ tự gọi dependency có history. |
| `PlaceOrderCodIT.java` | Bổ sung assert initial status history cho đơn COD. |
| `MomoIpnIT.java` | Bổ sung assert initial status history cho order tạo từ Momo IPN. |
| `VnPayIpnIT.java` | Bổ sung assert initial status history cho order tạo từ VNPAY IPN. |
| `VnPayIpnTransactionServiceTest.java` | Cập nhật mock/expectation liên quan đến luồng IPN mới. |
| `AbstractPostgresIntegrationTest.java` | Bổ sung schema initialization, event probe, service wrapper để giả lập lỗi loyalty/history trong integration test. |
| `cleanup_place_order.sql` | Bổ sung cleanup bảng history để test không bị dữ liệu tồn. |

## 8. Kết quả nghiệp vụ đạt được

Sau nhánh này, Staff có thể quản lí vòng đời đơn hàng đầy đủ hơn:

- Tìm và xem đơn hàng để xử lí.
- Xem timeline lịch sử trạng thái của từng đơn.
- Xác nhận đơn mới.
- Chuyển đơn sang đang giao.
- Xác nhận giao thành công và hoàn tất payment COD/điểm tích lũy.
- Hủy đơn khi còn ở trạng thái được phép, đồng thời khôi phục tồn kho và voucher usage.
- Admin và customer được thông báo khi Staff hủy đơn.
- Hệ thống có cơ chế chặn xung đột trạng thái và xung đột thao tác đồng thời.

## 9. Ghi chú rủi ro/lưu ý

- Hủy đơn online đã thanh toán không tự động refund; chỉ đánh dấu `requiresManualRefundReview`.
- Timeline chỉ bắt đầu đầy đủ cho đơn được tạo sau khi có logic `recordInitialStatus`; dữ liệu cũ nếu đã có sẵn trước đó cần script bổ sung nếu muốn có timeline lịch sử.
- Một số chuỗi message tiếng Việt trong code hiện tại có dấu hiệu lỗi mã hóa, nhưng file báo cáo này không sửa code theo đúng yêu cầu.
