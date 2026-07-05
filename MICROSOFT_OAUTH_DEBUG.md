# Microsoft OAuth2 Login Debug Guide

## Cấu hình hiện tại

### application.properties
```properties
spring.security.oauth2.client.registration.microsoft.client-id=YOUR_MICROSOFT_CLIENT_ID
spring.security.oauth2.client.registration.microsoft.client-secret=YOUR_MICROSOFT_CLIENT_SECRET
spring.security.oauth2.client.registration.microsoft.scope=openid,profile,email,User.Read
spring.security.oauth2.client.registration.microsoft.redirect-uri={baseUrl}/login/oauth2/code/microsoft
```

### Azure Portal - Redirect URIs cần có
```
http://localhost:8080/login/oauth2/code/microsoft
```

## Các lỗi thường gặp và cách fix

### 1. AADSTS50011: The redirect URI specified in the request does not match

**Nguyên nhân:** Redirect URI trong Azure Portal khác với trong application.properties

**Giải pháp:**
- Vào Azure Portal > App registrations > Your App > Authentication
- Thêm chính xác URI: `http://localhost:8080/login/oauth2/code/microsoft`
- Platform type phải là **Web**
- Nhấn Save

### 2. AADSTS7000218: The request body must contain the following parameter: 'client_assertion'

**Nguyên nhân:** Client secret đã hết hạn hoặc không đúng

**Giải pháp:**
- Vào Azure Portal > Certificates & secrets
- Tạo New client secret
- Copy secret VALUE (không phải Secret ID)
- Cập nhật vào `application.properties`

### 3. AADSTS650053: The application tried to access a service that your organization hasn't subscribed to

**Nguyên nhân:** Thiếu API permissions

**Giải pháp:**
- Vào Azure Portal > API permissions
- Add permission > Microsoft Graph
- Thêm: `openid`, `profile`, `email`, `User.Read`
- Grant admin consent (nếu cần)

### 4. Invalid state parameter

**Nguyên nhân:** Session bị mất hoặc CSRF token không khớp

**Giải pháp:**
- Clear browser cookies
- Restart application
- Thử lại

### 5. 401 Unauthorized sau khi login thành công

**Nguyên nhân:** User chưa có trong database hoặc role không đúng

**Giải pháp:**
- Check CustomOAuth2UserService
- Kiểm tra email từ Microsoft có được lưu vào DB không
- Kiểm tra role được gán cho user mới

## Test OAuth2 Configuration

### 1. Kiểm tra OAuth2 endpoint
```
GET http://localhost:8080/oauth2/authorization/microsoft
```

Endpoint này sẽ redirect bạn đến Microsoft login page.

### 2. Kiểm tra sau khi login
Sau khi login thành công, bạn sẽ được redirect về:
```
http://localhost:8080/login/oauth2/code/microsoft?code=...&state=...
```

Spring Security sẽ tự động xử lý callback này.

### 3. Kiểm tra user được tạo trong database
```sql
SELECT * FROM users WHERE email LIKE '%microsoft%' OR email LIKE '%@%';
```

## Debug trong code

### Bật debug logging cho OAuth2
Thêm vào `application.properties`:
```properties
logging.level.org.springframework.security.oauth2=DEBUG
logging.level.org.springframework.security.web=DEBUG
```

### Xem thông tin user sau khi login
Trong `CustomOAuth2UserService.java`, thêm logging:
```java
System.out.println("OAuth2 Provider: " + clientName);
System.out.println("Email: " + email);
System.out.println("Name: " + name);
System.out.println("Attributes: " + oAuth2User.getAttributes());
```

## Checklist trước khi test

- [ ] Azure App Registration đã tạo
- [ ] Client ID và Client Secret đã copy đúng
- [ ] Redirect URI khớp chính xác: `http://localhost:8080/login/oauth2/code/microsoft`
- [ ] Platform type là **Web** (không phải Single-page application)
- [ ] ID tokens đã được bật trong Implicit grant
- [ ] API permissions: `openid`, `profile`, `email`, `User.Read`
- [ ] Tenant ID đúng trong authorization/token URI
- [ ] Application đang chạy trên port 8080
- [ ] Database có default Department (id=1) và JobPosition (id=1)

## Nếu vẫn lỗi

1. Stop application
2. Clear browser cache và cookies
3. Xóa thư mục `target/`
4. Rebuild: `./mvnw clean compile`
5. Restart application
6. Thử lại với InPrivate/Incognito window

## URLs quan trọng

- **Login page:** http://localhost:8080/login
- **OAuth2 Microsoft endpoint:** http://localhost:8080/oauth2/authorization/microsoft
- **Callback URL:** http://localhost:8080/login/oauth2/code/microsoft
- **After login:** http://localhost:8080/user1/dashboard

## Contact Info

Nếu vẫn gặp vấn đề, gửi thông tin sau:
1. Screenshot lỗi từ browser
2. Console logs từ browser (F12 > Console)
3. Application logs (terminal output)
4. Azure Portal Authentication settings screenshot
