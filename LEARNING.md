# DeliveryFlow — Spring Boot Learning Guide

A hands-on, concept-by-concept guide to mastering Spring Boot using the **DeliveryFlow** codebase.

---

## 🗺️ Learning Roadmap & Progress Tracker

### Phase 1: Already Implemented in DeliveryFlow (Study & Understand)
- [x] **Module 1: Spring Core & The Container**
  - Spring vs Spring Boot
  - IoC (Inversion of Control) & Beans
  - ApplicationContext
  - `@SpringBootApplication` & Auto-configuration
  - Starter Dependencies (`pom.xml`)
- [x] **Module 2: Dependency Injection & Stereotypes**
  - Dependency Injection (DI) explained
  - `@RestController` vs `@Service` vs `@Repository` vs `@Component`
  - Constructor Injection vs `@Autowired` (Why `@RequiredArgsConstructor` is used)
  - `@Configuration` and `@Bean`
- [x] **Module 3: Spring Web MVC & REST APIs**
  - REST & HTTP Status Codes (200, 201, 204, 400, 404, 409)
  - Mapping Annotations: `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
  - Handling Inputs: `@RequestBody` vs `@PathVariable`
  - DTOs (Data Transfer Objects) using Java Records
  - Request Validation with Jakarta (`@Valid`, `@NotBlank`, `@Email`, etc.)
  - Global Exception Handling with `@RestControllerAdvice` & `@ExceptionHandler`
- [x] **Module 4: Spring Data JPA & Persistence**
  - What is JPA & Hibernate?
  - Entity Mapping: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`
  - Relationships: `@ManyToOne`
  - Lazy vs Eager Loading (`FetchType.LAZY` & avoiding the N+1 problem)
  - `JpaRepository` interface
  - Derived Query Methods (`findByStatus`, `existsByEmail`)
  - Custom JPQL Queries (`@Query` with `join fetch`)
  - Transaction Management with `@Transactional`

---

### Phase 2: Missing Topics (To Be Built Step-by-Step)
- [ ] **Module 5: Advanced Web & Query Parameters**
  - Query parameters with `@RequestParam` (Filtering & Pagination)
  - HTTP headers with `@RequestHeader` (Request tracking / Client IDs)
- [ ] **Module 6: Advanced JPA Relationships**
  - `@OneToOne` (e.g., Driver <-> DriverLicense / VehicleDetails)
  - `@OneToMany` (e.g., Order <-> StatusHistory / AuditLog)
  - `@ManyToMany` (e.g., Driver <-> DeliveryZones)
- [ ] **Module 7: Spring Core Deep-Dive & Internals**
  - Generic `@Component`
  - Bean Scopes (`Singleton` vs `Prototype`)
  - Bean Lifecycle (`@PostConstruct` and `@PreDestroy`)
  - Spring Profiles (`application-dev.properties` vs `application-prod.properties`)
  - AOP (Aspect-Oriented Programming) basics for execution logging
- [ ] **Module 8: Security & Authentication**
  - Authentication vs Authorization
  - Spring Security fundamentals
  - JWT (JSON Web Tokens) stateless authentication

---

# Phase 1: Deep Dive into Implemented Concepts

---

## Module 1: Spring Core & The Container

### 1. Spring vs Spring Boot

#### 💡 The Simple Theory
- **Spring Framework**: Like buying a collection of car parts (engine, wheels, chassis) from a shop. You have full flexibility, but you have to assemble every wire, configure XML or tons of annotations, configure Tomcat server, configure Hibernate, Jackson JSON, etc.
- **Spring Boot**: Like buying a pre-assembled, turn-key car. It takes the Spring Framework and adds:
  1. **Opinionated Starters**: Pre-bundled dependency packages.
  2. **Auto-configuration**: Detects what's on your classpath and automatically configures it.
  3. **Embedded Server**: Comes with Tomcat built-in so you just run a `main()` method.

#### ⚙️ How it works in DeliveryFlow
Look at [pom.xml](file:///c:/Users/Admin/Documents/Github/deliverytracker/pom.xml#L5-L10):
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.0</version>
</parent>
```
Instead of declaring versions for 50 different libraries, the parent POM manages all compatible versions for you.

---

### 2. IoC (Inversion of Control), Beans, and ApplicationContext

#### 💡 The Simple Theory
- **Without IoC (Normal Java)**: When `OrderController` needs an `OrderService`, it does:
  ```java
  OrderService service = new OrderService(); // You control object creation
  ```
  *Problem*: Tightly coupled, hard to test, hard to share instances.
- **With IoC (Inversion of Control)**: You give up control of object creation. You tell Spring: *"Here are my classes. You create them, configure them, and manage them."*
- **Bean**: Any Java object managed by the Spring IoC container.
- **ApplicationContext**: The "brain" or box that holds all these Beans and wires them together.

#### ⚙️ How it works in DeliveryFlow
Look at [OpenApiConfig.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/config/OpenApiConfig.java#L8-L15):
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deliveryFlowOpenApi() {
        return new OpenAPI().info(new Info().title("DeliveryFlow API"));
    }
}
```
- `@Configuration`: Tells Spring *"This class contains Bean factory recipes."*
- `@Bean`: Tells Spring *"Run this method on startup, take the returned `OpenAPI` object, and register it as a Bean inside the ApplicationContext."*

When your test runs in [DeliveryflowApplicationTests.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/test/java/com/deliveryflow/DeliveryflowApplicationTests.java#L6-L11):
```java
@SpringBootTest
class DeliveryflowApplicationTests {
    @Test
    void contextLoads() {
        // Verifies the ApplicationContext successfully starts up and all beans wire together!
    }
}
```

---

### 3. `@SpringBootApplication` & Auto-configuration

#### 💡 The Simple Theory
`@SpringBootApplication` is a 3-in-1 mega annotation placed on the main class:
1. `@SpringBootConfiguration`: Marks it as a configuration class.
2. `@EnableAutoConfiguration`: Tells Spring Boot to guess what you need (e.g. "I see MySQL driver on the classpath? Let me configure a DataSource!").
3. `@ComponentScan`: Tells Spring to scan the current package (`com.deliveryflow`) and all sub-packages for `@Service`, `@RestController`, `@Repository`, etc.

#### ⚙️ How it works in DeliveryFlow
Look at [DeliveryflowApplication.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/DeliveryflowApplication.java#L6-L12):
```java
@SpringBootApplication
public class DeliveryflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryflowApplication.class, args);
    }
}
```
When you run this `main()` method:
1. Tomcat web server starts on port 8080.
2. `application.properties` is loaded.
3. Database connection pool is established.
4. All controllers and services are created and wired together.

---

## Module 2: Dependency Injection & Stereotypes

### 1. Dependency Injection (DI) & Constructor Injection

#### 💡 The Simple Theory
**Dependency Injection** is how IoC is implemented. If Class A depends on Class B, you don't instantiate Class B inside Class A; instead, Class B is "injected" into Class A.

There are two common ways to inject:
1. **Field Injection (`@Autowired`)**:
   ```java
   @Autowired
   private OrderService orderService; // Hidden dependency, harder to unit-test
   ```
2. **Constructor Injection (Recommended Best Practice)**:
   ```java
   private final OrderService orderService;

   public OrderController(OrderService orderService) {
       this.orderService = orderService;
   }
   ```
   *Why Constructor Injection is better*:
   - Dependencies can be marked `final` (immutable and thread-safe).
   - Prevents NullPointerExceptions (the class cannot be created without its dependencies).
   - In modern Spring, if a class has one constructor, `@Autowired` is optional and omitted!

#### ⚙️ How it works in DeliveryFlow
Instead of typing the boilerplate constructor by hand, DeliveryFlow uses Lombok's `@RequiredArgsConstructor`!
Look at [CustomerController.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/controller/CustomerController.java#L22-L28):
```java
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor // Automatically generates the constructor for all 'final' fields
public class CustomerController {

    private final CustomerService customerService; // Injected automatically!
}
```

---

### 2. Stereotype Annotations: `@RestController` vs `@Service` vs `@Repository` vs `@Component`

#### 💡 The Simple Theory
All 4 tell Spring: *"Create a Bean of this class"*. Why have 4 different names?
- **`@Component`**: The generic parent stereotype. Use when a class doesn't belong to web, service, or persistence layers (e.g. a utility calculator or file parser).
- **`@RestController`**: Special `@Component` for the **Web Layer**. Combines `@Controller` + `@ResponseBody`. Automatically converts Java return objects directly into JSON.
- **`@Service`**: Special `@Component` for the **Business Logic Layer**. Where calculations, workflows, and transactional rules live.
- **`@Repository`**: Special `@Component` for the **Persistence Layer** (Database operations). Translates database SQL exceptions into Spring's `DataAccessException` hierarchy.

#### ⚙️ How it works in DeliveryFlow
- Web Layer: [CustomerController.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/controller/CustomerController.java#L22) has `@RestController`.
- Business Layer: [CustomerService.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/service/CustomerService.java#L17) has `@Service`.
- Database Layer: [CustomerRepository.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/repository/CustomerRepository.java#L6) extends `JpaRepository`. (Spring Data automatically registers it as a repository bean).

---

## Module 3: Spring Web MVC & REST APIs

### 1. HTTP Methods & Mappings

#### 💡 The Simple Theory
REST APIs map standard HTTP methods to CRUD operations:
- `GET` -> Retrieve data (safe, idempotent)
- `POST` -> Create new data
- `PUT` -> Update/replace existing data
- `DELETE` -> Remove or cancel data

#### ⚙️ How it works in DeliveryFlow
Look at [CustomerController.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/controller/CustomerController.java#L30-L53):
```java
@PostMapping          // POST /api/customers
public ResponseEntity<CustomerResponse> create(...) { ... }

@GetMapping           // GET /api/customers
public List<CustomerResponse> findAll() { ... }

@GetMapping("/{id}")  // GET /api/customers/1
public CustomerResponse findById(@PathVariable Long id) { ... }

@DeleteMapping("/{id}") // DELETE /api/customers/1
public ResponseEntity<Void> delete(@PathVariable Long id) { ... }
```

---

### 2. `@PathVariable` vs `@RequestBody`

#### 💡 The Simple Theory
- **`@PathVariable`**: Extracts a variable embedded right in the URL path (e.g., `/api/orders/{id}` -> `/api/orders/5`).
- **`@RequestBody`**: Reads the HTTP request payload (JSON string sent from frontend) and deserializes it into a Java object using Jackson.

#### ⚙️ How it works in DeliveryFlow
Look at [OrderController.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/controller/OrderController.java#L57-L68):
```java
// Path Variables: order ID and driver ID are in the URL path
@PutMapping("/{id}/assign/{driverId}")
public OrderResponse assignDriver(@PathVariable Long id, @PathVariable Long driverId) {
    return orderService.assignDriver(id, driverId);
}

// Request Body: new status payload sent as JSON {"status": "PICKED_UP"}
@PutMapping("/{id}/status")
public OrderResponse updateStatus(@PathVariable Long id,
                                  @Valid @RequestBody StatusUpdateRequest request) {
    return orderService.updateStatus(id, request.status());
}
```

---

### 3. DTOs (Data Transfer Objects) and Validation

#### 💡 The Simple Theory
- **Why DTOs?** Never expose your database `@Entity` directly to the web layer! Doing so causes security vulnerabilities, exposes passwords/internal IDs, and leads to infinite loops when serializing relationships to JSON.
- **Java Records**: DeliveryFlow uses modern Java `record`s for DTOs. They are immutable, concise, and clean.
- **Jakarta Bean Validation**: Annotations like `@NotBlank`, `@Email`, `@Size`, `@Pattern` placed on DTO fields to guard against bad user input.

#### ⚙️ How it works in DeliveryFlow
Look at [CustomerRequest.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/dto/CustomerRequest.java#L8-L26):
```java
public record CustomerRequest(
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    String name,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    String email,

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "\\d{10}", message = "Phone must be exactly 10 digits")
    String phone,

    @NotBlank(message = "Address is required")
    String address
) {}
```
In the controller, you add `@Valid`:
```java
public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request)
```
If someone sends an invalid email or 5-digit phone number, Spring halts execution before your service even runs!

---

### 4. Global Exception Handling with `@RestControllerAdvice`

#### 💡 The Simple Theory
Instead of wrapping every single controller method in ugly `try-catch` blocks, Spring gives you **Centralized Exception Handling**:
- When any service or controller throws an exception (e.g., `ResourceNotFoundException`), Spring intercepts it.
- An `@ExceptionHandler` method converts that exception into a structured JSON error response and returns the proper HTTP status code (404 Not Found, 400 Bad Request, 409 Conflict).

#### ⚙️ How it works in DeliveryFlow
Look at [GlobalExceptionHandler.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/exception/GlobalExceptionHandler.java#L26-L45):
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request); // Returns 404
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRule(BusinessRuleException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request); // Returns 409
    }
}
```

---

## Module 4: Spring Data JPA & Persistence

### 1. `@Entity`, `@Id`, `@GeneratedValue`

#### 💡 The Simple Theory
- **JPA (Java Persistence API)**: A specification that maps Java classes to database tables.
- **Hibernate**: The underlying engine implementing JPA.
- `@Entity`: Marks a class as a database table row.
- `@Id`: Marks the primary key.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: Delegates ID generation to MySQL's `AUTO_INCREMENT`.

#### ⚙️ How it works in DeliveryFlow
Look at [Customer.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/entity/Customer.java#L18-L29):
```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;
}
```

---

### 2. Relationships (`@ManyToOne`) & Lazy vs Eager Loading

#### 💡 The Simple Theory
- In delivery systems, **many** orders belong to **one** customer (`@ManyToOne`).
- **`FetchType.EAGER`**: Whenever you load an order, Hibernate automatically queries and loads the customer too. (Can cause huge performance degradation).
- **`FetchType.LAZY`**: Hibernate only loads the order. It will NOT load the customer from the database until you explicitly call `order.getCustomer().getName()`.
- **The N+1 Problem**: If you fetch 100 orders and then loop through them calling `order.getCustomer()`, Hibernate executes 1 query for orders + 100 separate queries for customers (101 queries total!).

#### ⚙️ How it works in DeliveryFlow
Look at [DeliveryOrder.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/entity/DeliveryOrder.java#L51-L58):
```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "customer_id", nullable = false)
private Customer customer;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "driver_id")
private Driver driver;
```
DeliveryFlow solves the N+1 problem with **JPQL Fetch Joins** in [OrderRepository.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/repository/OrderRepository.java#L22-L28):
```java
@Query("""
        select o from DeliveryOrder o
        left join fetch o.customer
        left join fetch o.driver
        order by o.id desc
        """)
List<DeliveryOrder> findAllWithDetails();
```
`join fetch` loads the order, customer, and driver in **1 single SQL JOIN query**!

---

### 3. Derived Queries vs JPQL

#### 💡 The Simple Theory
- **Derived Query Methods**: You just declare the method name in your interface; Spring Data inspects the name and generates the SQL automatically!
- **JPQL (`@Query`)**: When queries are complex (joins, aggregations), you write JPQL (Java Persistence Query Language) targeting Java entities rather than raw SQL tables.

#### ⚙️ How it works in DeliveryFlow
- **Derived query** in [DriverRepository.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/repository/DriverRepository.java#L11-L15):
  ```java
  List<Driver> findByStatus(DriverStatus status);
  boolean existsByPhone(String phone);
  ```
- **JPQL query** in [OrderRepository.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/repository/OrderRepository.java#L56-L57):
  ```java
  @Query("select coalesce(max(o.id), 0) from DeliveryOrder o")
  long findMaxOrderId();
  ```

---

### 4. Transactions (`@Transactional`)

#### 💡 The Simple Theory
A database transaction is an **all-or-nothing** unit of work (ACID property):
- If step 1 succeeds but step 2 throws an exception, step 1 is automatically **rolled back** to prevent corrupted data.
- `@Transactional(readOnly = true)`: Tells Hibernate this is a read-only query; Hibernate optimizes performance by skipping dirty-checking on entities.

#### ⚙️ How it works in DeliveryFlow
Look at [OrderService.java](file:///c:/Users/Admin/Documents/Github/deliverytracker/src/main/java/com/deliveryflow/service/OrderService.java#L90-L108):
```java
@Transactional
public OrderResponse assignDriver(Long orderId, Long driverId) {
    DeliveryOrder order = getOrderOrThrow(orderId);
    Driver driver = driverRepository.findById(driverId)...;

    // Step 1: change driver to BUSY
    driver.setStatus(DriverStatus.BUSY);

    // Step 2: change order to ASSIGNED
    order.setDriver(driver);
    order.setStatus(OrderStatus.ASSIGNED);

    return OrderResponse.from(order);
}
```
If an error happens anywhere inside `assignDriver()`, both the driver's status and order's status revert back to their original state.

---

## 🎯 Next Steps: Learning Phase 2 (Building Missing Features)

Whenever you are ready, we will build each of the missing topics into DeliveryFlow one by one:
1. **Feature 1**: Add `@RequestParam` & Pagination/Filtering to the Orders API.
2. **Feature 2**: Add `@RequestHeader` for tracking client requests.
3. **Feature 3**: Add `@OneToMany` Order History Timeline.
4. **Feature 4**: Add `@Component`, `@PostConstruct`, and Bean Scopes.
5. **Feature 5**: Add Spring AOP for execution time logging.
6. **Feature 6**: Add Spring Security & JWT authentication.
