# BẢNG PHÂN CÔNG VÀ KHỐI LƯỢNG CÔNG VIỆC DỰ ÁN

Bảng dưới đây liệt kê chi tiết từng đầu công việc (tính năng / usecase) đã được phân công và hoàn thiện bởi các thành viên trong nhóm 10. Mỗi hạng mục được định nghĩa là một gói công việc hoàn chỉnh (bao gồm thiết kế giao diện, xử lý logic nghiệp vụ và tương tác dữ liệu cơ sở dữ liệu), thể hiện đúng khối lượng và vai trò thực tế của từng cá nhân trong hệ thống "Website bán quần áo".

## 1. Thành viên: LÊ QUANG HƯNG (23110110)

| STT | Hạng mục công việc (Tính năng) | Mô tả chi tiết đầu công việc (Nhiệm vụ hoàn thành) |
|:---:|:---|:---|
| 1 | **Xây dựng chức năng Đăng ký tài khoản** | Thiết kế giao diện và phát triển logic đăng ký, xác thực dữ liệu đầu vào, kiểm tra trùng lặp email/SĐT và mã hóa mật khẩu người dùng trước khi lưu. |
| 2 | **Xây dựng chức năng Đăng nhập & Đăng xuất** | Phát triển luồng xác thực người dùng, kiểm tra thông tin tài khoản, cấp quyền truy cập, tạo và hủy phiên làm việc (session/token) an toàn. |
| 3 | **Xử lý Quên mật khẩu và Đặt lại mật khẩu** | Xây dựng luồng khôi phục mật khẩu, tạo mã xác thực, tích hợp gửi email chứa đường dẫn khôi phục và xử lý logic cập nhật mật khẩu mới. |
| 4 | **Tính năng Đổi mật khẩu bảo mật** | Phát triển chức năng cho phép người dùng đang đăng nhập có thể xác thực lại mật khẩu cũ và thiết lập mật khẩu mới. |
| 5 | **Quản lý thông tin cá nhân khách hàng** | Thiết kế form và API cho phép khách hàng xem và cập nhật các thông tin hồ sơ cơ bản (họ tên, SĐT, ngày sinh, giới tính). |
| 6 | **Quản lý sổ địa chỉ giao hàng** | Phát triển module lưu trữ nhiều địa chỉ cho một khách hàng, bao gồm chức năng thêm mới, chỉnh sửa, xóa và thiết lập địa chỉ giao hàng mặc định. |
| 7 | **Quản lý tài khoản người dùng (Admin)** | Xây dựng trang quản trị hiển thị danh sách người dùng, công cụ lọc, khóa/mở khóa tài khoản khách hàng và phân quyền hệ thống. |
| 8 | **Tính năng Điểm tích lũy và Hạng thành viên** | Phát triển logic tự động tính điểm tích lũy từ lịch sử mua hàng, nâng/hạ hạng thành viên và hiển thị thông tin trực quan trên trang cá nhân khách hàng. |
| 9 | **Hiển thị danh sách sản phẩm theo danh mục** | Xây dựng trang chuyên mục (Nam, Nữ, Trẻ em) dưới dạng lưới (grid), tích hợp thuật toán phân trang dữ liệu và tối ưu hóa tải nội dung. |
| 10 | **Tìm kiếm sản phẩm thông minh** | Phát triển thanh tìm kiếm sản phẩm theo từ khóa (tên, mã) tích hợp tính năng tự động gợi ý kết quả (autocomplete) ngay khi nhập liệu. |
| 11 | **Bộ lọc sản phẩm đa tiêu chí** | Xây dựng hệ thống lọc kết quả động dựa trên nhiều thuộc tính cùng lúc (kích cỡ, màu sắc, khoảng giá, chất liệu) không cần tải lại toàn trang. |
| 12 | **Chức năng Sắp xếp sản phẩm** | Cài đặt các thuật toán sắp xếp danh sách sản phẩm theo yêu cầu: giá tăng/giảm, sản phẩm mới nhất, sản phẩm bán chạy nhất. |
| 13 | **Quản lý danh sách Yêu thích (Wishlist)** | Thiết kế và phát triển tính năng "thả tim" để thêm/xóa sản phẩm khỏi danh sách yêu thích cá nhân, quản lý kho lưu trữ trạng thái người dùng. |
| 14 | **Xây dựng thuật toán Sản phẩm gợi ý** | Phát triển khối hiển thị các sản phẩm gợi ý dựa trên lịch sử mua hàng, danh mục đang xem hoặc xu hướng sản phẩm nổi bật. |
| 15 | **Quản lý Banner / Slider trang chủ (Staff)** | Xây dựng công cụ cho nhân viên tải lên (upload) hình ảnh banner, thiết lập liên kết (link) chiến dịch và cấu hình thời gian hiển thị động. |

<br>

## 2. Thành viên: NGUYỄN THÁI BẢO (23110078)

| STT | Hạng mục công việc (Tính năng) | Mô tả chi tiết đầu công việc (Nhiệm vụ hoàn thành) |
|:---:|:---|:---|
| 16 | **Xây dựng trang Chi tiết sản phẩm** | Phát triển giao diện chi tiết, hiển thị hình ảnh đa góc (gallery), mô tả chi tiết, hướng dẫn bảo quản, bảng size và lượng tồn kho theo từng biến thể. |
| 17 | **Xử lý Thêm sản phẩm vào giỏ hàng** | Xây dựng logic chọn phân loại hàng (màu sắc, kích thước) và số lượng, kiểm tra tồn kho trực tiếp và cập nhật nhanh vào giỏ hàng thu nhỏ (mini-cart). |
| 18 | **Quản lý Giỏ hàng (Cart) toàn diện** | Thiết kế trang giỏ hàng chi tiết, cho phép thay đổi số lượng, xóa sản phẩm khỏi giỏ, và hệ thống tự động tính toán lại tổng tiền tạm tính. |
| 19 | **Quản lý và tra cứu Lịch sử đặt hàng** | Phát triển chức năng cho khách hàng xem lại toàn bộ các đơn hàng đã đặt, theo dõi trạng thái hiện tại (đang giao, hoàn thành) và xem biên lai chi tiết. |
| 20 | **Chức năng Hủy đơn hàng từ khách hàng** | Xây dựng luồng cho phép khách hàng tự thao tác hủy đơn đối với các đơn hàng ở trạng thái "Chờ xác nhận" và cập nhật lại trạng thái hệ thống. |
| 21 | **Viết Đánh giá sản phẩm (Review)** | Phát triển tính năng cho khách hàng chấm điểm bằng sao (1-5), nhập nội dung bình luận và đính kèm hình ảnh thực tế cho các sản phẩm đã hoàn thành. |
| 22 | **Hiển thị và lọc Đánh giá sản phẩm** | Xây dựng khu vực hiển thị nhận xét trên trang chi tiết sản phẩm, tính toán điểm sao trung bình và tích hợp bộ lọc đánh giá (theo số sao, đánh giá có hình). |

<br>

## 3. Thành viên: ĐÀO NGUYỄN NHẬT ANH (23110073)

| STT | Hạng mục công việc (Tính năng) | Mô tả chi tiết đầu công việc (Nhiệm vụ hoàn thành) |
|:---:|:---|:---|
| 23 | **Thêm sản phẩm mới (dành cho Staff)** | Phát triển module nhập liệu cho nhân viên thêm mới sản phẩm, khai báo các thuộc tính cơ bản, chi tiết, upload bộ ảnh và khởi tạo thông tin biến thể. |
| 24 | **Cập nhật và Xóa/Ẩn sản phẩm** | Xây dựng tính năng chỉnh sửa thông tin sản phẩm đã tạo, cập nhật hình ảnh và cung cấp chức năng ẩn sản phẩm thay vì xóa hoàn toàn để bảo vệ dữ liệu. |
| 25 | **Quản trị Danh mục sản phẩm (Category)** | Phát triển module quản lý sơ đồ danh mục đa cấp, cho phép thêm mới, chỉnh sửa, xóa và sắp xếp thứ tự hiển thị của các danh mục cha/con. |
| 26 | **Quản lý Bộ sưu tập (Collection)** | Xây dựng tính năng tạo các bộ sưu tập chiến dịch, gán hàng loạt sản phẩm vào bộ sưu tập, thiết lập banner và thời hạn hiển thị cho từng sự kiện. |
| 27 | **Tra cứu sản phẩm nâng cao (Nội bộ)** | Thiết kế bộ công cụ tìm kiếm và lọc dữ liệu sản phẩm trong hệ thống quản trị, hỗ trợ nhân viên tra cứu nhanh theo mã SKU, tên, danh mục, trạng thái. |
| 28 | **Cập nhật số lượng Tồn kho trực tiếp** | Phát triển giao diện thao tác nhanh cho phép nhân viên điều chỉnh, tăng/giảm trực tiếp số lượng tồn kho của từng biến thể sản phẩm cụ thể. |
| 29 | **Kiểm duyệt Đánh giá sản phẩm** | Xây dựng công cụ quản lý đánh giá cho nhân viên, cho phép xem xét, duyệt hiển thị hoặc xóa/ẩn các đánh giá vi phạm tiêu chuẩn cộng đồng từ khách hàng. |
| 30 | **Thống kê Doanh thu (Dashboard Admin)** | Thiết kế và phát triển các biểu đồ trực quan (chart) để tổng hợp, phân tích và báo cáo doanh thu cửa hàng theo các mốc thời gian: ngày, tuần, tháng, năm. |
| 31 | **Báo cáo Sản phẩm bán chạy** | Xây dựng chức năng tính toán, trích xuất dữ liệu và hiển thị danh sách Top các sản phẩm có số lượng bán ra hoặc mang lại doanh thu cao nhất. |
| 32 | **Trích xuất Báo cáo Khách hàng thân thiết** | Phát triển tính năng xếp hạng khách hàng dựa trên tổng chi tiêu và điểm tích lũy, trích xuất dữ liệu phục vụ các chiến dịch chăm sóc khách hàng (CRM). |

<br>

## 4. Thành viên: NGUYỄN ĐOÀN TRƯỜNG VĨ (23110173)

| STT | Hạng mục công việc (Tính năng) | Mô tả chi tiết đầu công việc (Nhiệm vụ hoàn thành) |
|:---:|:---|:---|
| 33 | **Áp dụng Voucher / Mã giảm giá** | Phát triển thuật toán kiểm tra tính hợp lệ của mã giảm giá (thời hạn, điều kiện áp dụng, số lượt dùng) và tự động trừ tiền chiết khấu vào tổng hóa đơn. |
| 34 | **Đặt hàng và Thanh toán (Checkout)** | Xây dựng quy trình thanh toán (Checkout) hoàn chỉnh: tiếp nhận địa chỉ giao, chọn phương thức thanh toán, lưu đơn hàng vào hệ thống và tích hợp chuyển hướng. |
| 35 | **Quản lý xử lý Đơn hàng (Staff)** | Phát triển bảng điều khiển đơn hàng cho nhân viên: tiếp nhận đơn mới, cập nhật trạng thái (đang đóng gói, đang giao hàng, hoàn thành) và xử lý yêu cầu hủy. |
| 36 | **Quản trị mã Khuyến mãi & Voucher** | Xây dựng công cụ cho phép nhân viên cửa hàng tạo các mã giảm giá mới, cấu hình loại giảm giá (phần trăm, tiền mặt) và các ràng buộc sử dụng liên quan. |
| 37 | **Hệ thống Báo cáo Tồn kho & Cảnh báo** | Phát triển module phân tích tồn kho hiện tại, thống kê các mặt hàng sắp hết (dưới định mức) và hiển thị cảnh báo để nhân viên chủ động kế hoạch nhập hàng. |

<br>

## 5. Công việc chung & Phối hợp

| STT | Thành viên tham gia | Hạng mục công việc | Mô tả chi tiết đầu công việc (Nhiệm vụ hoàn thành) |
|:---:|:---|:---|:---|
| 38 | **Nhật Anh, Vĩ** | **Viết Báo cáo Kỹ thuật (Report Class/Pattern)** | Phân tích, tổng hợp và viết tài liệu báo cáo kỹ thuật chi tiết về cấu trúc, chức năng và việc áp dụng Design Pattern cho các class trong toàn bộ hệ thống. |
| 39 | **Toàn nhóm** | **Thực hiện Video Demo, Slide & Thuyết trình** | Lên kịch bản, quay và dựng video demo sản phẩm hoàn chỉnh. Chuẩn bị slide thuyết trình, kịch bản trả lời phản biện và thực hiện báo cáo bảo vệ đồ án cuối kỳ. |

---
**Lưu ý định dạng:** *Tài liệu này được viết bằng Markdown chuẩn, hỗ trợ xuất (Export) hoặc sao chép (Copy-Paste) trực tiếp sang định dạng Microsoft Word giữ nguyên toàn bộ cấu trúc bảng và tiêu đề.*
