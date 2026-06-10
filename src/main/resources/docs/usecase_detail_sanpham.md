# Mô tả chi tiết Use Case — Module Sản phẩm & Danh mục

---

## UC-10: Xem danh sách sản phẩm theo danh mục

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-10 |
| **Tên UC** | Xem danh sách sản phẩm theo danh mục |
| **Tác nhân chính** | Khách vãng lai (KVL), Khách hàng (KH), Nhân viên (NV) |
| **Mô tả** | Người dùng duyệt sản phẩm theo danh mục (Nam > Áo > Áo Polo), xem sản phẩm dạng lưới có phân trang |
| **Tiền điều kiện** | Không |
| **Hậu điều kiện** | Danh sách sản phẩm thuộc danh mục được hiển thị |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Chọn một danh mục từ thanh điều hướng hoặc menu | Hiển thị danh sách SP thuộc danh mục dạng lưới (grid), mỗi SP có: hình ảnh, tên, giá gốc, giá bán, biến thể màu |
| 2 | Cuộn hoặc chuyển trang | Tải thêm SP theo phân trang |
| 3 | (Tùy chọn) Nhấn vào một SP | Chuyển đến UC-14: Xem chi tiết sản phẩm |

---

## UC-11: Tìm kiếm sản phẩm

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-11 |
| **Tên UC** | Tìm kiếm sản phẩm |
| **Tác nhân chính** | Khách vãng lai (KVL), Khách hàng (KH), Nhân viên (NV) |
| **Mô tả** | Người dùng tìm kiếm SP theo từ khóa với gợi ý tự động (autocomplete) |
| **Tiền điều kiện** | Không |
| **Hậu điều kiện** | Danh sách kết quả tìm kiếm được hiển thị |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Nhấn vào thanh tìm kiếm | Hiển thị modal tìm kiếm với từ khóa phổ biến, danh mục gợi ý |
| 2 | Gõ từ khóa (ví dụ: "áo polo") | Hiển thị gợi ý autocomplete realtime (tên SP, danh mục khớp) |
| 3 | Chọn gợi ý hoặc nhấn Enter | Hiển thị trang kết quả tìm kiếm dạng lưới, có phân trang |
| 4 | (Tùy chọn) Áp dụng bộ lọc / sắp xếp | Kết hợp với UC-12, UC-13 |

**Luồng ngoại lệ:**

| Mã | Điều kiện | Xử lý |
|----|-----------|-------|
| 3a | Không tìm thấy SP nào khớp | Hiển thị "Không tìm thấy sản phẩm phù hợp" kèm gợi ý danh mục liên quan |

---

## UC-12: Lọc sản phẩm

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-12 |
| **Tên UC** | Lọc sản phẩm |
| **Tác nhân chính** | Khách vãng lai (KVL), Khách hàng (KH) |
| **Mô tả** | Người dùng lọc danh sách SP theo nhiều tiêu chí: size, màu sắc, chất liệu, khoảng giá |
| **Tiền điều kiện** | Đang ở trang danh sách SP (danh mục hoặc kết quả tìm kiếm) |
| **Hậu điều kiện** | Danh sách SP được lọc theo tiêu chí, filter tags hiển thị |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Chọn một hoặc nhiều tiêu chí lọc (size: M, L; màu: Đen; giá: 200k–500k) | |
| 2 | | Lọc danh sách SP khớp, hiển thị filter tags đang áp dụng, cập nhật số lượng SP |
| 3 | (Tùy chọn) Nhấn "X" trên một filter tag để bỏ tiêu chí đó | Cập nhật lại danh sách |
| 4 | (Tùy chọn) Nhấn "Xóa tất cả bộ lọc" | Hiển thị lại toàn bộ SP không lọc |

---

## UC-13: Sắp xếp sản phẩm

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-13 |
| **Tên UC** | Sắp xếp sản phẩm |
| **Tác nhân chính** | Khách vãng lai (KVL), Khách hàng (KH) |
| **Mô tả** | Người dùng sắp xếp danh sách SP theo tiêu chí: giá tăng/giảm, mới nhất, bán chạy |
| **Tiền điều kiện** | Đang ở trang danh sách SP |
| **Hậu điều kiện** | Danh sách SP được sắp xếp lại |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Chọn tiêu chí sắp xếp từ dropdown (Giá tăng dần / Giá giảm dần / Mới nhất / Bán chạy) | Sắp xếp lại danh sách SP, giữ nguyên bộ lọc đang áp dụng |

---

## UC-14: Xem chi tiết sản phẩm

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-14 |
| **Tên UC** | Xem chi tiết sản phẩm |
| **Tác nhân chính** | Khách vãng lai (KVL), Khách hàng (KH) |
| **Mô tả** | Xem thông tin chi tiết SP: hình ảnh đa góc, mô tả, chất liệu, bảng size, giá, đánh giá, SP gợi ý |
| **Tiền điều kiện** | SP tồn tại và đang ở trạng thái hiển thị |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc). Nếu KH đăng nhập, lưu vào lịch sử xem |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Nhấn vào SP từ danh sách | Hiển thị trang chi tiết SP: gallery hình ảnh đa góc, tên SP, giá bán (giá gốc nếu có KM), mô tả, chất liệu, hướng dẫn bảo quản |
| 2 | | Hiển thị: danh sách biến thể (size + màu), bảng quy đổi size, số lượng tồn kho mỗi biến thể |
| 3 | | Hiển thị: đánh giá từ KH (rating trung bình, danh sách review), carousel sản phẩm gợi ý |
| 4 | (Tùy chọn) Chọn size + màu → nhấn "Thêm vào giỏ hàng" | Chuyển sang UC-17: Thêm sản phẩm vào giỏ hàng |

---

## UC-15: Thêm sản phẩm mới (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-15 |
| **Tên UC** | Thêm sản phẩm mới |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV thêm sản phẩm mới vào hệ thống với đầy đủ thông tin và ít nhất 1 biến thể |
| **Tiền điều kiện** | NV đã đăng nhập vào trang quản trị |
| **Hậu điều kiện** | SP mới được tạo trong CSDL với mã tự sinh, có ít nhất 1 biến thể |
| **Quy định liên quan** | QĐ1, QĐ2 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý sản phẩm" → nhấn "Thêm SP mới" | Hiển thị form thêm SP |
| 2 | Nhập: tên SP, mô tả, chất liệu, chọn danh mục, giá gốc, giá bán | |
| 3 | Upload hình ảnh (tối thiểu 1 ảnh) | Preview hình ảnh, lưu tạm |
| 4 | Thêm ít nhất 1 biến thể: chọn size, màu, nhập số lượng tồn, SKU tự sinh | Kiểm tra SKU không trùng |
| 5 | Nhấn "Tạo sản phẩm" | Validate: tên không trùng trong cùng danh mục, có ≥ 1 ảnh, có ≥ 1 biến thể. Tạo mã SP tự động, lưu CSDL |
| 6 | | Hiển thị "Thêm sản phẩm thành công", chuyển về danh sách SP |

**Luồng ngoại lệ:**

| Mã | Điều kiện | Xử lý |
|----|-----------|-------|
| 5a | Tên SP trùng trong cùng danh mục | Hiển thị lỗi "Tên sản phẩm đã tồn tại trong danh mục này" |
| 5b | Chưa có hình ảnh nào | Hiển thị lỗi "Vui lòng upload ít nhất 1 hình ảnh" |
| 5c | Chưa có biến thể nào | Hiển thị lỗi "Vui lòng thêm ít nhất 1 biến thể (size + màu)" |

---

## UC-16: Cập nhật / Xóa sản phẩm (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-16 |
| **Tên UC** | Cập nhật / Xóa sản phẩm |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV cập nhật thông tin SP hoặc ẩn/xóa mềm SP khỏi hệ thống |
| **Tiền điều kiện** | NV đã đăng nhập, SP tồn tại trong CSDL |
| **Hậu điều kiện** | Thông tin SP được cập nhật hoặc SP bị ẩn |
| **Quy định liên quan** | QĐ3, QĐ4 |

**Luồng sự kiện chính (Cập nhật):**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Chọn SP từ danh sách → nhấn "Sửa" | Hiển thị form cập nhật với dữ liệu hiện tại (mã SP readonly) |
| 2 | Chỉnh sửa thông tin: tên, mô tả, giá, hình ảnh, biến thể | |
| 3 | Nhấn "Lưu thay đổi" | Validate dữ liệu, cập nhật CSDL. Giá mới chỉ áp dụng cho đơn hàng mới |
| 4 | | Hiển thị "Cập nhật thành công" |

**Luồng thay thế (Xóa/Ẩn):**

| Mã | Điều kiện | Xử lý |
|----|-----------|-------|
| 1a | NV nhấn "Xóa/Ẩn" trên SP | Kiểm tra SP đã có đơn hàng chưa |
| 1a.1 | SP chưa có đơn hàng nào | Xác nhận → Xóa vật lý khỏi CSDL |
| 1a.2 | SP đã có đơn hàng | Chỉ thực hiện xóa mềm (đặt trạng thái "Ẩn"), SP không hiển thị trên storefront nhưng vẫn tồn tại trong CSDL |

---

## UC-16b: Quản lý danh mục sản phẩm (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-16b |
| **Tên UC** | Quản lý danh mục sản phẩm |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV thực hiện CRUD danh mục sản phẩm đa cấp (tối đa 3 cấp) |
| **Tiền điều kiện** | NV đã đăng nhập vào trang quản trị |
| **Hậu điều kiện** | Danh mục được thêm/sửa/xóa |
| **Quy định liên quan** | QĐ5 |

**Luồng sự kiện chính (Thêm danh mục):**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý danh mục" → nhấn "Thêm" | Hiển thị form: tên danh mục, danh mục cha (nếu có), mô tả, thứ tự hiển thị |
| 2 | Nhập thông tin, chọn danh mục cha | Kiểm tra cấp (tối đa 3 cấp) |
| 3 | Nhấn "Lưu" | Tạo danh mục mới |

**Luồng ngoại lệ:**

| Mã | Điều kiện | Xử lý |
|----|-----------|-------|
| 2a | Vượt quá 3 cấp | Hiển thị lỗi "Danh mục chỉ hỗ trợ tối đa 3 cấp" |
| Xóa | Danh mục còn chứa SP | Hiển thị lỗi "Không thể xóa danh mục đang chứa sản phẩm. Vui lòng di chuyển hoặc xóa SP trước" |

---

## UC-16c: Quản lý bộ sưu tập (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-16c |
| **Tên UC** | Quản lý bộ sưu tập |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV tạo, cập nhật, xóa bộ sưu tập (BST); gắn SP vào BST, đặt banner và thời gian hiển thị |
| **Tiền điều kiện** | NV đã đăng nhập |
| **Hậu điều kiện** | BST được cập nhật |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý bộ sưu tập" → "Tạo BST mới" | Hiển thị form: tên BST, mô tả, upload banner, chọn ngày bắt đầu/kết thúc hiển thị |
| 2 | Nhập thông tin, upload banner | |
| 3 | Tìm và gắn SP vào BST | Cho phép tìm kiếm SP, thêm vào danh sách BST |
| 4 | Nhấn "Lưu" | Tạo BST, tự động hiển thị trên storefront khi đến ngày bắt đầu |

---

## UC-16d: Tra cứu sản phẩm (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-16d |
| **Tên UC** | Tra cứu sản phẩm |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV tìm kiếm SP trong trang quản trị theo mã, tên, danh mục, trạng thái |
| **Tiền điều kiện** | NV đã đăng nhập |
| **Hậu điều kiện** | Không thay đổi dữ liệu (chỉ đọc) |
| **Quy định liên quan** | — |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Truy cập "Quản lý sản phẩm" | Hiển thị danh sách SP phân trang, bộ lọc: theo mã, tên, danh mục, trạng thái (Hiển thị/Ẩn/Hết hàng) |
| 2 | Nhập tiêu chí tìm kiếm / chọn bộ lọc | Lọc và hiển thị kết quả khớp |
| 3 | Nhấn vào SP để xem chi tiết | Hiển thị: thông tin SP, danh sách biến thể, số tồn kho, lịch sử thay đổi giá |

---

## UC-16e: Cập nhật tồn kho (Staff)

| Thuộc tính | Mô tả |
|---|---|
| **Mã UC** | UC-16e |
| **Tên UC** | Cập nhật số lượng tồn kho |
| **Tác nhân chính** | Nhân viên (NV) |
| **Mô tả** | NV cập nhật số lượng tồn kho cho biến thể SP. Hệ thống cũng tự động cập nhật khi có đơn hàng |
| **Tiền điều kiện** | NV đã đăng nhập, SP và biến thể tồn tại |
| **Hậu điều kiện** | Tồn kho được cập nhật, cảnh báo nếu < 10 |
| **Quy định liên quan** | QĐ6 |

**Luồng sự kiện chính:**

| Bước | Tác nhân | Hệ thống |
|------|----------|----------|
| 1 | Chọn SP → xem danh sách biến thể | Hiển thị biến thể với số tồn kho hiện tại |
| 2 | Nhập số lượng tồn kho mới cho biến thể | |
| 3 | Nhấn "Lưu" | Cập nhật tồn kho. Nếu tồn < 10, hiển thị cảnh báo "Sản phẩm sắp hết hàng" |
