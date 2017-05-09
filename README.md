# ffbe-grinder
Java application to grind stages in Final Fantasy Brave Exvius.

Currently only Earth Shrine with lapis refresh is implemented.

# Dependencies
Requires ffmpeg and adb to be installed. You can get ffmpeg
[here](https://ffmpeg.org/download.html) and adb
[here](https://developer.android.com/studio/index.html#downloads)
(you probably just want the command line tools).

# Configuration
If ffmpeg or adb are not on the standard path you need to create
a file called *application.yml* in the root of the project with
the following contents:

```
adb:
    location: /path/to/adb
ffmpeg:
    location: /path/to/ffmpeg
```

# Running
Run the app by executing `./gradlew bootRun`.

```$ ./gradlew bootRun```
