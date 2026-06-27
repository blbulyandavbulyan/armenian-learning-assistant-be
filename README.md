# armenian-learning-assistant-be
This is the backend for Armenian language learning assistant.

Tech stack:
* Spring Boot 4
* Java 25
* Spring AI
* python3 + piper (for text to speech)
* Gemini model for phrases generation

## MCP servers
There is github mcp server, if you want to use it, you have to create .env file in .agents folder with your personal access token, like this:
```
GITHUB_PERSONAL_ACCESS_TOKEN=YOUR_TOKEN
```

## Running locally
Make sure you have maven and Docker installed.
When you run the application locally you have to provide:
* `local` as your Spring Boot Profile
* `GEMINI_API_KEY` as environment variable, with your gemini api key
* `STORAGE_FOLDER_NAME` as environment variable, pointing to an existing directory where local files will be stored (e.g., `C:\arm-learn-files` on Windows), because the default is a Linux path.

### 🐳 Running external services (PostgreSQL & Piper TTS)
The application relies on PostgreSQL and Piper TTS. You can easily run both of them locally via Docker Compose:
```bash
docker compose up -d
```
This will start the PostgreSQL database and build the Piper TTS container with the Armenian Neural Weights automatically.

## Testing
When running tests locally, always use `mvn verify` instead of `mvn test`.
The `mvn test` command only runs unit tests, so it completely skips all integration tests (any class ending with `*IT`). To ensure both unit and integration tests run, use:
```bash
mvn verify
```
