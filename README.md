# ffbe-grinder
Java application to grind stages in Final Fantasy Brave Exvius.

Currently only Earth Shrine with lapis refresh is implemented.

# Dependencies
Requires ffmpeg and adb to be installed.

# Running
Run the app by executing `./gradlew bootRun`. If adb or ffmpeg aren't on the standard path you need to pass the location of them in with `--adb.location` or `--ffmpeg.location`:

```$ ./gradlew bootRun --adb.location=~/bin/adb```
