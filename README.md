# 🏋️ FitPlanner
Online Fitness Program Planner & Workout Tracker

## 📌 Overview

FitPlanner is a web-based fitness application that allows users to create, manage, and track personalized workout programs.
The platform helps users organize their training, record completed workouts, and monitor progress over time.

The application is designed to be simple to use while still supporting structured and customizable training programs.

## 🧩 Application Flow

### 🏗️ Workout Planning

Users can create workout programs by selecting exercises from a shared exercise library.
Each program consists of training days, and each exercise can be configured with:
- 🧮 Sets
- 🔁 Repetitions
- ⚖️ Weight
- ⏱️ Rest time
- 📝 Personal notes

Programs can be saved for personal use or published as public or sample programs.

### 📚 Exercise Library

FitPlanner includes a shared exercise library containing:
- 🏷️ Exercise descriptions
- 🧠 Categories (muscle group, type)
- 📈 Difficulty level
- 🏋️ Required equipment
- 🎥 Images and videos

The exercise library is managed by administrators to ensure consistency and accuracy.

### ▶️ Workout Execution

During a workout session, users can:
- ▶️ Start a workout based on a saved program
- ✅ Mark exercises and sets as completed
- 📊 Record actual repetitions and used weights
- 📝 Add notes for each exercise

Each completed workout is stored as a workout session.

## 📊 Progress Tracking

The application provides tools for tracking fitness progress, including:
- 🗂️ Workout history
- 📈 Performance tracking per exercise
- 📆 Weekly and monthly summaries
- 🏆 Personal bests (planned feature)

These features help users analyze their performance and stay motivated.

## 👥 User Roles

### 👀 Anonymous
- Browse general information
- Preview exercises and sample programs

### 🧍 Registered User
- Create and manage workout programs
- Log workout sessions
- View personal statistics
- Edit profile and fitness goals

### 🧑‍🏫 Trainer
- Create public or sample workout programs
- View aggregated client statistics

### 🛠️ Administrator
- Manage exercises and categories
- Manage users and assign roles
- Moderate public programs

## 🖱️ Controls and Interaction

- The application is accessed through a web interface
- Built using HTML, CSS, JavaScript, and Thymeleaf
- Interaction is handled through forms, buttons, and UI components
- No special input devices are required

## 💾 Saving Progress

- User data is automatically stored in the database
- Users can log out and continue later without losing progress
- Session-based authentication ensures secure access

## ✨ Features

- 🏗️ Custom workout program builder
- 📚 Shared exercise library
- ▶️ Workout session logging
- 📊 Progress tracking and statistics
- 🔐 Role-based access control
- 🛠️ Administrative management tools
- 🔌 REST-based backend architecture

## 🧪 Technical Details

- ⚙️ Backend: Spring Boot (Java)
- 🎨 Frontend: Thymeleaf, HTML, CSS, JavaScript
- 🗄️ Database: Relational database with JPA/Hibernate
- 🏛️ Architecture: MVC with REST API
- ⚡ Real-time features prepared using SSE or WebSocket

## 🚧 Project Status

- ✅ Core backend functionality implemented
- 🎨 User interface under active development
- 📊 Statistics and real-time features planned
- 🎓 Developed as a university project

## 📝 Notes

- This project is intended for educational and portfolio purposes
- The codebase is modular and easy to extend
- Future improvements may include mobile support and advanced analytics
