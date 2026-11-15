# TeamUp – Campus Activity & Team Management System

TeamUp is a Java-based desktop application designed to help university students discover campus activities, create events, and connect with other students who want to participate.

The application provides a centralized platform for managing student activities, handling participation requests, and organizing teams through a simple and user-friendly interface.

## Features

### 1. Student Welcome & Identification
- Enter student name and SRM registration number.
- Validate student registration numbers.
- Switch between demo student profiles.
- Store student information in the local database.

### 2. Home Page
- Explore the TeamUp platform through a modern home page.
- View activity categories and featured activities.
- Discover how the platform works.
- Browse platform information and FAQs.

### 3. Explore Activities
- Browse available campus activities.
- Search activities dynamically.
- Filter activities by category.
- View activity details.

### 4. Create & Manage Activities
- Create new campus activities.
- Edit activities created by the student.
- Delete hosted activities.
- Set activity details, schedules, and maximum capacity.

### 5. Join Activity System
- Submit requests to join activities.
- Prevent duplicate requests and self-joining.
- Allow organizers to accept or reject requests.
- Track pending, accepted, and rejected requests.
- Enforce activity capacity limits.
- Automatically update activity status when capacity is reached.

### 6. Activity Details & Organizer Contact
- View complete activity information.
- View participant details.
- Access organizer contact information.
- Use email and phone contact actions.

### 7. My Activities
- View activities hosted by the student.
- Manage activities created by the student.
- Access activity management options.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java 21 | Core application development |
| JavaFX 21 | Desktop graphical user interface |
| JavaFX CSS | Application styling |
| Maven | Dependency and build management |
| SQLite | Local database storage |
| JDBC | Database connectivity |
| Ikonli | Icons and visual elements |
| Git & GitHub | Version control |

## Project Structure

```text
TeamUp/
│
├── .mvn/
│   └── wrapper/
│
├── lib/
│   └── Runtime dependency JAR files
│
├── src/
│   └── teamup/
│       ├── Main.java
│       ├── MainApp.java
│       │
│       ├── db/
│       │   ├── DatabaseManager.java
│       │   └── Database verification classes
│       │
│       ├── model/
│       │   ├── Activity.java
│       │   ├── ContactValidator.java
│       │   ├── JoinRequest.java
│       │   ├── JoinResult.java
│       │   ├── Student.java
│       │   ├── StudentSession.java
│       │   └── StudentValidator.java
│       │
│       └── ui/
│           ├── MainWindow.java
│           ├── PageNavigator.java
│           ├── UITheme.java
│           │
│           ├── components/
│           ├── pages/
│           ├── images/
│           └── styles.css
│
├── pom.xml
├── mvnw
├── mvnw.cmd
├── run.bat
├── compile_and_verify.bat
├── .gitignore
└── README.md
```

## Getting Started

### Prerequisites

- Windows 10 or Windows 11
- Java Development Kit (JDK) 21
- Internet connection for the initial Maven Wrapper setup, if Maven needs to be downloaded

### Run the Application

1. Clone the repository:

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

2. Open the project folder:

```bash
cd TeamUp
```

3. Run the application:

```powershell
.\run.bat
```

### Compile and Verify

To compile the project and run its verification checks:

```powershell
.\compile_and_verify.bat
```

## Database

TeamUp uses SQLite for local data storage.

- Student information is stored locally.
- Activities and join requests are maintained in the database.
- Database operations use JDBC.
- The database file is generated locally at `data/teamup.db`.

The local database is excluded from version control.

## Application Scope

TeamUp is an academic desktop application prototype developed to demonstrate Java programming, object-oriented concepts, JavaFX interface development, database connectivity, and application workflows.

The application uses a local student identification system for demonstration. It does not implement password-based authentication or a production cloud backend.

## Future Enhancements

- Additional activity management capabilities.
- Improved reporting and analytics.
- Further usability and interface improvements.

## Author

**Harish Kumar Chappati**

B.Tech – Computer Science and Engineering  
SRM University-AP

---

*TeamUp – Discover Activities. Connect with People. Build Together.*