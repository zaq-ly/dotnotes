# PRD — .notes

## 1. Overview
Nama aplikasi: .notes
Aplikasi Android untuk membuat **catatan, reminder, alarm**, serta integrasi **Gmail dan Google Calendar**.

Konsep utama:

**Note → Reminder → Alarm + Gmail + Calendar**

User dapat membuat catatan biasa atau mengubahnya menjadi reminder dengan tanggal, jam, prioritas, dan integrasi tambahan.

---

## 2. Core Features

### Notes
- Create, edit, delete note
- Title + content
- Search
- Pin/archive
- Category/tag

### Reminder
- Convert note → reminder
- Date + time
- Repeat
- Priority: Low / Normal / High / Urgent
- Enable/disable alarm
- Snooze
- Done/dismiss

### Alarm
- Android `AlarmManager`
- Notification
- Sound/vibration
- Tetap bekerja saat aplikasi ditutup
- Reschedule setelah device restart

### Gmail
Opsional per reminder.

User dapat memilih:
- Gmail ON/OFF
- Recipient
- Send time

Contoh:

`Reminder aktif → Alarm + Gmail`

### Google Calendar
Opsional per reminder.

- Create Calendar event
- Update event
- Delete event
- Simpan `calendarEventId`

### Priority Mode

User dapat memilih:

```text
Normal   → Alarm
High     → Alarm + Notification
Urgent   → Alarm + Gmail + Calendar
Custom   → User menentukan sendiri
```

---

## 3. Main Screens

```text
Home
├── Today's Reminders
├── Upcoming
└── Recent Notes

Notes
├── All
├── Pinned
└── Categories

Create/Edit Note

Reminder
├── Date
├── Time
├── Repeat
├── Priority
├── Alarm
├── Gmail
└── Calendar

Settings
├── Notifications
├── Gmail
├── Calendar
├── Theme
└── Account
```

---

## 4. Architecture

Gunakan:

**MVVM + Repository + Offline First**

```text
Compose UI
    ↓
ViewModel
    ↓
Repository
    ├── Room
    ├── Supabase
    ├── Gmail API
    └── Calendar API
```

Reminder:

```text
Room
 ↓
AlarmManager
 ↓
BroadcastReceiver
 ↓
Notification
```

---

## 5. Tech Stack

### Android
- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- ViewModel
- StateFlow
- Hilt
- Room
- DataStore
- Retrofit + OkHttp
- WorkManager
- AlarmManager

### Backend — Rp0 MVP
**Supabase Free**

Digunakan untuk:
- Authentication
- PostgreSQL
- Cloud sync
- Row Level Security
- Edge Functions jika diperlukan

### Google
- Google OAuth
- Gmail API
- Google Calendar API

### Development
- Android Studio
- GitHub Free

---

## 6. Database

### Note

```text
id
userId
title
content
category
isPinned
isArchived
createdAt
updatedAt
```

### Reminder

```text
id
noteId
scheduledAt
priority
repeat
alarmEnabled
gmailEnabled
calendarEnabled
gmailRecipient
calendarEventId
status
```

---

## 7. Offline First

Catatan dan alarm **harus tetap bekerja tanpa internet**.

```text
User
 ↓
Room
 ↓
AlarmManager
```

Jika internet tersedia:

```text
Room
 ↓
Sync Queue
 ↓
Supabase
```

Gmail dan Calendar membutuhkan koneksi internet.

---

## 8. Authentication

Gunakan **Google OAuth**.

Permission Gmail hanya diminta jika user mengaktifkan Gmail.

Permission Calendar hanya diminta jika user mengaktifkan Calendar.

Gunakan prinsip **least privilege**.

---

## 9. MVP

### Phase 1
- Notes
- Room
- Search
- Pin/archive

### Phase 2
- Reminder
- AlarmManager
- Notification
- Snooze
- Priority

### Phase 3
- Google Login
- Gmail API
- Calendar API

### Phase 4
- Supabase
- Cloud sync
- Sync queue

Fitur AI, collaboration, WhatsApp, iOS, dan fitur kompleks lainnya **di luar MVP**.

---

## 10. Target Biaya

**Rp0 untuk MVP**, selama masih dalam free tier.

| Service | Cost |
|---|---:|
| Kotlin | Rp0 |
| Android Studio | Rp0 |
| Compose | Rp0 |
| Room | Rp0 |
| Supabase Free | Rp0 |
| Gmail API | Rp0 |
| Calendar API | Rp0 |
| GitHub Free | Rp0 |

---

## 11. Core User Flow

```text
Create Note
    ↓
Create Reminder
    ↓
Set Date + Time
    ↓
Set Priority
    ↓
Choose:
Alarm / Gmail / Calendar
    ↓
Save
    ↓
Alarm triggered
    ↓
Notification
    ↓
Optional Gmail + Calendar
```

## Product Goal

Membuat satu tempat untuk mengubah **catatan menjadi tindakan yang benar-benar mengingatkan user**, tanpa bergantung pada internet untuk fungsi alarm utama.

**Stack final: Kotlin + Compose + Room + AlarmManager + Supabase + Gmail API + Google Calendar API.**
