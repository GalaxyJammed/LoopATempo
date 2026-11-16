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

- **Custom tasks**, such as "Drink 10 cups of water", which you check off yourself each day. A custom task can also link to a website, and visiting that site can check it off automatically.
- **App habits**, which count automatically when you spend enough time in an app, such as a fitness or reading app.

Each habit builds a streak of consecutive days. Missing a day resets it to zero, unless you have a rest day to cover it. Everything stays on your device.

<div align="center">
  <img width="100" height="100" alt="LoopATempo icon" src="https://github.com/user-attachments/assets/d3ed7073-eb95-4abf-ad3d-6c73897330ea" />
  <br>
  <em>"Your everyday tasks, now with more purpose"</em>
</div>

## Features

### ✅ Habits and Streaks
- Tap **+** to add a custom task or pick an installed app
- Every habit has a checkbox. App habits are ticked automatically from your usage, so tapping their checkbox reminds you to open the app
- An app counts once you've used it past its time limit. The default is **5 minutes**. Set it to **0** to count the moment you open the app
- Each app habit can have its own time limit and name, set in the **Edit** tab
- Custom tasks can have an icon, from the built-in set or from your gallery
- Custom tasks can have a **website**. A website icon appears on the task until you choose your own icon
- Streaks show on Home and in Analytics

### 🔥 Rest Days
- Every 5 habits you complete in one day earns one rest day, up to 3 at a time
- A rest day is used automatically on a day when a streak would break, and it covers every habit you missed that day
- Your streak continues on a covered day, but that day doesn't add to the count
- Rest days appear as blue flames on Home

### ⏰ Habit Reminders
- Turned on by default, in **Settings → Reminders**, at 20:00
- Set a different time for any habit in **Edit**, or choose a time when you create a custom task
- When a reminder is due, the notification lists the habits you haven't completed in that hour
  - One habit: "You haven't completed your "x" task!" with a **Mark as done** button, or **Open app** for app habits
  - Several habits: "You haven't completed a bunch of tasks!" with **Mark all custom tasks as done**
- Android may delay reminders by a few minutes to save battery

### 🌐 Website Tasks
- Add a website to a custom task, for example `youtube.com`
- Tap the external link icon on Home to open the site
- **Manual check:** tick the task yourself, like any other custom task
- **Website auto-check** (optional, off by default): when you visit the site in a supported browser, the task is checked and you get a notification. See [Website auto-check](#website-auto-check) below

### ✏️ Edit
- The same list with a menu on each habit: **Edit** or **Delete**
- Edit a name and time limit for app habits, or a name, icon, website and reminder for custom tasks
- Deleting a habit shows an **Undo deletion** bar for 5 seconds. If you don't undo it, the habit and its history are removed

### 🏠 Home
- Your habits as a checklist, with today's progress in the header
- **Filter** button above the Profile tab, with two groups you can combine:
  - **Main:** Group by type (apps or custom tasks first), or by completion (completed or not completed first). Turn Main on or off with its switch.
  - **Sub:** Sort by streak (highest or lowest), alphabetically, or by date created
- Your filter choice is remembered when you reopen the app
- Each app streak shows a 🔥 next to its count

### 📱 Home Screen Widget
- Add **Task List** from your launcher's widget picker
- Custom tasks show a checkbox you can tap from the home screen
- App tasks show ↗. Tapping one opens that app
- Settings in **Settings → Widget**:
  - **Hide completed tasks** leaves finished tasks out, for both custom and app tasks
  - **Cross out completed tasks** adds a strikethrough to finished tasks
  - **Widget filter** lists not-completed tasks first, with a labelled "Not completed" and "Completed" line between them

### 📊 Analytics
- **Best streak on app** and **best streak on custom task**
- **Check-ins by weekday**, with your busiest day highlighted
- **Monthly review:** a calendar where each day is shaded by how many check-ins it has. Pick any earlier month from the calendar button, with check-in and active-day totals for that month, and that month's most productive weekday
- **Summary:** total check-ins, active days, the number of apps and custom tasks, and your all-time most productive weekday. If there isn't enough data to show a clear weekday, it says "Not enough data". Ties show every tied day, such as `Mon/Fri`

### 🏆 Profile
- **Milestones:** 15 achievements, from your first check-in to a 180-day streak, a 10-habit routine, or checking in on all seven weekdays. Each locked milestone shows progress
- **Fix:** appears only when something needs attention, such as battery restrictions or blocked notifications. It walks you through each fix
- **About:** the app version, release notes from this repository's GitHub releases, and a privacy summary
- **Settings**

When a newer version is released, Profile shows a red **!** instead of the usual icon. Tapping it opens About, with an **Update now** button that downloads the new APK and opens Android's installer, and a **View Changelog** button.

### ⚙️ Settings
- **Appearance:** dark or light mode
- **Tasks:**
  - **Default streak time** for apps, where 0 means instant
  - **Keep tracking alive:** on shows a silent notification and checks about every 30 seconds. Off, with no notification, checks about every 15 minutes
  - **Website auto-check:** see below
- **Reminders:**
  - **Habit reminders**, on by default
  - **Default reminder time**, shown only while habit reminders are on
  - **Streak completed alerts**, an optional notification when an app streak is completed or a website task is checked
- **Widget:** hide completed tasks, cross out completed tasks, and the widget filter
- **Backup:** export all habits, check-ins and settings to a file, or import a file to replace everything on the phone

### Website Auto-check

Website auto-check is optional. It uses Android's Accessibility service, and you turn it on in **Settings → Tasks**. It reads the address bar of these browsers only:

Chrome, Edge, Brave, Firefox, Samsung Internet and Opera

It compares only the domain, such as `youtube.com`, with your website tasks. It never stores URLs or browsing history.

Android turns off Accessibility services after every app update. When that happens, LoopATempo shows a red warning on Profile and in Settings. Turn the service back on in Accessibility settings to clear it.

### 🔔 Background Tracking
- Tracking keeps running after you close the app, as long as Usage access is on
- If **Keep tracking alive** is on, tracking restarts after the phone reboots
- Some phones, especially Xiaomi, Huawei, and some Samsung models, stop background apps unless you allow Autostart. The Fix screen explains how to change this

## Setup

1. Download `loopatempo{vX.Y.Z}.apk` from the [Releases](../../releases) page
2. Open the file. If Android asks, allow installs from your browser or file manager
3. Open LoopATempo and tap **Open Usage access settings**. Turn on LoopATempo. This is required to detect app use
4. Allow notifications when prompted
5. Optional: in **Profile → Settings → Tasks**, choose whether to keep tracking alive
6. Optional: in **Settings → Tasks**, turn on **Website auto-check** and enable LoopATempo in Accessibility settings
7. Check **Profile → Fix**. Turn off battery restrictions for LoopATempo if it's listed
8. Optional: long-press your home screen, open Widgets, and add **Task List**

## Permissions and Privacy

- **Usage access** (`PACKAGE_USAGE_STATS`) is required to see when you use other apps. Only the daily streak result is saved, not the usage times.
- **Notifications** (`POST_NOTIFICATIONS`) are used for habit reminders, streak alerts, and the Keep tracking alive status.
- **Foreground service** (`FOREGROUND_SERVICE`) keeps tracking running when Keep tracking alive is on.
- **Ignore battery optimisations** (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) helps tracking keep running. You can turn this on from the Fix screen.
- **Receive boot completed** (`RECEIVE_BOOT_COMPLETED`) restarts tracking and reminders after a reboot.
- **Internet access** (`INTERNET`) is used for:
  - reading public release notes and checking for updates from GitHub, when you open About and on launch
  - downloading an update when you tap **Update now**
  - downloading `favicon.ico` from the websites in your website tasks, to show their icons
- **Install packages** (`REQUEST_INSTALL_PACKAGES`) lets LoopATempo open the installer for a downloaded update.
- **Accessibility service** (optional) reads the address bar of the supported browsers listed above, only when Website auto-check is on.

Your habits, check-ins, settings and backups stay on your device. LoopATempo has no accounts, analytics, advertising, or crash reporting. Full details are in [PRIVACY.md](PRIVACY.md).

## Built With

- [Kotlin](https://kotlinlang.org/) and [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3
- [Room](https://developer.android.com/jetpack/androidx/releases/room) for the local database, with versioned migrations
- [WorkManager](https://developer.android.com/jetpack/androidx/releases/work) for background checks
- `UsageStatsManager` for reading app usage
- Android foreground services, `AlarmManager` and `NotificationManager` for tracking, reminders and alerts
- `AccessibilityService` for optional website auto-check
- `AppWidgetProvider` and `RemoteViews` for the home screen widget
- `DownloadManager` for update downloads
- All preferences, including widget and filter settings, are saved in the Room database

## ❓ Frequently Asked Questions

### Does LoopATempo work offline?
Yes. Habits, streaks, reminders, rest days and the widget all work without an internet connection. The network is used only for release notes, update checks and downloads, and website icons.

### Why does LoopATempo need Usage access?
Android only tells apps which other apps you've used if you grant this permission. LoopATempo uses it to count app habits. It doesn't save how long you used each app.

### Why are app streaks sometimes late?
With Keep tracking alive off, LoopATempo checks usage about every 15 minutes, and Android may delay checks further to save battery. Turn on Keep tracking alive for checks about every 30 seconds.

### Why do I see a notification when Keep tracking alive is on?
Android requires a notification while a background service runs, so tracking can continue. It's silent and low priority. Turn Keep tracking alive off if you'd rather not see it.

### Why does a website task not check off when I visit the site?
Check that Website auto-check is on and that LoopATempo is enabled in Accessibility settings. Visits only count in the browsers listed above. The site's domain must match the website you entered, such as `youtube.com`, and the task is checked once per day.

### Why did Website auto-check turn off after an update?
Android turns off Accessibility services when an app is updated. Turn LoopATempo back on in Accessibility settings. The red warning on Profile disappears once it's on again.

### How do I earn rest days?
Complete 5 habits in one day to earn one rest day. You can hold up to 3. They're used automatically when a streak would otherwise break.

### Is LoopATempo on the Google Play Store?
No. It's distributed through GitHub releases only.

## Known Limitations

- The tracking service doesn't restart after a reboot on some phones until the app is opened once.
- Some phone makers close background apps aggressively, even with the Fix steps.
- Reminders and streak checks can be a few minutes late, depending on battery saving.
- Website auto-check depends on each supported browser's address bar. A browser update can change it, which stops auto-check until the app is updated.

## Bug Reports and Suggestions

- Report bugs or suggest features by opening an [issue](../../issues) or [discussion](https://github.com/GalaxyJammed/LoopATempo/discussions)
- When reporting a bug, include your Android version, your phone's brand, and what the screen shows

## License

MIT. See [LICENSE](LICENSE).

</div>
