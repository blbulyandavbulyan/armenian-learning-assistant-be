---
name: backend-reviewer
description: Analyzes and reviews Java Spring Boot production code and test suites. Enforces architectural separation, 12-Factor standards, resilience, strict integration testing boundaries, and usage of Testcontainers.
---

# Spring Boot Code & Test Reviewer

## When to use this skill
Every time user requests code review on this project.

## Workflow
1. [ ] **Scope the Review:** Identify the layer of the application being reviewed (Web, Service, Data, Configuration, Security, or Tests).
2. [ ] **Production Static Analysis:** Scan for dependency injection patterns, proper annotation usage, separation of concerns, and API design.
3. [ ] **Performance, Security, & Resilience:** Check for N+1 queries, global exception handling, Actuator security, thread pool configurations, and logging contexts.
4. [ ] **Testing Boundaries Audit:** Verify environment parity (Testcontainers) and ensure controllers are tested via end-to-end integration tests without intermediate mocking.
5. [ ] **Formulate Feedback:** Structure the response using a "Plan-Validate-Execute" pattern, providing the specific violations and the corrected code.

## Instructions

### 1. Architectural Boundaries & State

**Dependency Injection & Instantiation**
* **Constructor Injection:** ALWAYS flag field injection (`@Autowired` on fields). Enforce constructor injection using `final` fields (or Lombok's `@RequiredArgsConstructor`).
* **Framework Agnosticism:** Core business logic should not depend on Spring context objects (`ApplicationContext`, `BeanFactory`). Pass required dependencies via constructors.

**State Management & Thread Safety**
* **Stateless Beans:** Flag any mutable state (e.g., non-final class fields, collections that get modified) inside singleton beans (`@Controller`, `@Service`, `@Repository`, `@Component`). Singletons must be strictly stateless.
* **Contextual State:** If state must be tracked per request, enforce the use of Spring's `@RequestScope`, `ThreadLocal` (with guaranteed cleanup via Interceptors/Filters), or standard security contexts (`SecurityContextHolder`).
* **Stateless APIs:** REST controllers must not rely on `HttpSession` for storing conversational state. Enforce stateless session management (e.g., JWT).

**Application Layering & Isolation**
* **Controllers (Web Layer):** Must only handle HTTP routing, deserialization, input validation, and delegating to Services. Reject any business logic, transactional control, or direct database calls in Controllers.
* **Services (Business Layer):** Must contain core business logic and orchestrate domain models. Ensure `@Transactional` is applied here (not in controllers). Reject HTTP-aware objects (e.g., `HttpServletRequest`, `ResponseEntity`) in the service layer.
* **Repositories (Data Layer):** Must only handle persistence and query execution. Reject business logic or data transformation in repositories.

**Domain & Data Boundaries (DTOs vs Entities)**
* **No Entity Leakage:** Reject returning JPA `@Entity` classes directly from REST endpoints or passing them to external clients.
* **Immutability for DTOs:** Enforce the use of Java `record` classes for DTOs to prevent lazy-loading exceptions, data leakage, and unintended modifications.
* **Explicit Mapping:** Disallow manual, inline object mapping scattered in business methods. Enforce explicit mappers (e.g., MapStruct) or dedicated transformation methods at the boundary layers.

**Modularity & Package Visibility**
* **Package-Private by Default:** Enforce package-private (default) visibility for implementation classes (`ServiceImpl`), internal repositories, and utility helpers. Expose only interfaces, records, and primary facade services publicly to enforce module boundaries.
* **Interface Segregation:** Flag the premature abstraction of creating 1:1 `FooService` interface and `FooServiceImpl` class pairs unless multiple implementations exist or dynamic proxying specifically requires it. Keep boundaries simple but strict.

**Event-Driven Decoupling**
* **Cross-Domain Decoupling:** Flag synchronous calls between unrelated domain services (e.g., `OrderService` calling `EmailService` directly).
* **Application Events:** Enforce the use of Spring Application Events (`ApplicationEventPublisher`, `@EventListener`, `@TransactionalEventListener`) to decouple secondary side-effects from core business workflows.

**External Integrations (Ports & Adapters)**
* **Infrastructure Isolation:** Third-party APIs, external SDKs (e.g., Stripe, Twilio, OpenAI/Gemini), and message brokers MUST be wrapped behind custom domain interfaces (Adapters).
* **No SDK Bleed:** Core services must never import or consume third-party infrastructure classes directly.

### 2. Data Access & Transactions
- **Read-Only:** Enforce `@Transactional(readOnly = true)` on Service methods that only fetch data.
- **N+1 Problem:** Identify and warn against the N+1 select problem. Recommend `JOIN FETCH` in `@Query` or `@EntityGraph` for fetching associations.
- **Batching:** Flag the use of `save()` in a loop. Recommend `saveAll()` for batch operations.
- **Lombok Pitfalls:** Strictly reject the use of Lombok's `@Data`, `@EqualsAndHashCode`, or `@ToString` on JPA `@Entity` classes, as they trigger lazy-loading exceptions and severe memory leaks. Enforce `@Getter` and `@Setter` instead.

### 3. API Design & Error Handling
- **REST Best Practices:** Ensure API responses return standard structures or `ResponseEntity` for dynamic status codes. Verify proper use of HTTP methods (GET for safe reads, POST for creation, PUT/PATCH for updates).
- **Validation:** Verify that `@Valid` or `@Validated` is used alongside standard Jakarta Bean Validation annotations (e.g., `@NotNull`, `@Size`).
- **Global Exceptions:** Reject inline `try-catch` blocks in controllers for standard domain exceptions. Enforce the use of `@RestControllerAdvice` and `@ExceptionHandler` for global exception routing.

### 4. Configuration, Resilience, & Security
- **Configuration:** Reject scattered `@Value("${...}")` annotations. Enforce the use of `@ConfigurationProperties` for type-safe, validated, and grouped configuration structures. Ensure secrets are externalized.
- **Resilience:** If `@Async` is used, flag the reliance on Spring's default simple task executor. Enforce the configuration of a custom `ThreadPoolTaskExecutor` to prevent out-of-memory errors. Recommend circuit breakers (e.g., Resilience4j) for external API calls.
- **Security:** Enforce method-level security (`@PreAuthorize`, `@PostFilter`) in the Service layer. Check for proper CORS configuration and stateless session management for REST APIs. Ensure Actuator endpoints (like `/env` or `/heapdump`) are securely hidden or restricted.
- **Observability:** Reject `System.out.println` or `e.printStackTrace()`. Enforce structured logging via SLF4J. Suggest adding Correlation IDs (e.g., via Micrometer Tracing or MDC) to logs for cross-service request tracking.

### 5. Integration Testing Boundaries (The Default)
- **Controllers = Integration Tests:** Controllers must ALWAYS be tested as true integration tests. Strictly reject `@WebMvcTest` for standard controller testing, as it encourages brittle service mocking. Enforce `@SpringBootTest` so the request flows through the real internal stack (Controller → Service → Mapper).
- **No Intermediate Mocking:** When testing entry points, strictly reject mocking of intermediate internal layers, services, or mappers. The entire internal stack must run with real components.
- **Lowest External Boundary:** Enforce mocking at the *lowest possible external infrastructure boundary* (the actual outbound call).
    - For Spring AI / Gemini flows: Mock the `ChatClient` bean (the HTTP call to the AI provider), not the service layer wrapping it.
    - For third-party REST APIs: Recommend WireMock or mocking the HTTP client interface.
    - For message brokers: Mock the broker client interface.
- **Database Flows:** Enforce the use of Testcontainers for database-backed flows to ensure true integration behavior. Reject in-memory databases like H2 for testing production code, as they lack environment parity.

### 6. Unit Testing, Coverage & Quality
- **Unit Test Boundaries:** Enforce strict isolation *only* for complex domain logic or utilities. Mock adjacent service layers to isolate the specific class under test. Reject loading the Spring Context (`@SpringBootTest`) for pure unit tests; they should run fast using standard JUnit and Mockito.
- **Test Slices:** Reserve `@DataJpaTest` strictly for repository-level queries. Reserve `@WebMvcTest` ONLY for explicitly testing HTTP serialization/deserialization isolated from the rest of the app.
- **Coverage Strategy:** Do not demand redundant unit tests. If a behavior is fully exercised end-to-end by an Integration Test, a separate unit test for each internal layer is *not required*. Test against the observable API contract to allow safe refactoring.
- **Readability:** Enforce a clear structure inside test methods using Arrange-Act-Assert or Given-When-Then blocks separated by blank lines. Recommend fluent assertion libraries like AssertJ (`assertThat(...)`) over basic JUnit assertions.

## Resources
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/reference/)
- [12-Factor App Methodology](https://12factor.net/)
- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/reference/testing/index.html)
- [Testcontainers Documentation](https://testcontainers.com/)
- scripts/run-checkstyle.sh
- examples/optimal-controller-service-pattern.md