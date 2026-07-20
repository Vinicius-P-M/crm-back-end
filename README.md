# CRM Lead Management API

This is the **backend** of a full-stack CRM system. The companion frontend (Next.js) lives in a separate repository: [crm-front-end](https://github.com/Vinicius-P-M/crm-front-end).

## 📖 Overview
This project is a RESTful API developed for managing sales leads, focusing on traceability, efficient status workflows, and secure data persistence. The application was built to demonstrate solid software engineering principles, scalable system architecture, and stateless authentication with role-based access control. It is consumed by a Next.js frontend as the sole client of this API.

## 💼 Business Applicability
The system addresses the need to centralise the lifecycle of a potential customer (lead), while ensuring that only authorised personnel can act on that data. With this backend, companies can:
- Register new leads with automated validations.
- Monitor lead evolution through different lifecycle stages (statuses).
- Track the date of the last interaction, ensuring no lead is left behind in the sales funnel.
- Restrict who can view versus who can create/update leads, based on the employee's role (e.g. an intern can read leads but not create or update them).

## 🏗️ Architectural Decisions
The architecture was designed following the **Layered Architecture** pattern, aiming for low coupling and high cohesion:

- **Separation of Concerns:** Logic was divided so that each component has a single responsibility:
    - **Model:** Represents business entities and persistence rules (JPA).
    - **Repository:** Data abstraction layer using Spring Data JPA to reduce boilerplate code.
    - **Service:** Orchestration layer where business rules and validations reside, ensuring the Controller remains lean.
    - **Controller:** Entry point (API Layer) that exposes HTTP endpoints and manages the interface contract (JSON).
    - **Security:** Cross-cutting layer (filters + configuration) responsible for authentication and authorization, kept fully decoupled from business logic — controllers have no awareness of who is allowed to call them.
- **Dependency Injection (IoC):** Extensive use of the Spring Framework to manage component lifecycles, facilitating testing and maintenance.
- **Stateless Authentication (JWT):** Instead of server-side sessions, the API issues signed JSON Web Tokens on login. Every subsequent request carries its own proof of identity in the `Authorization` header, which removes the need for shared session storage and allows the API to scale horizontally without sticky sessions.
- **Role-Based Authorization:** Access rules are declared centrally in the Spring Security filter chain (route + HTTP method + required role), rather than scattered across controller methods — keeping the full permission map auditable in one place.
- **Externalised Configuration:** Secrets (database password, JWT signing key) are injected via environment variables, never hardcoded, following the twelve-factor app principle.
- **Data Formatting:** Utilisation of `@JsonFormat` to ensure date exchange respects the local standard (`dd/MM/yyyy`) without compromising the `LocalDateTime` object integrity at the server level.

## 🔐 Authentication & Authorization
Authentication is handled via **JWT (JSON Web Token)**:

1. A user registers (`POST /api/auth/register`) with an email, password, and role. The password is hashed with BCrypt before persistence.
2. The user logs in (`POST /api/auth/login`) with email and password. On success, the API returns a signed JWT.
3. The token must be sent on every subsequent request as `Authorization: Bearer <token>`.
4. A dedicated filter validates the token on each request and loads the user's role from the database, which the security layer then checks against the permission rules for that route.

**Available roles:** `COORDENADOR`, `ANALISTA`, `ESTAGIARIO`.

**Permission matrix for `/api/leads`:**

| Route | Method | Allowed roles |
|---|---|---|
| `/api/leads/**` | `GET` | `COORDENADOR`, `ANALISTA`, `ESTAGIARIO` |
| `/api/leads/**` | `POST` | `COORDENADOR`, `ANALISTA` |
| `/api/leads/**` | `PUT` | `COORDENADOR`, `ANALISTA` |

Requests without a valid token receive a `401 Unauthorized`; authenticated requests without the required role receive a `403 Forbidden` — both with a clear JSON body describing the error.

## 🚀 Technologies Used
- **Java 17**
- **Spring Boot 4**
- **Spring Security** (authentication & authorization)
- **JJWT** (JSON Web Token generation and validation)
- **Spring Data JPA (Hibernate)**
- **PostgreSQL**, hosted on **Supabase**
- **Maven** (Dependency Management)
- **Docker** (containerised build and run)
- **H2 Database** (in-memory, used for automated tests only)

## ⚙️ Environment Variables
The application reads secrets from environment variables — never commit real values. Copy `.env.example` to `.env` and fill it in:

```bash
SUPABASE_DB_PASSWORD=your_supabase_db_password
JWT_SECRET=a_long_random_base64_secret
```

`.env` is loaded automatically at startup and is excluded from version control via `.gitignore`.

## 🛠️ How to Run

### Locally with Maven
1. Clone this repository.
2. Ensure you have Java JDK 17+ installed.
3. Create a `.env` file as described above.
4. In your terminal, within the root folder, run:
   ```bash
   ./mvnw spring-boot:run
   ```
5. The API will be available at `http://localhost:8082`.

### With Docker
1. Create a `.env` file as described above.
2. Run:
   ```bash
   docker compose up --build
   ```

## 📮 Main Endpoints

| Method | Route | Description | Auth required |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register a new user | No |
| `POST` | `/api/auth/login` | Log in and receive a JWT | No |
| `GET` | `/api/auth/me` | Get the authenticated user's email and role | Yes (any role) |
| `GET` | `/api/leads` | List all leads | Yes (any role) |
| `GET` | `/api/leads/recentes` | List the 10 most recently created/updated leads | Yes (any role) |
| `GET` | `/api/leads/stats` | Aggregated count and contract value per status | Yes (any role) |
| `GET` | `/api/leads/{id}` | Get a single lead by ID | Yes (any role) |
| `POST` | `/api/leads` | Create a new lead | Yes (`COORDENADOR`, `ANALISTA`) |
| `PUT` | `/api/leads/{id}/status` | Update a lead's status | Yes (`COORDENADOR`, `ANALISTA`) |

## 🖥️ Frontend

This API has no server-rendered views or CORS configuration open to browsers — it is designed to be consumed exclusively through a **Backend-for-Frontend (BFF)** pattern. The [crm-front-end](https://github.com/Vinicius-P-M/crm-front-end) Next.js application talks to this API exclusively server-side (Server Components and Server Actions), storing the JWT in an `httpOnly` cookie that never reaches the browser's JavaScript. See that repository's README for the frontend architecture.
