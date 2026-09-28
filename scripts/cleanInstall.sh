./gradlew clean
./gradlew assembleRelease
~/Android/Sdk/build-tools/36.0.0/apksigner sign \
  --ks ~/.android/debug.keystore \
  --ks-key-alias androiddebugkey \
  --ks-pass pass:android \
  --key-pass pass:android \
  app/build/outputs/apk/release/app-release-unsigned.apk
adb install -r app/build/outputs/apk/release/app-release-unsigned.apk