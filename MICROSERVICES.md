# HRMS Microservices

Kien truc hien tai duoc mo rong theo huong strangler pattern: monolith HRMS van chay nhu cu, cac domain moi duoc tach dan ra service rieng.

## Service da them

### API Gateway

Thu muc:

```text
services/api-gateway
```

Port mac dinh:

```text
8088
```

Route:

- `GET /api/notifications/**` -> notification-service
- `/api/hrms/**` -> monolith HRMS tai `http://localhost:8080`
- `/hrms/**` -> monolith HRMS tai `http://localhost:8080`

### Notification Service

Thu muc:

```text
services/notification-service
```

Port mac dinh:

```text
8082
```

DB rieng:

```text
hr_notification_service
```

API:

```text
GET  /api/notifications/users/{userId}
GET  /api/notifications/users/{userId}/unread-count
POST /api/notifications
PATCH /api/notifications/{id}/read
```

Kafka consumer:

```text
topic: hr-notifications
group: notification-service
```

## Chay microservices

Can Docker Desktop dang chay.

```text
start-microservices.bat
```

Dung:

```text
stop-microservices.bat
```

URL kiem tra:

```text
http://localhost:8088/actuator/health
http://localhost:8082/actuator/health
```

## Test nhanh API notification qua gateway

Tao notification:

```bash
curl -X POST http://localhost:8088/api/notifications ^
  -H "Content-Type: application/json" ^
  -d "{\"recipientUserId\":1,\"title\":\"Test\",\"message\":\"Microservice notification OK\",\"severity\":\"INFO\",\"source\":\"HRMS\"}"
```

Lay notification cua user id 1:

```bash
curl http://localhost:8088/api/notifications/users/1
```

## Buoc tach tiep theo

1. Monolith publish event `ORDER_APPROVED`, `PRODUCT_APPROVED`, `LEAVE_APPROVED` ra Kafka theo schema chuan.
2. Notification Service consume event va ghi notification DB rieng.
3. Frontend HRMS doi API notification tu monolith sang gateway:

```text
http://localhost:8088/api/notifications/users/{userId}
```

4. Sau khi Notification Service on dinh moi tach tiep Sales/Marketplace Service.
