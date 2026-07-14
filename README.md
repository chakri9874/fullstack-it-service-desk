# Full-Stack IT Service Desk and Ticket Management System

A full-stack IT Service Desk and Ticket Management System built with **Java, Spring Boot, React, MySQL, and REST APIs**. The application supports employee ticket submission, IT support workflows, admin review, role-based access, ticket assignment, escalation handling, and dashboard metrics for monitoring service desk activity.

---

## Features

### Employee Workflow
- Submit support tickets with category, priority, and description.
- Track tickets submitted by the logged-in employee.
- View ticket status updates across the ticket lifecycle.

### IT Support Workflow
- View assigned tickets and support queue.
- Accept or reject assigned tickets.
- Update ticket status during resolution.
- Work within a configurable weekly rejection limit.

### Admin Workflow
- Review escalated tickets.
- Assign escalated tickets to IT support users.
- View role-filtered user directory.
- Monitor ticket activity through dashboard metrics.

### Ticket Lifecycle
- Create and track support tickets.
- Assign tickets to IT support users.
- Automatically assign tickets to the IT support user with the lowest active workload.
- Escalate unaccepted tickets after a configured timeout.
- Use a scheduled background job to move stale pending assignments to an admin review queue.

### Security
- Role-based authentication and authorization.
- JWT-based login flow.
- BCrypt password hashing.
- Separate access for Employee, IT Support, and Admin roles across backend APIs and frontend pages.

### Dashboard
- Summary metrics for service desk monitoring.
- Ticket counts by status, priority, category, and assignment.
- React dashboard views using backend-provided metrics.

---

## Tech Stack

### Backend
- Java
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- Hibernate
- MySQL
- Bean Validation
- JWT
- BCrypt
- Maven

### Frontend
- React
- React Router
- Axios
- Recharts
- Vite
- JavaScript
- CSS

### Tools
- Postman
- Git
- GitHub
- IntelliJ IDEA / VS Code/ Cursor
- MySQL Workbench

---

## Project Structure

```text
fullstack-it-service-desk
├── backend
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │   └── com.itservicedesk.backend
│   │       │       ├── config
│   │       │       ├── controller
│   │       │       ├── dto
│   │       │       ├── entity
│   │       │       ├── enums
│   │       │       ├── exception
│   │       │       ├── repository
│   │       │       ├── security
│   │       │       └── service
│   │       └── resources
│   └── pom.xml
│
├── frontend
│   ├── src
│   │   ├── api
│   │   ├── components
│   │   ├── pages
│   │   ├── App.jsx
│   │   └── index.css
│   └── package.json
│
└── README.md
```

---

## Backend Overview
### Main Backend Responsibilities
- Expose REST APIs for authentication, users, tickets, and dashboards.
- Persist users and tickets in MySQL using Spring Data JPA and Hibernate.
- Validate incoming requests using Bean Validation.
- Return DTO-based API responses.
- Handle validation errors and application exceptions centrally.
- Enforce role-based access using Spring Security and JWT.
- Run scheduled escalation logic for unaccepted ticket assignments.

## Frontend Overview
### Main Frontend Responsibilities
- Provide separate views for Employee, IT Support, and Admin workflows.
- Call backend APIs using Axios.
- Handle page navigation using React Router.
- Display service desk dashboard metrics.
- Support ticket submission, ticket tracking, support actions, admin review, and user directory views.
