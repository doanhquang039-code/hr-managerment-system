# HR Management System

HR Management System là đề tài xây dựng hệ thống quản lý nhân sự cho doanh nghiệp, tập trung vào các nghiệp vụ HRMS như quản lý nhân viên, phòng ban, chức vụ, hợp đồng, chấm công, nghỉ phép, bảng lương, tuyển dụng, đào tạo, KPI, tài liệu, tài sản, thông báo và các luồng hỗ trợ vận hành nội bộ.

Dự án sử dụng Spring Boot làm nền backend chính, Thymeleaf cho giao diện server-rendered, đồng thời bổ sung React theo hướng "island" cho các màn hình phức tạp như dashboard widget, LMS/course, monitor hoặc các tính năng tương tác cao. Cách làm này giúp giữ cấu trúc Spring Boot + Thymeleaf hiện tại nhưng vẫn có thể mở rộng UI hiện đại hơn ở từng phần.

## Công nghệ sử dụng

- Java 21
- Spring Boot 3.4.1
- Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf, Bootstrap, Bootstrap Icons
- React 18 dạng CDN/island cho một số màn hình tương tác
- MySQL
- Flyway Migration
- Redis Cache
- Apache Kafka cho audit/event pipeline
- Cloudinary cho upload ảnh/video
- Spring Mail, SendGrid
- OAuth2 Login Google/Facebook
- Actuator, Micrometer, Prometheus, Zipkin tracing
- Apache POI, iText/OpenPDF cho export Excel/PDF
- Docker, Docker Compose
- Testcontainers, JUnit, Spring Security Test

## Chức năng chính

- Quản lý nhân viên, role, phòng ban, chức vụ và nhóm.
- Quản lý hợp đồng lao động theo loại nhân viên.
- Quản lý chấm công, ca làm việc, nghỉ phép và tăng ca.
- Quản lý bảng lương, thanh toán, chi phí và tài sản.
- Tuyển dụng: tin tuyển dụng, ứng viên, pipeline, trạng thái tuyển.
- Onboarding/offboarding checklist cho nhân viên mới hoặc nghỉ việc.
- LMS/đào tạo: khóa học, video Cloudinary, tiến độ học, thư viện video.
- KPI/OKR, đánh giá hiệu suất, kỹ năng nhân viên.
- Thông báo nội bộ, tài liệu, QR code, khảo sát, vinh danh.
- Health Insight: dự đoán sức khỏe làm việc dựa trên dữ liệu sinh hoạt.
- System Monitor: theo dõi Database, Redis, Kafka, Cloud, Email, Audit log.
- Audit log và Kafka event pipeline cho các thao tác quan trọng.

## Yêu cầu môi trường

- JDK 21
- Maven Wrapper đã có sẵn trong dự án
- MySQL 8 hoặc Docker Compose
- Redis/Kafka là tùy chọn khi chạy dev; profile `dev` có thể tắt các service chậm
- Tài khoản Cloudinary nếu muốn upload ảnh/video thật

## Cấu hình nhanh

Copy file mẫu:

```powershell
Copy-Item .env.example .env
```

Sau đó chỉnh các biến cần thiết trong `.env`:

```env
MYSQL_DATABASE=hr_management_system
MYSQL_USER=hr_user
MYSQL_PASSWORD=hr_password
MYSQL_ROOT_PASSWORD=root

CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

MAIL_USERNAME=
MAIL_PASSWORD=

GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=

FACEBOOK_CLIENT_ID=
FACEBOOK_CLIENT_SECRET=

AI_GEMINI_ENABLED=false
GEMINI_API_KEY=
```

Nếu chỉ chạy local để demo giao diện và chức năng cơ bản, có thể dùng profile `dev` vì profile này đã tắt Kafka, Google Drive, Firebase, AWS S3, SendGrid, RabbitMQ, Elasticsearch và tracing để khởi động nhanh hơn.

## Cách chạy dự án

Chạy bằng Maven Wrapper:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

Hoặc build/compile nhanh:

```powershell
.\mvnw.cmd -DskipTests compile
```

Nếu dùng Docker Compose cho MySQL và các service phụ:

```powershell
docker compose up -d
```

Sau đó mở trình duyệt:

```text
http://localhost:8080
```

## Tài khoản và phân quyền

Hệ thống có nhiều role phục vụ các luồng nghiệp vụ khác nhau:

- `ADMIN`: quản trị toàn hệ thống.
- `HR`: nghiệp vụ nhân sự, tuyển dụng, đào tạo, hồ sơ.
- `MANAGER`: quản lý team, duyệt nghỉ, theo dõi công việc và báo cáo.
- `USER`: nhân viên, xem hồ sơ, chấm công, khóa học, công việc cá nhân.
- `HIRING`: xử lý tin tuyển dụng và ứng viên.

Dữ liệu tài khoản mẫu phụ thuộc vào migration/seed data hiện có trong database. Nếu database trống, cần chạy Flyway migration và tạo user admin trước khi đăng nhập.

## Cấu trúc thư mục chính

```text
src/main/java/com/example/hr
  controllers/     Controller render page và xử lý form
  api/             REST API
  models/          Entity JPA
  repository/      Spring Data repository
  service/         Business logic
  config/          Security, Kafka, cache, app config

src/main/resources
  templates/       Thymeleaf pages
  templates/fragments/ Sidebar, layout fragment dùng chung
  static/js/       JavaScript, React islands
  static/css/      CSS tĩnh
  db/migration/    Flyway migration
```

## Ghi chú phát triển

- Ưu tiên dùng `fragments/admin-sidebar.html` để đồng bộ menu admin giữa các trang.
- Các màn phức tạp nên mount React bằng `data-hrms-react` thay vì viết lại toàn bộ frontend.
- Các tính năng upload video/ảnh nên đi qua Cloudinary để dữ liệu không phụ thuộc filesystem local.
- Kafka dùng cho audit/event pipeline; khi chạy dev có thể tắt để khởi động nhanh.
- Flyway quản lý schema, hạn chế sửa database thủ công ngoài migration.

## Kiểm tra

Compile:

```powershell
.\mvnw.cmd -q -DskipTests compile
```

Chạy test:

```powershell
.\mvnw.cmd test
```

Chạy integration test khi cần:

```powershell
.\mvnw.cmd verify -Pintegration-tests
```

## Định hướng hoàn thiện tiếp

- Hoàn thiện workflow tuyển dụng sang tạo nhân viên, hợp đồng, onboarding và khóa học bắt buộc.
- Tách thêm các màn tương tác cao sang React island: task board, survey builder, onboarding checklist, system/Kafka monitor.
- Chuẩn hóa toàn bộ template cũ về UTF-8 và fragment chung.
- Bổ sung seed data và test cho các enum filter/menu option.
- Hoàn thiện dashboard theo từng role: admin, HR, manager, user, hiring.
