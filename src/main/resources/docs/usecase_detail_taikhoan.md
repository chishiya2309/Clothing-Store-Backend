# Mô tả chi tiết Use Case — Module Tài khoản & Xác thực

---

## UC-01: Đăng ký tài khoản

| Thuộc tính             | Mô tả                                                                                                              |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------ |
| **Mã UC**              | UC-01                                                                                                              |
| **Tên UC**             | Đăng ký tài khoản                                                                                                  |
| **Tác nhân chính**     | Khách vãng lai (KVL)                                                                                               |
| **Tác nhân phụ**       | Hệ thống Email (Brevo), Google OAuth                                                                               |
| **Mô tả**              | Khách vãng lai tạo tài khoản mới trên hệ thống bằng email hoặc tài khoản Google để trở thành Khách hàng đã đăng ký |
| **Tiền điều kiện**     | Khách vãng lai truy cập vào trang đăng ký                                                                          |
| **Hậu điều kiện**      | Tài khoản mới được tạo, email xác thực được gửi. Sau khi xác thực, KH có thể đăng nhập                             |
| **Quy định liên quan** | QĐ16                                                                                                               |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                        | Hệ thống                                                                       |
| ---- | ----------------------------------------------- | ------------------------------------------------------------------------------ |
| 1    | Chọn "Đăng ký" trên giao diện                   | Hiển thị form đăng ký (Email, Google)                                          |
| 2    | Nhập họ tên, email, mật khẩu, xác nhận mật khẩu |                                                                                |
| 3    | Nhấn nút "Đăng ký"                              | Kiểm tra tính hợp lệ: email chưa tồn tại, mật khẩu ≥ 8 ký tự (có chữ hoa + số) |
| 4    |                                                 | Tạo tài khoản ở trạng thái "Chưa xác thực", gửi mã OTP qua email               |
| 5    | Mở email, nhập mã OTP xác thực                  | Xác thực mã OTP, kích hoạt tài khoản                                           |
| 6    |                                                 | Hiển thị thông báo "Đăng ký thành công", chuyển đến trang đăng nhập            |

**Luồng thay thế:**

| Mã  | Điều kiện                      | Xử lý                                                                                                                                                 |
| --- | ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| 2a  | KVL chọn "Đăng ký bằng Google" | Hệ thống chuyển hướng đến Google OAuth. Sau khi xác thực Google thành công, tạo tài khoản tự động với thông tin từ Google, bỏ qua bước xác thực email |
| 5a  | Mã OTP hết hạn                 | Hiển thị thông báo "Mã OTP đã hết hạn". Cho phép gửi lại OTP                                                                                          |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                       | Xử lý                                                              |
| --- | ------------------------------- | ------------------------------------------------------------------ |
| 3a  | Email đã tồn tại trong hệ thống | Hiển thị lỗi "Email đã được đăng ký"                               |
| 3b  | Mật khẩu không đủ yêu cầu       | Hiển thị lỗi "Mật khẩu phải tối thiểu 8 ký tự, chứa chữ hoa và số" |
| 5b  | Nhập sai OTP quá 5 lần          | Khóa tạm tài khoản 15 phút, hiển thị thông báo                     |

---

## UC-02: Đăng nhập

| Thuộc tính             | Mô tả                                                                                                               |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------- |
| **Mã UC**              | UC-02                                                                                                               |
| **Tên UC**             | Đăng nhập                                                                                                           |
| **Tác nhân chính**     | Khách vãng lai (KVL)                                                                                                |
| **Tác nhân phụ**       | Google OAuth                                                                                                        |
| **Mô tả**              | Người dùng đăng nhập vào hệ thống bằng email/mật khẩu hoặc Google OAuth để truy cập các chức năng được phân quyền   |
| **Tiền điều kiện**     | Người dùng đã có tài khoản đã xác thực trên hệ thống                                                                |
| **Hậu điều kiện**      | Người dùng được xác thực, phiên đăng nhập được tạo, hệ thống chuyển hướng theo vai trò (Admin/Nhân viên/Khách hàng) |
| **Quy định liên quan** | —                                                                                                                   |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                | Hệ thống                                                                                                    |
| ---- | --------------------------------------- | ----------------------------------------------------------------------------------------------------------- |
| 1    | Truy cập trang đăng nhập                | Hiển thị form đăng nhập                                                                                     |
| 2    | Nhập email và mật khẩu                  |                                                                                                             |
| 3    | (Tùy chọn) Đánh dấu "Ghi nhớ đăng nhập" |                                                                                                             |
| 4    | Nhấn "Đăng nhập"                        | Xác thực thông tin đăng nhập                                                                                |
| 5    |                                         | Tạo JWT token (access + refresh). Nếu chọn "Ghi nhớ", refresh token có TTL dài hơn (30 ngày thay vì 7 ngày) |
| 6    |                                         | Chuyển hướng đến trang chính phù hợp với vai trò                                                            |

**Luồng thay thế:**

| Mã  | Điều kiện                               | Xử lý                                                                                            |
| --- | --------------------------------------- | ------------------------------------------------------------------------------------------------ |
| 2a  | Người dùng chọn "Đăng nhập bằng Google" | Chuyển hướng đến Google OAuth. Xác thực ID Token từ Google, tìm tài khoản tương ứng và đăng nhập |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                      | Xử lý                                                                       |
| --- | ------------------------------ | --------------------------------------------------------------------------- |
| 4a  | Email hoặc mật khẩu không đúng | Hiển thị "Email hoặc mật khẩu không chính xác"                              |
| 4b  | Tài khoản bị khóa              | Hiển thị "Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên"             |
| 4c  | Tài khoản chưa xác thực email  | Hiển thị "Vui lòng xác thực email trước khi đăng nhập" kèm link gửi lại OTP |

---

## UC-03: Đăng xuất

| Thuộc tính             | Mô tả                                                                 |
| ---------------------- | --------------------------------------------------------------------- |
| **Mã UC**              | UC-03                                                                 |
| **Tên UC**             | Đăng xuất                                                             |
| **Tác nhân chính**     | Quản trị viên (AD), Nhân viên (NV), Khách hàng (KH)                   |
| **Mô tả**              | Người dùng đăng xuất khỏi hệ thống, hủy phiên đăng nhập hiện tại      |
| **Tiền điều kiện**     | Người dùng đã đăng nhập                                               |
| **Hậu điều kiện**      | Phiên đăng nhập bị hủy, refresh token bị thu hồi, chuyển về trang chủ |
| **Quy định liên quan** | —                                                                     |

**Luồng sự kiện chính:**

| Bước | Tác nhân             | Hệ thống                                            |
| ---- | -------------------- | --------------------------------------------------- |
| 1    | Nhấn nút "Đăng xuất" |                                                     |
| 2    |                      | Thu hồi refresh token trong Redis, xóa cookie phiên |
| 3    |                      | Chuyển hướng về trang chủ                           |

---

## UC-04: Quên mật khẩu / Đặt lại mật khẩu

| Thuộc tính             | Mô tả                                                                             |
| ---------------------- | --------------------------------------------------------------------------------- |
| **Mã UC**              | UC-04                                                                             |
| **Tên UC**             | Quên mật khẩu / Đặt lại mật khẩu                                                  |
| **Tác nhân chính**     | Khách vãng lai (KVL), Khách hàng (KH)                                             |
| **Tác nhân phụ**       | Hệ thống Email                                                                    |
| **Mô tả**              | Người dùng yêu cầu đặt lại mật khẩu khi quên, hệ thống gửi link đặt lại qua email |
| **Tiền điều kiện**     | Người dùng có tài khoản email đã đăng ký                                          |
| **Hậu điều kiện**      | Mật khẩu được cập nhật, tất cả phiên đăng nhập cũ bị thu hồi                      |
| **Quy định liên quan** | QĐ15                                                                              |

**Luồng sự kiện chính:**

| Bước | Tác nhân                               | Hệ thống                                                                  |
| ---- | -------------------------------------- | ------------------------------------------------------------------------- |
| 1    | Nhấn "Quên mật khẩu" ở trang đăng nhập | Hiển thị form nhập email                                                  |
| 2    | Nhập email đã đăng ký                  |                                                                           |
| 3    | Nhấn "Gửi yêu cầu"                     | Kiểm tra email tồn tại, gửi link đặt lại mật khẩu (có thời hạn 15 phút)   |
| 4    | Mở email, nhấn link đặt lại mật khẩu   | Hiển thị form đặt lại mật khẩu                                            |
| 5    | Nhập mật khẩu mới và xác nhận          | Kiểm tra mật khẩu mới hợp lệ (≥ 8 ký tự, chữ hoa + số)                    |
| 6    |                                        | Cập nhật mật khẩu, thu hồi tất cả token cũ, hiển thị thông báo thành công |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                     | Xử lý                                                                |
| --- | ----------------------------- | -------------------------------------------------------------------- |
| 3a  | Email không tồn tại           | Vẫn hiển thị "Nếu email tồn tại, link đặt lại đã được gửi" (bảo mật) |
| 4a  | Link đã hết hạn (quá 15 phút) | Hiển thị "Link đã hết hạn. Vui lòng yêu cầu đặt lại mật khẩu mới"    |

---

## UC-05: Đổi mật khẩu

| Thuộc tính             | Mô tả                                               |
| ---------------------- | --------------------------------------------------- |
| **Mã UC**              | UC-05                                               |
| **Tên UC**             | Đổi mật khẩu                                        |
| **Tác nhân chính**     | Khách hàng (KH), Nhân viên (NV), Quản trị viên (AD) |
| **Mô tả**              | Người dùng thay đổi mật khẩu hiện tại của tài khoản |
| **Tiền điều kiện**     | Người dùng đã đăng nhập                             |
| **Hậu điều kiện**      | Mật khẩu được cập nhật thành công                   |
| **Quy định liên quan** | QĐ15                                                |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                                    | Hệ thống                                                                  |
| ---- | ----------------------------------------------------------- | ------------------------------------------------------------------------- |
| 1    | Truy cập "Đổi mật khẩu" trong phần quản lý tài khoản        | Hiển thị form đổi mật khẩu                                                |
| 2    | Nhập mật khẩu hiện tại, mật khẩu mới, xác nhận mật khẩu mới |                                                                           |
| 3    | Nhấn "Lưu thay đổi"                                         | Xác thực mật khẩu hiện tại đúng                                           |
| 4    |                                                             | Kiểm tra mật khẩu mới hợp lệ (≥ 8 ký tự, chữ hoa + số), cập nhật mật khẩu |
| 5    |                                                             | Hiển thị thông báo "Đổi mật khẩu thành công"                              |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                      | Xử lý                                                          |
| --- | ------------------------------ | -------------------------------------------------------------- |
| 3a  | Mật khẩu hiện tại không đúng   | Hiển thị lỗi "Mật khẩu hiện tại không chính xác"               |
| 4a  | Mật khẩu mới không đủ yêu cầu  | Hiển thị lỗi cụ thể (thiếu chữ hoa, thiếu số, quá ngắn)        |
| 4b  | Mật khẩu mới trùng mật khẩu cũ | Hiển thị lỗi "Mật khẩu mới không được trùng mật khẩu hiện tại" |

---

## UC-06: Quản lý thông tin cá nhân

| Thuộc tính             | Mô tả                                                                           |
| ---------------------- | ------------------------------------------------------------------------------- |
| **Mã UC**              | UC-06                                                                           |
| **Tên UC**             | Quản lý thông tin cá nhân                                                       |
| **Tác nhân chính**     | Khách hàng (KH)                                                                 |
| **Mô tả**              | Khách hàng xem và cập nhật thông tin cá nhân: họ tên, SĐT, ngày sinh, giới tính |
| **Tiền điều kiện**     | KH đã đăng nhập                                                                 |
| **Hậu điều kiện**      | Thông tin cá nhân được cập nhật trong CSDL                                      |
| **Quy định liên quan** | QĐ14                                                                            |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                                           | Hệ thống                                                                          |
| ---- | ------------------------------------------------------------------ | --------------------------------------------------------------------------------- |
| 1    | Truy cập "Thông tin cá nhân"                                       | Hiển thị thông tin hiện tại (họ tên, email (readonly), SĐT, ngày sinh, giới tính) |
| 2    | Chỉnh sửa các trường được phép (họ tên, SĐT, ngày sinh, giới tính) |                                                                                   |
| 3    | Nhấn "Lưu thay đổi"                                                | Validate dữ liệu, cập nhật vào CSDL                                               |
| 4    |                                                                    | Hiển thị thông báo "Cập nhật thành công"                                          |

**Luồng ngoại lệ:**

| Mã  | Điều kiện              | Xử lý                                                  |
| --- | ---------------------- | ------------------------------------------------------ |
| 2a  | KH cố sửa trường email | Trường email ở trạng thái readonly, không cho phép sửa |
| 3a  | SĐT không hợp lệ       | Hiển thị lỗi "Số điện thoại không hợp lệ"              |

---

## UC-07: Quản lý địa chỉ giao hàng

| Thuộc tính             | Mô tả                                                                      |
| ---------------------- | -------------------------------------------------------------------------- |
| **Mã UC**              | UC-07                                                                      |
| **Tên UC**             | Quản lý địa chỉ giao hàng                                                  |
| **Tác nhân chính**     | Khách hàng (KH)                                                            |
| **Mô tả**              | KH quản lý danh sách nhiều địa chỉ giao hàng: thêm, sửa, xóa, đặt mặc định |
| **Tiền điều kiện**     | KH đã đăng nhập                                                            |
| **Hậu điều kiện**      | Danh sách địa chỉ được cập nhật                                            |
| **Quy định liên quan** | —                                                                          |

**Luồng sự kiện chính (Thêm địa chỉ):**

| Bước | Tác nhân                                                                       | Hệ thống                                                                    |
| ---- | ------------------------------------------------------------------------------ | --------------------------------------------------------------------------- |
| 1    | Truy cập "Quản lý địa chỉ", nhấn "Thêm địa chỉ"                                | Hiển thị form thêm địa chỉ                                                  |
| 2    | Nhập: tên người nhận, SĐT, tỉnh/thành, quận/huyện, phường/xã, địa chỉ chi tiết |                                                                             |
| 3    | (Tùy chọn) Đánh dấu "Đặt làm mặc định"                                         |                                                                             |
| 4    | Nhấn "Lưu"                                                                     | Validate dữ liệu, lưu địa chỉ mới. Nếu đặt mặc định, bỏ mặc định địa chỉ cũ |

**Luồng thay thế:**

| Mã  | Điều kiện                             | Xử lý                                                                                    |
| --- | ------------------------------------- | ---------------------------------------------------------------------------------------- |
| 1a  | KH chọn "Sửa" trên một địa chỉ có sẵn | Hiển thị form sửa với dữ liệu hiện tại, cho phép cập nhật                                |
| 1b  | KH chọn "Xóa" trên một địa chỉ        | Xác nhận xóa, xóa địa chỉ khỏi danh sách. Không cho xóa nếu là địa chỉ mặc định duy nhất |

---

## UC-08: Quản lý tài khoản người dùng (Admin)

| Thuộc tính             | Mô tả                                                                                |
| ---------------------- | ------------------------------------------------------------------------------------ |
| **Mã UC**              | UC-08                                                                                |
| **Tên UC**             | Quản lý tài khoản người dùng                                                         |
| **Tác nhân chính**     | Quản trị viên (AD)                                                                   |
| **Mô tả**              | Admin xem danh sách tài khoản, khóa/mở khóa tài khoản, cấp/thu hồi vai trò Nhân viên |
| **Tiền điều kiện**     | Admin đã đăng nhập vào trang quản trị                                                |
| **Hậu điều kiện**      | Trạng thái hoặc quyền tài khoản được cập nhật                                        |
| **Quy định liên quan** | —                                                                                    |

**Luồng sự kiện chính (Xem danh sách):**

| Bước | Tác nhân                           | Hệ thống                                                                                   |
| ---- | ---------------------------------- | ------------------------------------------------------------------------------------------ |
| 1    | Truy cập "Quản lý người dùng"      | Hiển thị danh sách tài khoản (phân trang), có tìm kiếm theo tên/email/SĐT                  |
| 2    | Chọn một tài khoản để xem chi tiết | Hiển thị thông tin chi tiết: họ tên, email, vai trò, trạng thái, ngày tạo, hạng thành viên |

**Luồng thay thế:**

| Mã  | Điều kiện                            | Xử lý                                                                                |
| --- | ------------------------------------ | ------------------------------------------------------------------------------------ |
| 2a  | Admin nhấn "Khóa tài khoản"          | Xác nhận, cập nhật trạng thái thành "Bị khóa". Tất cả phiên đăng nhập bị thu hồi     |
| 2b  | Admin nhấn "Mở khóa tài khoản"       | Cập nhật trạng thái thành "Hoạt động"                                                |
| 2c  | Admin nhấn "Cấp quyền Nhân viên"     | Gán vai trò STAFF cho tài khoản. Người dùng có thể truy cập trang quản trị nghiệp vụ |
| 2d  | Admin nhấn "Thu hồi quyền Nhân viên" | Xóa vai trò STAFF. Người dùng chỉ còn quyền Khách hàng                               |

**Luồng ngoại lệ:**

| Mã  | Điều kiện                          | Xử lý                                                  |
| --- | ---------------------------------- | ------------------------------------------------------ |
| 2e  | Admin cố khóa chính tài khoản mình | Hiển thị lỗi "Không thể khóa tài khoản của chính mình" |

---

## UC-09: Xem điểm tích lũy và hạng thành viên

| Thuộc tính             | Mô tả                                                                     |
| ---------------------- | ------------------------------------------------------------------------- |
| **Mã UC**              | UC-09                                                                     |
| **Tên UC**             | Xem điểm tích lũy và hạng thành viên                                      |
| **Tác nhân chính**     | Khách hàng (KH)                                                           |
| **Mô tả**              | KH xem số điểm tích lũy hiện tại, hạng thành viên và các ưu đãi tương ứng |
| **Tiền điều kiện**     | KH đã đăng nhập                                                           |
| **Hậu điều kiện**      | Không thay đổi dữ liệu (chỉ đọc)                                          |
| **Quy định liên quan** | 1 điểm = 1.000 VNĐ mua hàng                                               |

**Luồng sự kiện chính:**

| Bước | Tác nhân                                       | Hệ thống                                                                                                                   |
| ---- | ---------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| 1    | Truy cập "Hạng thành viên" trong trang cá nhân | Hiển thị: điểm tích lũy hiện tại, hạng hiện tại (Đồng/Bạc/Vàng/Kim cương), ưu đãi của hạng, điểm cần để lên hạng tiếp theo |
