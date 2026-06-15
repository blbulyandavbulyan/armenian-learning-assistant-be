# Armenian Learning Assistant Backend

## Project Overview
This project is the backend service for the Armenian Learning Assistant. It leverages AI to generate useful Armenian phrases, provides text-to-speech (TTS) capabilities for Armenian, and manages a database of phrases and their translations.

### Core Technologies
- **Java 25** & **Spring Boot 4.0.6**
- **Spring AI**: Integration with Google Gemini for phrase generation and embeddings.
- **Spring Modulith**: Modular architecture for better maintainability.
- **PostgreSQL**: Primary database with **pgvector** for semantic search capabilities.
- **Flyway**: Database migration management.
- **Piper TTS**: A fast, local neural text-to-speech engine (Python-based).
- **Lombok**: Reducing boilerplate code.

### Key Components
- **`PhraseOrchestrator`**: The central service that coordinates the flow of saving phrases, generating their audio via TTS, and storing the resulting media.
- **`PiperTextToSpeechService`**: Manages a persistent Piper process to provide high-performance Armenian speech synthesis.
- **`PhrasesChatService`**: Interacts with Gemini to generate contextual Armenian phrases based on user-provided topics.
- **`LocalObjectStorageService`**: Handles the storage of generated audio files on the local filesystem.

---

## Building and Running

### Prerequisites
- **Java 25**
- **Maven**
- **PostgreSQL** with `pgvector` extension.
- **Python 3** (for Piper TTS).
- **Gemini API Key**.

### Local Setup
1.  **Configure TTS (Piper)**:
    Follow the instructions in `README.md` to set up the Python virtual environment (`myenv`) and download the Armenian neural weights (`.onnx` and `.json` files).
2.  **Database**:
    Ensure PostgreSQL is running and the `vector` extension is available. Use the provided `docker-compose.yml` or manual setup.
3.  **Environment Variables**:
    - `GEMINI_API_KEY`: Your Google AI API key.
4.  **Run with 'local' profile**:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=local
    ```

### Key Commands
- **Build**: `mvn clean install`
- **Test**: `mvn test`
- **Checkstyle**: `mvn checkstyle:check` (Enforced during the `compile` phase).

---

## Development Conventions

### Architecture
- The project follows **Spring Modulith** principles. Aim for high cohesion within packages and low coupling between modules.
- Business logic should ideally reside in the `core` or specific domain packages (`phrase`, `ai`).
- API contracts are defined in the `api` package.

### Coding Style
- **Checkstyle**: Enforced via `checkstyle/checkstyle.xml`. Ensure your code complies before submitting.
- **Lombok**: Widely used. Prefer `@RequiredArgsConstructor` for dependency injection.
- **Immutability**: Use Java `record` types for DTOs and value objects where appropriate.

### Database
- All schema changes MUST be done via Flyway migrations in `src/main/resources/db/migration`.
- The `phrases` table uses `pgvector` (`vector(1536)`) for embeddings.

### Text-to-Speech (TTS)
- Currently, only Armenian (`hy`) is supported.
- The `PiperTextToSpeechService` communicates with the Piper binary via a subprocess. Ensure the paths in `application-local.yaml` match your local environment.

---

## Project Structure
- `src/main/java/.../larm/ai`: AI-related services (Gemini, TTS).
- `src/main/java/.../larm/api`: REST Controllers and Request/Response DTOs.
- `src/main/java/.../larm/core`: Orchestration logic.
- `src/main/java/.../larm/phrase`: Domain logic for phrases, translations, and media.
- `src/main/java/.../larm/storage`: Abstractions and implementations for object storage.
- `src/main/resources/prompts`: Markdown files containing system prompts for AI models.

---

## Repository Agent Guidelines

This document outlines the architectural boundaries and engineering standards for this repository. All AI agents must adhere to these baselines.

### 1. General Principles
- **Idiomatic Code:** Follow standard language conventions (e.g., standard Java/Spring, TypeScript, etc., depending on your project). Do not introduce over-engineered design patterns unless explicitly requested.
- **Minimalism:** Do not add speculative features, "future-proofing" code, or extra utility methods that aren't requested by the current task.
- **Error Handling:** Use the existing project exception handling patterns. Do not invent new custom exceptions unless required.

### 2. Testing Strategy & Boundaries
- **Unit Tests:** Mock all external dependencies and adjacent service layers to isolate the class under test.
- **Integration Tests (IT):**
  - When testing controllers or entry points (e.g., `SomeControllerIT`), **do not mock intermediate internal layers, services, or mappers**. The entire internal stack (controller → service → mapper) must run with real components.
  - Use real components, database test containers, or active application contexts to ensure true integration behavior.
  - Mock at the **lowest possible external infrastructure boundary** — i.e., the actual outbound call. For Spring AI / Gemini, this means mocking the `ChatClient` bean (which represents the HTTP call to the AI provider), not the service layer on top of it. This ensures you can safely refactor any internal implementation without touching tests, as long as the observable API contract doesn't change.
  - For database-backed flows, use Testcontainers. For AI-backed flows, mock `ChatClient`. For message brokers, mock the broker client.
- **Coverage:** Every new feature must include corresponding tests before completion. If a behaviour is fully exercised end-to-end by an IT (controller → service → mapper → response JSON), a separate unit test for each internal layer is **not required** unless the layer has complex logic that is not reachable via the happy path IT.

### 3. Scope of Work (The "Do Not Touch" Rule)
- Do not refactor unrelated files or change configuration properties unless it is a direct dependency of the task.
- Leave existing, untouched code exactly as it is to prevent regression.
