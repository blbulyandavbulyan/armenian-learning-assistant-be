package com.blbulyandavbulyan.larm.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.blbulyandavbulyan.larm.BaseIT;
import com.blbulyandavbulyan.larm.dao.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseUserJwtConverterIT extends BaseIT {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DatabaseUserJwtConverter converter;

    private Jwt createJwtToken(String issuer, String subject) {
        return Jwt.withTokenValue("mock-test-token")
                .header("alg", "none")
                .claim("iss", issuer)
                .subject(subject)
                .build();
    }

    @Test
    void shouldSaveUserToDatabase_whenUserIsNotPresent() {
        assertThat(userRepository.count()).isZero();
        Jwt jwtToken = createJwtToken("https://new-issuer.com", "new-subject");
        DatabaseUserJwtAuthenticationToken actualToken = converter.convert(jwtToken);
        assertThat(actualToken).isNotNull();

        UUID returnedId = actualToken.getUserId();
        assertThat(userRepository.count()).isEqualTo(1);
        var userOptional = userRepository.findById(returnedId);
        assertThat(userOptional).hasValueSatisfying(user -> {
            assertThat(user.getId()).isEqualTo(returnedId);
            assertThat(user.getIssuer()).isEqualTo("https://new-issuer.com");
            assertThat(user.getSubject()).isEqualTo("new-subject");
        });
    }

    @Test
    @Sql(scripts = "/sql-test-scripts/insert-test-user-for-jwt.sql")
    void shouldReturnExistingUserId_andNotSaveAnything_whenUserIsPresent() {
        assertThat(userRepository.count()).isEqualTo(1);
        Jwt jwtToken = createJwtToken("https://existing-issuer.com", "existing-subject");
        DatabaseUserJwtAuthenticationToken actualToken = converter.convert(jwtToken);
        assertThat(actualToken).isNotNull();

        UUID returnedId = actualToken.getUserId();
        UUID expectedId = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
        assertThat(returnedId).isEqualTo(expectedId);
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldHandleConcurrentRequestsForNewUser() throws Exception {
        int threadsCount = 5;
        try (ExecutorService executorService = Executors.newFixedThreadPool(threadsCount)) {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(threadsCount);
            Jwt jwtToken = createJwtToken("https://concurrent-issuer.com", "concurrent-subject");

            List<Future<DatabaseUserJwtAuthenticationToken>> futures = new ArrayList<>();

            for (int i = 0; i < threadsCount; i++) {
                futures.add(executorService.submit(() -> {
                    startLatch.await();
                    try {
                        return converter.convert(jwtToken);
                    } finally {
                        endLatch.countDown();
                    }
                }));
            }

            startLatch.countDown(); // Release all threads at once
            endLatch.await(); // Wait for all threads to finish

            Set<UUID> uniqueIds = new HashSet<>();
            for (Future<DatabaseUserJwtAuthenticationToken> future : futures) {
                // This will throw ExecutionException if a DataIntegrityViolationException occurred
                uniqueIds.add(future.get().getUserId());
            }

            // All threads should have acquired the exact same UUID
            assertThat(uniqueIds).hasSize(1);
            // And there should only be exactly one user in the database
            assertThat(userRepository.count()).isEqualTo(1);

            executorService.shutdown();
        }
    }
}
