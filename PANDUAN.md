# Panduan WakeTag

Dokumen ini dibuat untuk membantu menjelaskan aplikasi saat presentasi.
Tidak perlu paham semua kode — cukup paham **alurnya**.

## 1. Apa itu WakeTag?

WakeTag adalah aplikasi alarm yang hanya bisa dimatikan dengan menempelkan
**tag NFC** yang sudah didaftarkan.

Alarm biasa (tombol "Matikan") tetap disediakan sebagai pembanding saat demo.

## 2. Batasan versi ini

- Hanya ada 2 metode: **Normal** dan **Tag NFC**.
- Satu tag NFC dipakai untuk semua alarm NFC.
- Alarm berbunyi **setiap hari** pada jam yang sama.
- Setiap alarm bisa diberi **label** (Kuliah, Kerja, Pribadi, atau teks sendiri).
- Halaman utama menampilkan **alarm berikutnya** lengkap dengan hitung mundur
  hidup, misalnya "6 jam 12 menit lagi" atau "5 menit lagi" untuk snooze.
- Ada tombol **Tunda 5 menit** untuk menunda alarm sebentar.
- Semua data tersimpan di HP. Tidak perlu internet dan tidak ada login.
- Ada tombol **"Matikan paksa"** sebagai jalur darurat kalau tag hilang.

## 3. Cara memakai (alur demo)

1. Buka aplikasi, izinkan notifikasi jika diminta.
2. Di kartu **"Tag NFC"**, tekan **Daftarkan Tag**, lalu tempelkan tag
   NFC ke belakang HP. UID tag akan muncul.
3. Tekan tombol **Alarm baru**, ketuk angka jam/menit, pilih label
   **Kuliah/Kerja/Pribadi** (atau tulis label sendiri), pilih metode
   **Tag NFC**, lalu **Simpan**.
4. Atur alarm sekitar 2 menit ke depan.
5. Tunjukkan panel **Alarm berikutnya** di halaman utama: ada label, jam, dan
   hitung mundur seperti "2 menit lagi".
6. Saat alarm berbunyi, tunjukkan bahwa tidak ada tombol "Matikan" biasa.
7. Tempelkan tag NFC → alarm berhenti.
8. Buka aplikasi lagi → alarm masih berstatus **Aktif** (akan berbunyi lagi besok).

Catatan panel **Alarm berikutnya**:

- Kalau alarm hari ini sudah lewat, panel tetap menampilkan alarm besok, tetapi
  dengan hitung mundur seperti "16 jam 20 menit lagi", bukan hanya "Besok".
- Saat alarm ditunda, panel langsung berubah menjadi sumber **Tunda** dan
  menghitung "5 menit lagi".
- Hitung mundur diperbarui tiap detik dan otomatis berubah ke "30 detik lagi"
  saat tersisa kurang dari satu menit.

Catatan **label**:

- Label yang tersedia: **Kuliah**, **Kerja**, **Pribadi**, atau teks bebas.
- Warna label dibedakan: Kuliah hijau mint, Kerja amber, Pribadi abu-biru.
- Label tampil di kartu alarm, panel alarm berikutnya, notifikasi, dan layar
  alarm yang sedang berbunyi.

Catatan tombol **Tunda 5 menit**:

- Alarm berhenti sementara, lalu berbunyi lagi 5 menit kemudian.
- Snooze dijadwalkan terpisah dari alarm harian, jadi jadwal besok tidak rusak.
- Saat alarm berbunyi lagi, alarm NFC tetap hanya bisa dimatikan dengan tag.

Catatan tombol back:

- Di halaman utama, tekan back **dua kali** untuk keluar.
- Di layar alarm, tombol back tidak bisa menutup alarm. Alarm harus dimatikan
  dulu lewat tombol atau tag NFC.

## 4. Alur kode saat alarm berbunyi

Cukup hafal 5 langkah ini:

1. **`alarm/WakeTagAlarmManager.kt`** meminta Android membangunkan aplikasi
   pada jam alarm.
2. **`receiver/AlarmReceiver.kt`** menerima sinyal dari Android, lalu
   menjadwalkan alarm untuk besok dan menyalakan pemutar suara.
3. **`service/AlarmPlaybackService.kt`** + **`AlarmSoundPlayer.kt`** memutar
   suara alarm dan getaran.
4. **`ui/screen/AlarmScreenActivity.kt`** membuka layar alarm di atas lockscreen.
5. **`nfc/NfcManager.kt`** membaca UID tag. UID tersebut dibandingkan dengan
   UID yang disimpan oleh **`nfc/NfcTagStore.kt`**.
   Kalau cocok → **`receiver/AlarmDismissReceiver.kt`** menghentikan suara.

Kalau pengguna memilih **Tunda 5 menit**, `WakeTagAlarmManager.scheduleSnooze`
membuat jadwal sekali pakai dengan request code berbeda. Jadi alarm harian
tetap aman, dan alarm berbunyi lagi setelah 5 menit. Waktu tunda juga dicatat
oleh `alarm/SnoozeStore.kt` supaya halaman utama bisa menampilkan hitung mundur
"5 menit lagi".

## 5. Peta folder

| Folder / file | Tugas | Analogi |
|---|---|---|
| `MainActivity.kt` | Mengatur layar utama dan perpindahan layar | Resepsionis |
| `ui/screen/HomeScreen.kt` | Menampilkan daftar alarm, panel alarm berikutnya, dan kartu tag NFC | Etalase |
| `ui/screen/CreateAlarmScreen.kt` | Form membuat/mengubah alarm dan label | Formulir |
| `ui/component/AlarmLabelPill.kt` | Label berwarna Kuliah/Kerja/Pribadi | Stiker nama |
| `data/` | Menyimpan alarm dan label di database Room | Lemari arsip |
| `alarm/` | Menjadwalkan alarm dan menyimpan waktu snooze | Jam weker |
| `receiver/` | Menerima sinyal dari Android | Penjaga |
| `service/` | Memutar suara dan getaran | Pengeras suara |
| `nfc/NfcManager.kt` | Membaca UID tag NFC | Pembaca kartu |
| `nfc/NfcTagStore.kt` | Menyimpan UID tag terdaftar | Kunci cadangan |
| `ui/screen/AlarmScreenActivity.kt` | Layar saat alarm berbunyi | Pintu darurat |

## 6. Kalimat singkat untuk menjelaskan

> "Alarm dijadwalkan dengan AlarmManager. Saat waktunya, receiver menyalakan
> foreground service untuk memutar suara, lalu membuka layar alarm. Di layar
> inilah WakeTag menambahkan verifikasi: alarm hanya bisa berhenti kalau
> pengguna menempelkan tag NFC yang UID-nya sama dengan tag yang sudah
> didaftarkan."

## 7. Pertanyaan dosen yang mungkin muncul

**Apa bedanya dengan alarm biasa?**
Alarm biasa bisa dimatikan dari kasur. WakeTag memaksa pengguna berdiri dan
mengambil tag yang sudah ditaruh di tempat lain.

**Apa bedanya dengan Alarmy?**
Alarmy memakai foto/QR/matematika. WakeTag memakai tag fisik yang murah dan
bisa ditempel di kamar mandi atau meja belajar.

**Kenapa memakai SharedPreferences, bukan database?**
Karena yang disimpan hanya satu UID tag. Untuk data yang lebih besar seperti
daftar alarm, aplikasi tetap memakai database Room.

**Bagaimana kalau tag hilang?**
Tag bisa didaftarkan ulang di halaman utama. Kalau alarm sedang berbunyi dan
tag tidak ada, tersedia tombol "Matikan paksa" yang meminta konfirmasi.

**Apakah alarm berulang?**
Ya, alarm berbunyi setiap hari pada jam yang sama. Setelah dimatikan, alarm
tetap aktif dan jadwal besok sudah disiapkan.

**Apa fungsi label alarm?**
Supaya pengguna langsung tahu alarm itu untuk apa. Label disimpan sebagai kolom
baru di database Room. Database versi 1 otomatis dimigrasi ke versi 2 dengan
kolom `label`, sehingga alarm lama tidak hilang.

**Bagaimana hitung mundur dihitung?**
Halaman utama membandingkan waktu alarm berikutnya dengan jam HP setiap detik.
Hasilnya ditampilkan sebagai "5 menit lagi", "1 jam 5 menit lagi", atau
"2 hari 3 jam lagi". Untuk snooze, sumber waktunya berasal dari `SnoozeStore`.

**Apa fungsi tombol Tunda 5 menit?**
Menghentikan alarm sementara lalu menjadwalkan bunyi lagi 5 menit kemudian.
Snooze memakai request code sendiri di `WakeTagAlarmManager`, sehingga tidak
menimpa jadwal alarm harian. Kalau alarm sedang di-snooze lalu alarm
dinonaktifkan atau dihapus, snooze ikut dibatalkan.

**Apakah butuh internet?**
Tidak. Semua proses berjalan lokal di HP.

## 8. Checklist sebelum presentasi

- [ ] NFC di HP aktif.
- [ ] Tag NFC sudah terdaftar di aplikasi.
- [ ] Izin notifikasi dan alarm tepat waktu sudah diberikan.
- [ ] Battery optimization untuk WakeTag dimatikan (penting di Xiaomi/Oppo/Vivo).
- [ ] Volume media/alarm sudah dinaikkan.
- [ ] Siapkan 2 tag NFC (satu terdaftar, satu tidak terdaftar untuk demo tag salah).
- [ ] Rekam video demo sebagai cadangan.
- [ ] Isi daya HP dan matikan mode jangan ganggu.

## 9. Build APK

Lewat Android Studio: **Build → Build Bundle(s) / APK(s) → Build APK(s)**.

Lewat terminal:

```bash
./gradlew assembleDebug
```

Hasilnya ada di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 10. Saran susunan presentasi (5 menit)

1. **Masalah** (30 detik): alarm biasa terlalu mudah dimatikan dari kasur.
2. **Solusi** (30 detik): alarm hanya berhenti dengan tag NFC.
3. **Demo** (2 menit): daftarkan tag → buat alarm → alarm berbunyi → coba tag
   salah → tag benar. Sebutkan tombol **Tunda 5 menit** sebagai fitur tambahan.
4. **Penjelasan alur kode** (1 menit): 5 langkah pada bagian 4.
5. **Batasan dan pengembangan** (1 menit): satu tag, alarm harian, rencana
   fitur riwayat bangun dan beberapa tag per alarm.

## 11. Catatan desain

- Palet **"midnight → sunrise"**: latar gelap ink, aksen amber hangat, dan
  versi terang bernuansa kertas krem.
- Panel alarm berikutnya memakai gradasi halus dan lingkaran samar seperti
  matahari terbit, bukan bayangan tebal.
- Label alarm memakai warna kategori agar daftar mudah dipindai.
- Angka jam memakai **monospace** agar lebar digit stabil seperti jam digital.
- Kartu memakai garis tipis dan ruang lega, bukan bayangan tebal, supaya
  tampilan terasa tenang.
- Layar alarm selalu gelap agar nyaman dilihat saat baru bangun dan fokus
  pada waktu serta instruksi tag NFC.
- Tombol **Tunda 5 menit** diletakkan terpisah dari aksi utama agar tidak
  mengalahkan tujuan utama alarm.
