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
