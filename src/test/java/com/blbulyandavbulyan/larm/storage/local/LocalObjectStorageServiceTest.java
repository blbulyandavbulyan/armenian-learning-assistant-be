package com.blbulyandavbulyan.larm.storage.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.blbulyandavbulyan.larm.storage.StoredObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class LocalObjectStorageServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void storeObject_withRelativeFolder_reportsAbsoluteBucketPath() throws IOException {
        // Configure the service with a path relative to the working directory, as application-test.yaml does
        String relativeFolder = Path.of("").toAbsolutePath().relativize(tempDir).toString();
        LocalObjectStorageService service = new LocalObjectStorageService(relativeFolder);
        byte[] bytes = {1, 2, 3};

        StoredObject storedObject = service.storeObject(bytes, "object.bin");

        assertThat(Path.of(storedObject.storageBucket()))
                .isAbsolute()
                .isEqualTo(tempDir.toAbsolutePath().normalize());
        assertThat(Files.readAllBytes(tempDir.resolve("object.bin"))).isEqualTo(bytes);
    }
}
