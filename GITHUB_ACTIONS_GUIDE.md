# Cara Build Jar Otomatis via GitHub Actions

Folder `.github/workflows/build.yml` sudah disiapkan. Workflow ini akan
otomatis build mod menjadi `.jar` untuk **Minecraft 26.1.1** dan **26.1.2**
setiap kali kamu push ke GitHub — tanpa perlu install apa pun di komputer.

## Langkah-langkah

### 1. Buat repository baru di GitHub
- Buka https://github.com/new
- Beri nama bebas, misalnya `botpvp-mod`
- Pilih **Public** atau **Private** (keduanya bisa pakai Actions gratis)
- Klik **Create repository**

### 2. Push project ini ke repo tersebut

Di folder hasil extract `botpvp-mod` (yang sudah berisi `.github/workflows/build.yml`):

```bash
cd botpvp-mod
git init
git add .
git commit -m "Initial commit - BotPvP mod"
git branch -M main
git remote add origin https://github.com/USERNAME/REPO-NAME.git
git push -u origin main
```

Ganti `USERNAME/REPO-NAME` dengan repo kamu sendiri.

### 3. Lihat proses build

1. Buka repo kamu di GitHub
2. Klik tab **Actions**
3. Akan muncul workflow run bernama **"Build BotPvP Jar"** yang otomatis jalan
4. Tunggu sampai status jadi ✅ centang hijau (biasanya 3–8 menit, karena
   Gradle perlu download Minecraft + Fabric + mappings di server GitHub)

### 4. Download jar hasil build

1. Masih di tab **Actions**, klik workflow run yang sudah selesai (✅)
2. Scroll ke bawah ke bagian **Artifacts**
3. Akan ada 2 file zip:
   - `botpvp-mc26.1.1` → jar untuk Minecraft 26.1.1
   - `botpvp-mc26.1.2` → jar untuk Minecraft 26.1.2
4. Klik untuk download, lalu extract — di dalamnya ada file
   `botpvp-1.0.0.jar` yang tinggal dimasukkan ke folder `mods`

## Build ulang manual (tanpa push baru)

Kalau mau trigger build tanpa mengubah kode:

1. Tab **Actions** → pilih workflow **"Build BotPvP Jar"** di sidebar kiri
2. Klik tombol **Run workflow** (dropdown di kanan atas) → **Run workflow**

## Kalau build gagal (❌)

Klik run yang gagal → klik job yang merah → baca log error. Penyebab paling umum:

- **Versi Fabric API tidak cocok** dengan `minecraft_version` → cek versi
  terbaru yang tersedia di https://modrinth.com/mod/fabric-api/versions
  lalu update `fabric_version` di matrix pada `build.yml`
- **Yarn mappings belum tersedia** untuk versi MC tersebut (versi MC yang
  sangat baru kadang mappings-nya belum di-publish) → cek
  https://maven.fabricmc.net/net/fabricmc/yarn/

Kalau errornya berkaitan dengan hal di atas, kirim log error-nya ke saya
dan saya bantu sesuaikan `gradle.properties` / `build.yml`.
