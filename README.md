# Smartthings-SqueezeBox-Control
Smartthings Control for Logitech Media Server and Players (Squeezebox)

These device handlers provides Smartthings control of multiple Logitech Media Server (Squeezebox) players through a single server interface.

**NEW VERSION**  Version 2.0 has been uploaded.  It is a rewrite using a different communication protocal with LMS.  You do not need to reinstall devices, only update both Device Handlers.  However, **preset buttons have been converted as well, so they will need to be changed**.  See notes below for details. 

There is an optional smartapp to associate momentary buttons with player functions.  See separate documentation for usage.

#### Current version: (See notes below for details)

**Server** 2.0

**Player** 2.0

### INSTALLATION INSTRUCTIONS

1.  Install 2 device handlers, squeeze-music-server and squeeze-music-player

2.  In the IDE in the My Devices tab, create a device using the squeeze-music-server handler.   Enter the ip and port address for the server in preferences.  This works best if the ip is static.  This info can be found in the server settings information tab. Use any temporary name for Device Network Id. You can add the player info now, but it is not required.  If you do, players will be created as part of step 3 when the Device Network Id is update.

3.  **IMPORTANT**  Do not skip this or things will not work.  While in the IDE, update the Deivce Network Id field with the Hex equivalent of the ip and port values.  The value has been calcuated and displayed in the device details in the IDE as part of currentActivity.  You can do this as part of step 2 if you know how to find this value, but for simplicity's sake, it is calculated and displayed for you.

4.  Add players by entering the MAC id of the player.  If things are working correctly, a device will be created for each player entered and the player count will adjust accordingly.  Each Player device will be named using the name from the LMS Server data.

Once players are created, everything is installed and should be working. By default, the stop command includes a turn off shuffle command as well.  This can be turned off through a setting in the player.

At this point your only interaction with the server device should be to add or delete players.  All other actions and commands are done through the player device.  Each player is controlled separately.

### Player Notes

Players can be added up to 5 at a time.  If you need more than 5 players, since players are not deleted, additon players can be added by simply entering new MAC values and saving the player settings again. 

Players are not automatically deleted unless the server device is deleted.  This is to minimize impact on any smart apps installed.  If a player device needs deleting, manually delete it in the app or IDE first, and then edit the settings preferences in the server and remove the MAC id from the list.  Otherwise the player will be recreated the next time settings are updated.  Testing found that after deleting a player, settings needs to be resaved to properly update the device.  Otherwise, it will throw errors.

Player status updates are done immediately any time there is interaction with the device, and by polling once a minute to reflect changes made outside of Smartthings through the web interface or a phone app such as Squeezer.  This is due to not having async responses for lan devices currently.  Making this choice eliminated the need for a bridge program, which was a complexity I didn't want to take on. 

By default, the stop command includes a turn off shuffle command as well.  This can be turned off through a setting in the player.


### Player Functions Implemented

- on()
- off()
- play()
- pause()
- stop()
- previousTrack()
- nextTrack()
- mute()
- unmute()
- setLevel(level)  volume control, values 0-100
- setPlaybackShuffle(controlInput)   string controlInput values "0" \- Off "1" \- Shuffle by Song "2" \- Shuffle by Album
- setPlaybackRepeatMode(mode) string mode values "0" \- Off "1" \- Repeat Song "2" \- Repeat Playlist
- speak(msg)  string msg is message to say
- playTrack(uri) string uri is a track path valid in LMS
- def playTrackAndResume(uri, duration, volume)  string uri is a track path valid in LMS
- def playTrackAndRestore(uri, duration, volume) string uri is a track path valid in LMS



### To Do Wish List

- [X] Implement power on/off button


### Versions

**Server**

**2.0**  08/16/2018 Communication with LMS changed to JSON method.  Devices to not need to be reinstalled.  This change eliminates the need for LMS to be in English.

**1.2**  06/29/2018 Implemented the ability to process multiple server commands as a set to for sequential processing and allow for delays. Fixed volume to report in correct increments of 10.

**1.1**  06/04/2018 Implemented communication method compatible with LMS 7.9 since original method was discontinued.  This introduced two functional changes.  Shuffle now correctly reports all the states in LMS.  Volume is reported in increments of 9.  Any volume can still be sent to the player, but the returned reported volume will be reported as a factor of 9. 

**1.0**  05/24/2018 Initial release

**Player**

**2.0**  08/16/2018 Commands changed to JSON format.  
         Power button added, along with on() and off() commands.
         Volume now reports full range 0-100. Negative values are reported when the player is muted.  
         Temporary playlist saved for notifications is now deleted once no longer needed.  
         **Preset buttons updated to use JSON formatted commands.**  See separate documentation on how use them.         
         Additional voices added for speak function to allow for expanded language support.  Search internet for Amazon Polly Voices for details about available voices.

**1.2**  06/29/2018 Implemented repeat function, speech synthesis, play track and resume, play track and restore, and set of 3 buttons to use as presets. Ther are several new player settings, all of which remain optional, to customize the function of each individual player.  See custom control documentation on how to use preset buttons.
 
**1.0.1** 06/04/2018 Add additional state for Shuffle to match new server reporting values.
 
**1.0**   05/24/2018 Initial release
