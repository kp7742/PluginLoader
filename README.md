## PluginLoader
PluginLoader is a tiny system to dynamically load apk in Android. I used it in many occasions during my development work. I got inspirations from Magisk's Stub app.

## Features
- Able to Download plugin from remote location using REST apis
- Only crafted plugin apks are supported(refer 'app' module)
- Can load resources from plugin apk
- Activity and Service are working
- Tiny stub apk of 64kb for demo
- It supports Android 5.0+

## Notes
- Not every device may supported directly, Modification may need.

## How to Build
- Use Android Studio to build it.

## Demo
- Download [Stub App](https://github.com/kp7742/PluginLoader/blob/main/release/stub-debug.apk?raw=true) and install it.
- In first start, it will download and store plugin apk.
- After that it will automatically load and start plugin app.

## Credits
- [Tinker](https://github.com/Tencent/tinker): Resource Patch
- [Paranoid](https://github.com/MichaelRocks/paranoid): String Obfuscation
- [ProcessPhoenix](https://github.com/JakeWharton/ProcessPhoenix): App Restart

## Technology Communication
> Email: patel.kuldip91@gmail.com