# LoopATempo Privacy

LoopATempo stores your habits, check-ins, and settings on your device.

## Network access
The app connects to the internet in these cases only:

- **Release notes and update check.** On launch, and when you open **Profile → About**, the app reads the public release list for this app from GitHub (`api.github.com`). It uses this to show release notes and to tell you when a newer version is available. Nothing about you or your data is sent. GitHub receives the usual connection details, such as your IP address, as with any website visit.
- **Updates.** If you tap **Update now**, the app downloads the new version's APK from GitHub into your Downloads folder, then opens Android's installer.
- **Website icons.** When a custom task has a website, the app downloads that website's `favicon.ico` directly from the site, so it can show the site's icon next to the task. The site receives a normal request for its own icon. Icons are cached on your device.

## What the app reads
- **App usage time** through Android's Usage access permission. It is used only to count daily app streaks. Usage times are not saved. Only the daily streak result is stored.
- **Browser address bar (optional).** If you turn on **Website auto-check** and enable LoopATempo in Accessibility settings, the app reads the address bar of Chrome, Edge, Brave, Firefox, Samsung Internet and Opera. It only compares the domain with your website tasks. If it matches, a check-in is saved. Full URLs and browsing history are never stored or sent. Other apps and screen content are never read. Turn the setting off to stop this at any time.

## Background tracking
If "Keep tracking alive" is on, LoopATempo runs a background service so it can check app usage while you use other apps. Android shows a silent notification while this runs. When the phone restarts, the app restarts this service if the setting is on. You can turn it off in Settings → Tasks.

## Reminders
Habit reminders are scheduled on your device. They don't use the internet.

## Export and import
Export saves a backup file wherever you choose, such as Downloads or cloud storage you use yourself. Import replaces everything in the app with the contents of a backup file you pick. The file is not encrypted, so anyone with it can read your habit names, website tasks and check-in history. Store it carefully.

## Other data
The app has no accounts, analytics, advertising, or crash reporting, and it does not send your data anywhere.

## Removing your data
- Delete habits in the Edit tab, or uninstall the app to remove all local data. Uninstalling also removes cached website icons.
- Turn off Website auto-check, or remove LoopATempo from Accessibility settings.
- Delete any exported backup files and downloaded update APKs you no longer need.
