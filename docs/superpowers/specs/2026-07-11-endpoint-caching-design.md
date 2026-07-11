# Endpoint Caching Design

## Purpose
Improve performance and reduce redundant database hits and client requests by implementing HTTP caching for specific endpoints and server-side caching for database lookups.

## Requirements
- `GET /dialogues/{id}`: Cache for 1 week on the client/proxy via HTTP `Cache-Control` header. Cache server-side to prevent redundant database queries.
- `GET /assets/{id}`: Cache for 1 week on the client/proxy via HTTP `Cache-Control` header. **Do not** cache server-side to avoid JVM memory exhaustion from large audio binaries.

## Components & Architecture

### Client-Side HTTP Caching
- **`DialoguesApi` / `DialogueController`**:
  - The `getDialogue` method signature will be updated to return `ResponseEntity<GetDialogueResponse>`.
  - The method will chain `.cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())` onto the response builder.
- **`AssetsController`**:
  - The `getAsset` method (which already returns `ResponseEntity<Resource>`) will chain `.cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())` before returning the body.

### Server-Side Caching (Spring Cache)
- We will leverage the existing `@EnableCaching` configuration from `CacheConfig.java`.
- **`DialogueRetrievalService`**:
  - Add `@Cacheable("dialogues")` to the `getDialogue(UUID id)` method to cache database results in memory.
- **`AssetService`**:
  - Deliberately omitted from server-side caching to prevent loading large media files into JVM memory.

## Testing Strategy
- **Integration Tests (`DialogueControllerIT` & `AssetsControllerIT`)**:
  - Verify that the HTTP response contains the header: `Cache-Control: max-age=604800, public, ...` (actually it returns `max-age=604800, public`).
- **Server-Side Cache Verification**:
  - Ensure tests covering `DialogueRetrievalService` or `DialogueController` function correctly without interference from caching side effects. If necessary, clearing the cache between tests should be done if side-effects are observed.
