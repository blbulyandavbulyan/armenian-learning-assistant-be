package com.blbulyandavbulyan.larm.phrase.dao;

import java.time.Instant;
import java.util.UUID;

public interface MediaMother {

    interface DefaultMedia {
        UUID ID = UUID.fromString("49340802-1f6a-46f1-a620-e9c5d48be0bb");
        UUID PHRASE_ID = PhraseMother.DefaultPhrase.ID;
        StorageProvider STORAGE_PROVIDER = StorageProvider.LOCAL;
        String STORAGE_KEY = PHRASE_ID.toString() + ID + ".wav";
        String CONTENT_TYPE = "audio/wav";
        int SIZE_IN_BYTES = 4;
        String AI_MODEL_USED = "hy_AM-medium";
        String VOICE_IDENTIFIER = "gor-medium";
        Instant CREATED_AT = Instant.parse("2026-06-13T10:00:00Z");

        static Builder builder() {
            return MediaMother.builder()
                    .withId(ID)
                    .withStorageProvider(STORAGE_PROVIDER)
                    .withStorageKey(STORAGE_KEY)
                    .withContentType(CONTENT_TYPE)
                    .withSizeInBytes(SIZE_IN_BYTES)
                    .withAiModelUsed(AI_MODEL_USED)
                    .withVoiceIdentifier(VOICE_IDENTIFIER)
                    .withCreatedAt(CREATED_AT);
        }

    }

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private UUID id;
        private Phrase phrase;
        private StorageProvider storageProvider;
        private String storageBucket;
        private String storageKey;
        private String contentType;
        private Integer sizeInBytes;
        private String aiModelUsed;
        private String voiceIdentifier;
        private Instant createdAt;

        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder withPhrase(Phrase phrase) {
            this.phrase = phrase;
            return this;
        }

        public Builder withStorageProvider(StorageProvider storageProvider) {
            this.storageProvider = storageProvider;
            return this;
        }

        public Builder withStorageBucket(String storageBucket) {
            this.storageBucket = storageBucket;
            return this;
        }

        public Builder withStorageKey(String storageKey) {
            this.storageKey = storageKey;
            return this;
        }

        public Builder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder withSizeInBytes(int sizeInBytes) {
            this.sizeInBytes = sizeInBytes;
            return this;
        }

        public Builder withAiModelUsed(String aiModelUsed) {
            this.aiModelUsed = aiModelUsed;
            return this;
        }

        public Builder withVoiceIdentifier(String voiceIdentifier) {
            this.voiceIdentifier = voiceIdentifier;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Media build() {
            return new Media(id, phrase, storageProvider, storageBucket, storageKey,
                    contentType, sizeInBytes, aiModelUsed, voiceIdentifier, createdAt);
        }
    }
}
