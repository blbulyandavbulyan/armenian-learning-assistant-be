---
name: main-code-reviewer
description: Analyzes and reviews Java Spring Boot code for architectural separation, performance, security, and idiomatic framework usage. Use when a user asks to review production code of the application (not the test code)
---

# Spring Boot Code Reviewer

## When to use this skill
- The user requests a code review for a Spring Boot application or specific Java classes.
- The user asks to audit database interactions, transactional boundaries, or REST API endpoints.
- The user pastes code containing common Spring annotations (`@RestController`, `@Service`, `@Repository`, `@Transactional`, etc.).
- The user asks for best practices regarding Spring Boot architecture or dependency injection.

## Workflow
1. [ ] **Scope the Review:** Identify the layer of the application being reviewed (Web, Service, Data, Configuration, or Security).
2. [ ] **Static Analysis:** Scan for dependency injection patterns, proper annotation usage, and separation of concerns.
3. [ ] **Performance & Data Audit:** Check for N+1 query issues, proper pagination, and read-only transaction optimizations.
4. [ ] **Security & Error Handling Audit:** Verify global exception handling and input validation.
5. [ ] **Formulate Feedback:** Structure the response using a "Plan-Validate-Execute" pattern, providing the specific violations and the corrected code using medium-freedom code block templates.

## Instructions

- **Dependency Injection & State:**
    - ALWAYS flag field injection (`@Autowired` on fields). Enforce constructor injection using `final` fields (or Lombok's `@RequiredArgsConstructor`).
    - Flag any mutable state inside singleton beans (Controllers, Services).

- **Architectural Boundaries (Separation of Concerns):**
    - **Controllers:** Must only handle HTTP routing, input validation, and delegating to Services. Reject any business logic or direct database calls in Controllers.
    - **Services:** Must contain core business logic. Ensure `@Transactional` is applied at the class or method level here, not in Controllers.
    - **Data Transfer Objects (DTOs):** Reject returning JPA `@Entity` classes directly from REST endpoints. Enforce the use of Java `record` classes for DTOs to prevent lazy-loading exceptions and data leakage.

- **Data Access & Transactions (Spring Data JPA):**
    - Enforce `@Transactional(readOnly = true)` on Service methods that only fetch data.
    - Identify and warn against the N+1 select problem. Recommend `JOIN FETCH` in `@Query` or EntityGraphs for fetching associations.
    - Flag the use of `save()` in a loop. Recommend `saveAll()` for batch operations.

- **Error Handling & Validation:**
    - Reject inline `try-catch` blocks in controllers for standard domain exceptions.
    - Enforce the use of `@RestControllerAdvice` and `@ExceptionHandler` for global exception routing.
    - Verify that `@Valid` or `@Validated` is used alongside standard Jakarta Bean Validation annotations (e.g., `@NotNull`, `@Size`).

- **API Design & Best Practices:**
    - Ensure API responses return standard structures or `ResponseEntity` for dynamic status codes.
    - Verify proper use of HTTP methods (GET for safe reads, POST for creation, PUT/PATCH for updates).
