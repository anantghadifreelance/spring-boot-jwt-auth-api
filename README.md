# Spring Boot JWT Auth API

![CI](https://github.com/anantghadifreelance/spring-boot-jwt-auth-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?logo=springboot&logoColor=white)
![License](https://img.shields.io/badge/license-MIT-blue)

A production-style REST API showing **stateless JWT authentication** with **Spring Boot 3** and **Spring Security 6**: registration, login, role-based access (USER / ADMIN), refresh-token rotation and logout.

## Features

- Register and log in with BCrypt-hashed passwords
- Short-lived **access tokens** (JWT, HS256, 15 min) carrying the user's roles
- Long-lived **refresh tokens** stored in the database, **rotated on every use** and revoked on logout
- Role-based authorization: `/api/admin/**` is ADMIN only
- Stateless security filter chain with a custom `OncePerRequestFilter`
- Validation and consistent RFC 7807 `ProblemDetail` error responses
- Swagger UI / OpenAPI docs, H2 in-memory database for zero-setup runs
- Integration tests with MockMvc and GitHub Actions CI

## Tech stack

Java 17 · Spring Boot 3.3 · Spring Security 6 · Spring Data JPA · H2 · JJWT 0.12 · springdoc-openapi · JUnit 5 · Maven

## Project structure

```
src/main/java/com/anantghadi/jwtauth
├── auth/        AuthController, AuthService, refresh tokens, DTOs
├── security/    JwtService (sign/verify), JwtAuthFilter
├── config/      SecurityConfig, AdminSeeder
├── user/        User entity (UserDetails), Role, repository
├── api/         Protected endpoints (/api/users/me, /api/admin/users)
└── exception/   ApiException, GlobalExceptionHandler
```

## Run it

```bash
mvn spring-boot:run
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- A demo admin is created on startup: `admin@example.com` / `Admin@12345` (override with `ADMIN_EMAIL` / `ADMIN_PASSWORD`)
- Set `JWT_SECRET` (a Base64-encoded 256-bit key) for anything beyond local use

Run the tests:

```bash
mvn verify
```

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create an account, returns token pair |
| POST | `/api/auth/login` | Public | Log in, returns token pair |
| POST | `/api/auth/refresh` | Public | Exchange a refresh token for a new pair (old one revoked) |
| POST | `/api/auth/logout` | Public | Revoke the user's refresh tokens |
| GET | `/api/users/me` | USER / ADMIN | Current user's profile |
| GET | `/api/admin/users` | ADMIN | List all users |

## Try it with curl

```bash
# Register
curl -s -X POST localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Anant","email":"anant@example.com","password":"Password123"}'

# Use the accessToken from the response
curl -s localhost:8080/api/users/me -H "Authorization: Bearer <accessToken>"

# Refresh
curl -s -X POST localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"<refreshToken>"}'
```

## Design notes

- **Why refresh-token rotation?** A stolen refresh token can only be used once; the next use by the real client fails, which limits the damage.
- **Why stateless?** No server session means the API scales horizontally behind a load balancer.
- **Swapping the database:** replace the H2 dependency and datasource settings with MySQL or PostgreSQL; no code changes needed.

## Author

**Anant Ghadi**, Senior Java Full Stack Developer
[LinkedIn](https://www.linkedin.com/in/anantghadi/) · [Topmate](https://topmate.io/anantghadi) · [Dev.to](https://dev.to/anantghadi)

Need a Spring Boot API built or reviewed? I'm available for freelance work.
