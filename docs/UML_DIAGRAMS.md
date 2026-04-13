# RideSync UML Diagrams

## Use Case Diagram

```mermaid
graph TD
    A[RideSync System] --> B[User Registration & Profile Management]
    A --> C[Ride Creation & Matching]
    A --> D[Real-time Ride Tracking]
    A --> E[Payment & Rating System]
    A --> F[Notification System]
    A --> G[Ride History & Analytics]
    A --> H[Emergency Contact System]
    A --> I[Preferences & Settings]
    
    B --> J[Driver Registration]
    B --> K[Passenger Registration]
    B --> L[Profile Verification]
    B --> M[Profile Updates]
    
    C --> N[Create Ride Offer]
    C --> O[Search Rides]
    C --> P[Book Ride]
    C --> Q[Automatic Matching]
    
    D --> R[Start Ride]
    D --> S[Track Location]
    D --> T[ETA Updates]
    D --> U[Complete Ride]
    
    E --> V[Fare Calculation]
    E --> W[Payment Processing]
    E --> X[Driver Rating]
    E --> Y[Passenger Rating]
    
    F --> Z[Send Notifications]
    F --> AA[View Notifications]
    F --> BB[Mark as Read]
    
    G --> CC[View History]
    G --> DD[View Statistics]
    G --> EE[Generate Reports]
    
    H --> FF[Add Emergency Contact]
    H --> GG[Trigger Emergency Alert]
    H --> HH[Share Trip Details]
    
    I --> II[Update Preferences]
    I --> JJ[Notification Settings]
    I --> KK[Privacy Settings]
```

## Class Diagram

```mermaid
classDiagram
    class User {
        -Long id
        -String name
        -String email
        -String phone
        -String password
        -UserRole role
        -boolean verified
        -double rating
        -String licenseNumber
        -String vehicleModel
        +register()
        +updateProfile()
        +getRating()
    }
    
    class Ride {
        -Long id
        -String source
        -String destination
        -int totalSeats
        -double farePerSeat
        -LocalDateTime departureTime
        -RideStatus status
        -User driver
        -List~Booking~ bookings
        +createRide()
        +startRide()
        +completeRide()
        +getAvailableSeats()
    }
    
    class Booking {
        -Long id
        -Ride ride
        -User passenger
        -int seatsBooked
        -BookingStatus status
        -Payment payment
        +bookRide()
        +cancelBooking()
        +processPayment()
    }
    
    class Payment {
        -Long id
        -Booking booking
        -double amount
        -PaymentStatus status
        -String paymentMethod
        +processPayment()
        +refundPayment()
    }
    
    class Rating {
        -Long id
        -Booking booking
        -User ratedUser
        -User ratingUser
        -int rating
        -String comment
        +rateUser()
        +getAverageRating()
    }
    
    class Notification {
        -Long id
        -User user
        -String message
        -NotificationType type
        -boolean isRead
        -LocalDateTime createdAt
        +sendNotification()
        +markAsRead()
    }
    
    class EmergencyContact {
        -Long id
        -User user
        -String name
        -String phone
        -String relationship
        +addContact()
        +triggerAlert()
    }
    
    class UserPreferences {
        -Long id
        -User user
        -String preferredLanguage
        -boolean notificationEmail
        -boolean allowLocationSharing
        +updatePreferences()
        +getSettings()
    }
    
    class DijkstraRoutingAlgorithm {
        -Map~String, Map~String, Double~~ graph
        +findShortestPath(String, String)
        +calculateDistance(String, String)
        +calculateEstimatedTime(String, String)
    }
    
    class RouteResult {
        -List~String~ path
        -double totalDistance
        -long estimatedTimeMinutes
        -double estimatedFare
        +getPath()
        +getTotalDistance()
    }
    
    User ||--o{ Ride : creates
    User ||--o{ Booking : makes
    Ride ||--o{ Booking : contains
    Booking ||--|| Payment : has
    Booking ||--|| Rating : receives
    User ||--o{ Rating : gives
    User ||--o{ Notification : receives
    User ||--o{ EmergencyContact : has
    User ||--|| UserPreferences : has
    DijkstraRoutingAlgorithm ..> RouteResult : creates
```

## Activity Diagram - Ride Booking Process

```mermaid
activityDiagram
    start
    :User searches for rides;
    :System finds available rides;
    :User selects ride;
    :User enters seat count;
    :System calculates fare;
    :User confirms booking;
    :System creates booking;
    :System processes payment;
    :Payment successful?;
    if (Payment successful?) then (yes)
        :System sends confirmation;
        :System notifies driver;
        :Booking confirmed;
    else (no)
        :System shows error;
        :User can retry;
    endif
    stop
```

## Activity Diagram - Ride Tracking

```mermaid
activityDiagram
    start
    :Driver starts ride;
    :System begins tracking;
    :Update location every 30 seconds;
    :Calculate progress percentage;
    :Update ETA;
    :Passengers view live tracking;
    :Ride completed?;
    if (Ride completed?) then (yes)
        :System stops tracking;
        :System marks ride complete;
        :System notifies passengers;
        :System processes payments;
        :System requests ratings;
    else (no)
        :Continue tracking;
    endif
    stop
```

## State Diagram - Ride Status

```mermaid
stateDiagram-v2
    [*] --> PUBLISHED
    PUBLISHED --> BOOKED : passenger books
    BOOKED --> IN_PROGRESS : driver starts
    IN_PROGRESS --> COMPLETED : ride ends
    BOOKED --> CANCELLED : cancellation
    PUBLISHED --> CANCELLED : driver cancels
    IN_PROGRESS --> CANCELLED : emergency cancel
    COMPLETED --> [*]
    CANCELLED --> [*]
```

## State Diagram - Booking Status

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> CONFIRMED : payment processed
    CONFIRMED --> PAID : payment confirmed
    PAID --> IN_PROGRESS : ride starts
    IN_PROGRESS --> COMPLETED : ride ends
    PENDING --> CANCELLED : user cancels
    CONFIRMED --> CANCELLED : user cancels
    PAID --> CANCELLED : refund processed
    COMPLETED --> [*]
    CANCELLED --> [*]
```

## Design Patterns Implementation

### 1. Singleton Pattern - DatabaseConnectionManager
```mermaid
classDiagram
    class DatabaseConnectionManager {
        -static DatabaseConnectionManager instance
        -Connection connection
        -private DatabaseConnectionManager()
        +static DatabaseConnectionManager getInstance()
        +Connection getConnection()
    }
    
    note for DatabaseConnectionManager "Ensures only one database connection instance exists"
```

### 2. Factory Pattern - UserFactory
```mermaid
classDiagram
    class UserFactory {
        +createDriver(String, String, String, String, String, String)
        +createPassenger(String, String, String, String)
        +createAdmin(String, String, String, String)
    }
    
    class User {
        <<abstract>>
    }
    
    class Driver {
        +String licenseNumber
        +String vehicleModel
    }
    
    class Passenger {
    }
    
    class Admin {
    }
    
    User <|-- Driver
    User <|-- Passenger
    User <|-- Admin
    UserFactory ..> User : creates
```

### 3. Builder Pattern - RideBuilder
```mermaid
classDiagram
    class RideBuilder {
        -String source
        -String destination
        -int totalSeats
        -double farePerSeat
        +RideBuilder source(String)
        +RideBuilder destination(String)
        +RideBuilder totalSeats(int)
        +RideBuilder farePerSeat(double)
        +Ride build()
    }
    
    class Ride {
        -String source
        -String destination
        -int totalSeats
        -double farePerSeat
    }
    
    RideBuilder ..> Ride : builds
```

### 4. Strategy Pattern - RoutingAlgorithm
```mermaid
classDiagram
    class RoutingAlgorithm {
        <<interface>>
        +RouteResult findShortestPath(String, String)
        +double calculateDistance(String, String)
    }
    
    class DijkstraRoutingAlgorithm {
        -Map graph
        +RouteResult findShortestPath(String, String)
        +double calculateDistance(String, String)
    }
    
    class AStarRoutingAlgorithm {
        +RouteResult findShortestPath(String, String)
        +double calculateDistance(String, String)
    }
    
    RoutingAlgorithm <|.. DijkstraRoutingAlgorithm
    RoutingAlgorithm <|.. AStarRoutingAlgorithm
```

### 5. Observer Pattern - RideStatusManager
```mermaid
classDiagram
    class RideStatusManager {
        -List~RideStatusObserver~ observers
        +addObserver(RideStatusObserver)
        +removeObserver(RideStatusObserver)
        +notifyObservers(RideStatus)
    }
    
    class RideStatusObserver {
        <<interface>>
        +update(RideStatus)
    }
    
    class DriverNotificationObserver {
        +update(RideStatus)
    }
    
    class RiderNotificationObserver {
        +update(RideStatus)
    }
    
    RideStatusManager --> RideStatusObserver
    RideStatusObserver <|.. DriverNotificationObserver
    RideStatusObserver <|.. RiderNotificationObserver
```

### 6. Command Pattern - RideBookingCommand
```mermaid
classDiagram
    class RideBookingCommand {
        <<interface>>
        +execute()
        +undo()
        +String getDescription()
    }
    
    class BookRideCommand {
        -Long rideId
        -Long passengerId
        -int seats
        +execute()
        +undo()
    }
    
    class CancelRideCommand {
        -Long bookingId
        +execute()
        +undo()
    }
    
    class RideBookingInvoker {
        -Stack~RideBookingCommand~ commandHistory
        +executeCommand(RideBookingCommand)
        +undo()
        +redo()
    }
    
    RideBookingCommand <|.. BookRideCommand
    RideBookingCommand <|.. CancelRideCommand
    RideBookingInvoker --> RideBookingCommand
```

### 7. Adapter Pattern - MapDataAdapter
```mermaid
classDiagram
    class MapDataAdapter {
        +RouteResult adaptExternalMapData(ExternalMapData)
        +ExternalMapRequest adaptToExternalRequest(String, String)
    }
    
    class ExternalMapService {
        <<external>>
        +ExternalMapData getRoute(ExternalMapRequest)
    }
    
    class RouteResult {
        -List~String~ path
        -double distance
    }
    
    MapDataAdapter --> ExternalMapService : adapts
    MapDataAdapter --> RouteResult : converts to
```

### 8. Facade Pattern - RideServiceFacade
```mermaid
classDiagram
    class RideServiceFacade {
        -RideService rideService
        -BookingService bookingService
        -PaymentService paymentService
        -DijkstraRoutingAlgorithm routingAlgorithm
        +BookingResult bookCompleteRide(Long, Long, int)
        +RideCreationResult createRideWithRoute(...)
        +List~Ride~ searchOptimizedRides(String, String)
    }
    
    class RideService {
        +createRide(Ride)
        +getRideById(Long)
    }
    
    class BookingService {
        +createBooking(Long, Long, int)
        +cancelBooking(Long)
    }
    
    class PaymentService {
        +processPayment(Long, double)
        +refundPayment(Long)
    }
    
    RideServiceFacade --> RideService
    RideServiceFacade --> BookingService
    RideServiceFacade --> PaymentService
```

## Component Diagram

```mermaid
graph TB
    subgraph "Frontend Layer"
        A[JavaFX Application]
        B[Controllers]
        C[Views FXML]
        D[Models]
    end
    
    subgraph "Backend Layer"
        E[Spring Boot Controllers]
        F[Service Layer]
        G[Repository Layer]
        H[Domain Models]
    end
    
    subgraph "Infrastructure Layer"
        I[Database H2/PostgreSQL]
        J[Routing Algorithm]
        K[Payment Gateway]
        L[Notification Service]
    end
    
    A --> B
    B --> C
    B --> D
    D --> E
    E --> F
    F --> G
    G --> H
    F --> J
    F --> K
    F --> L
    G --> I
```

## Deployment Diagram

```mermaid
graph TB
    subgraph "Client Machine"
        A[JavaFX Desktop App]
    end
    
    subgraph "Application Server"
        B[Spring Boot Backend]
        C[REST API]
        D[Business Logic]
        E[Data Access Layer]
    end
    
    subgraph "Database Server"
        F[H2/PostgreSQL Database]
    end
    
    subgraph "External Services"
        G[Payment Gateway]
        H[Notification Service]
        I[Map Service]
    end
    
    A --> C
    C --> D
    D --> E
    E --> F
    D --> G
    D --> H
    D --> I
```

## Sequence Diagram - User Registration

```mermaid
sequenceDiagram
    participant U as User
    participant UI as JavaFX UI
    participant C as Controller
    participant S as Service
    participant R as Repository
    participant DB as Database
    
    U->>UI: Enter registration details
    UI->>C: submitRegistration()
    C->>S: registerUser()
    S->>R: save()
    R->>DB: INSERT INTO users
    DB-->>R: User created
    R-->>S: User object
    S-->>C: Registration success
    C-->>UI: Show success message
    UI-->>U: Registration successful
```

## Sequence Diagram - Ride Booking

```mermaid
sequenceDiagram
    participant P as Passenger
    participant UI as JavaFX UI
    participant C as Controller
    participant RS as RideService
    participant BS as BookingService
    participant PS as PaymentService
    participant RA as RoutingAlgorithm
    
    P->>UI: Search rides
    UI->>C: searchRides(source, dest)
    C->>RS: findAvailableRides()
    RS-->>C: List of rides
    C-->>UI: Display rides
    P->>UI: Select ride & book
    UI->>C: bookRide(rideId, seats)
    C->>RA: calculateRoute()
    RA-->>C: RouteResult
    C->>BS: createBooking()
    BS->>PS: processPayment()
    PS-->>BS: Payment confirmed
    BS-->>C: Booking created
    C-->>UI: Booking success
    UI-->>P: Confirmation
```
