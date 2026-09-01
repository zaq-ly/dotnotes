# Security Policy

## Supported Versions

Only the latest release version of **.notes (dotnotes)** is actively supported with security fixes.

| Version | Supported          |
| ------- | ------------------ |
| 1.21.x  | :white_check_mark: |
| < 1.20  | :x:                |

## Security Architecture & Guarantees

1. **Offline-First & Local Storage**: Notes are stored directly in the device's private SQLite database managed via Room. No data is sent over the network unless explicitly enabled via Cloud Sync.
2. **Network Security**: All network communications (Supabase sync and GitHub update check) strictly enforce TLS 1.3 / HTTPS. Cleartext HTTP traffic is disabled application-wide.
3. **Backup Protection**: Physical device USB extraction (`adb backup`) is explicitly disabled to protect local note privacy.
4. **Cloud Isolation (RLS)**: Cloud synchronization uses Supabase Row Level Security (RLS), cryptographically isolating user notes using JWT authentication.

## Reporting a Vulnerability

If you discover a potential security vulnerability in **.notes**, please report it responsibly:

1. **Do not create a public GitHub issue** for undisclosed security vulnerabilities.
2. Please disclose via private email to the maintainer or use the GitHub Private Vulnerability Reporting feature under the **Security** tab.
3. Please provide a detailed summary of the vulnerability, reproduction steps, and potential impact.

We appreciate your assistance in keeping **.notes** safe and privacy-respecting for everyone.
