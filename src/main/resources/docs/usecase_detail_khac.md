# Mô tả chi tiết Use Case — Module Đánh giá, Voucher, Wishlist, Thống kê & Nội dung

---

## UC-24: Đánh giá sản phẩm

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-24 |
| **Tên UC** | Đánh giá sản phẩm |
| **Tác nhân chính** | Khách hàng (KH) |
| **Mô tả** | KH đánh giá SP đã mua: chấm sao (1–5), viết bình luận, đính kèm ảnh |
| **Tiền điều kiện** | KH đã đăng nhập, đã mua SP, đơn hàng ở trạng thái "Hoàn thành", chưa đánh giá SP này trong đơn này |
| **Hậu điều kiện** | Đánh giá được tạo ở trạng thái "Chờ duyệt", rating trung bình SP được cập nhật sau khi duyệt |
| **Quy định liên quan** | QĐ9, QĐ13 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập trang chi tiết SP hoặc lịch sử đơn hàng → nhấn "Đánh giá" | Hiển thị form đánh giá: chọn sao (1–5), nhập nội dung, upload ảnh |
| 2 | Chọn số sao (1–5) | |
| 3 | Nhập nội dung đánh giá (tối thiểu 10 ký tự) | |
| 4 | (Tùy chọn) Đính kèm ảnh (tối đa 5 ảnh) | Preview ảnh |
| 5 | Nhấn "Gửi đánh giá" | Validate: nội dung ≥ 10 ký tự, ảnh ≤ 5. Tạo đánh giá trạng thái "Chờ duyệt" |
| 6 | | Hiển thị "Đánh giá đã được gửi, đang chờ duyệt" |

**Luồng ngoại lệ:**

| Mã | Điều kiện | Xử lý |
|----|-----------|-------|
| 1a | KH chưa mua SP hoặc đơn chưa hoàn thành | Nút "Đánh giá" không hiển thị |
| 1b | KH đã đánh giá SP này trong đơn này rồi | Hiển thị "Bạn đã đánh giá sản phẩm này" |
| 3a | Nội dung < 10 ký tự | Hiển thị "Nội dung đánh giá cần tối thiểu 10 ký tự" |
| 4a | Upload > 5 ảnh | Hiển thị "Chỉ được đính kèm tối đa 5 ảnh" |

---

## UC-25: Xem đánh giá sản phẩm

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-25 |
| **Tên UC** | Xem đánh giá sản phẩm |
| **Tác nhân chính** | Khách vãng lai (KVL), Khách hàng (KH) |
| **Mô tả** | Xem danh sách đánh giá đã duyệt của SP, lọc theo số sao, có ảnh, đã phản hồi |
| **Tiền điều kiện** | SP tồn tại và có đánh giá đã duyệt |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc) |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Cuộn đến phần đánh giá trên trang chi tiết SP | Hiển thị: rating trung bình, phân bổ sao (5★: n%, 4★: n%,...), danh sách review |
| 2 | (Tùy chọn) Lọc theo: số sao, có ảnh, đã phản hồi | Lọc và hiển thị review khớp |

---

## UC-26: Duyệt / Xóa đánh giá sản phẩm (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-26 |
| **Tên UC** | Duyệt / Xóa đánh giá sản phẩm |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV duyệt đánh giá chờ duyệt, phản hồi review, xóa review vi phạm |
| **Tiền điều kiện** | NV đã đăng nhập, có đánh giá chờ duyệt |
| **Hậu điều kiện** | Đánh giá được duyệt (hiển thị) hoặc xóa |
| **Quy định liên quan** | QĐ9 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý đánh giá" | Hiển thị danh sách đánh giá: tab "Chờ duyệt" / "Đã duyệt" / "Đã xóa" |
| 2 | Xem nội dung đánh giá, hình ảnh đính kèm | |
| 3a | Nhấn "Duyệt" | Cập nhật trạng thái → "Đã duyệt", review hiển thị trên storefront, cập nhật rating trung bình SP |
| 3b | Nhấn "Xóa" (vi phạm) | Nhập lý do xóa. Cập nhật trạng thái → "Đã xóa", review bị ẩn |
| 4 | (Tùy chọn) Nhấn "Phản hồi" | Nhập nội dung phản hồi, lưu và hiển thị dưới review trên storefront |

---

## UC-27: Tạo / Quản lý voucher khuyến mãi (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-27 |
| **Tên UC** | Tạo / Quản lý voucher khuyến mãi |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV tạo, cập nhật, vô hiệu hóa voucher/mã giảm giá |
| **Tiền điều kiện** | NV đã đăng nhập |
| **Hậu điều kiện** | Voucher được tạo/cập nhật trong CSDL |
| **Quy định liên quan** | QĐ7 |

**Luồng sự kiện chính (Tạo voucher):**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý khuyến mãi" → "Tạo voucher" | Hiển thị form tạo voucher |
| 2 | Nhập: mã voucher, loại (% hoặc số tiền), giá trị, điều kiện đơn tối thiểu, ngày bắt đầu, ngày kết thúc, số lượng giới hạn | |
| 3 | Nhấn "Tạo" | Validate: mã duy nhất, ngày kết thúc > ngày bắt đầu, giá trị > 0. Lưu voucher |
| 4 | | Hiển thị "Tạo voucher thành công" |

**Luồng ngoại lệ:**

| Mã | Điều kiện | Xử lý |
|----|-----------|-------|
| 3a | Mã voucher đã tồn tại | Hiển thị "Mã voucher đã tồn tại, vui lòng chọn mã khác" |
| 3b | Ngày kết thúc ≤ ngày bắt đầu | Hiển thị "Ngày kết thúc phải sau ngày bắt đầu" |

---

## UC-28: Lưu sản phẩm yêu thích (Wishlist)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-28 |
| **Tên UC** | Lưu sản phẩm yêu thích |
| **Tác nhân chính** | Khách hàng (KH) |
| **Mô tả** | KH thêm/xóa SP vào danh sách yêu thích để xem lại sau |
| **Tiền điều kiện** | KH đã đăng nhập |
| **Hậu điều kiện** | Danh sách wishlist được cập nhật |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Nhấn icon "Yêu thích" (trái tim) trên SP | Toggle trạng thái: nếu chưa yêu thích → thêm, nếu đã yêu thích → xóa |
| 2 | | Cập nhật icon (trái tim đỏ = đã yêu thích, trái tim rỗng = chưa) |
| 3 | Truy cập "Sản phẩm yêu thích" trong trang cá nhân | Hiển thị danh sách SP đã yêu thích dạng lưới |

---

## UC-29: Xem sản phẩm gợi ý

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-29 |
| **Tên UC** | Xem sản phẩm gợi ý |
| **Tác nhân chính** | Khách hàng (KH) |
| **Mô tả** | Hệ thống hiển thị SP gợi ý dựa trên lịch sử xem/mua của KH |
| **Tiền điều kiện** | KH đã đăng nhập, có lịch sử xem/mua SP |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc) |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập trang chi tiết SP hoặc trang chủ | Hiển thị carousel "Gợi ý sản phẩm" dựa trên: SP cùng danh mục đã xem, SP cùng danh mục đã mua, SP phổ biến trong danh mục tương tự |

---

## UC-30: Thống kê doanh thu (Admin)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-30 |
| **Tên UC** | Thống kê doanh thu |
| **Tác nhân chính** | Quản trị viên (AD) |
| **Mô tả** | Admin xem báo cáo doanh thu theo khoảng thời gian, danh mục, sản phẩm |
| **Tiền điều kiện** | Admin đã đăng nhập |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc) |
| **Quy định liên quan** | CT1 |
| **Biểu mẫu** | BM1 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Thống kê" → "Doanh thu" | Hiển thị dashboard doanh thu: biểu đồ doanh thu theo thời gian, bảng tóm tắt |
| 2 | Chọn khoảng thời gian (ngày/tuần/tháng/năm) | Cập nhật biểu đồ và bảng thống kê |
| 3 | | Hiển thị BM1: tổng đơn hàng, đơn hoàn thành, đơn hủy, tổng doanh thu, tổng giảm giá, doanh thu thực |
| 4 | (Tùy chọn) Nhấn "Xuất báo cáo" | Tải file báo cáo (Excel/PDF) |

---

## UC-31: Thống kê sản phẩm bán chạy

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-31 |
| **Tên UC** | Thống kê sản phẩm bán chạy |
| **Tác nhân chính** | Quản trị viên (AD), Nhân viên (NV) |
| **Mô tả** | Xem top SP bán chạy theo số lượng bán và doanh thu |
| **Tiền điều kiện** | Đã đăng nhập với quyền AD hoặc NV |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc) |
| **Quy định liên quan** | — |
| **Biểu mẫu** | BM2 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Thống kê" → "SP bán chạy" | Hiển thị BM2: bảng xếp hạng SP theo số lượng bán, doanh thu |
| 2 | Chọn khoảng thời gian và danh mục (tùy chọn) | Cập nhật bảng xếp hạng |

---

## UC-32: Xem báo cáo tồn kho (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-32 |
| **Tên UC** | Xem báo cáo tồn kho |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV xem báo cáo tồn kho, cảnh báo SP sắp hết hàng |
| **Tiền điều kiện** | NV đã đăng nhập |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc) |
| **Quy định liên quan** | QĐ6 |
| **Biểu mẫu** | BM3 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Báo cáo" → "Tồn kho" | Hiển thị BM3: danh sách biến thể SP với tồn kho, trạng thái (Còn hàng / Sắp hết / Hết hàng) |
| 2 | | Highlight các biến thể có tồn < 10 (cảnh báo "Sắp hết hàng") |
| 3 | (Tùy chọn) Lọc theo trạng thái, danh mục | Cập nhật danh sách |

---

## UC-33: Báo cáo khách hàng thân thiết (Admin)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-33 |
| **Tên UC** | Báo cáo khách hàng thân thiết |
| **Tác nhân chính** | Quản trị viên (AD) |
| **Mô tả** | Admin xem danh sách KH theo hạng thành viên, điểm tích lũy, tổng chi tiêu |
| **Tiền điều kiện** | Admin đã đăng nhập |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc) |
| **Quy định liên quan** | — |
| **Biểu mẫu** | BM5 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Báo cáo" → "KH thân thiết" | Hiển thị BM5: danh sách KH kèm hạng, điểm tích lũy, tổng chi tiêu, số đơn hàng |
| 2 | (Tùy chọn) Lọc theo hạng (Đồng/Bạc/Vàng/Kim cương) | Cập nhật danh sách |
| 3 | (Tùy chọn) Nhấn "Xuất báo cáo" | Tải file báo cáo |

---

## UC-34: Quản lý banner / slider trang chủ (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-34 |
| **Tên UC** | Quản lý banner / slider trang chủ |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV upload hình banner, đặt link điều hướng, thứ tự hiển thị, thời gian hiển thị |
| **Tiền điều kiện** | NV đã đăng nhập |
| **Hậu điều kiện** | Banner được cập nhật, hiển thị trên trang chủ storefront |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý banner" → "Thêm banner" | Hiển thị form: upload hình, nhập link điều hướng, thứ tự, ngày bắt đầu/kết thúc |
| 2 | Upload hình banner, điền thông tin | Preview banner |
| 3 | Nhấn "Lưu" | Lưu banner, tự động hiển thị trên trang chủ theo thời gian và thứ tự |

---

## UC-35: Quản lý bài viết blog / tin tức (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-35 |
| **Tên UC** | Quản lý bài viết blog / tin tức |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV tạo, sửa, xóa bài viết blog/tin tức, gắn tag và danh mục tin |
| **Tiền điều kiện** | NV đã đăng nhập |
| **Hậu điều kiện** | Bài viết được cập nhật trên hệ thống |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý blog" → "Tạo bài viết" | Hiển thị editor: tiêu đề, nội dung (rich text), ảnh bìa, danh mục tin, tags |
| 2 | Soạn nội dung, upload ảnh, chọn danh mục + tags | |
| 3 | Nhấn "Xuất bản" hoặc "Lưu nháp" | Lưu bài viết với trạng thái tương ứng |

---

## UC-36: Quản lý thông tin khách hàng (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-36 |
| **Tên UC** | Quản lý thông tin khách hàng |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV xem, tìm kiếm thông tin KH, phân hạng thành viên |
| **Tiền điều kiện** | NV đã đăng nhập |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc, trừ phân hạng) |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý KH" | Hiển thị danh sách KH phân trang, tìm kiếm theo tên/email/SĐT/hạng |
| 2 | Nhấn vào KH để xem chi tiết | Hiển thị: thông tin cá nhân, hạng thành viên, điểm tích lũy, lịch sử đơn hàng |
