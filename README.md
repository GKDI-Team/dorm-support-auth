# dorm-support-auth

Auth-service issues JWT access tokens after a successful login. Protected requests
use `Authorization: Bearer <token>`. The signing secret and token lifetime are
configured with `JWT_SECRET` and `JWT_TTL`.

## API documentation

When the service is running, Swagger UI is available at `/swagger-ui.html` and the service contract is available at `/openapi.yaml`.
