# Online Learning Platform Management System

This is a demo project for a Spring Boot application that manages users, courses, and learning platforms. It showcases a dual-database architecture using MySQL for transactional data (writes) and MongoDB for denormalized, read-optimized data.

## Features

-   **RESTful API**: Exposes endpoints for CRUD operations on Users, Courses, and Platforms.
-   **Dual-Database Architecture**:
    -   **MySQL**: Acts as the source of truth for all transactional data (Users, Courses, Platforms).
    -   **MongoDB**: Stores denormalized `Platform` documents, embedding courses and enrolled users for efficient read operations.
-   **Asynchronous Data Synchronization**: Changes made to the SQL database (e.g., creating a user, enrolling in a course) trigger an asynchronous process to update the corresponding documents in MongoDB.
-   **User Management**: Create, read, update, and delete users.
-   **Course Management**: Create, read, update, and delete courses.
-   **Platform Management**: Create, read, update, and delete platforms, which are collections of courses.
-   **Enrollment System**: Allows users to be enrolled in multiple courses.
-   **Validation**: Input DTOs are validated at the controller level.
-   **Global Exception Handling**: Centralized handling for API errors.

## Technologies Used

-   **Backend**: Java 17, Spring Boot 3.5.0
-   **Data Persistence**:
    -   Spring Data JPA & Hibernate
    -   Spring Data MongoDB
-   **Databases**:
    -   MySQL
    -   MongoDB
-   **Build Tool**: Apache Maven
-   **DevTools**: Spring Boot DevTools for live reload.

## Prerequisites

Before you begin, ensure you have the following installed:
-   JDK 17
-   Apache Maven
-   MySQL Server
-   MongoDB Community Server

## Setup and Installation

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/Maou3434/java_intern.git
    cd java_intern
    ```

2.  **Configure Databases:**
    -   Ensure your MySQL and MongoDB servers are running.
    -   Create a new database in MySQL named `springboot_jpa_demo`.
        ```sql
        CREATE DATABASE springboot_jpa_demo;
        ```
    -   The application will connect to a MongoDB database named `dbtest`. MongoDB will create it automatically on the first connection if it doesn't exist.

3.  **Set Environment Variables:**
    The application uses environment variables for database credentials. Set them in your operating system or IDE's run configuration.
    -   `DB_USERNAME`: Your MySQL username.
    -   `DB_PASSWORD`: Your MySQL password.

    Alternatively, you can modify `src/main/resources/application.properties` directly, but using environment variables is recommended.

    ```properties
    # src/main/resources/application.properties
    spring.datasource.username=your_mysql_username
    spring.datasource.password=your_mysql_password
    ```

4.  **Build and Run the Application:**
    Use the Maven wrapper to build and run the project.
    ```sh
    ./mvnw spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

## API Endpoints

The application exposes the following REST endpoints:

-   **Users**: `/api/users`
-   **Courses**: `/api/courses`
-   **Platforms**: `/api/platforms`

Each endpoint supports standard CRUD operations (GET, POST, PUT, DELETE).

## Data Flow

The application follows a layered architecture (Controller -> Service -> Repository). A key feature is the data synchronization from MySQL to MongoDB. Here are the data flows for key operations.

### 1. Create a Platform (Write Operation)

This flow demonstrates how data is written to MySQL and then asynchronously synced to MongoDB.

**`POST /api/platforms`**

1.  **`PlatformController`**:
    -   Receives the HTTP POST request with a `PlatformDTO` in the body.
    -   Validates the DTO.
    -   Uses `PlatformMapper` to convert the `PlatformDTO` into a `Platform` JPA entity. This involves fetching associated `Course` entities from the database via the `PlatformService`.
    -   Calls `platformService.createPlatform()` with the `Platform` entity.
    -   Maps the returned, persisted `Platform` entity back to a `PlatformDTO` and sends it in the HTTP response.

2.  **`PlatformService`**:
    -   Receives the `Platform` entity.
    -   Within a `@Transactional` method, it sets the bidirectional relationship by linking each `Course` back to the `Platform`.
    -   Calls `platformRepository.save(platform)` to persist the new platform and its associated courses to the **MySQL database**.
    -   Calls `platformSyncService.syncToMongo(savedPlatform)` to trigger an asynchronous update to MongoDB.

3.  **`PlatformSyncService`**:
    -   This service runs in a separate thread (`@EnableAsync`).
    -   It receives the `Platform` entity from the `PlatformService`.
    -   It constructs a `PlatformDocument` (a MongoDB document). This involves:
        -   Fetching all users enrolled in the platform's courses from MySQL via `UserRepository`.
        -   Creating `CourseEmbed` and `UserEmbed` objects to create a denormalized document.
    -   Calls `platformDocRepository.save(doc)` to save the complete, denormalized `PlatformDocument` to the **MongoDB database**.

### 2. Read Platform Users (Read Operation)

This flow demonstrates a read-optimized query directly from MongoDB.

**`GET /api/platforms/{mongoId}/users`**

1.  **`PlatformController`**:
    -   Receives the HTTP GET request with the platform's MongoDB ID (`mongoId`).
    -   Calls `platformService.getUsersByPlatformIdFromMongo(mongoId)`.
    -   Receives a list of `UserDTO`s and returns them in the HTTP response.

2.  **`PlatformService`**:
    -   Calls `platformDocRepository.findById(mongoId)` to fetch the `PlatformDocument` directly from **MongoDB**.
    -   If the document is found, it extracts the embedded user information from all courses within the document.
    -   It aggregates this data and maps it to a list of `UserDTO`s.

### 3. Enroll a User in Courses (Update Operation)

This flow shows how an update to one entity (User) can trigger a sync of another (Platform).

**`POST /api/users/{id}/courses`**

1.  **`UserController`**:
    -   Receives the user ID and a set of `courseIds`.
    -   Calls `userService.enrollUserInCourses(userId, courseIds)`.
    -   Maps the updated `User` entity to a `UserDTO` and returns it.

2.  **`UserService`**:
    -   Within a `@Transactional` method, it fetches the `User` and the `Course` entities from **MySQL**.
    -   It updates the `user_course` join table by modifying the set of courses associated with the user.
    -   Calls `userRepository.save(user)` to persist the changes.
    -   Calls `platformSyncService.syncPlatformsByCourses(allAffectedCourses)`. This is a crucial step: it ensures that all platforms containing the affected courses are updated.

3.  **`PlatformSyncService`**:
    -   Receives the set of affected `Course` entities.
    -   It determines the unique set of `Platform`s associated with these courses.
    -   For each unique platform, it calls `syncToMongo(platform)`, which follows the same logic as in the "Create a Platform" flow, rebuilding and saving the `PlatformDocument` in **MongoDB**.

### 4. Delete a Platform (Delete Operation)

This flow shows how a delete operation is propagated to both databases.

**`DELETE /api/platforms/{id}`**

1.  **`PlatformController`**:
    -   Receives the HTTP DELETE request with the platform `id`.
    -   Calls `platformService.deletePlatformById(id)`.
    -   Returns the DTO of the deleted platform.

2.  **`PlatformService`**:
    -   Within a `@Transactional` method, it fetches the `Platform` from **MySQL** to ensure it exists.
    -   Calls `platformRepository.delete(platform)`. Due to `CascadeType.ALL` and `orphanRemoval=true` on the `Platform`-`Course` relationship, all courses associated with the platform are also deleted from **MySQL**.
    -   Calls `platformSyncService.deletePlatformFromMongo(id)` to trigger an asynchronous deletion from MongoDB.

3.  **`PlatformSyncService`**:
    -   Receives the platform ID.
    -   Finds the corresponding document in **MongoDB** by its ID and deletes it using `platformDocRepository.delete()`.