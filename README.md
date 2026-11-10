<div align="center">

# LoopATempo

**An Android app for building daily streaks, from checklists you tick off and apps you use every day**

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-29-blue)
![License](https://img.shields.io/badge/License-MIT-lightgrey?logo=mit)
![GitHub release](https://img.shields.io/github/v/release/galaxyjammed/LoopATempo?logo=github&label=GitHub%20release)
![Project Status](https://img.shields.io/badge/Project%20Status-WIP-orange)

⭐ **If LoopATempo helps you, please star this repository. It helps other people find the project.**

</div>

## What is LoopATempo?

LoopATempo helps you keep streaks going. You can add two kinds of habits:

- **Custom tasks**, such as "Drink 10 cups of water", which you check off yourself each day.
- **App habits**, which count automatically when you spend enough time in an app, such as a fitness or reading app.

Each habit builds a streak of consecutive days. Missing a day resets it to zero. Everything stays on your device.

<div align="center">
  <img width="100" height="100" alt="FoEcho icon" src="https://github.com/user-attachments/assets/d3ed7073-eb95-4abf-ad3d-6c73897330ea" />
  <br>
  <em>"Your everyday tasks, now with more purpose"</em>
</div>

## Features

### ✅ Habits and Streaks
- Tap **+** to add a custom task or pick an installed app
- Custom tasks have a checkbox. App habits are completed automatically from your usage
- An app counts once you've used it past its time limit. The default is **5 minutes**, which you can change in Settings
- Each app habit can have its own time limit and name, set in the **Edit** tab
- Custom tasks can have an icon, from the built-in set or from your gallery
- Streaks show on Home and in Analytics

### 🏠 Home
- Your habits as a checklist, with today's progress in the header
- **Filter** button above the Profile tab, with two groups you can combine:
  - **Main:** Group by type (apps or custom tasks first), or by completion (completed or not completed first). Turn Main on or off with its switch.
  - **Sub:** Sort by streak (highest or lowest), alphabetically, or by date created
- Your filter choice is remembered when you reopen the app

### ✏️ Edit
- The same list with a menu on each habit: **Edit** or **Delete**
- Edit a name and time limit for app habits, or a name and icon for custom tasks
- Deleting a habit also deletes its history, after a confirmation

### 📊 Analytics
- **Best streak on app** and **best streak on custom task**
- **Check-ins by weekday**, with your busiest day highlighted
- **Monthly review:** a calendar where each day is shaded by how many check-ins it has. Pick any earlier month from the calendar button, with check-in and active-day totals for that month
- **Summary:** total check-ins, active days, the number of apps and custom tasks, and your all-time most productive weekday

### 🏆 Profile
- **Milestones:** eight achievements, from your first check-in to a 100-day streak or a perfect day
- **Fix:** appears only when something needs attention, such as battery restrictions or blocked notifications. It walks you through each fix
- **About:** the app version, plus release notes from this repository's GitHub releases
- **Settings**

### ⚙️ Settings
- **Appearance:** dark or light mode
- **Tasks:**
  - Default streak time for apps
  - **Keep tracking alive**: on shows a silent notification and checks about every 30 seconds. Off, with no notification, checks about every 15 minutes
- **Reminders:** an optional notification when an app streak is completed
- **Backup:** export all habits, check-ins and settings to a file, or import a file to replace everything on the phone

### 🔔 Background Tracking
- Tracking keeps running after you close the app, as long as Usage access is on
- If **Keep tracking alive** is on, tracking restarts after the phone reboots
- Some phones, especially Xiaomi, Huawei, and some Samsung models, stop background apps unless you allow Autostart. The Fix screen explains how to change this

## Setup

1. Download `loopatempo{vX.Y.Z}.apk` from the [Releases](../../releases) page
2. Open the file. If Android asks, allow installs from your browser or file manager
3. Open LoopATempo and tap **Open Usage access settings**. Turn on LoopATempo. This is required to detect app use
4. Allow notifications when prompted
5. Optional: go to **Profile → Settings → Tasks** and check **Keep tracking alive** on/off depending on how you want to use the app
6. Check **Profile → Fix**. Turn off battery restrictions for LoopATempo if it's listed

## Permissions and Privacy

- **Usage access** (`PACKAGE_USAGE_STATS`) is required to see when you use other apps. Only the daily streak result is saved, not the usage times.
- **Notifications** (`POST_NOTIFICATIONS`) are used for streak alerts and the Keep tracking alive status.
- **Foreground service** (`FOREGROUND_SERVICE`) keeps tracking running when Keep tracking alive is on.
- **Ignore battery optimisations** (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) helps tracking keep running. You can turn this on from the Fix screen.
- **Receive boot completed** (`RECEIVE_BOOT_COMPLETED`) restarts tracking after a reboot, if Keep tracking alive is on.
- **Internet access** (`INTERNET`) is used for one optional request, when you open **Profile → About**, to read the public release notes from GitHub. Nothing about you or your data is sent.

Your habits, check-ins, settings and backups stay on your device. LoopATempo has no accounts, analytics, advertising, or crash reporting.

## Built With

- [Kotlin](https://kotlinlang.org/) and [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- [Room](https://developer.android.com/jetpack/androidx/releases/room) for the local database, with versioned migrations
- [WorkManager](https://developer.android.com/jetpack/androidx/releases/work) for background checks
- `UsageStatsManager` for reading app usage
- Android foreground services and `NotificationManager` for tracking and alerts
- `SharedPreferences`-free storage: all preferences are saved in the Room database

## ❓ Frequently Asked Questions

### Does LoopATempo work offline?
Yes. Everything works without an internet connection. Only **Profile → About** needs internet, to show release notes. It shows a message and a retry button when you're offline.

### Why does LoopATempo need Usage access?
Android only tells apps which other apps you've used if you grant this permission. LoopATempo uses it to count app habits. It doesn't save how long you used each app.

### Why are app streaks sometimes late?
With Keep tracking alive off, LoopATempo checks usage about every 15 minutes, and Android may delay checks further to save battery. Turn on Keep tracking alive for checks about every 30 seconds.

### Why do I see a notification when Keep tracking alive is on?
Android requires a notification while a background service runs, so tracking can continue. It's silent and low priority. Turn Keep tracking alive off if you'd rather not see it.

### My streaks stop working after a restart. What do I do?
Some phones block apps from starting at boot. Open **Profile → Fix**, then turn on Autostart for LoopATempo in your phone's settings.

### Will updating keep my data?
Yes, if the new version is signed with the same key as the one you installed. Use **Settings → Backup → Export backup** before big changes, just in case.

### Is LoopATempo on the Google Play Store?
No. It's distributed through GitHub releases only.

## Bug Reports and Suggestions

- Report bugs or suggest features by opening an [issue](../../issues) or [discussion](https://github.com/GalaxyJammed/LoopATempo/discussions)

## License

MIT

</div>
