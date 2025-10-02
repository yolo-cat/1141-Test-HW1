# Food Delivery Platform Backend

A comprehensive food delivery platform backend system built with Java, Spring Framework, and Log4j 2. This system handles the complete order lifecycle from customer placement to delivery completion with robust exception handling, state management, and detailed audit logging.

## Features

### Core Functionality
- **Order Management**: Complete order lifecycle from creation to delivery
- **Restaurant Operations**: Accept/reject orders, manage preparation states, capacity management  
- **Delivery Service**: Driver assignment, delivery tracking, capacity management
- **State Management**: Enum-based order state transitions with validation
- **Exception Handling**: Comprehensive custom exception hierarchy with business logic validation
- **Audit Logging**: Structured logging with Log4j 2 for monitoring and troubleshooting

### Technical Highlights
- **Clean Architecture**: Layered design with clear separation of concerns
- **Custom Exceptions**: Domain-specific checked exceptions for business logic errors
- **State Validation**: Robust order state transition validation with detailed error messages
- **Comprehensive Logging**: INFO, WARN, and ERROR level logging with context information
- **Integration Testing**: End-to-end tests covering complete order workflows
- **Type Safety**: Strong typing with validation annotations

## Architecture

```
├── Controllers (Exception Handling)
├── Services (Business Logic)
├── Repositories (Data Access)
├── Models (Domain Entities)
└── Exceptions (Custom Exception Hierarchy)
```

### Key Components

#### Order Status Management
- **OrderStatus Enum**: Defines all possible order states with transition validation
- **State Transitions**: PENDING → ACCEPTED → PREPARING → READY_FOR_DELIVERY → IN_DELIVERY → DELIVERED
- **Terminal States**: CANCELLED, REJECTED, DELIVERED (no further transitions allowed)

#### Exception Hierarchy
- **DeliveryPlatformException**: Base checked exception for all business logic errors
- **RestaurantUnavailableException**: When restaurant cannot accept orders
- **InvalidOrderStateException**: When invalid state transitions are attempted
- **DeliveryAssignmentException**: When driver assignment fails
- **OrderValidationException**: When order data is invalid

#### Services
- **OrderService**: Customer order operations (create, cancel, update)
- **RestaurantService**: Restaurant operations (accept, reject, prepare, ready)
- **DeliveryService**: Delivery operations (assign driver, complete delivery)
- **OrderLoggingService**: Centralized logging with structured messages

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Building the Project
```bash
mvn clean compile
```

### Running Tests
```bash
mvn test
```

### Project Structure
```
src/
├── main/java/com/deliveryplatform/
│   ├── exceptions/          # Custom exception hierarchy
│   ├── models/             # Domain entities (Order, Restaurant, OrderItem, OrderStatus)
│   ├── repositories/       # Data access interfaces and implementations
│   ├── services/           # Business logic services
│   └── controllers/        # Exception handlers
├── main/resources/
│   └── log4j2.xml         # Logging configuration
└── test/java/com/deliveryplatform/
    ├── exceptions/         # Exception tests
    ├── models/            # Entity tests  
    ├── services/          # Service tests
    └── integration/       # End-to-end integration tests
```

## Order Lifecycle Examples

### Successful Order Flow
1. **Customer creates order** → OrderStatus.PENDING
2. **Restaurant accepts** → OrderStatus.ACCEPTED  
3. **Restaurant starts preparing** → OrderStatus.PREPARING
4. **Restaurant marks ready** → OrderStatus.READY_FOR_DELIVERY
5. **Driver assigned** → OrderStatus.IN_DELIVERY
6. **Delivery completed** → OrderStatus.DELIVERED

### Exception Scenarios
- **Restaurant Unavailable**: When restaurant is closed or at capacity
- **Invalid State Transition**: Attempting to skip required workflow steps
- **Delivery Assignment Failure**: When no drivers are available
- **Order Validation Error**: When order data is incomplete or invalid

## Logging

### Log Levels
- **INFO**: Normal operations (order creation, status changes, successful operations)
- **WARN**: Business exceptions, potential issues, performance warnings
- **ERROR**: System errors, unchecked exceptions, critical failures

### Log Configuration
- **Console Logging**: Real-time monitoring during development
- **File Logging**: Persistent logs with rotation (10MB files, 10 file retention)
- **Error Logs**: Separate error file for critical issues
- **Structured Messages**: Consistent format with order IDs and context

### Sample Log Output
```
2024-10-02 14:29:51.742 [main] INFO BUSINESS_EVENTS - Order created: orderId=ORD-00B1D19F, customerId=CUST-004, restaurantId=REST-001, itemCount=2, totalAmount=37.96
2024-10-02 14:29:51.746 [main] WARN com.deliveryplatform.services.OrderLoggingService - Business exception occurred: orderId=ORD-00B1D19F, errorCode=RESTAURANT_UNAVAILABLE, message=Restaurant REST-001 is currently unavailable
```

## Testing

### Test Coverage
- **Unit Tests**: 83+ tests covering all core functionality
- **Integration Tests**: End-to-end order lifecycle scenarios
- **Exception Testing**: Comprehensive validation of error handling
- **State Transition Testing**: All valid and invalid state changes

### Key Test Scenarios
- Complete order lifecycle (PENDING → DELIVERED)
- Restaurant rejection and order cancellation
- Driver capacity management and assignment
- Invalid state transitions and error handling
- Order validation with various invalid data scenarios

### Running Specific Tests
```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest="*Test"

# Run integration tests
mvn test -Dtest="*IntegrationTest"
```

## Configuration

### Log4j 2 Configuration
The logging configuration supports:
- Multiple appenders (Console, File, Error File)
- Rolling file policies with size and time-based triggers
- Different log levels for different packages
- Structured message formatting

### Repository Configuration
- In-memory repositories for development and testing
- Interface-based design for easy swapping to database implementations
- Concurrent data structures for thread safety

## Error Handling Strategy

### Checked Exceptions (Business Logic)
All business rule violations throw checked exceptions that must be handled:
- Forces explicit error handling in calling code
- Provides detailed error context and codes
- Enables proper logging and user feedback

### Unchecked Exception Handling
System errors are caught by global exception handlers:
- Prevents system crashes from unexpected errors
- Logs full stack traces for debugging
- Returns appropriate HTTP status codes

### Exception Context
All exceptions include:
- Unique error codes for programmatic handling
- Detailed error messages for users
- Timestamps for audit trails
- Relevant entity IDs (order, restaurant, driver)

## Development Guidelines

### Adding New Features
1. Define domain models with proper validation
2. Create custom exceptions for business rule violations  
3. Implement service layer with comprehensive logging
4. Add repository methods as needed
5. Write unit and integration tests
6. Update documentation

### Error Handling Best Practices
- Use checked exceptions for business logic errors
- Include relevant context in exception messages
- Log all exceptions with appropriate levels
- Provide user-friendly error responses
- Test both success and failure scenarios

### Logging Best Practices
- Log all business operations at INFO level
- Log business exceptions at WARN level
- Log system errors at ERROR level with stack traces
- Include order IDs and user context in log messages
- Use structured logging for easy parsing

## Future Enhancements

### Potential Improvements
- Database persistence (JPA/Hibernate integration)
- REST API endpoints for external access
- Real-time notifications (WebSocket/Server-Sent Events)
- Payment processing integration
- Geographic routing for driver assignment
- Performance monitoring and metrics
- Caching layer for improved performance

### Scalability Considerations
- Database sharding by geographic region
- Message queue integration for asynchronous processing  
- Microservices architecture for independent scaling
- Load balancing and horizontal scaling
- Event sourcing for complete audit trails

## License

This project is developed as part of a food delivery platform requirements implementation with comprehensive exception handling and logging capabilities.