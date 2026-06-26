# C. PHẦN KẾT LUẬN {#c-phần-kết-luận .P1}

## 1. Kết quả đạt được {#kết-quả-đạt-được .P1}

Sau quá trình nghiên cứu, phân tích và thực nghiệm xây dựng hệ thống thương mại điện tử trực tuyến (Clothing Store), nhóm sinh viên thực hiện đồ án đã gặt hái được những kết quả đáng kể trên cả ba bình diện: lý thuyết, kỹ năng thực hành và chất lượng sản phẩm cuối. 

### 1.1. Về mặt kiến thức {#về-mặt-kiến-thức .P2}
- **Thấu hiểu nền tảng cấu trúc phần mềm:** Nắm vững và hệ thống hóa được các nguyên lý thiết kế phần mềm hướng đối tượng cốt lõi (như bộ nguyên lý SOLID, DRY, KISS). 
- **Làm chủ Mẫu thiết kế (Design Patterns):** Đi sâu nghiên cứu và thấu hiểu bản chất của 20 mẫu thiết kế kinh điển thuộc nhóm GoF (Creational, Structural, Behavioral) cũng như các mẫu kiến trúc và quản trị dữ liệu (Architecture & Data Patterns). Không chỉ dừng ở lý thuyết, nhóm đã hiểu rõ hoàn cảnh áp dụng (Context), vấn đề cần giải quyết (Problem) và giải pháp (Solution) của từng mẫu.
- **Tư duy kiến trúc hệ thống (System Architecture):** Hiểu rõ cơ chế hoạt động của các khung làm việc (Frameworks) hiện đại như Spring Boot (thông qua cơ chế IoC, AOP) và cách tổ chức luồng dữ liệu bảo mật, nhất quán trong hệ thống web đa tầng.

### 1.2. Về mặt kỹ năng {#về-mặt-kỹ-năng .P2}
- **Kỹ năng phân tích và thiết kế:** Chuyển đổi thành công các yêu cầu nghiệp vụ (Business Requirements) phức tạp thành bản thiết kế lớp (Class Diagram), sơ đồ cơ sở dữ liệu (ERD) và hợp đồng API (API Contracts) minh bạch, rõ ràng.
- **Kỹ năng tái cấu trúc (Refactoring) và viết mã sạch (Clean Code):** Rèn luyện được khả năng phát hiện "code smell", giải quyết triệt để vấn đề phụ thuộc chằng chịt (Tight Coupling) và các khối lệnh điều kiện phức tạp (spaghetti `if-else`) thông qua việc tích hợp mẫu thiết kế (như Strategy, State, Chain of Responsibility). 
- **Kỹ năng tích hợp hệ thống bên thứ ba:** Khai thác hiệu quả mẫu Adapter và Facade để giao tiếp trơn tru với các nền tảng ngoại vi (Cổng thanh toán MoMo, VNPay; Dịch vụ Brevo Email) mà không làm ảnh hưởng đến mã nguồn lõi.

### 1.3. Về mặt sản phẩm {#về-mặt-sản-phẩm .P2}
- **Hoàn thiện hệ thống thực tiễn:** Xây dựng thành công một ứng dụng bán quần áo trực tuyến vận hành ổn định, bao hàm toàn bộ các nghiệp vụ phức tạp của thương mại điện tử: từ quản lý danh mục, sản phẩm, quản lý phiên giỏ hàng, áp dụng Voucher, xử lý thanh toán bất đồng bộ, đến quy trình kiểm duyệt đánh giá tự động.
- **Kiến trúc mã nguồn chất lượng cao:** Sản phẩm là minh chứng rõ nét cho việc áp dụng thành công 20 mẫu thiết kế vào thực tiễn. Hệ thống đạt được độ linh hoạt (Flexibility) và tính mở rộng (Extensibility) cao, dễ dàng bảo trì và bổ sung các tính năng mới mà không phá vỡ cấu trúc hiện tại.

## 2. Những mặt hạn chế {#những-mặt-hạn-chế .P1}

Mặc dù hệ thống đã đáp ứng tốt các yêu cầu đề ra ban đầu, song do giới hạn về mặt thời gian và nguồn lực triển khai, đồ án vẫn còn một số điểm cần khắc phục:
- **Giới hạn trong tối ưu hóa hiệu năng truy xuất:** Kiến trúc hiện tại chủ yếu thao tác trực tiếp với cơ sở dữ liệu quan hệ (RDBMS). Chưa áp dụng triệt để cơ chế bộ nhớ đệm (Caching với Redis) ở các API truy xuất danh sách sản phẩm lớn, có khả năng dẫn đến nghẽn cổ chai (Bottleneck) khi lưu lượng truy cập tăng đột biến.
- **Công cụ tìm kiếm chưa chuyên sâu:** Tính năng tìm kiếm động mới dừng ở mức sử dụng Criteria API (Specification Pattern) của JPA. Chưa tích hợp các công cụ tìm kiếm toàn văn bản chuyên biệt (như Elasticsearch) nhằm mang lại trải nghiệm truy vấn nhanh và gợi ý thông minh hơn.
- **Độ bao phủ kiểm thử (Test Coverage):** Hệ thống mới tập trung hoàn thiện tính năng và kiến trúc lõi, tỷ lệ bao phủ của các bài kiểm thử tự động (Unit Test, Integration Test) chưa đạt mức tối đa cho các kịch bản ngoại lệ hiếm gặp.

## 3. Hướng phát triển tương lai {#hướng-phát-triển-tương-lai .P1}

Dựa trên nền tảng kiến trúc linh hoạt đã được thiết lập, nhóm đề xuất các định hướng phát triển và nâng cấp hệ thống trong tương lai:
- **Chuyển đổi sang Kiến trúc Vi dịch vụ (Microservices):** Bóc tách các miền nghiệp vụ lớn (như Quản lý người dùng, Giỏ hàng, Thanh toán, Đơn hàng) thành các dịch vụ độc lập. Triển khai mẫu thiết kế CQRS (Command Query Responsibility Segregation) và Event Sourcing nhằm tối ưu hóa tải cho hệ thống lớn.
- **Tích hợp Trí tuệ Nhân tạo (AI):** Ứng dụng Học máy (Machine Learning) để xây dựng hệ thống gợi ý sản phẩm (Recommendation System) được cá nhân hóa; đồng thời áp dụng xử lý ngôn ngữ tự nhiên (NLP) để phân tích sắc thái đánh giá của khách hàng chuyên sâu hơn.
- **Nâng cấp hạ tầng triển khai (DevOps):** Áp dụng tự động hóa hoàn toàn quy trình CI/CD, Container hóa toàn bộ hệ thống bằng Docker và đóng gói triển khai trên các cụm điều phối container như Kubernetes (K8s) trên nền tảng đám mây (AWS/GCP), đảm bảo khả năng chịu lỗi (High Availability) và tự động mở rộng (Auto-scaling).
