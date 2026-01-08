# Github Action Build
To build this project via Github Actions/Gradle, you must provide the necessary system libraries that are normally available in the AOSP build environment.
Specifically, you may need:
1. A custom `android.jar` with hidden APIs exposed.
2. The LineageOS SDK jar.

Place these in a `libs/` directory and uncomment the lines in `build.gradle` if you wish to achieve a successful standalone compilation.
The provided workflow uses standard Gradle but will likely fail without these system dependencies.
