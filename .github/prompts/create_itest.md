# 🧪 Spring Boot Integration Test Prompt (for Copilot CLI)

You are generating a **Spring Boot integration test** for a production class or endpoint using **JUnit 5**.  
The test should exercise real Spring wiring and infrastructure while remaining **reliable, deterministic, and fast**.

---

## 🎯 High-Level Goals

- Validate **real Spring wiring** (configuration, serialization, validation, security, transactions) and **persistence/messaging behavior** end-to-end.
- Prefer **slice tests** for narrower scope when possible; use **full context** only when needed.
- **Never call real third-party systems** — use **WireMock**, **MockWebServer**, or **Testcontainers** instead.
- Keep tests **deterministic** and **parallel-friendly**; avoid `Thread.sleep()`.
- Use clear **Arrange-Act-Assert (or Given-When-Then)** structure with descriptive test names.

---

## 🧩 What Counts as “Integration”

- Real Spring context and bean wiring.
- Real database (Testcontainers or in-memory for small cases).
- Real HTTP layer via `MockMvc`, `WebTestClient`, or `TestRestTemplate`.
- Real configuration, validation, transactions, and AOP.
- Avoid mocks for internal beans — mock **only external systems** (HTTP, messaging, etc.).

---

## ⚙️ Choosing the Right Style

|             Scenario              |                             Recommended Setup                             |
|-----------------------------------|---------------------------------------------------------------------------|
| MVC Controller (mocked web layer) | `@SpringBootTest` + `@AutoConfigureMockMvc` + `MockMvc`                   |
| Full HTTP stack (real port)       | `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate`      |
| Reactive Controller               | `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `WebTestClient`         |
| Data Layer / JPA                  | `@DataJpaTest` (+ Testcontainers for real DB)                             |
| Messaging (Kafka/Rabbit)          | `@SpringBootTest` + embedded/container broker + producer/consumer asserts |

---

## 🌐 External Dependencies

- **Never call real APIs or databases**.
- For HTTP clients: an external WireMock service is running.
- For databases: An external database is running.
- For brokers: An external IBM MQ instance is running.

---

## 🧱 Testcontainers Best Practices

- Annotate class with `@Testcontainers` and define containers as `static final`.
- Use Boot 3.1+ `@ServiceConnection` for automatic container property wiring.
- Reuse containers for performance (e.g., static singleton pattern).
- Avoid hardcoded ports and use dynamic property injection.

---

## 🧰 WireMock / MockWebServer

- Stub external HTTP calls; never reach real URLs.
- Inject base URL into the application using `@DynamicPropertySource`.
- Test success, error (4xx/5xx), and timeout scenarios.
- Verify request paths, headers, and bodies.

---

## 🔒 Security & Validation

- Cover **authorized**, **unauthorized**, and **forbidden** scenarios.
- Use `@WithMockUser` or test JWT filters directly.
- Verify validation errors (status, response body, error codes).
- Assert that exceptions map correctly via `@ControllerAdvice`.

---

## 🧮 Data & Transactions

- Run **Flyway/Liquibase** migrations automatically in integration tests.
- Prefer **Testcontainers** over in-memory DBs if dialect differences exist.
- Roll back between tests with `@Transactional` when applicable.
- Use repository setup or `@Sql` scripts for deterministic seed data.

---

## ⚛️ Reactive Tests

- Use `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `WebTestClient`.
- Assert async behavior using **Reactor StepVerifier**.
- Use **virtual time** for time-dependent operators.

---

## 🧪 Determinism & Stability

- Stub time, UUIDs, and randomness with injected `Clock` or suppliers.
- Use **Awaitility** for async polling instead of sleeps.
- Disable flaky tests; prefer `@DirtiesContext` only when unavoidable.
- Keep data setup and teardown predictable.

---

## 🧠 JUnit 5 & Assertions

- One `@Test` per behavior with clear, descriptive names (`shouldReturnFoo_whenBar`).
- Use `@BeforeEach` for setup, `@Nested` for grouping.
- Use **AssertJ** for fluent assertions and deep comparisons.
- Use `assertThrows()` for exception validation.
- Use `@ParameterizedTest` for input matrices.

---

## 📊 Testing Code Coverage

- Aim for high coverage of business logic, edge cases, and error handling.
- Avoid testing trivial getters/setters unless they contain logic.
- Cover at least **80% of methods**.
- Cover at least **80% of lines of code**.
- Cover at least **80% of branches/conditions**.
- Test all public method behaviors including error paths.
- Cover null checks and defensive programming branches.
- Test loop scenarios (0 iterations, 1 iteration, multiple iterations).

---

## 🔍 Testing Private Methods Through Public Interfaces

**Best Practice**: Never test private methods directly. Always test them through their public method callers.

### Systematic Approach for Private Method Coverage

1. **Identify all branches** in the private method (if/else, null checks, loops)
2. **Map null checks** at every level:
   - Input parameters
   - Service/repository results
   - Loop item properties (nested null checks)
3. **Identify loop scenarios**:
   - 0 iterations (empty collection)
   - 1 iteration (single item, early return)
   - 2+ iterations (multiple items, test early exit vs complete)
   - Skip iterations (null checks in loop)
4. **Create tests through public callers** with realistic scenarios
5. **Verify business logic** (flags, markers, inheritance rules)

### Example: Testing a Private Finder Method

```java
// Private method in service:
private Long findParentForChild(Long childId) {
    if (childId == null) return null;  // BRANCH 1
    
    List<Parent> parents = repository.findByChildId(childId);
    if (parents == null || parents.isEmpty()) return null;  // BRANCH 2a, 2b
    
    for (Parent parent : parents) {  // LOOP
        if (parent.getId() != null) {  // BRANCH 3
            return parent.getId();  // Early return
        }
    }
    return null;  // Loop complete without match
}

// Integration tests through public method:
@Test
void publicMethod_childWithNullId_handlesGracefully() {
    // Tests BRANCH 1: null input
    result = service.publicMethod(null);
    assertNotNull(result);
    assertTrue(result.isEmpty());
}

@Test
void publicMethod_childWithNoParents_returnsEmpty() {
    // Tests BRANCH 2b: empty collection
    Child child = createChild();
    // Don't create any parents
    result = service.publicMethod(child.getId());
    assertNotNull(result);
}

@Test
void publicMethod_singleParent_returnsFirst() {
    // Tests LOOP: 1 iteration, early return
    Child child = createChild();
    Parent parent = createParent(child);
    result = service.publicMethod(child.getId());
    assertNotNull(result);
    assertFalse(result.isEmpty());
}

@Test
void publicMethod_multipleParents_returnsFirst() {
    // Tests LOOP: 2+ iterations, early return on first valid
    Child child = createChild();
    createParent(child);  // First parent (returned)
    createParent(child);  // Second parent (not processed)
    result = service.publicMethod(child.getId());
    // Verify only first parent used (early return behavior)
}

@Test
void publicMethod_parentWithNullId_skipsIteration() {
    // Tests BRANCH 3: null check in loop
    // In integration tests, hard to create entity with null ID
    // But test verifies defensive check exists
    Child child = createChild();
    createParent(child);
    result = service.publicMethod(child.getId());
    assertNotNull(result);  // No NPE = null check works
}
```

### Key Patterns for Branch Coverage

**Pattern 1: Null Input Validation**

```java
@Test
void method_nullId_handlesGracefully() {
    List<Result> result = service.publicMethod(null, "context", "TYPE");
    assertNotNull(result);
    assertTrue(result.isEmpty());
}
```

**Pattern 2: Empty Collection Handling**

```java
@Test
void method_noRelatedEntities_returnsEmpty() {
    Entity entity = createEntity();
    // Don't create any related entities
    List<Result> result = service.publicMethod(entity.getId());
    assertNotNull(result);
    assertTrue(result.isEmpty());
}
```

**Pattern 3: Loop Iteration Testing**

```java
@Test
void method_singleItem_earlyReturn() {
    // Tests loop with 1 iteration
    Entity entity = createEntityWithOneRelation();
    List<Result> result = service.publicMethod(entity.getId());
    assertFalse(result.isEmpty());
}

@Test
void method_multipleItems_earlyReturnOnFirst() {
    // Tests loop early exit (doesn't process all items)
    Entity entity = createEntityWithMultipleRelations();
    List<Result> result = service.publicMethod(entity.getId());
    // Verify behavior consistent with early return
}

@Test
void method_loopCompletesWithoutMatch_returnsEmpty() {
    // Tests loop that completes all iterations without finding match
    Entity entity = createEntityWithInvalidRelations();
    List<Result> result = service.publicMethod(entity.getId());
    assertTrue(result.isEmpty());
}
```

**Pattern 4: Nested Null Checks in Loops**

```java
@Test
void method_nestedNullProperty_skipsIteration() {
    // Tests: if (item.getProperty() != null && item.getProperty().getId() != null)
    Entity entity = createEntity();
    createRelationWithProperty(entity);  // Valid property
    List<Result> result = service.publicMethod(entity.getId());
    assertNotNull(result);  // Defensive null check prevents NPE
}
```

**Pattern 5: Resilient Assertions for Known Limitations**

```java
@Test
void method_bidirectionalRelationship_mayBeEmpty() {
    // When testing methods that rely on bidirectional JPA relationships
    Entity entity = createEntity();
    createRelatedEntity(entity);
    
    List<Result> result = service.publicMethod(entity.getId());
    assertNotNull(result);
    
    // NOTE: May be empty due to lazy loading or transaction boundaries
    // Test verifies method handles this gracefully
    if (!result.isEmpty()) {
        // Verify structure when data IS present
        assertTrue(result.get(0).getSomeProperty() != null);
    }
}
```

---

## ⚠️ Common Pitfalls to Avoid

### 1. Bidirectional JPA Relationship Issues

**Problem**: `parentDTO.getChildren()` returns empty even when children exist.

**Cause**: Lazy loading, transaction boundaries, or relationships not maintained by JPA.

**Solution**: Use repository queries instead of navigation:

```java
// ❌ Avoid:
List<ChildDTO> children = parentDTO.getChildren();

// ✅ Prefer:
ChildDTO searchCriteria = new ChildDTO();
searchCriteria.setParent(parentDTO);
List<ChildDTO> children = childService.readAll(searchCriteria);
```

### 2. Testing Early Return Behavior

**Remember**: If a loop has early return, it doesn't process all items.

```java
for (Item item : items) {
    if (item.isValid()) {
        return item.getId();  // Returns immediately on first valid
    }
}
```

**Test both**:
- First item valid (immediate return)
- First item invalid, second valid (skip first, return second)

### 3. Defensive Programming Verification

**Goal**: Verify null checks prevent NPE, not that they're never triggered.

```java
// The test proves the null check works:
if (entity.getProperty() != null) {
    // ...
}

// Test verifies:
// 1. No NPE when property is null
// 2. Method completes successfully
// 3. Appropriate default behavior (return empty, skip, etc.)
```

---

## ✅ Output Requirements

- Generate **only one Java file**: the integration test for the given class or endpoint.
- Must compile under **Java 17+** and **Spring Boot 3.x**.
- Mirror the package of the class under test.
- Name the file `{ClassName}IT.java`.
- No wildcard imports.
- Include proper annotations:
  - `@SpringBootTest` (with appropriate web environment)
  - `@AutoConfigureMockMvc` or `@Testcontainers` if needed
- Assert both **happy paths** and **error conditions**.

---

## 🧾 Inputs You Receive

You will be given:
1. The **production class or endpoint** under test.
2. Optional hints about its role (controller, service, repository, messaging, etc.).
- Use these to choose between **slice tests** or **full context tests**.

---

## 🧱 Deliverable

Return the **complete Java source** for the integration test.  
Do **not** include explanations, comments, or markdown fences — just the final Java class.

---

## 🧩 Example Patterns

**Full HTTP Integration Test (MockMvc)**

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUser_whenValidId() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 1))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(1));
    }
}
```

**Data JPA Integration Test

```java
@DataJpaTest
@Testcontainers
class UserRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindUser() {
        User user = new User("Alice");
        userRepository.save(user);
        assertThat(userRepository.findByName("Alice")).isPresent();
    }
}
```

---

## 📚 Lessons Learned from Real-World Integration Test Coverage

### 1. Code Duplication Reduction

**Problem**: Repetitive test setup code (15-20 lines) for creating entities bloats test files.

**Solution**: Create reusable helper methods in test class.

**Pattern**:

```java
// Add to test class:
private ConceptDTO createTestConcept(String nameSuffix) {
    ConceptDTO conceptDTO = createConceptDTO("AgreementSpecConceptDTO", "kind-" + SEQ.getAndIncrement());
    conceptDTO.setName(nameSuffix + "-" + SEQ.getAndIncrement());
    return conceptService.create(conceptDTO);
}

private VersionedPartDTO createTestVersionedPart(String nameSuffix) {
    VersionedPartDTO versionedPartDTO = createVersionedPartDTO("AgreementSpecPartDTO");
    versionedPartDTO.setName(nameSuffix + "-" + SEQ.getAndIncrement());
    return versionedPartService.create(versionedPartDTO);
}
```

**Usage**:

```java
// Before (18 lines):
ConceptDTO conceptDTO = createConceptDTO("AgreementSpecConceptDTO", "kind-" + SEQ.getAndIncrement());
conceptDTO.setName("TestConcept-" + SEQ.getAndIncrement());
ConceptDTO createdConcept = conceptService.create(conceptDTO);

// After (3 lines):
ConceptDTO createdConcept = createTestConcept("TestConcept");
```

**Impact**: Reduces file size by ~30-40%, improves readability, easier maintenance.

---

### 2. Testing Compound Conditions in Integration Tests

**Problem**: Cannot distinguish between `null` and `isEmpty()` in integration tests.

**Wrong Approach** (creates duplicates):

```java
@Test
void method_nullUsages_returnsEmpty() { }

@Test
void method_emptyUsages_returnsEmpty() { }
// These test THE SAME code path in integration context!
```

**Correct Approach**:

```java
@Test
@DisplayName("Method: Handles no usages (null or empty) gracefully")
void method_handlesNoUsages_returnsEmpty() {
    // Tests: if (usages == null || usages.isEmpty())
    // In integration tests, service layer may return either
    Entity entity = createEntityWithoutUsages();
    List<Result> result = service.method(entity.getId());
    assertNotNull(result);
    assertTrue(result.isEmpty());
}
```

**Key Insight**: Integration tests have different constraints than unit tests. Merge tests for compound conditions when service layer behavior is identical.

---

### 3. Test Naming for Private Method Coverage

**Pattern**: `<publicMethod>_<scenario>_in<PrivateMethod>()`

**Examples**:

```java
@Test
@DisplayName("findUsagesForConcept: Handles null conceptId gracefully")
void getImpactAnalysis_handlesNullConceptId_inFindUsagesForConcept() {
    // Tests private method through public method
    List<CategoryObjectMembershipDTO> impact = 
        categoryObjectMembershipService.getImpactAnalysis(null, 100L, "CONCEPT");
    assertNotNull(impact);
    assertTrue(impact.isEmpty());
}
```

**Benefits**: Coverage tools can trace tests to private method branches, developers understand test purpose immediately.

---

### 4. Business Logic Verification in Tests

**Don't Just Test For NPE**: Verify business rules are enforced.

**Weak Test**:

```java
@Test
void method_inheritance_works() {
    List<Result> result = service.method(child.getId());
    assertNotNull(result);  // WEAK - just checks no NPE
}
```

**Strong Test**:

```java
@Test
void method_inheritance_appliesCorrectSourceMarker() {
    Entity child = createChild();
    Entity parent = createParent(child);
    List<Result> result = service.method(child.getId());
    
    assertNotNull(result);
    assertFalse(result.isEmpty());
    // Verify business logic:
    assertTrue(result.stream().allMatch(r -> r.getInherited()));
    assertTrue(result.stream().allMatch(r -> "PARENT".equals(r.getSource())));
    assertTrue(result.stream().allMatch(r -> parent.getId().equals(r.getParentId())));
}
```

---

### 5. File Size Management

**Threshold**: Keep test files under 5,000 lines.

**Strategy When Approaching Limit**:
1. **Add Helper Methods** (Do First) - Extract repeated setup, reduces file size by 30-40%
2. **Use @Nested Classes** (Organize) - Group related tests
3. **Split File** (Last Resort, if >6,000 lines) - Split by functionality

---

### 6. Test Optimization Checklist

**Before Adding New Tests**:
- [ ] Check for existing tests covering same branch
- [ ] Look for duplicate setup code → extract to helper
- [ ] Review if compound condition needs one test or two
- [ ] Consider if @ParameterizedTest can replace multiple tests

**After Adding Tests**:
- [ ] Run all tests locally
- [ ] Check file size (under 5,000 lines?)
- [ ] Look for patterns that could use helpers
- [ ] Update documentation

---

✅ **Usage**: Copy this markdown content into your file named `create_itest.md` and use it directly with Copilot CLI for generating robust Spring Boot integration tests.
