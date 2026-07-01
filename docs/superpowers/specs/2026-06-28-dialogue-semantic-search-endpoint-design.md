# Dialogue Semantic Search Endpoint Design

## Overview
Implement a semantic search endpoint for generated dialogues. This endpoint will accept a free-text search query, vectorize it using the AI embedding model, and query the PostgreSQL database using pgvector similarity operators to find the top 50 matching dialogues.

## Architecture & Data Flow

1. **API Layer (`DialogueController` / `DialoguesApi`)**
   - New endpoint: `GET /dialogues/search`
   - Request parameters: `query` (String, required).
   - Validation: The `query` parameter must not be empty or blank.
   - Response: A list of the top 50 matching summaries (`SearchDialoguesResponse`), containing the dialogue `id` and `title` structured as follows:
```json
{
  "dialogues": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "title": {
        "id": "11111111-1111-1111-1111-111111111111",
        "phrase": "Շուկայում",
        "isoLanguageCode": "hy",
        "transcription": "Shukayum",
        "translations": [
          {
            "id": "11111111-1111-1111-1111-11111111111a",
            "isoLanguageCode": "en",
            "translationText": "At the market"
          }
        ],
        "assets": [
          {
            "contentType": "audio/wav",
            "url": "http://localhost/assets/11111111-1111-1111-1111-11111111111b"
          }
        ]
      }
    }
  ]
}
```

2. **Caching Layer (Spring Cache + Caffeine)**
   - To avoid redundant embedding model calls and database queries for identical search terms, we will introduce caching.
   - Using `@Cacheable("semanticSearch")` on the service method.
   - Spring Boot Starter Cache and Caffeine will be added as dependencies.
   - The maximum cache size will be configurable via `application.properties` (e.g., `spring.cache.caffeine.spec=maximumSize=100`) to prevent out-of-memory errors.
   - We will utilize `spring-boot-starter-cache` which will enable seamless migration to Redis later if necessary.

3. **Service Layer (`DialogueSearchService`)**
   - A new service responsible for handling semantic search logic.
   - **Query Normalization:** The input query string will be trimmed (`query.trim()`) to ensure consistent cache hits and clean input.
   - **Vectorization:** Utilize the existing `EmbeddingModel` bean or a dedicated vectorization service to convert the normalized query string into a `float[]` embedding (3072 dimensions).
   - **Delegation:** Call the repository to fetch the top 50 results. Note: External API calls MUST NOT be executed within `@Transactional` methods to avoid database connection pool exhaustion.

4. **Data Access Layer (`DialogueRepository`)**
   - A new query method to perform the nearest neighbor search.
   - We will use `@EntityGraph` to eagerly fetch the `title`, `title.translations`, and `title.mediaSet` collections in a single efficient query.
   - Example Query Strategy: `ORDER BY l2_distance(d.embedding, :queryEmbedding)` with a Spring Data `Limit.of(50)` applied to the method.

## Configuration & Dependencies
- Add `spring-boot-starter-cache` to `pom.xml`.
- Add `com.github.ben-manes.caffeine:caffeine` to `pom.xml`.
- Add `@EnableCaching` to a configuration class (or the main application class).
- Add cache property to `application.yaml` (e.g., `spring.cache.type=caffeine` and `spring.cache.caffeine.spec=maximumSize=100,expireAfterAccess=1h`).

## Error Handling
- Validate that the query is provided. Returns `400 Bad Request` on empty queries via standard `@Valid` or `@NotBlank` handling in `RestControllerAdvice`.
- If the embedding model fails, standard Spring AI exceptions will propagate to a `500 Internal Server Error`.

## Testing
- Integration testing (`DialogueControllerIT`) using Testcontainers and mocked `EmbeddingModel` to ensure:
  - Vector similarity query correctly returns expected matches.
  - The endpoint correctly returns the expected top results without pagination parameters.
  - The query is trimmed before processing.
  - Caching is applied (by verifying the mock embedding model is only called once for repeated queries).
  - SQL queries use the correct `LIMIT 50` and eager fetching via the entity graph.
