# Design System — .notes

**Arah visual:** Minimalis (referensi: Notion, Google Keep)
**Accent color:** Monokrom (abu-abu netral), menggunakan palet Material 3

---

## 1. Logo & Identitas

- Ikon & Logo: Tipografi teks monokrom hitam-putih **.notes** dengan huruf 'o' dimodifikasi menyerupai lipatan halaman/kertas catatan (lihat `Logo dotnotes v.2.jpg`)
- Nama aplikasi ditulis lowercase dengan titik di depan: **.notes**
- **Asset logo dikelola manual oleh developer** (ditempatkan langsung di folder asset project) — AI coding agent tidak perlu/tidak boleh generate asset logo sendiri

---

## 2. Palet Warna

### Light Mode
| Token | Hex | Penggunaan |
|---|---|---|
| Background | `#FFFFFF` | Latar utama |
| Surface | `#F7F8FA` | Card, container |
| Text Primary | `#1A1A1A` | Judul, teks utama |
| Text Secondary | `#6B7280` | Deskripsi, metadata |
| Border | `#E5E7EB` | Garis tipis antar elemen |
| Accent (Primary) | `#6B7280` | Tombol utama, link, elemen aktif |

### Dark Mode
| Token | Hex | Penggunaan |
|---|---|---|
| Background | `#121212` | Latar utama |
| Surface | `#1E1E1E` | Card, container |
| Text Primary | `#EDEDED` | Judul, teks utama |
| Text Secondary | `#9CA3AF` | Deskripsi, metadata |
| Border | `#2D2D2D` | Garis tipis antar elemen |
| Accent (Primary) | `#6B7280` | Identik dengan light mode untuk konsistensi monokrom |

### Priority Indicator (dot/border tipis di card, bukan warna mencolok)
| Level | Warna | Hex |
|---|---|---|
| Notifikasi biasa | Abu netral | `#9CA3AF` |
| Notifikasi + Email | Kuning soft | `#F5B94C` |
| Notifikasi + Email + Alarm | Merah soft | `#EF6B6B` |

---

## 3. Tipografi

- **Font family:** Inter (fallback: Plus Jakarta Sans)
- **Weight yang dipakai:** Regular (400), Medium (500), SemiBold (600) — hindari Bold berat kecuali untuk judul besar

| Elemen | Size | Weight |
|---|---|---|
| Judul halaman (H1) | 24sp | SemiBold |
| Judul catatan (di card) | 16sp | Medium |
| Body / deskripsi | 14sp | Regular |
| Caption / metadata (tanggal, dll) | 12sp | Regular |
| Tombol | 14sp | Medium |

---

## 4. Spacing Scale

Gunakan skala kelipatan 4:
`4, 8, 12, 16, 24, 32, 48`

- Padding card: 16
- Jarak antar card di list: 8
- Padding halaman (margin kiri-kanan): 16-20
- Jarak antar section: 24-32

---

## 5. Komponen

### Card (item catatan di list)
- Background: `Surface`
- Border radius: 12
- Border: 1px `Border` (tanpa shadow tebal — cukup shadow sangat tipis atau tanpa shadow sama sekali)
- Left indicator tipis (2-3px) sesuai warna priority, jika reminder aktif

### Button
- Primary: background `Accent Primary`, teks putih, radius 8
- Secondary/outline: border `Border`, teks `Text Primary`, background transparan
- Tidak pakai bentuk pill penuh (radius besar) — tetap radius 8-12 biar konsisten sama gaya minimalis

### Input Field
- Border tipis `Border`, radius 8
- Focus state: border berubah ke `Accent Primary`
- Tanpa background fill mencolok

### Bottom Navigation / FAB
- FAB (tombol tambah catatan): warna `Accent Primary`, icon putih, shape circle atau rounded-square radius 16

### Icon Style
- Line icon (outline), bukan filled — konsisten dengan gaya minimalis
- Ukuran standar: 20-24px

---

## 6. Dark/Light Mode Behavior

- Default: ikuti system theme (`ThemeMode.system`)
- User bisa override manual di Settings, tersimpan di `SharedPreferences`
- Transisi antar tema: instan, tanpa animasi berlebihan

---

## 7. Splash Screen

- Background: putih (light mode) / `#121212` (dark mode) — mengikuti sistem, bukan warna gradient logo secara penuh
- Konten: logo `.notes` di tengah, ukuran proporsional (sekitar 30-35% lebar layar)
- Tidak ada teks tambahan/tagline di splash — cukup logo saja, biar clean sesuai gaya minimalis
- Durasi: singkat, hanya selama proses inisialisasi (cek sesi login, load local DB) — bukan splash dekoratif dengan delay buatan
- Implementasi: pakai `flutter_native_splash` package (generate splash native Android dari asset logo yang sudah disiapkan developer secara manual, sesuai catatan branding di atas — bukan di-generate otomatis oleh AI agent)
- Transisi ke halaman berikutnya (Login/Home): fade sederhana, tanpa animasi rumit

---

## 8. Catatan untuk AI Coding Agent

Saat generate UI Flutter, gunakan token warna di atas sebagai `ColorScheme` custom (bukan default Material generik). Konsisten pakai `Inter` sebagai `fontFamily` di `ThemeData`. Hindari elemen dekoratif berlebihan (tanpa neumorphism, tanpa gradient di background utama — gradient hanya dipakai di logo/splash screen).
