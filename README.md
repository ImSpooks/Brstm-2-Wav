# Brstm 2 Wav

This is a program that converts your BRSTM files to a WAV file.

## What is a BRSTM file?

A BRSTM file is an audio file used for old Nintendo consoles. This audio file is similiar to a WAV or MP3 file but a BRSTM file contains a looping point.

`.brstm` files are not included included in the download.

## How to use this program

1. Open the program
2. Click on the 'Select file' button to select a file. *You can selected multiple files*
3. Click on the 'Convert file' button to convert the selected brstm file into a wav file.

The converted audio file will be in the directory `output` as `<file>.wav`

+ To delete an entry you need to select a file and press the `delete` key on your keyboard.
+ To edit an entry you need to select a file and press `enter` to change it.

### Settings

+ Audio Format: *`Default: WAV`* 
    + WAV: Converts to a `WAV` file
    + MP3: Converts to a `MP3` file
    + OGG: Converts to a `OGG` file
+ Loop times: Determines how many times the file gets looped. E.g. `0` means it will play once, `5` plays it once times and loops back to the `LoopStartSamble` value until it looped `x` amount of times.
