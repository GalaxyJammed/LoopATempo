# LoopATempo Privacy

LoopATempo stores your habits, check-ins, settings and usage minutes on your device.

## Network access
The app connects to the internet in these cases only:

- **Release notes and update check.** On launch, and when you open **Profile → About**, the app reads the public release list for this app from GitHub (`api.github.com`). It uses this to show release notes and to tell you when a newer version is available. Nothing about you or your data is sent. GitHub receives the usual connection details, such as your IP address, as with any website visit.
- **Updates.** If you tap **Update now**, the app downloads the new version's APK from GitHub into your Downloads folder, then opens Android's installer.
- **Website icons.** When a custom task has a website, the app downloads that website's `favicon.ico` directly from the site. The site receives a normal request for its own icon. Icons are cached on your device.

## What the app reads and stores
- **App usage (Usage access).** LoopATempo reads which apps you open and for how long, so app habits can count.
  - It saves the total minutes per day **only for the apps you've added** as habits. These totals power the "Minutes per app" section of Analytics.
  - It doesn't save session times or any apps you haven't added.
  - Only the daily streak result is used for streaks.
- **Browser address bar (optional).** If you turn on **Website auto-check** and enable LoopATempo in Accessibility settings, the app reads the address bar of Chrome, Edge, Brave, Firefox, Samsung Internet and Opera. It only compares the domain with your website tasks. If it matches, a check-in is saved. Full URLs and browsing history are never stored or sent. Other apps and screen content are never read. Turn the setting off to stop this at any time.

## Background tracking
If "Keep tracking alive" is on, LoopATempo runs a background service so it can check app usage while you use other apps. Android shows a silent notification while this runs. When the phone restarts, the app restarts this service if the setting is on. You can turn it off in Settings → Tasks. The Fix screen in Profile shows whether tracking is running and what happened after your last restart.

## Reminders
Habit reminders are scheduled on your device. They don't use the internet.

## Share cards
When you tap a share button in Analytics, LoopATempo draws an image on your device and opens Android's share sheet. Nothing is sent until you choose an app to share with. The image is kept temporarily in the app's cache, which Android can clear at any time.

## Export and import
Export saves a backup file wherever you choose, such as Downloads or cloud storage you use yourself. The backup includes your habits, check-ins, settings and usage minutes. Import replaces everything in the app with the contents of a backup file you pick. The file is not encrypted, so anyone with it can read your habit names, website tasks, check-in history and usage minutes. Store it carefully.

## Other data
The app has no accounts, analytics, advertising, or crash reporting, and it does not send your data anywhere.

## Removing your data
- Delete habits in the Edit tab. This also deletes that app's saved minutes. Uninstalling the app removes all local data.
- Turn off Website auto-check, or remove LoopATempo from Accessibility settings.
- Delete any exported backup files, downloaded update APKs and shared images you no longer need.
