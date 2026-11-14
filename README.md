# WakeTag

WakeTag adalah aplikasi alarm Android yang membantu pengguna benar-benar
bangun dengan mewajibkan tag NFC untuk mematikan alarm. Alarm dapat dijadwalkan
setiap hari dan dilengkapi label serta fitur tunda 5 menit.

## Fitur utama

- Alarm harian dengan hitung mundur alarm berikutnya.
- Mode mematikan alarm normal atau menggunakan tag NFC.
- Pendaftaran satu tag NFC untuk digunakan pada alarm.
- Snooze selama 5 menit.
- Label alarm seperti Kuliah, Kerja, dan Pribadi.
- Data tersimpan secara lokal tanpa akun atau koneksi internet.

## Teknologi

- Kotlin
- Jetpack Compose
- Room Database
- Android AlarmManager
- NFC

## Menjalankan proyek

Buka proyek ini di Android Studio, lalu jalankan pada perangkat Android
minimal API 26. Untuk membuat APK debug melalui terminal:

```bash
./gradlew assembleDebug
```

APK tersedia di `app/build/outputs/apk/debug/app-debug.apk`.

Pastikan NFC aktif dan izin notifikasi serta alarm tepat waktu telah diberikan
agar seluruh fitur berjalan dengan baik.
