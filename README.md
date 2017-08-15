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

If adb is on the path, you don't need to include it's location and vice versa for ffmpeg.

# Running
Run the app by executing `./gradlew bootRun`.

```$ ./gradlew bootRun```

# Grinding
The main point of this program is to grind stages over and over again
(for example to earn TMR percentage) by sending commands through the Android debugger.
This is done by repeatedly starting a stage and pressing "Repeat" in combat. To achieve this
your party needs to be set up so that it can clear whatever stage you're grinding by doing this.

## Preparations
Run the stage once, set up your characters the way you need them to and execute their actions, then press
"Repeat" to see that it works. When you have completed the stage and are back at the stage selection
screen you start the "Stage Griding" script and let it repeat untill you either stop it yourself
or you run out of lapis.

