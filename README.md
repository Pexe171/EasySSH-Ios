# EasySSH

EasySSH is an Android APK for quick SSH access to VPS and AWS EC2 machines using a saved PEM/OpenSSH private key. The MVP stores machine profiles locally, encrypts private keys with Android Keystore, supports fixed or rotating public IPs, and opens one interactive terminal session.

## MVP Features

- Dark Android UI built with Kotlin and Jetpack Compose.
- Machine profile list with alias, SSH user, port, fixed IP/DNS or rotating IP.
- PEM/OpenSSH key import through Android's document picker.
- Private key copied into app-private storage and encrypted with AES-GCM through Android Keystore.
- SSH shell with SSHJ and host key Trust On First Use confirmation.
- Terminal rendered with local `xterm.js` assets inside a restricted WebView.
- Debug APK build for direct testing on a phone.

No backend or PostgreSQL database is used in the MVP. SSH connections go directly from the Android device to the VPS.

## Requirements

- Android SDK installed at `C:\Users\ikezn\AppData\Local\Android\Sdk`.
- JDK 17. A portable JDK is expected at `.tools\jdk17-dist\jdk-17.0.19+10` in this workspace.
- Gradle Wrapper from this repository.
- Android phone with USB debugging enabled, or an emulator.

## Build

From PowerShell:

```powershell
.\scripts\build-debug.ps1
```

Equivalent manual command:

```powershell
$env:JAVA_HOME=(Resolve-Path '.tools\jdk17-dist\jdk-17.0.19+10').Path
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat testDebugUnitTest assembleDebug
```

The debug APK is generated at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

Install on a connected Android device:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## AWS EC2 Usage

1. Create a machine profile with an alias.
2. Choose the SSH user. Common AWS users are `ec2-user` for Amazon Linux and `ubuntu` for Ubuntu.
3. Keep port `22` unless the instance uses a custom SSH port.
4. Use fixed IP/DNS for stable EC2 public DNS, or enable rotating IP if the public IP changes.
5. Import the `.pem` private key.
6. Connect and confirm the host fingerprint on first use.

The MVP supports private keys without passphrase. Passphrase support and multiple terminal tabs are planned later.

## Security Notes

- `android:allowBackup` is disabled and backup rules exclude app files and database.
- Private keys are encrypted before being written to app-private storage.
- A decrypted key is written only to a temporary cache file for SSHJ authentication and deleted immediately after authentication setup.
- Host key fingerprint changes are blocked by SSHJ verification.

## Optional Local SSH Test With Docker

This is optional and does not replace testing against the real AWS VPS.

```powershell
$env:EASYSSH_TEST_PUBLIC_KEY = "ssh-ed25519 AAAA..."
docker compose up --build ssh-test
```

Android emulator host: `10.0.2.2`, port `2222`, user `easyssh`.

For a physical Android phone, use the LAN IP of this Windows machine and port `2222`.

## References

- Android Compose: https://developer.android.com/develop/ui/compose/documentation
- Android app architecture: https://developer.android.com/topic/architecture/recommendations
- Android Keystore: https://developer.android.com/privacy-and-security/keystore
- Android cryptography: https://developer.android.com/privacy-and-security/cryptography
- Android Storage Access Framework: https://developer.android.com/guide/topics/providers/document-provider
- Room: https://developer.android.com/training/data-storage/room
- xterm.js: https://xtermjs.org/docs/
- SSHJ: https://github.com/hierynomus/sshj

