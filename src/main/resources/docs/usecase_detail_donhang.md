# Mô tả chi tiết Use Case — Module Đơn hàng & Giỏ hàng

---

## UC-17: Thêm sản phẩm vào giỏ hàng

| Thuộc tính             | Mô tả                                                                                                                |
| ---------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **Mã UC**              | UC-17                                                                                                                |
| **Tên UC**             | Thêm sản phẩm vào giỏ hàng                                                                                           |
| **Tác nhân chính**     | Khách hàng (KH), Khách vãng lai (KVL)                                                                                |
| **Mô tả**              | Người dùng chọn biến thể SP (size + màu) và thêm vào giỏ hàng. KH lưu trên server, KVL lưu trên session/localStorage |
| **Tiền điều kiện**     | SP còn hàng, biến thể được chọn còn tồn kho                                                                          |
| **Hậu điều kiện**      | SP được thêm vào giỏ hàng, số lượng và tổng tiền được cập nhật                                                       |
| **Quy định liên quan** | QĐ10                                                                                                                 |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                    | Hệ thống                                                              |
| ---- | ------------------------------------------- | --------------------------------------------------------------------- |
| 1    | Tại trang chi tiết SP, chọn size và màu sắc | Hiển thị tồn kho của biến thể được chọn                               |
| 2    | Chọn số lượng                               | Kiểm tra số lượng ≤ tồn kho                                           |
| 3    | Nhấn "Thêm vào giỏ hàng"                    | Thêm biến thể vào giỏ hàng, hiển thị mini cart cập nhật nhanh         |
| 4    |                                             | Mini cart hiển thị: tên SP, biến thể, SL, đơn giá, tổng tiền giỏ hàng |

**Luồng thay thế:**

| Mã  | Điều kiện                          | Xử lý                                                            |
| --- | ---------------------------------- | ---------------------------------------------------------------- |
| 3a  | SP đã có trong giỏ (cùng biến thể) | Cộng thêm số lượng vào dòng hiện tại. Kiểm tra tổng SL ≤ tồn kho |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                   | Xử lý                                                       |
| --- | --------------------------- | ----------------------------------------------------------- |
| 1a  | Chưa chọn size hoặc màu     | Hiển thị "Vui lòng chọn size và màu sắc"                    |
| 2a  | Số lượng vượt quá tồn kho   | Hiển thị "Chỉ còn {n} sản phẩm trong kho"                   |
| 2b  | Biến thể hết hàng (tồn = 0) | Nút "Thêm vào giỏ hàng" bị vô hiệu hóa, hiển thị "Hết hàng" |

---

## UC-18: Quản lý giỏ hàng

| Thuộc tính             | Mô tả                                                                 |
| ---------------------- | --------------------------------------------------------------------- |
| **Mã UC**              | UC-18                                                                 |
| **Tên UC**             | Quản lý giỏ hàng                                                      |
| **Tác nhân chính**     | Khách hàng (KH), Khách vãng lai (KVL)                                 |
| **Mô tả**              | Xem, sửa số lượng, xóa SP trong giỏ hàng. Tổng tiền cập nhật realtime |
| **Tiền điều kiện**     | Giỏ hàng có ít nhất 1 SP                                              |
| **Hậu điều kiện**      | Giỏ hàng được cập nhật                                                |
| **Quy định liên quan** | QĐ10                                                                  |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                        | Hệ thống                                                                           |
| ---- | ----------------------------------------------- | ---------------------------------------------------------------------------------- |
| 1    | Nhấn icon giỏ hàng hoặc truy cập trang giỏ hàng | Hiển thị danh sách SP: hình ảnh, tên, biến thể (size/màu), đơn giá, SL, thành tiền |
| 2    | Tăng/giảm số lượng một dòng                     | Kiểm tra SL mới ≤ tồn kho, cập nhật thành tiền và tổng giỏ hàng realtime           |
| 3    | (Tùy chọn) Nhấn "Xóa" trên một dòng             | Xác nhận, xóa SP khỏi giỏ hàng, cập nhật tổng                                      |
| 4    | Nhấn "Tiến hành đặt hàng"                       | Chuyển sang UC-20: Đặt hàng và thanh toán                                          |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                               | Xử lý                                                           |
| --- | --------------------------------------- | --------------------------------------------------------------- |
| 2a  | Tồn kho biến thể đã thay đổi (hết hàng) | Hiển thị cảnh báo "Sản phẩm đã hết hàng hoặc không đủ số lượng" |

---

## UC-19: Áp dụng voucher / mã giảm giá

| Thuộc tính             | Mô tả                                                   |
| ---------------------- | ------------------------------------------------------- |
| **Mã UC**              | UC-19                                                   |
| **Tên UC**             | Áp dụng voucher / mã giảm giá                           |
| **Tác nhân chính**     | Khách hàng (KH)                                         |
| **Mô tả**              | KH nhập mã voucher để áp dụng giảm giá cho đơn hàng     |
| **Tiền điều kiện**     | KH đã đăng nhập, giỏ hàng có SP, đang ở bước thanh toán |
| **Hậu điều kiện**      | Voucher được áp dụng, tổng tiền giảm tương ứng          |
| **Quy định liên quan** | QĐ11, CT2                                               |

**Luồng sự kiện chính:**

| Bước | Tác nhân                            | Hệ thống                                                                                          |
| ---- | ----------------------------------- | ------------------------------------------------------------------------------------------------- |
| 1    | Nhập mã voucher vào ô "Mã giảm giá" |                                                                                                   |
| 2    | Nhấn "Áp dụng"                      | Kiểm tra: mã tồn tại, còn hạn, đủ điều kiện đơn tối thiểu, chưa hết lượt dùng                     |
| 3    |                                     | Tính số tiền giảm (CT2): Nếu loại %: Giảm = Tổng SP × %. Nếu loại cố định: Giảm = Giá trị voucher |
| 4    |                                     | Cập nhật tổng tiền = Tổng SP − Giảm giá + Phí ship. Hiển thị chi tiết giảm giá                    |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                           | Xử lý                                                                         |
| --- | ----------------------------------- | ----------------------------------------------------------------------------- |
| 2a  | Mã voucher không tồn tại            | Hiển thị "Mã giảm giá không hợp lệ"                                           |
| 2b  | Voucher đã hết hạn                  | Hiển thị "Mã giảm giá đã hết hạn"                                             |
| 2c  | Đơn hàng chưa đạt giá trị tối thiểu | Hiển thị "Đơn hàng cần tối thiểu {n}đ để áp dụng voucher này"                 |
| 2d  | Voucher đã hết lượt sử dụng         | Hiển thị "Mã giảm giá đã hết lượt sử dụng"                                    |
| 2e  | Đơn đã có voucher khác              | Hiển thị "Mỗi đơn hàng chỉ được áp dụng 1 mã giảm giá. Bạn có muốn thay thế?" |

---

## UC-20: Đặt hàng và thanh toán

| Thuộc tính             | Mô tả                                                                                  |
| ---------------------- | -------------------------------------------------------------------------------------- |
| **Mã UC**              | UC-20                                                                                  |
| **Tên UC**             | Đặt hàng và thanh toán                                                                 |
| **Tác nhân chính**     | Khách hàng (KH)                                                                        |
| **Tác nhân phụ**       | Cổng thanh toán (VNPay, MoMo, ZaloPay)                                                 |
| **Mô tả**              | KH xác nhận đơn hàng, chọn địa chỉ giao, phương thức thanh toán và hoàn tất đặt hàng   |
| **Tiền điều kiện**     | KH đã đăng nhập, giỏ hàng có ≥ 1 SP, tất cả SP còn đủ tồn kho                          |
| **Hậu điều kiện**      | Đơn hàng được tạo với trạng thái "Chờ xác nhận", tồn kho giảm, email xác nhận được gửi |
| **Quy định liên quan** | CT1, QĐ6                                                                               |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                                              | Hệ thống                                                                            |
| ---- | --------------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| 1    | Từ giỏ hàng, nhấn "Tiến hành đặt hàng"                                | Hiển thị trang checkout: danh sách SP, tóm tắt đơn hàng                             |
| 2    | Chọn/thêm địa chỉ giao hàng                                           | Tính phí vận chuyển dựa trên khu vực                                                |
| 3    | (Tùy chọn) Nhập mã voucher                                            | UC-19: Áp dụng voucher                                                              |
| 4    | Chọn phương thức thanh toán (COD / VNPay / MoMo / ZaloPay / Visa/ATM) |                                                                                     |
| 5    | Xem tổng tiền: Σ(đơn giá × SL) + phí ship − giảm giá                  |                                                                                     |
| 6    | Nhấn "Đặt hàng"                                                       | Kiểm tra lại tồn kho lần cuối. Tạo đơn hàng trạng thái "Chờ xác nhận"               |
| 7    |                                                                       | Giảm tồn kho các biến thể. Gửi email xác nhận đơn hàng cho KH                       |
| 8    | (Nếu thanh toán online)                                               | Chuyển hướng đến cổng thanh toán. Xử lý callback khi thanh toán thành công/thất bại |
| 9    |                                                                       | Hiển thị "Đặt hàng thành công" với mã đơn hàng                                      |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                                    | Xử lý                                                                                                                          |
| --- | -------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| 6a  | Một biến thể đã hết hàng (giữa lúc checkout) | Hiển thị "Sản phẩm {tên} đã hết hàng. Vui lòng cập nhật giỏ hàng"                                                              |
| 8a  | Thanh toán online thất bại                   | Đơn hàng chuyển trạng thái "Thanh toán thất bại", không giảm tồn kho. Hiển thị "Thanh toán không thành công. Vui lòng thử lại" |

---

## UC-21: Xem lịch sử đơn hàng

| Thuộc tính             | Mô tả                                                                        |
| ---------------------- | ---------------------------------------------------------------------------- |
| **Mã UC**              | UC-21                                                                        |
| **Tên UC**             | Xem lịch sử đơn hàng                                                         |
| **Tác nhân chính**     | Khách hàng (KH)                                                              |
| **Mô tả**              | KH xem danh sách đơn hàng đã đặt, lọc theo trạng thái, xem chi tiết từng đơn |
| **Tiền điều kiện**     | KH đã đăng nhập                                                              |
| **Hậu điều kiện**      | Không thay đổi dữ liệu (chỉ đọc)                                             |
| **Quy định liên quan** | —                                                                            |

**Luồng sự kiện chính:**

| Bước | Tác nhân                    | Hệ thống                                                                                                          |
| ---- | --------------------------- | ----------------------------------------------------------------------------------------------------------------- |
| 1    | Truy cập "Đơn hàng của tôi" | Hiển thị danh sách đơn: mã ĐH, ngày đặt, tổng tiền, trạng thái. Có tab lọc theo trạng thái                        |
| 2    | Nhấn vào một đơn hàng       | Hiển thị chi tiết: danh sách SP (hình, tên, biến thể, SL, giá), địa chỉ giao, phương thức TT, timeline trạng thái |

---

## UC-22: Hủy đơn hàng

| Thuộc tính             | Mô tả                                                                        |
| ---------------------- | ---------------------------------------------------------------------------- |
| **Mã UC**              | UC-22                                                                        |
| **Tên UC**             | Hủy đơn hàng                                                                 |
| **Tác nhân chính**     | Khách hàng (KH)                                                              |
| **Mô tả**              | KH hủy đơn hàng đang ở trạng thái "Chờ xác nhận"                             |
| **Tiền điều kiện**     | KH đã đăng nhập, đơn hàng ở trạng thái "Chờ xác nhận"                        |
| **Hậu điều kiện**      | Đơn hàng chuyển sang "Đã hủy", tồn kho được hoàn, voucher được hoàn (nếu có) |
| **Quy định liên quan** | QĐ12, QĐ6                                                                    |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                   | Hệ thống                                                |
| ---- | ------------------------------------------ | ------------------------------------------------------- |
| 1    | Vào chi tiết đơn hàng, nhấn "Hủy đơn hàng" | Hiển thị dialog xác nhận hủy, yêu cầu chọn lý do hủy    |
| 2    | Chọn lý do hủy, nhấn "Xác nhận hủy"        | Cập nhật trạng thái → "Đã hủy"                          |
| 3    |                                            | Hoàn tồn kho cho tất cả biến thể trong đơn              |
| 4    |                                            | Hoàn voucher (nếu đã áp dụng): tăng lại số lượt sử dụng |
| 5    |                                            | Gửi email thông báo hủy đơn cho KH                      |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                                  | Xử lý                                                                                                  |
| --- | ------------------------------------------ | ------------------------------------------------------------------------------------------------------ |
| 1a  | Đơn hàng không ở trạng thái "Chờ xác nhận" | Nút "Hủy" bị ẩn/vô hiệu. Nếu gọi API trực tiếp: trả lỗi "Đơn hàng không thể hủy ở trạng thái hiện tại" |

---

## UC-23: Quản lý đơn hàng (Staff)

| Thuộc tính             | Mô tả                                                         |
| ---------------------- | ------------------------------------------------------------- |
| **Mã UC**              | UC-23                                                         |
| **Tên UC**             | Quản lý đơn hàng                                              |
| **Tác nhân chính**     | Nhân viên (NV)                                                |
| **Mô tả**              | NV xem, xác nhận, cập nhật trạng thái, hủy đơn hàng           |
| **Tiền điều kiện**     | NV đã đăng nhập vào trang quản trị                            |
| **Hậu điều kiện**      | Trạng thái đơn hàng được cập nhật, email thông báo gửi cho KH |
| **Quy định liên quan** | QĐ8, QĐ6                                                      |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                    | Hệ thống                                                                                      |
| ---- | ------------------------------------------- | --------------------------------------------------------------------------------------------- |
| 1    | Truy cập "Quản lý đơn hàng"                 | Hiển thị danh sách đơn phân trang, lọc theo trạng thái/ngày/KH                                |
| 2    | Chọn đơn hàng để xem chi tiết               | Hiển thị: thông tin KH, danh sách SP, tổng tiền, địa chỉ, phương thức TT, timeline trạng thái |
| 3    | Nhấn "Xác nhận" (Chờ xác nhận → Đang xử lý) | Cập nhật trạng thái, gửi email cho KH                                                         |
| 4    | Nhấn "Giao hàng" (Đang xử lý → Đang giao)   | Cập nhật trạng thái, gửi email cho KH                                                         |
| 5    | Nhấn "Hoàn thành" (Đang giao → Hoàn thành)  | Cập nhật trạng thái, tính điểm tích lũy cho KH, gửi email cho KH                              |

**Luồng thay thế:**

| Mã  | Điều kiện         | Xử lý                                                                                          |
| --- | ----------------- | ---------------------------------------------------------------------------------------------- |
| 3a  | NV nhấn "Hủy đơn" | Nhập lý do hủy. Cập nhật trạng thái → "Đã hủy". Hoàn tồn kho + voucher. Gửi email thông báo KH |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                                                         | Xử lý                                                      |
| --- | ----------------------------------------------------------------- | ---------------------------------------------------------- |
| 3b  | Cập nhật trạng thái không hợp lệ (ví dụ: Hoàn thành → Đang xử lý) | Hiển thị lỗi "Không thể chuyển từ trạng thái {A} sang {B}" |
