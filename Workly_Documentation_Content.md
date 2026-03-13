# Workly Application Documentation

## 1. Introduction
The advent of digital technology and mobile applications has fundamentally shifted how local services and workforce management operate. In modern urban and semi-urban environments, there is a recurring daily need for trusted, efficient, and readily available professional services such as domestic cleaning, maintenance, and administrative assistance. Concurrently, from a business or agency perspective, managing the workforce that provides these services presents significant logistical challenges. Ensuring that workers are at the right location at the right time, while maintaining transparent records, has historically been a cumbersome process. 

The "Workly" application is conceived and developed to directly address these dual challenges. Built as a native Android application using cutting-edge technologies like Kotlin and Jetpack Compose, Workly serves as a unified platform. It provides consumers with an intuitive interface to discover and seamlessly book daily needs services, while equipping administrators and service providers with robust tools—like GPS location tracking and live photographic evidence via CameraX—to maintain immutable, verifiable work logs. This document outlines the comprehensive system design, requirements analysis, and implementation details of the Workly project.

### 1.1 Existing System 
Traditionally, hiring professional cleaners, mechanics, or other daily service providers relies heavily on scattered, unorganized, and often analog systems. Customers typically have to look up local offline directories, rely on word-of-mouth recommendations, or search through fragmented online bulletin boards. They must then call providers, negotiate prices over the phone, and deal with verbal confirmations that frequently lead to miscommunication regarding time, scope, or cost of the service. 

Additionally, from a workforce management perspective, tracking employee locations and ensuring accountability currently requires physical attendance registers, delayed WhatsApp message updates, or multiple disjointed tracking apps. Employees working in the field—like cleaners assigned to different houses—often self-report their arrival times, leaving room for inaccuracies and disputes. The existing system lacks a centralized, transparent platform where users can book verified services instantly, and where administration can track personnel locations and work completion efficiently in real-time.

### 1.2 Need for the New System
A comprehensive digital transformation is necessary to bridge the widening gap between consumers looking for reliable daily services and the service providers executing them. The need for a new system arises directly from the modern demand for an on-demand, mobile-first solution that prioritizes convenience, absolute trust, and strict accountability. 

Consumers require a unified, single-point-of-contact platform to discover services, view transparent pricing structures without aggressive negotiation, and seamlessly book appointments with a few taps on their smartphones. Conversely, employers, service managers, and clients require an automated, foolproof way to capture precise worker locations and maintain unalterable work logs. By utilizing modern GPS technologies and live camera integration, the new system forces the generation of verifiable metadata (time, date, latitude, and longitude). An integrated application eliminates manual human errors, drastically reduces administrative overhead, minimizes disputes over attendance or service delivery, and significantly elevates the overall customer and employee experience.

### 1.3 Objective of the Proposed System
The core objective of the "Workly" application is to engineer a comprehensive, all-in-one Android mobile solution that simplifies the booking of daily services while simultaneously offering robust, anti-spoofing workforce tracking capabilities. Specifically, the system aims to achieve the following:
- **Streamlined User Experience:** Provide an intuitive, dynamic, and accessible User Interface (UI) utilizing the modern Jetpack Compose framework, ensuring minimal learning curve for users of all technical backgrounds.
- **Secure Identity Management:** Facilitate secure, encrypted user registration and authentication protocols via Firebase Authentication, ensuring data privacy and secure session management.
- **Effortless Service Booking:** Allow users to browse and book daily services (such as House Cleaning, Commercial Cleaning, and Maintenance) seamlessly, capturing user preferences and precise service locations.
- **Verifiable Work Logging:** Implement an automated Work Log feature that captures live photos utilizing CameraX, pairing them instantly with highly accurate GPS coordinates and server-timestamped data to verify a worker's physical presence at a job site.
- **Real-Time Data Synchronization:** Store user profiles, booking states, and work logs securely in real-time capitalizing on Firebase Firestore for cloud syncing, alongside Room Database for robust local persistence and offline caching capabilities.

### 1.4 Problem Definition
Currently, finding trusted professionals for domestic and daily needs is a notoriously slow, fragmented, and unreliable process. There exists no standard assurance of quality, transparent pricing, or guaranteed punctuality in the unorganized local service sector. 

For workforce and field-service management, maintaining accurate records of employee locations and remote field visits is highly susceptible to spoofing, buddy-punching, and general inaccuracy. Managers lack real-time visibility into field operations. Therefore, the core problem is the absolute lack of a unified, verifiable digital application that successfully handles both the consumer-facing service execution (booking) and the employer-facing verification (location and visual logging) under one cohesive Android ecosystem. Workly is designed to solve this exact problem by acting as the definitive bridge between service booking and execution verification.

### 1.5 Scope of the Project and Core Components
The scope of the Workly project encompasses the end-to-end development of a fully functional, production-ready Android mobile application using the Kotlin programming language. The core components and operational boundaries of the project include:
- **Authentication & Identity Module:** Comprehensive flow handling user Login, Registration, and Password Reset utilizing Firebase Auth.
- **Home/Dashboard Navigation Module:** A central navigational hub displaying available service categories, active bookings, and quick-action shortcuts for smooth app traversal.
- **Service Booking Engine:** A multi-step structured flow allowing users to select a primary category (e.g., Cleaning), specify sub-services (local cleaning, office cleaning), input exact location details, and finalize the booking request.
- **Location & Visual Work Log Module:** A highly specialized, security-focused feature utilizing the Android CameraX API to take a live photo. Simultaneously, it leverages Google Play Location Services to retrieve highly accurate GPS coordinates (Latitude/Longitude), combining this with real-time Date/Time stamps to create an undeniable record of presence.
- **Backend & Cloud Infrastructure:** Complete, bidirectional integration with Firebase Firestore for remote data synchronization and the Room Database library for robust local SQL data persistence, ensuring data integrity and fast retrieval times.

### 1.6 Project Profile
- **Project Title:** Workly - Integrated Daily Needs Booking and Verifiable Work Logging Application
- **Target Platform:** Android Mobile OS (Smartphones and Tablets)
- **Primary Programming Language:** Kotlin
- **UI Toolkit/Framework:** Jetpack Compose (Adhering to Material Design 3 Guidelines)
- **Cloud Backend & Authentication:** Google Firebase (Firebase Authentication, Cloud Firestore)
- **Local Database:** Room Persistence Library (SQLite Abstraction)
- **Hardware Integration:** Google Location Services (GPS), CameraX API
- **Architectural Pattern:** MVVM (Model-View-ViewModel) with Clean Architecture principles handling UI State Management.

### 1.7 Assumptions and Constraints
**Assumptions:**
- End-users possess a functional Android smartphone running a compatible operating system version (Android 7.0 / API Level 24 "Nougat" or higher).
- Users have access to an active, reasonably stable internet connection (Wi-Fi or Cellular Data) to facilitate Firebase Authentication and real-time Firestore synchronization.
- Users will grant the necessary runtime permissions (Camera, Fine/Coarse Location, Foreground Services) required for the core features of the application to execute.
- Service execution personnel are verified externally by the administrative agency before being assigned to the platform's bookings.

**Constraints:**
- The application currently relies heavily on Google Mobile Services (GMS) and Google Play Services for precise location fetching; devices lacking GMS (like certain newer Huawei models) may experience degraded functionality.
- Capturing high-resolution photos via CameraX as part of the daily work log may consume significant local device storage over time if the images are not periodically compressed, uploaded, or deleted via an internal cache management system.
- Direct hardware limitations of the user's device (e.g., a broken GPS module, poor lighting for the camera, or extremely low RAM) will directly impact the reliability and speed of the Work Log verification module.

### 1.8 Advantages and Limitations
**Advantages:**
- **Unparalleled Convenience:** Provides a singular, cohesive app environment for managing multiple daily service workflows, reducing "app fatigue."
- **Cryptographic-like Verifiability:** The combination of forced live-camera capture and immediate GPS coordinate-stamping ensures that work logs are accurate, trustworthy, and nearly impossible to spoof compared to manual entry.
- **Cloud-Native Scalability:** The utilization of Firebase Cloud Firestore allows the application's backend to effortlessly scale concurrently as the user base and data volume grow, without requiring manual server deployment or maintenance.
- **State-of-the-Art UI/UX:** Built entirely using declarative Jetpack Compose, the app offers fluid animations, rapid rendering, and a highly responsive user experience compared to traditional XML layouts.

**Limitations:**
- The platform is currently strictly restricted to the Android mobile ecosystem; no equivalent iOS application or Web Administration Dashboard counterpart currently exists for cross-platform users.
- True offline support is limited; while the Room database can cache local states and logs, core functionalities like creating a new booking, authenticating a new session, or uploading a final work log mandate an active internet connection.
- The application currently operates as a booking and logging facilitator and does not inherently support integrated, in-app secure payment gateways (transactions are presumed to be handled externally, via third-party UPI, or via cash on delivery).

### 1.9 Proposed Timeline Chart
*(Note: Replace the generic weeks below with your actual project timeline/dates, e.g., "Jan 1 - Jan 7")*
- **Phase 1 (Week 1-2): Requirement Gathering & Analysis:** Conducting feasibility studies, finalizing functional requirements, and outlining system design architectures.
- **Phase 2 (Week 3): Prototyping & Database Modeling:** Designing UI/UX wireframes and structuring the Firebase NoSQL collections alongside local Room Entity relationships.
- **Phase 3 (Week 4-5): Foundation Implementation:** Setting up the MVVM architecture, configuring Jetpack Compose NavGraphs, and integrating Firebase Authentication (Login/Register modules).
- **Phase 4 (Week 6-7): Core Logic Development:** Developing the Service Booking flow, Home Dashboard, and connecting UI states to Firestore backend repositories.
- **Phase 5 (Week 8-9): Hardware & Sensor Integration:** Implementing the complex Work Log module, securing CameraX permissions, extracting precise GPS coordinates, and binding Location/Image data into unified log objects.
- **Phase 6 (Week 10): Testing & Quality Assurance:** Conducting rigorous Unit Testing on ViewModels, Integration Testing on Firebase connections, and debugging UI anomalies on physical devices.
- **Phase 7 (Week 11): User Acceptance Testing (UAT):** Distributing the Alpha APK for faculty and peer review, gathering feedback, and polishing the final user experience.
- **Phase 8 (Week 12): Deployment & Documentation:** Compiling final codebase, preparing the comprehensive Agile documentation report, and final project submission/presentation.

## 4.3 Implementation of Business Logic

This section details the core data models (Entities) and their connection to the logic implemented within the respective Activities and ViewModels in the Workly application. The business logic strictly separates frontend UI state management from backend data persistence via Firebase Firestore and Room.

### 4.3.1 Data Layer & Entity Logic

The application relies on several core data classes located in the `com.example.workly.data` package.

**1. Booking Entity (`Booking.kt`)**
The `Booking` model is the primary class coordinating a scheduled service.
*   **Location:** `app/src/main/java/com/example/workly/data/Booking.kt`
*   **Key Fields & Logic:**
    *   `id`: `String` - Unique document identifier generated by `FirebaseFirestore.collection("bookings").document().id`.
    *   `userId`: `String` - References the authenticated user creating the booking.
    *   `serviceName` & `serviceCategory`: `String` - Identifies the booked service type.
    *   `providerId` & `providerName`: `String` - Link to the specialist executing the work.
    *   `address`, `latitude`, `longitude`: Defines precisely where the service occurs. The `BookingActivity` populates this via Google Location Services.
    *   `basePrice` & `finalPrice`: `Double` - Handles the pricing and dynamic rate changes from the Provider.
    *   `status`: `String` - Tracks the lifecycle (`Pending`, `Confirmed`, `InProgress`, `Completed`, `Cancelled`).
    *   `paymentStatus` & `paymentMethod`: `String` - Records successful transactions (`Cash`, `Card`, etc.).

**2. Provider Entity (`Provider.kt`)**
Represents the professional attempting to execute the service request.
*   **Location:** `app/src/main/java/com/example/workly/data/Provider.kt`
*   **Key Fields & Logic:**
    *   `hourlyRate`: `Double` - Dictates the final price calculation in `BookingActivity`.
    *   `specialties`: `List<String>` - Used by `ProviderListActivity` to filter out relevant providers for the selected service category.
    *   `latitude`, `longitude`: `Double` - Used in conjunction with the User's coordinates in `AIMatcher` to dynamically calculate an "AI Matched" proximity/relevance score.

**3. WorkLog Entity (`WorkLog.kt`)**
A local entity used to securely store anti-spoofing metadata before syncing to the cloud.
*   **Location:** `app/src/main/java/com/example/workly/data/WorkLog.kt`
*   **Database:** Annotated with `@Entity(tableName = "work_logs")` for Room SQLite persistence.
*   **Key Fields & Logic:**
    *   `latitude` / `longitude`: Securely fetched directly via device GPS at the time of tracking.
    *   `imagePath`: Local URI string pointing to the securely captured CameraX photographic evidence.
    *   `timestamp`: Immutable `Long` captured specifically when the action is triggered.

### 4.3.2 Authentication Logic Flow

Authentication entirely abstracts Firebase SDK implementations.

*   **Location:** `app/src/main/java/com/example/workly/auth/LoginActivity.kt` & `AuthSelectionActivity.kt`
*   **Logic Implementation:**
    *   **Email/Password Flow:** Checks for non-empty credentials and invokes `FirebaseAuth.getInstance().signInWithEmailAndPassword()`. Success redirects via `Intent` to `HomeActivity`, flushing the backstack (`finishAffinity()`).
    *   **Google Sign-In Flow:** Utilizes `GoogleSignInClient` and ActivityResultLauncher. It fetches an `idToken` from Google and translates it into a Firebase `GoogleAuthProvider.getCredential()`. Secure state tracking removes the need for local SQL session management.

### 4.3.3 Booking Execution Flow

The service booking process is split across several dedicated Android components to ensure single-responsibility.

*   **1. Booking Details Capture (`BookingActivity.kt`)**
    *   **Logic:** Uses Google `FusedLocationProviderClient` coupled with Geocoder to reverse-map latitude/longitude into a physical string address. Collects precise Date/Time utilizing `DatePickerDialog`.
    *   **Function:** `saveBookingToFirestore()` maps the local variables directly to a `Booking` data class instance and executes a `.set()` operation against the Firestore `/bookings` collection.

*   **2. Provider Matching (`ProviderListActivity.kt` & `AIMatcher.kt`)**
    *   **Logic:** Executes a Firebase Query filtering the `/providers` collection by `serviceCategory` (`whereArrayContains("specialties", category)`).
    *   **Scoring Logic:** Feeds the resultant providers into the `AIMatcher.calculateScore` algorithm which triangulates the Provider's Lat/Lon against the User's Lat/Lon to rank the list by proximity and rating dynamically.

---
*(Note: Sections 4.4 through 11 should be filled out subsequently based on your specific diagrams, tests, and methodologies.)*

