# dorm-support-auth

## О сервисе

Сервис авторизации и управления пользователями.

**Технологии:** Java, Spring Boot, PostgreSQL, JWT.

**Основные ручки через Gateway:**

- `POST /api/v1/auth/login` — вход.
- `GET /api/v1/auth/me` — текущий пользователь.
- `POST /api/v1/auth/logout` — выход.
- `POST /api/v1/admin/users` — создание пользователя.

## Запуск

Из папки `dorm-support-infrastructure`:

```bash
docker compose up --build auth-service
```

Compose также запустит PostgreSQL и Kafka. Для доступа через Gateway поднимите весь проект командой `make up` из папки инфраструктуры.

После успешного входа сервис выдаёт токен доступа JWT. Для защищённых запросов
используется заголовок `Authorization: Bearer <token>`. Секрет подписи и срок
действия токена задаются переменными `JWT_SECRET` и `JWT_TTL`.

## Документация API

При запущенном сервисе Swagger UI доступен по адресу `/swagger-ui.html`, а описание API — по адресу `/openapi.yaml`.
