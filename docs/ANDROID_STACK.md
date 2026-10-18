TC Lab — Android Technology Stack

1. Purpose

This document records the initial technology choices for TC Lab, a native, offline-first Android application for plant tissue culture learning, laboratory records, experiment management, and scientific analysis.

2. Technology Decisions

- Programming language: Kotlin.
- User interface: Jetpack Compose with Material 3.
- Application type: Native Android application.
- Minimum Android version: Android 8.0 (API 26), subject to build and device compatibility testing.
- Build system: Gradle with Kotlin DSL.
- Android build plugin: Android Gradle Plugin, using a mutually compatible, pinned version.
- Local database: SQLite through AndroidX Room.
- Database migrations: Explicit, versioned Room migrations that preserve existing user data.
- Background work: AndroidX WorkManager where suitable.
- Reminders: Android notifications and scheduled work, respecting notification permissions and Android background-execution restrictions.
- Photographs and attachments: App-private file storage, with metadata and references stored in Room.
- Theme: Dark-first interface with clear navigation, readable text, and accessible controls.
- Build automation: GitHub Actions to compile the app and publish APK artifacts for testing.

3. Offline-First Requirements

TC Lab must support its core record-keeping features without an internet connection.

Plant records, protocol notes, chemical inventory, medium formulations, equipment, batches, observations, task history, and experiment results must persist locally.

Internet access may later support research retrieval, optional synchronization, and source updates. Core local records must not depend on an active internet connection.

4. Scientific Data Integrity

The application must distinguish published evidence, user measurements, derived calculations, and educational suggestions.

Protocol versions used by existing experiments must remain identifiable. Database migrations, imports, and restores must not silently discard or overwrite experimental records.

5. Safety and Reliability

Chemical safety information and protocol limitations must be clearly presented. The app must not imply that tissue culture automatically produces pathogen-free plants.

Reminder schedules must use the phone's local time zone for display, preserve task history, and recover pending schedules where Android permits.

6. Build and Release Requirements

- Keep the Android source code in the TC Lab repository.
- Pin compatible build-tool versions rather than relying on changing defaults.
- Build a debug APK through GitHub Actions.
- Install and test the APK on the target Android phone.
- Test offline persistence, app restart, reminders, database migrations, and backup restoration before release.
- Keep signing credentials and secret keys out of public source files.

7. Decision Status

These are the initial technology decisions. Exact dependency versions, package name, Gradle configuration, and project structure must be confirmed during project creation and verified by a successful build.

No application code is implemented by this document alone.
