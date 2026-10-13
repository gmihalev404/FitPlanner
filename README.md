# FitPlanner

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-Server--Side%20UI-green)
![Gradle](https://img.shields.io/badge/Gradle-Build%20Tool-02303A)

**FitPlanner** is a full-stack fitness planning and workout tracking web application built with Java and Spring Boot.

Users can organize workout programs, schedule training sessions, record exercise performance, and review their training progress through a browser-based interface.

## Live Demo

**[Open FitPlanner](https://fitplanner.dns.army)**

The demo runs on Heroku with a hosted MySQL database. Availability may occasionally be affected by free-tier database limitations.

---

## Features

### Workout Programs
- Create and manage personalized workout programs.
- Select exercises from a shared exercise library.
- Configure training details such as sets, repetitions, and weights.
- Organize workouts across training days.

### Exercise Library
- Browse exercises with descriptions and categories.
- View exercise illustrations and supported media.
- Use exercises when building workout programs.

### Workout Sessions
- Schedule workouts and access them by date.
- Start a session from a workout program.
- Record performed sets, repetitions, and weights.
- Track exercise completion during a session.

### Progress and Statistics
- Review workout history and recorded performance.
- Follow training progress over time.
- Use calendar and statistics views to keep track of activity.

### User Experience
- User accounts and personalized workout data.
- Responsive browser interface with light and dark themes.
- Multilingual interface support.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Backend | Spring Boot 3.5.7, Spring MVC |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL |
| Frontend | Thymeleaf, HTML, CSS, JavaScript |
| Build Tool | Gradle |
| Media Storage | Cloudinary |
| Monitoring | Spring Boot Actuator |
| Hosting | Heroku |
| Testing | Spring Boot Test, JUnit, H2 |

---

## Architecture

FitPlanner uses a layered Spring Boot architecture:

```text
Browser
   |
   | HTTP
   v
Spring MVC Controllers
   |
   v
Services / Business Logic
   |
   v
Spring Data JPA Repositories
   |
   v
MySQL
```

Thymeleaf renders the web interface, while Cloudinary hosts uploaded media used by the application.

---

## Running Locally

### Requirements
- JDK 21
- MySQL
- Git

The repository includes the Gradle Wrapper, so a separate Gradle installation is not required.

### 1. Clone the repository

```bash
git clone https://github.com/gmihalev404/FitPlanner.git
cd FitPlanner
```

### 2. Configure the database

The default local configuration connects to:

```text
Host: localhost
Port: 3306
Database: FitPlanner
Username: root
```

Set the required environment variables, including `DB_PASSWORD`. You can override the database connection using `DB_URL` and `DB_USERNAME`.

The application also expects `ADMIN_CODE` and `TRAINER_CODE` configuration values. Media upload features may require additional Cloudinary credentials.

**Windows PowerShell:**

```powershell
$env:DB_PASSWORD="your_mysql_password"
$env:ADMIN_CODE="your_admin_code"
$env:TRAINER_CODE="your_trainer_code"
```

**Linux / macOS:**

```bash
export DB_PASSWORD="your_mysql_password"
export ADMIN_CODE="your_admin_code"
export TRAINER_CODE="your_trainer_code"
```

### 3. Start the application

**Windows:**

```powershell
.\gradlew.bat bootRun
```

**Linux / macOS:**

```bash
./gradlew bootRun
```

Open **http://localhost:8080**.

### Run Tests

**Windows:**

```powershell
.\gradlew.bat test
```

**Linux / macOS:**

```bash
./gradlew test
```

---

## Deployment

The live application is hosted on **Heroku**. Its MySQL database is hosted on **Aiven**, and exercise media is stored using **Cloudinary**.

Production configuration is supplied through environment variables rather than committing credentials to the repository. Spring Boot Actuator exposes a health endpoint for uptime monitoring.

---

## Project Status

FitPlanner is a deployed portfolio project with working workout planning and tracking functionality. Improvements to the exercise media library and user experience are ongoing.

The project was developed for learning and portfolio purposes, with a focus on Spring Boot, relational data modeling, server-rendered web interfaces, and production deployment.
