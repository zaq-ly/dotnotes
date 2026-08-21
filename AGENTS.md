# AGENTS.md — .notes

Dokumen ini dibaca AI coding agent **di awal tiap sesi kerja** di project ini. Jangan mulai coding sebelum baca dokumen relevan di bawah.

---

## 1. Dokumen Referensi (baca sesuai kebutuhan task)

| Dokumen | Isi | Kapan dibaca |
|---|---|---|
| `PRD-dotnotes.md` | Fitur, user flow, skema data, tech stack, scope | Sebelum bikin fitur baru |
| `design-system.md` | Warna, tipografi, spacing, komponen | Sebelum bikin/ubah UI |
| `ARCHITECTURE.md` | Struktur folder, layering, state management, offline-sync, reminder system | Sebelum ubah struktur project atau logic inti |
| `RELEASE-WORKFLOW.md` | Cara kerja auto-release & versioning | Sebelum ubah workflow release atau `pubspec.yaml` versi |

---

## 2. Aturan Wajib

- **Jangan generate/buat asset logo** (app icon, splash) — dikelola manual oleh developer, sudah dicatat di PRD & design-system.
- **Jangan hardcode warna/ukuran/teks** — pakai token dari `design-system.md` dan string dari `.arb` (localization).
- **Jangan taruh Supabase call langsung di widget** — ikuti alur `UI → Notifier → UseCase → Repository` (lihat `ARCHITECTURE.md` §3).
- **Jangan ubah versi di `pubspec.yaml` secara manual** — versi dikelola otomatis lewat `.github/workflows/release.yml` tiap commit ke `main` (lihat `RELEASE-WORKFLOW.md`).
- **Jangan asumsi skema tabel/kolom baru** di luar yang tercantum di PRD/ARCHITECTURE — kalau butuh, tanya dulu.
- **Email reminder (level 2) tidak boleh bergantung pada device** — logic-nya di Supabase Edge Function + `pg_cron`, bukan di app.

---

## 3. Kalau Ragu

Kalau ada instruksi yang tidak jelas atau bertentangan dengan dokumen di atas, **berhenti dan tanya** — jangan menebak atau berasumsi demi menyelesaikan task lebih cepat.
