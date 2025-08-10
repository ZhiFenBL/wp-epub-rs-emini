<p align="center">
  <img src="logo.png" alt="WattDownload Logo" width="200px">
</p>

<h1 align="center">WP (Downloader) | EPUB | Extra-Mini | Android/JVM</h1>

<p align="center">
  Extra-Minimal Android and JVM apps to Download Stories from WP in EPUB (version 3) format. <br/>
  Uses <a href="/wp-backend-rs-emini/wattpad-rs">wp-api-rs-emini</a> to download and <a href="/wp-backend-rs-emini/wp-epub-mini">wp-epub-mini</a> to generate apubs.
</p>

---

<div align="center">
  <a href="https://github.com/WattDownload/wp-epub-rs-emini/releases/latest">
    <img src="https://img.shields.io/badge/Download%20For%20Android%20or%20Desktop%20(JVM)%20now!-darkgreen?style=for-the-badge&logo=abdownloadmanager&logoColor=f5f5f5" alt="Download Android App">
  </a>
</div>

---

## What is this?
- A Blazing fast - Extra-Minimal Android and JVM apps to download any story from WP to epub (version 3) format - even purchased ones.

## Disclaimer
> [!WARNING]
> Using this app may violate Wattpad's [Terms of Service](https://policies.wattpad.com/terms/). These tools are provided for educational and personal backup purposes only. **USE AT YOUR OWN RISK.**

## Features
- Download any story under a minute.
- Download purchaed content (Premium chapter) - with WP credentials.
- Download stories with or without images embedded.
- Clean, Extra-Minimal UI.

## Showcase
- Android

https://github.com/user-attachments/assets/1e0de71b-f472-483b-b5fe-2017c144bef9

- JVM

https://github.com/user-attachments/assets/6e82db83-1bde-4295-b27a-f8cc989df8d6

---

## Get Started
### Android
 - You have 2 options:
   - Install from universal apk (Even it's name is `universal`, it only supports `arm64_v8`, `armeabi_v7a`, `x86_64`, `x86` architectures)
     - File name follows `android-universal-wprust-*.*.*.apk` pattern.
   - Install from architecture specific (abi-split) apks (Only supports `arm64_v8`, `armeabi_v7a`, `x86_64`, `x86`)
     - File name follows `android-[architecture]-wprust-*.*.*.apk` pattern, where [architecture] is one of `arm64_v8`, `armeabi_v7a`, `x86_64`, `x86`.

---

### JVM
 - Windows x86_64 : Has 2 options:
   - Use `zip` file with bundled JRE and use
     - File name follows `windows-x86_64-wprust-*.*.*-with-jre.zip` pattern.
   - Use jar file with your own installed JDK.
     - File name follows `windows-x86_64-wprust-*.*.*.jar` pattern.
 > [!WARNING]
 > You need to have `GraalVM 24` installed and configured to use the second option (Use jar file with your own installed JDK). ([Download](https://www.graalvm.org/downloads/) & [Configure](https://www.graalvm.org/latest/getting-started/windows/) ; After configuring `GraalVM 24`, execute `java -jar windows-x86_64-wprust-*.*.*.jar` in a terminal emulator. 
---
 - Linux x86_64 : Has 2 options:
   - Use `AppImage` file (bundled JRE) and use. (In Ubuntu you may need to install `libfuse2`)
     - File name follows `linux-x86_64-wprust-*.*.*-with-jre.AppImage`
   - Use jar file with your own installed JDK.
     - File name follows `linux-x86_64-wprust-*.*.*.jar` pattern
 > [!WARNING]
 > You need to have `GraalVM 24` installed and configured to use the second option (Use jar file with your own installed JDK). ([Download](https://www.graalvm.org/downloads/) & [Configure](https://www.graalvm.org/latest/getting-started/linux/) ; After configuring `GraalVM 24`, execute `java -jar windows-x86_64-wprust-*.*.*.jar` in a terminal emulator.
---
 - MacOS arm64 : Has only one option:
   - Use jar file with your own installed JDK.
     - File name follows `macos-x86_64-wprust-*.*.*.jar` pattern
 > [!WARNING]
 > You need to have `GraalVM 24` installed and configured to use the second option (Use jar file with your own installed JDK). ([Download](https://www.graalvm.org/downloads/) & [Configure](https://www.graalvm.org/latest/getting-started/macos/) ; After configuring `GraalVM 24`, execute `java -jar windows-x86_64-wprust-*.*.*.jar` in a terminal emulator.
---
## Get Started (Dev)
- Just reach refer source and figure our yourself - at least for now.

## Acknowledgements
- [AaronBenDaniel](https://github.com/AaronBenDaniel) for always helping with strict-robust testing, giving ideas, debugging, and inspiring.

---

> [!NOTE]
> `Wattpad` is a registered trademark of `Wattpad` & `Webtoon Entertainment Inc.`. This project is not affiliated with, endorsed, or sponsored by Wattpad.

<p align="center">© 2025 WattDownload.</p>
