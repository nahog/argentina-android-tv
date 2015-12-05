# ArgentinaTV (for Android TV)

## Objective

An IPTV player for android tv sdk with some styles and defaults for public available IPTV channels from Argentina.

The idea behind this app is to have access to IPTV services from my home country when I'm abroad as a Leanback (Android TV interface) app.

## How to install

- The easiest way to install this app on your Android TV is to first install EZ File Explorer or some similar app that lets you copy files from a computer.
- Build the app to generate an APK file.
- Copy the APK file from your computer to the TV using EZ File Explorer.
- Install the APK on the TV (EZ File Explorer provides this functionality by default but you should allow to install untrusted apps from the settings of the TV).

## How to use

The app is meant to be used with the remote control, and only uses the D-PAD and the Channel Up/Down buttons.

- Channel up and down moves between channels.
- D-PAD up and down also moves between channels. When the channel list is open moves between channels to tune a new one.
- D-PAD right and left moves between alternative streams of the same channel.
- Center button provides channel list and current channel OSD. Center again tune the new channel, back button close the menu without tunning a new channel.
- Volumen Up, Down and mute pass through and controls the TV function.

## Technical details

The only technical detail that stands out from the app is that even when a default channel list is provided, the app itself, uses a file in the external storage of the TV and is for the runtime channel list. The app can be configured to use any IPTV channel that is supported by the combination of your tv codecs and the android VideoView.

The file can be located in the sdcard/nahog.argetinatv/files/channels.txt location and the format is very basic, just one CHANNEL_NAME|CHANNEL_URL per line, if the channel name is repeated, the extra urls will be used as alternative streams for the channel. If there is any format error, the line is ignored.
