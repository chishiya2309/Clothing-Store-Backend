# Phân tích chức năng Voucher

> Module: Voucher / Mã giảm giá  
> Tác nhân chính: Staff, Customer  
> Trạng thái tài liệu: Đề xuất đặc tả chi tiết để phát triển backend và đối chiếu báo cáo

---

## 1. Kiểm tra tài liệu hiện có

Hiện tại hệ thống đã có nhắc đến voucher trong nhiều tài liệu, nhưng chưa có một file riêng phân tích đầy đủ module voucher.

Các tài liệu đã có nội dung liên quan:

| Tài liệu | Nội dung đã có |
|---|---|
| `Nhom10_FinalProject1.md` | Mô tả voucher/mã giảm giá là dữ liệu nghiệp vụ gồm mã voucher, loại giảm, giá trị, điều kiện áp dụng, ngày bắt đầu/kết thúc, số lượng và số lần đã dùng. Có QĐ7, QĐ11 và CT2 cho quy định/công thức voucher. |
| `usecase_detail_donhang.md` | Có UC-19: Khách hàng nhập mã voucher để áp dụng khi đặt hàng. |
| `usecase_detail_khac.md` | Có UC-27: Staff tạo/quản lý voucher khuyến mãi. |
| `infrastructure.md` | Có nhắc đến bảng `vouchers`, luồng áp dụng voucher khi đặt hàng và nhóm dữ liệu marketing. |
| `project_structure.md` | Có dự kiến `AdminVoucherController`, `VoucherService`, `Voucher.java` và pattern tính giảm giá voucher. Tài liệu cũ có nhắc Factory Pattern, nhưng module voucher nên ưu tiên Strategy Pattern để mở rộng thuật toán ưu đãi. |
| `database_schema.sql` | Đã có bảng `vouchers`, enum `discount_type`, liên kết `orders.voucher_id` và trường `orders.discount_amount`. |

Kết luận: đã có mô tả rời rạc về voucher, nhưng chưa có tài liệu riêng thể hiện rõ 2 nhóm người dùng Staff/Customer, luồng quản lý, luồng sử dụng, và định hướng áp dụng Strategy Pattern + State Pattern.

---

## 2. Mục tiêu chức năng

Voucher là mã giảm giá được Staff tạo và quản lý trong hệ thống. Customer sử dụng voucher khi đặt hàng bằng cách nhập chính xác mã voucher vào ô mã giảm giá ở bước checkout.

Trong giai đoạn hiện tại, hệ thống hỗ trợ 2 loại voucher:

| Loại voucher | Ý nghĩa | Ví dụ |
|---|---|---|
| `percentage` | Giảm theo phần trăm trên tổng tiền sản phẩm | Giảm 10%, tối đa 50.000đ |
| `fixed_amount` | Giảm một số tiền cố định | Giảm thẳng 30.000đ |

Trong tương lai, hệ thống có thể mở rộng thêm các loại ưu đãi khác như:

- `free_shipping`: miễn phí vận chuyển.
- `buy_one_get_one`: mua 1 tặng 1.
- `bundle_discount`: giảm giá theo combo sản phẩm.
- `category_discount`: giảm giá cho một nhóm danh mục nhất định.

Trong giai đoạn sau, voucher có thể được hiển thị trên banner hoặc khu vực khuyến mãi để Customer nhìn thấy và chọn nhanh hơn. Tuy nhiên ở giai đoạn hiện tại, Customer phải nhập đúng mã để hệ thống tìm thấy voucher.

---

## 3. Tác nhân và quyền hạn

| Tác nhân | Quyền / hành vi |
|---|---|
| Staff | Tạo mới voucher, xem danh sách voucher, sửa thông tin voucher, xóa hoặc vô hiệu hóa voucher. |
| Customer | Nhập mã voucher khi đặt hàng, kiểm tra voucher hợp lệ, áp dụng voucher vào đơn hàng. |

Ghi chú:

- Staff không trực tiếp áp dụng voucher cho đơn hàng của Customer trong luồng checkout thông thường.
- Admin không quản lý voucher trong phạm vi chức năng này; quyền quản lý voucher thuộc về Staff.
- Customer không được xem toàn bộ danh sách voucher trong giai đoạn hiện tại, trừ khi sau này voucher được public qua banner hoặc trang khuyến mãi.
- Mỗi đơn hàng chỉ áp dụng 1 voucher tại một thời điểm.

---

## 4. Dữ liệu voucher

Theo `database_design.md`, hệ thống quy định **kiểu tiền** là `NUMERIC(12,2)` trong PostgreSQL để đảm bảo tính chính xác, không dùng `FLOAT`/`DOUBLE` cho giá tiền. Khi ánh xạ sang Java, các trường tiền tệ dùng `BigDecimal`.

Các trường tiền tệ liên quan đến voucher và đơn hàng:

| Nhóm dữ liệu | PostgreSQL | Java | Ghi chú |
|---|---|---|---|
| Giá trị giảm tiền, mức giảm tối đa, đơn tối thiểu | `NUMERIC(12,2)` | `BigDecimal` | Dùng cho `fixed_amount`, `maxDiscountAmount`, `minOrderAmount`. |
| Tổng tiền hàng, phí ship, tiền giảm, tổng thanh toán | `NUMERIC(12,2)` | `BigDecimal` | Dùng khi Customer áp dụng voucher vào đơn hàng. |
| Phần trăm giảm | Hiện lưu chung trong `discount_value NUMERIC(12,2)` | `BigDecimal` | Với `percentage`, service validate giá trị không vượt quá `100`. |

Các thông tin chính của voucher:

| Trường | Kiểu DB | Kiểu Java | Mô tả |
|---|---|---|---|
| `code` | `VARCHAR(50)` | `String` | Mã voucher, duy nhất, Customer phải nhập chính xác để áp dụng. |
| `discountType` | `discount_type` | `DiscountType` | Loại giảm giá: `percentage` hoặc `fixed_amount`. |
| `discountValue` | `NUMERIC(12,2)` | `BigDecimal` | Giá trị giảm: phần trăm hoặc số tiền tùy theo loại voucher. |
| `maxDiscountAmount` | `NUMERIC(12,2)` | `BigDecimal` | Mức giảm tối đa, thường dùng cho voucher phần trăm. |
| `minOrderAmount` | `NUMERIC(12,2)` | `BigDecimal` | Giá trị đơn hàng tối thiểu để được áp dụng voucher. |
| `startDate` | `TIMESTAMPTZ` | `OffsetDateTime` | Thời điểm voucher bắt đầu có hiệu lực. |
| `endDate` | `TIMESTAMPTZ` | `OffsetDateTime` | Thời điểm voucher hết hiệu lực. |
| `usageLimit` | `INTEGER` | `Integer` | Tổng số lượt được phép sử dụng. |
| `timesUsed` | `INTEGER` | `Integer` | Số lượt đã sử dụng. |
| `isActive` | `BOOLEAN` | `Boolean` | Cho biết voucher đang bật hay đã bị vô hiệu hóa. |

Liên kết đơn hàng:

- `orders.voucher_id`: voucher đã được áp dụng vào đơn hàng.
- `orders.discount_amount`: số tiền giảm thực tế tại thời điểm đặt hàng.

Nên lưu `discount_amount` trong đơn hàng để đảm bảo lịch sử đơn hàng không bị thay đổi nếu Staff sửa voucher sau này.

---

## 5. Use Case Staff: Quản lý voucher

### UC-V01: Tạo voucher

| Thuộc tính | Mô tả |
|---|---|
| Tác nhân | Staff |
| Tiền điều kiện | Staff đã đăng nhập và có quyền quản lý voucher. |
| Hậu điều kiện | Voucher mới được lưu vào hệ thống. |

Luồng chính:

| Bước | Staff | Hệ thống |
|---|---|---|
| 1 | Truy cập màn hình quản lý voucher | Hiển thị danh sách voucher và nút tạo mới |
| 2 | Chọn tạo voucher | Hiển thị form nhập thông tin voucher |
| 3 | Nhập mã, loại giảm, giá trị, điều kiện đơn tối thiểu, thời gian hiệu lực, số lượt sử dụng | Kiểm tra dữ liệu đầu vào |
| 4 | Nhấn lưu | Tạo voucher và hiển thị thông báo thành công |

Luồng ngoại lệ:

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | Mã voucher đã tồn tại | Báo lỗi mã voucher bị trùng |
| E2 | Ngày kết thúc nhỏ hơn hoặc bằng ngày bắt đầu | Báo lỗi thời gian không hợp lệ |
| E3 | Giá trị giảm nhỏ hơn hoặc bằng 0 | Báo lỗi giá trị giảm không hợp lệ |
| E4 | Voucher phần trăm có giá trị lớn hơn 100 | Báo lỗi phần trăm giảm không hợp lệ |

### UC-V02: Sửa voucher

Staff có thể cập nhật thông tin voucher như tên/chú thích, thời hạn, số lượt sử dụng, điều kiện đơn tối thiểu, trạng thái bật/tắt.

Khuyến nghị:

- Không nên sửa `code` nếu voucher đã từng được dùng trong đơn hàng.
- Không nên sửa trực tiếp lịch sử giảm giá của các đơn hàng đã đặt.
- Nếu voucher đã phát sinh đơn hàng, các thay đổi chỉ áp dụng cho đơn hàng mới.

### UC-V03: Xóa hoặc vô hiệu hóa voucher

Trong thực tế nên ưu tiên vô hiệu hóa bằng `isActive = false` thay vì xóa vật lý.

Luồng chính:

| Bước | Staff | Hệ thống |
|---|---|---|
| 1 | Chọn voucher cần xóa/vô hiệu hóa | Hiển thị xác nhận |
| 2 | Xác nhận thao tác | Kiểm tra voucher đã từng được dùng chưa |
| 3 | Hoàn tất | Nếu đã có đơn hàng, chuyển sang vô hiệu hóa; nếu chưa dùng, có thể xóa tùy chính sách |

---

## 6. Use Case Customer: Áp dụng voucher khi đặt hàng

### UC-V04: Nhập mã voucher khi checkout

| Thuộc tính | Mô tả |
|---|---|
| Tác nhân | Customer |
| Tiền điều kiện | Customer đang ở bước đặt hàng và giỏ hàng có sản phẩm hợp lệ. |
| Hậu điều kiện | Voucher được áp dụng vào đơn hàng tạm tính. |

Luồng chính:

| Bước | Customer | Hệ thống |
|---|---|---|
| 1 | Nhập chính xác mã voucher vào ô mã giảm giá | Nhận mã và chuẩn hóa dữ liệu nhập |
| 2 | Nhấn áp dụng | Tìm voucher theo mã |
| 3 |  | Kiểm tra voucher còn hoạt động, còn hạn, chưa hết lượt, đủ điều kiện đơn tối thiểu |
| 4 |  | Tính số tiền giảm theo loại voucher |
| 5 |  | Cập nhật tổng tiền đơn hàng và hiển thị số tiền được giảm |

Luồng ngoại lệ:

| Mã | Điều kiện | Xử lý |
|---|---|---|
| E1 | Không tìm thấy mã voucher | Hiển thị "Mã giảm giá không hợp lệ" |
| E2 | Voucher chưa đến thời gian sử dụng | Hiển thị "Mã giảm giá chưa có hiệu lực" |
| E3 | Voucher đã hết hạn | Hiển thị "Mã giảm giá đã hết hạn" |
| E4 | Voucher đã hết lượt sử dụng | Hiển thị "Mã giảm giá đã hết lượt sử dụng" |
| E5 | Đơn hàng chưa đạt giá trị tối thiểu | Hiển thị số tiền tối thiểu cần đạt |
| E6 | Đơn hàng đã có voucher khác | Hỏi Customer có muốn thay thế voucher hiện tại không |

---

## 7. Quy định nghiệp vụ

| Mã | Quy định |
|---|---|
| QĐ-V01 | Mã voucher là duy nhất trong hệ thống. |
| QĐ-V02 | Customer phải nhập đúng mã voucher để tìm thấy voucher trong giai đoạn hiện tại. |
| QĐ-V03 | Mỗi đơn hàng chỉ áp dụng tối đa 1 voucher. |
| QĐ-V04 | Voucher chỉ hợp lệ khi `isActive = true`, trong thời gian hiệu lực và chưa hết lượt dùng. |
| QĐ-V05 | Voucher chỉ được áp dụng khi tổng tiền sản phẩm đạt `minOrderAmount`. |
| QĐ-V06 | Voucher phần trăm có thể có `maxDiscountAmount` để giới hạn số tiền giảm tối đa. |
| QĐ-V07 | Số tiền giảm không được vượt quá tổng tiền sản phẩm. |
| QĐ-V08 | Khi đơn hàng đặt thành công, tăng `timesUsed` của voucher. |
| QĐ-V09 | Khi đơn hàng bị hủy ở trạng thái cho phép hoàn voucher, giảm lại `timesUsed`. |
| QĐ-V10 | Đơn hàng phải lưu lại `discount_amount` thực tế để bảo toàn lịch sử. |

---

## 8. Công thức tính giảm giá

Với:

- `subtotal`: tổng tiền sản phẩm.
- `shippingFee`: phí vận chuyển.
- `discountValue`: giá trị giảm của voucher.
- `maxDiscountAmount`: mức giảm tối đa nếu có.

Voucher phần trăm:

```text
rawDiscount = subtotal * discountValue / 100
discount = min(rawDiscount, maxDiscountAmount nếu có, subtotal)
```

Voucher trừ thẳng:

```text
discount = min(discountValue, subtotal)
```

Tổng tiền đơn hàng:

```text
totalAmount = subtotal + shippingFee - discount
```

Các loại voucher mở rộng trong tương lai có thể không chỉ tạo ra `discountAmount`. Ví dụ:

| Loại mở rộng | Kết quả mong muốn |
|---|---|
| Free ship | Giảm `shippingFee`, có thể không làm đổi `discountAmount` của sản phẩm. |
| Mua 1 tặng 1 | Thêm sản phẩm tặng hoặc giảm giá một item cụ thể. |
| Combo/bundle | Giảm theo nhóm sản phẩm đạt điều kiện. |

Vì vậy phần tính toán nên trả về một kết quả áp dụng voucher tổng quát, không chỉ một con số giảm tiền.

---

## 9. Đề xuất áp dụng Strategy Pattern

### Mục tiêu

Strategy Pattern được dùng để tách từng cách tính ưu đãi voucher thành một chiến lược riêng. `VoucherService` không cần chứa nhiều nhánh `if/else` cho từng loại voucher, mà chỉ chọn đúng strategy rồi gọi xử lý.

### Ý tưởng áp dụng

Mỗi loại voucher có một class tính toán riêng:

| Strategy | Chức năng |
|---|---|
| `PercentageDiscountStrategy` | Tính giảm theo phần trăm, có xét mức giảm tối đa. |
| `FixedAmountDiscountStrategy` | Tính giảm số tiền cố định. |
| `FreeShippingStrategy` | Miễn hoặc giảm phí vận chuyển. |
| `BuyOneGetOneStrategy` | Xử lý ưu đãi mua 1 tặng 1. |
| `BundleDiscountStrategy` | Tính giảm giá cho combo/nhóm sản phẩm. |

### Cấu trúc đề xuất

```text
VoucherDiscountStrategy
└── apply(voucher, context): VoucherApplyResult

PercentageDiscountStrategy
FixedAmountDiscountStrategy
FreeShippingStrategy
BuyOneGetOneStrategy
BundleDiscountStrategy

VoucherDiscountStrategyResolver
└── resolve(discountType): VoucherDiscountStrategy
```

`VoucherApplyContext` nên chứa dữ liệu checkout cần thiết:

```text
VoucherApplyContext
├── customerId
├── cartItems
├── subtotal
├── shippingFee
├── appliedAt
└── orderId
```

`VoucherApplyResult` nên đủ rộng để hỗ trợ nhiều kiểu ưu đãi:

```text
VoucherApplyResult
├── discountAmount
├── shippingDiscountAmount
├── freeGiftItems
├── appliedVoucherCode
├── message
└── finalTotalAmount
```

### Lợi ích

- Dễ thêm loại voucher mới mà ít ảnh hưởng code cũ.
- `VoucherService` tập trung vào luồng nghiệp vụ: tìm voucher, kiểm tra điều kiện, gọi strategy, trả kết quả.
- Mỗi thuật toán giảm giá được test độc lập.
- Phù hợp với kế hoạch mở rộng free ship, mua 1 tặng 1, combo, giảm theo danh mục.

### Lưu ý

- Strategy chỉ xử lý cách tính ưu đãi, không nên tự truy cập database nếu không cần thiết.
- Kiểm tra trạng thái hiệu lực của voucher nên để State Pattern xử lý trước khi gọi strategy.
- Việc tăng/giảm `timesUsed` vẫn nằm ở tầng service/transaction để tránh sai lệch khi nhiều người đặt hàng đồng thời.

---

## 10. Đề xuất áp dụng State Pattern

### Mục tiêu

State Pattern được dùng để gom logic kiểm tra và xử lý voucher theo từng trạng thái, tránh để `VoucherService` chứa quá nhiều câu lệnh `if/else`.

Lưu ý: trong mô tả hiện tại có 2 loại voucher là trừ phần trăm và trừ thẳng. Hai loại này là `discountType` và nên được xử lý bằng Strategy Pattern. State Pattern phù hợp hơn để quản lý trạng thái hiệu lực của voucher.

### Các trạng thái đề xuất

| State | Ý nghĩa |
|---|---|
| `InactiveVoucherState` | Voucher bị Staff tắt hoặc vô hiệu hóa. |
| `UpcomingVoucherState` | Voucher chưa đến ngày bắt đầu. |
| `ActiveVoucherState` | Voucher đang hợp lệ để kiểm tra và áp dụng. |
| `ExpiredVoucherState` | Voucher đã quá ngày kết thúc. |
| `ExhaustedVoucherState` | Voucher đã hết lượt sử dụng. |

### Cấu trúc đề xuất

```text
VoucherState
├── validate(voucher, context)
└── apply(voucher, context)

InactiveVoucherState
UpcomingVoucherState
ActiveVoucherState
ExpiredVoucherState
ExhaustedVoucherState

VoucherStateResolver
└── resolve(voucher, now)
```

### Luồng xử lý

```text
Customer nhập mã
→ VoucherService tìm voucher theo code
→ VoucherStateResolver xác định state hiện tại
→ state.validate(...)
→ nếu hợp lệ: VoucherDiscountStrategyResolver chọn strategy theo discountType
→ strategy.apply(...)
→ trả về số tiền giảm và tổng tiền mới
```

### Lợi ích

- Mỗi trạng thái tự chịu trách nhiệm kiểm tra và báo lỗi phù hợp.
- Dễ mở rộng thêm trạng thái như `Scheduled`, `Paused`, `MemberOnly`, `CampaignEnded`.
- Giảm rủi ro sửa một điều kiện làm ảnh hưởng toàn bộ logic voucher.

---

## 11. Gợi ý API backend

### Staff API

Các endpoint dưới đây chỉ cho phép role `STAFF` truy cập.

| Method | Endpoint | Mục đích |
|---|---|---|
| `GET` | `/api/staff/vouchers` | Xem danh sách voucher |
| `GET` | `/api/staff/vouchers/{id}` | Xem chi tiết voucher |
| `POST` | `/api/staff/vouchers` | Tạo voucher mới |
| `PUT` | `/api/staff/vouchers/{id}` | Cập nhật voucher |
| `DELETE` | `/api/staff/vouchers/{id}` | Xóa hoặc vô hiệu hóa voucher |

### Customer API

| Method | Endpoint | Mục đích |
|---|---|---|
| `POST` | `/api/vouchers/apply` | Customer nhập mã voucher để tính giảm giá cho đơn hàng tạm tính |

Request áp dụng voucher:

```json
{
  "code": "SALE10",
  "subtotal": 500000
}
```

Response áp dụng voucher:

```json
{
  "code": "SALE10",
  "discountType": "percentage",
  "discountAmount": 50000,
  "subtotal": 500000,
  "shippingFee": 30000,
  "totalAmount": 480000
}
```

---

## 12. Gợi ý class backend

Lưu ý về tổ chức thư mục:

- Thư mục `pattern/` là nơi chứa các phần triển khai design pattern của hệ thống.
- Bên trong `pattern/` chia theo loại pattern trước, ví dụ `strategy/`, `state/`, `observer/`, `factory/`.
- Trong từng thư mục pattern sẽ chia tiếp theo chức năng áp dụng cụ thể, ví dụ `pattern/strategy/voucher` và `pattern/state/voucher`.
- File hiện tại `pattern/voucher.md` là tài liệu giải thích cách áp dụng Strategy Pattern và State Pattern cho chức năng voucher.

```text
backend/
├── controller/
│   ├── VoucherController.java              # Customer apply/remove voucher
│   └── staff/
│       └── StaffVoucherController.java     # Staff CRUD voucher

├── service/
│   ├── VoucherService.java
│   └── impl/
│       └── VoucherServiceImpl.java

├── repository/
│   └── VoucherRepository.java

├── dto/
│   ├── request/
│   │   ├── CreateVoucherRequest.java
│   │   ├── UpdateVoucherRequest.java
│   │   └── ApplyVoucherRequest.java
│   └── response/
│       ├── VoucherResponse.java
│       └── AppliedVoucherResponse.java

└── pattern/                                  # DESIGN PATTERNS
    ├── design_patterns_analysis.md           # Tổng hợp pattern của hệ thống
    ├── voucher.md                            # Tài liệu Strategy + State cho voucher
    │
    ├── strategy/                             # Strategy Pattern
    │   ├── payment/                          # Ứng dụng cho thanh toán
    │   │   ├── PaymentStrategy.java           # Interface
    │   │   ├── CodPaymentStrategy.java        # COD
    │   │   ├── VnPayPaymentStrategy.java      # VNPay
    │   │   ├── MoMoPaymentStrategy.java       # MoMo
    │   │   └── PaymentContext.java            # Context
    │   │
    │   └── voucher/                          # Ứng dụng cho voucher
    │       ├── VoucherDiscountStrategy.java   # Interface
    │       ├── PercentageDiscountStrategy.java# Giảm %
    │       ├── FixedAmountDiscountStrategy.java# Giảm cố định
    │       ├── FreeShippingStrategy.java      # Free ship, mở rộng sau
    │       ├── BuyOneGetOneStrategy.java      # Mua 1 tặng 1, mở rộng sau
    │       ├── VoucherApplyContext.java       # Dữ liệu đầu vào
    │       ├── VoucherApplyResult.java        # Kết quả áp dụng
    │       └── VoucherDiscountStrategyResolver.java
    │
    ├── state/                                # State Pattern
    │   └── voucher/                          # Ứng dụng cho trạng thái voucher
    │       ├── VoucherState.java             # Interface
    │       ├── ActiveVoucherState.java       # Đang hoạt động
    │       ├── InactiveVoucherState.java     # Bị tắt
    │       ├── UpcomingVoucherState.java     # Chưa bắt đầu
    │       ├── ExpiredVoucherState.java      # Hết hạn
    │       ├── ExhaustedVoucherState.java    # Hết lượt dùng
    │       └── VoucherStateResolver.java
    │
    ├── observer/                             # Observer Pattern
    │   └── order/                            # Ứng dụng cho đơn hàng
    │       ├── OrderEvent.java               # Event
    │       ├── OrderObserver.java            # Interface
    │       ├── EmailNotificationObserver.java# Gửi email
    │       ├── InventoryObserver.java        # Cập nhật tồn kho
    │       ├── LoyaltyPointObserver.java     # Cộng điểm
    │       └── OrderSubject.java             # Subject
    │
    └── factory/                              # Factory Method Pattern
        └── voucher/                          # Ứng dụng cho voucher nếu cần
            ├── DiscountCalculator.java       # Interface
            ├── PercentageDiscount.java       # Giảm %
            ├── FixedAmountDiscount.java      # Giảm cố định
            └── DiscountFactory.java          # Factory
```

---

## 13. Liên hệ với banner trong tương lai

Ở giai đoạn sau, voucher có thể được đưa lên banner hoặc khu vực khuyến mãi.

Đề xuất mở rộng:

- Banner có thể trỏ đến một voucher cụ thể bằng `voucherId` hoặc `voucherCode`.
- Customer click banner thì hệ thống copy mã hoặc tự điền mã vào checkout.
- Voucher public trên banner vẫn phải đi qua cùng luồng kiểm tra hợp lệ khi checkout.
- Không nên coi voucher hiển thị trên banner là chắc chắn áp dụng được, vì voucher có thể hết hạn hoặc hết lượt trước khi Customer thanh toán.

---

## 14. Kết luận

Module voucher hiện đã có nền tảng dữ liệu và use case rải rác trong tài liệu. File này gom lại thành đặc tả riêng cho 2 nhóm người dùng:

- Staff quản lý voucher: tạo mới, sửa, xóa/vô hiệu hóa.
- Customer sử dụng voucher: nhập đúng mã khi đặt hàng để tìm và áp dụng voucher.

Về design pattern:

- Strategy Pattern phù hợp để tách các thuật toán áp dụng voucher như giảm phần trăm, giảm tiền cố định, free ship, mua 1 tặng 1.
- State Pattern phù hợp để quản lý trạng thái hiệu lực của voucher như chưa bắt đầu, đang hoạt động, hết hạn, hết lượt, bị vô hiệu hóa.

Hai pattern này có thể kết hợp trong `VoucherService`: State kiểm tra voucher có được phép áp dụng hay không, Strategy tính ưu đãi cụ thể sau khi voucher hợp lệ.
