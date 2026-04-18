# Workout Tracker

A simple and intuitive Android application designed to help users log their exercises, track their progress, and view summaries of their fitness activities.

## 🚀 Features

- **User Authentication**: Secure multi-user support with Sign In and Sign Up screens.
- **In-Memory Storage**: Fast performance with RAM-based storage (data clears on app restart).
- **Workout Logging**: Easily add, edit, and delete workout entries specific to each user.
- **Track Details**: Log exercise name, sets, reps, weight, and category (Chest, Legs, Back, Arms).
- **Navigation Drawer**: Quick access to Dashboard, Add Workout, Summary, and Profile.
- **Profile Management**: Customize your profile with a name, age, gender, and a profile picture from your gallery.
- **Progress Summary**: Get personal insights into your total workouts, sets, reps, and estimated calories burned.
- **Sleek UI**: Modern interface with a navigation drawer and Material Design components.

## 🛠️ Technologies Used

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Platform**: Android
- **UI Architecture**: ViewBinding, RecyclerView, DrawerLayout, NavigationView
- **Development Environment**: Android Studio

## 📂 Project Structure

```text
app/src/main/main/java/com/example/workouttracker/
├── adapter/          # RecyclerView adapters
├── data/             # Repositories (AppRepository for users, WorkoutRepository for data)
├── model/            # Data models (Workout and User classes)
├── AuthActivity.kt   # Authentication entry point
├── SignInActivity.kt # User login
├── SignUpActivity.kt # User registration
├── MainActivity.kt   # Dashboard with Navigation Drawer
├── ProfileActivity.kt # User profile management
├── SummaryActivity.kt # Personal progress statistics
└── SplashActivity.kt  # Initial loading screen
```

## 📋 Prerequisites

- Android Studio Flamingo | 2022.2.1 or newer
- JDK 17
- Android Device or Emulator (API level 24+)

## ⚙️ Getting Started

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   ```
2. **Open the project**:
   Launch Android Studio and select **Open** to navigate to the project directory.
3. **Sync Gradle**:
   Wait for the project to sync and download all necessary dependencies.
4. **Run the App**:
   Click the **Run** button in the toolbar or press `Shift + F10`.

## 📊 Estimated Calories Formula

The app uses a simple volume-based calculation for estimated calories burned:
`Estimated Calories = Total Volume (Sets * Reps * Weight) * 0.1`

## 📝 Recent Updates

### 🔐 Multi-User System
- **Authentication Flow**: Replaced the static welcome screen with a full authentication system (`AuthActivity`, `SignInActivity`, `SignUpActivity`).
- **User-Specific Data**: Integrated a `workoutMap` in the repository to ensure each user only sees and manages their own workouts.
- **Form Validation**: Added robust client-side validation for emails, passwords (complexity requirements), and required fields.

### 🧭 Navigation & UI
- **Navigation Drawer**: Implemented a Material Design navigation drawer for quick access to app features.
- **Dynamic Header**: The drawer header automatically syncs with the logged-in user's profile picture, name, and email.
- **Modernized Layouts**: Updated `MainActivity` to support drawer interactions and improved empty-state visibility.

### 👤 Profile Management
- **Personalization**: Added a `ProfileActivity` where users can update their age, gender, and name.
- **Gallery Integration**: Users can now select and set a custom profile picture directly from their device gallery.
- **Persistent Session**: The app maintains the `currentUser` state throughout the session until logout.

### 🛠️ Technical Improvements
- **In-Memory Architecture**: Refactored the data layer to use a singleton `AppRepository` for managing state without a database.
- **Security & Flow**: Implemented back-stack clearing on login/logout to prevent unauthorized access via the back button.
- **Permissions**: Added necessary storage permissions for media access.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---
*Created as part of a MAD (Mobile Application Development) project.*
