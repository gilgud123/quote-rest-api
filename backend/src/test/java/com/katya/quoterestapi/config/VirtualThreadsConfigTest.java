package com.katya.quoterestapi.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test to verify Java 21 Virtual Threads are configured in the application.
 *
 * <p>Virtual threads (Project Loom) improve I/O throughput by allowing thousands of concurrent
 * lightweight threads. This test verifies that Spring Boot is configured to use virtual threads
 * for web request handling.
 */
@SpringBootTest
@ActiveProfiles("test")
class VirtualThreadsConfigTest {

  @Autowired private Environment environment;

  @Test
  void shouldHaveVirtualThreadsEnabled() {
    // Given: Application configuration

    // When: We check the virtual threads configuration property
    String virtualThreadsEnabled = environment.getProperty("spring.threads.virtual.enabled");

    // Then: Virtual threads should be enabled
    assertEquals(
        "true",
        virtualThreadsEnabled,
        "Virtual threads should be enabled in application configuration");
  }

  @Test
  void shouldCreateVirtualThreadProgrammatically() {
    // Given: Java 21 runtime with virtual thread support

    // When: We create a virtual thread programmatically
    Thread virtualThread =
        Thread.ofVirtual()
            .name("test-virtual-thread")
            .start(
                () -> {
                  // Simple task
                });

    // Then: It should be a virtual thread
    assertTrue(virtualThread.isVirtual(), "Thread should be virtual");
    assertTrue(
        virtualThread.getName().contains("test-virtual-thread"),
        "Thread should have the specified name");
  }
}
