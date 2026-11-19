# Security and privacy

This Android app intentionally has no `android.permission.INTERNET`, no analytics SDK,
no user account system, and no private cloud. The entire art generation engine runs on the device.

- Favorites and preference votes stay in Android private SharedPreferences.
- Export writes a PNG into `Pictures/Intelligent Kunst` through MediaStore only on user action.
- Sharing requires a separate user confirmation and uses the Android share sheet.
- DNA import is strictly parsed into numeric parameters and enumerated styles; no code evaluation.
- There are no secrets or backend keys in the project.

For vulnerability reports, open a private security advisory in the repository if available.
