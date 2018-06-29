/**
 *  Squeeze Music Player
 *
 *  Version 1.2 June 29, 2018 
 *
 *  Written by Melinda Little 2018
 *
 *  Smartthings control for Logitech Media players.  This is a child device handler that will only function when 
 *  created as part of the Squeeze Music Server. Many thanks to the Smartthings community for the numerous 
 *  code snippets and problem solving solutions that they have shared.
 *
 *
 *  Copyright 2016 SmartThings, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
 
 preferences {
 
	input name: "speechVoice", type: "enum", title: "Voice for Speech", options: ["Ivy(en-us)","Joanna(en-us)","Joey(en-us)","Justin(en-us)","Kendra(en-us)","Kimberly(en-us)","Salli(en-us)","Amy(en-gb)","Brian(en-gb)","Emma(en-gb)","Miguel(es-us)","Penelope(es-us)"], description: "Select voice to use for speech. Defaults to Salli", required: no
    input name: "speechVolume", type: "string", title: "Volume for Speech", description: "Desired volume for Speech Requests", required: no
	input name: "shuffleOff", type: "enum", title: "Turn off Shuffle/Repeat", options: ["shuffle", "repeat", "both", "none"], description: "Turn off shuffle and/or repeat with stop command? Defaults to both", required: no
    input name: "squeezeLite", type: "enum", title: "SqueezeLite", options: ["yes", "no"], description: "Is this a SqueezeLite player? (not Chromecast)? Defaults to no", required: no
    input name: "button1Command", type: "string", title: "Button 1 Command", description: "Server command for Button 1", required: no
    input name: "button1Extra", type: "enum", title: "Button 1 Extra Commands", options: ["shuffle", "repeat", "both", "none"], description: "Add shuffle and/or repeat to Button 1 Command? Defaults to none", required: no
	input name: "button2Command", type: "string", title: "Button 2 Command", description: "Server command for Button 2", required: no  
    input name: "button2Extra", type: "enum", title: "Button 2 Extra Commands", options: ["shuffle", "repeat", "both", "none"], description: "Add shuffle and/or repeat to Button 2 Command? Defaults to none", required: no
	input name: "button3Command", type: "string", title: "Button 3 Command", description: "Server command for Button 3", required: no
    input name: "button3Extra", type: "enum", title: "Button 3 Extra Commands", options: ["shuffle", "repeat", "both", "none"], description: "Add shuffle and/or repeat to Button 3 Command? Defaults to none", required: no
}

metadata {
	definition (
		name: "Squeeze Music Player",
		namespace: "Mellit7",
		author: "Melinda Little") {

		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Music Player"
        capability "Media Playback Shuffle"
        capability "Media Playback Repeat"
        capability "Speech Synthesis"
        
	    attribute "buttonOne", "string"
	    attribute "buttonTwo", "string"
   	    attribute "buttonThree", "string"
        attribute "playlistLength", "number"

        command "updatePlayer", ["string"]
        command "custom", ["string"]
        command "setPlaybackShuffle", ["string" ]
        command "playTrackAndResume", ["string", "string", "string"]
        command "playTrackAndRestore", ["string", "string", "string"]
		command "button1"
        command "button2"
        command "button3"
     
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4, canChangeIcon: true, canChangeBackground: true) {
			tileAttribute("device.status", key: "PRIMARY_CONTROL") {
				attributeState("paused", label:"Paused",)
				attributeState("playing", label:"Playing")
				attributeState("stopped", label:"Stopped")
			}
			tileAttribute("device.status", key: "MEDIA_STATUS") {
				attributeState("paused", label:"Paused", action:"music Player.play", nextState: "playing")
				attributeState("playing", label:"Playing", action:"music Player.pause", nextState: "paused")
				attributeState("stopped", label:"Stopped", action:"music Player.play", nextState: "playing")
			}
			tileAttribute("device.status", key: "PREVIOUS_TRACK") {
				attributeState("status", action:"music Player.previousTrack", defaultState: true)
			}
			tileAttribute("device.status", key: "NEXT_TRACK") {
				attributeState("status", action:"music Player.nextTrack", defaultState: true)
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState("level", action:"music Player.setLevel")
			}
			tileAttribute ("device.mute", key: "MEDIA_MUTED") {
				attributeState("unmuted", action:"music Player.mute", icon:"st.custom.sonos.unmuted", nextState: "muted")
				attributeState("muted", action:"music Player.unmute", icon:"st.custom.sonos.muted", nextState: "unmuted")
			}
			tileAttribute("device.trackDescription", key: "MARQUEE") {
				attributeState("trackDescription", label:"${currentValue}", defaultState: true)
			}
		}
		standardTile("stop", "device.status", width: 2, height: 2, decoration: "flat") {
			state "default", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
			state "grouped", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
		}
        

        
        standardTile("shuffle", "playbackShuffle", width: 2, height: 2, canChangeIcon: true) {
			state "0", label: 'Shuffle', action: "setPlaybackShuffle",  backgroundColor: "#dddddd", nextState: "1"
			state "1", label: 'Shuffle\nSong', action: "setPlaybackShuffle",  backgroundColor: "#79b821", nextState: "2"
            state "2", label: 'Shuffle\nAlbum', action: "setPlaybackShuffle",  backgroundColor: "#5fd5f9", nextState: "0"
		}
        
        standardTile("repeat", "playbackRepeatMode", width: 2, height: 2, canChangeIcon: true) {
			state "0", label: 'Repeat\nOff', action: "setPlaybackRepeatMode", backgroundColor: "#dddddd", nextState: "1"
			state "1", label: 'Repeat\nSong', action: "setPlaybackRepeatMode", backgroundColor: "#79b821", nextState: "2"
            state "2", label: 'Repeat\nPlaylist', action: "setPlaybackRepeatMode",  backgroundColor: "#5fd5f9", nextState: "0"
		}  

		standardTile("button1", "buttonOne", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Preset\n1', action: "button1", backgroundColor: "#dddddd", nextState: "on"
			state "on", label: 'Preset\n1', action: "button1", backgroundColor: "#79b821", nextState: "off"
		}  

		standardTile("button2", "buttonTwo", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Preset\n2', action: "button2", backgroundColor: "#dddddd", nextState: "on"
			state "on", label: 'Preset\n2', action: "button2", backgroundColor: "#79b821", nextState: "off"
		} 

		standardTile("button3", "buttonThree", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Preset\n3', action: "button3", backgroundColor: "#dddddd", nextState: "on"
			state "on", label: 'Preset\n3', action: "button3", backgroundColor: "#79b821", nextState: "off"
		} 

		main "mediaMulti"
		details(["mediaMulti","stop","shuffle","repeat", "button1", "button2", "button3"])
	}
}

def installed() {

	sendEvent(name: "level", value: 20)
	sendEvent(name: "mute", value: "unmuted")
	sendEvent(name: "status", value: "stopped")
	sendEvent(name: "trackDescription", value: "Stopped")
    
	setTimer()

}

def updated() {
	
    unschedule()
    setTimer()

}

def parse(description) {


} /* End of Parse */

def play() {

	def playerId = device.deviceNetworkId
    def path_data = "p0=play"

//	log.debug  "Path Command '${path_data}'"
	parent.makeLANcall(path_data, playerId, null)  

}

def pause() {

	def playerId = device.deviceNetworkId
    def path_data = "p0=pause&p1=1"
    
	parent.makeLANcall(path_data, playerId, null)

}

def stop() {

  	def playerId = device.deviceNetworkId
    def path_data
	if (shuffleOff == null || shuffleOff == "shuffle" || shuffleOff == "both") {setPlaybackShuffle("0")}
	if (shuffleOff == null || shuffleOff == "repeat" || shuffleOff == "both") {setPlaybackRepeatMode("0")}
	path_data = "p0=stop"
   	parent.makeLANcall(path_data, playerId, null)

}

def previousTrack() {
	
  	def playerId = device.deviceNetworkId
    def path_cmd = "p0=playlist&p1=jump&p2=-1"
    
	parent.makeLANcall(path_cmd, playerId, null)

}

def nextTrack() {

  	def playerId = device.deviceNetworkId
    def path_cmd = "p0=playlist&p1=jump&p2=%2b1"
	parent.makeLANcall(path_cmd, playerId, null)


}

def mute() {

	def playerId = device.deviceNetworkId
	def path_cmd = "p0=button&p1=muting"
	parent.makeLANcall(path_cmd, playerId, null)
   	sendEvent(name: "mute", value: "muted")
}

def unmute() {

	def playerId = device.deviceNetworkId 
	def path_cmd = "p0=button&p1=muting"
	parent.makeLANcall(path_cmd, playerId, null)
   	sendEvent(name: "mute", value: "unmuted")

}

def setLevel(level) {

	def playerId = device.deviceNetworkId
	def path_cmd = "p0=mixer&p1=volume&p2=${level}"
   	parent.makeLANcall(path_cmd, playerId, null)

}

def setPlaybackShuffle(controlInput) {

//	log.debug "SHUFFLE CONTROL INPUT ${controlInput}"
    
	def controlValue
    if (controlInput != null) {
    	controlValue = controlInput
    }
    else {
    	switch (device.currentValue("playbackShuffle")) {
 		  	case "0" :
            	controlValue = "1"
        		break        
 		  	case "1" :
            	controlValue = "2"
        		break         
 		  	case "2" :
            	controlValue = "0"
        		break         
	        default:
    			controlValue = "0"       
        }
    }
//    log.debug "SHUFFLE VALUE : ${controlValue}  ${device.currentValue("playbackShuffle")}"
  	def playerId = device.deviceNetworkId
   	def path_data = "p0=playlist&p1=shuffle&p2=${controlValue}"  
	parent.makeLANcall(path_data, playerId, null)

}
def setPlaybackRepeatMode(mode) {

//	log.debug "REPEAT INPUT PARM: ${mode}"

    def controlValue
    if (mode != null) {
    	controlValue = mode
    }
    else {
//	   	log.debug "REPEAT SWITCH REPORTS: ${device.currentValue("playbackRepeatMode")}"
    	switch (device.currentValue("playbackRepeatMode")) {
 		  	case "0" :
            	controlValue = "1"
        		break        
 		  	case "1" :
            	controlValue = "2"
        		break         
 		  	case "2" :
            	controlValue = "0"
        		break         
	        default:
    			controlValue = "0"       
        }
    }
//    log.debug "REPEAT VALUE : ${controlValue}  ${device.currentValue("playbackRepeatMode")}"

	def playerId = device.deviceNetworkId
   	def path_data = "p0=playlist&p1=repeat&p2=${controlValue}"  
	parent.makeLANcall(path_data, playerId, null)


}


def speak(msg) {

  	def playerId = device.deviceNetworkId
    def playerName = device.name.replaceAll(" ","%20")
	def currentRepeat = device.currentValue("playbackRepeatMode")
    def currentVolume = device.currentValue("level")
	def volume = speechVolume ?: null
//	log.debug "Speech volume : ${volume}"
    def currentLength = device.currentValue("playlistLength").toInteger() ?: 0
    def currentStatus = device.currentValue("status")

    def actions = []

    if (currentLength  > 0) {
    	actions << [type:"path", data: "p0=playlist&p1=save&p2=${playerName}-ST01list", player: playerId]
    	actions << [type:"delay", data: "200"]
    }
    actions << [type:"path", data: "p0=playlist&p1=repeat&p2=0", player: playerId]
    actions << [type:"delay", data: "200"]

	def sound = getSound(msg)
 
    def playURI = sound.uri.replaceFirst("^https","http")
//	log.debug playURI
   	actions << [type:"path", data: "p0=playlist&p1=play&p2=${playURI}&p3=Speech%20Notification", player: playerId]
   	if (volume) {actions << [type:"path", data: "p0=mixer&p1=volume&p2=${volume}", player: playerId]}  
    
    def durationDelay = Math.round((sound.duration.toInteger() * 1000) + 5000)
//    log.debug "DELAY : ${durationDelay}"
    actions << [type:"delay", data: durationDelay]

//    log.debug "${playerName} ${currentStatus}"
	actions = restorePlayer(actions, playerId, currentRepeat, currentVolume)
    if (currentLength  > 0) {
		actions << [type:"path", data: "p0=playlist&p1=resume&p2=${playerName}-ST01list", player: playerId]
		if (currentStatus != "playing") {
       	    actions << [type:"delay", data: "100"]
			actions << [type:"path", data: "p0=stop", player: playerId]        
        }        
    } else {
    	actions = stopAndClear(actions, playerId)
    }
 
//    log.debug actions
    
    if (playURI) {parent.multiHubAction(actions)}  else {log.debug "Speak Failed"}
  
}

def playTrack(trackToPlay) {

	def playerId = device.deviceNetworkId
	def path_cmd = "p0=playlist&p1=play&p2=${trackToPlay}"
	parent.makeLANcall(path_cmd, playerId, null)

}

def playTrackAndResume(uri, duration, volume=null) {

  	def playerId = device.deviceNetworkId
    def playerName = device.name.replaceAll(" ","%20")
	def currentRepeat = device.currentValue("playbackRepeatMode")
    def currentVolume = device.currentValue("level")
    def currentLength = device.currentValue("playlistLength").toInteger() ?: 0
    def currentStatus = device.currentValue("status")
    def actions = []
    if (currentLength  > 0) {
    	actions << [type:"path", data: "p0=playlist&p1=save&p2=${playerName}-ST01list", player: playerId]
    	actions << [type:"delay", data: "200"]
    }
    actions << [type:"path", data: "p0=playlist&p1=repeat&p2=0", player: playerId]
    actions << [type:"delay", data: "200"]
   
    def playURI = uri.replaceFirst("^https","http")
   	actions << [type:"path", data: "p0=playlist&p1=play&p2=${playURI}&p3=Play%20Track%20Request", player: playerId]
	if (volume) {actions << [type:"path", data: "p0=mixer&p1=volume&p2=${volume}", player: playerId]}     

	def durationDelay = Math.round((duration.toInteger() * 1000) + 5000)
//    log.debug "DELAY : ${durationDelay}"
    actions << [type:"delay", data: durationDelay]
//    log.debug "${playerName} ${currentStatus}"
	actions = restorePlayer(actions, playerId, currentRepeat, currentVolume)
    if (currentLength  > 0) {
		actions << [type:"path", data: "p0=playlist&p1=resume&p2=${playerName}-ST01list", player: playerId]
   	 	if (currentStatus != "playing") {
           	    actions << [type:"delay", data: "100"]
				actions << [type:"path", data: "p0=stop", player: playerId]        
    	}
	} else {
       	actions = stopAndClear(actions, playerId)
    }
    
    
    if (playURI) {parent.multiHubAction(actions)} else {log.debug "Play Track and Resume Failed"}

}

def playTrackAndRestore(uri, duration, volume=null) {
	log.debug "RESTORE VOLUME : ${volume}"
  	def playerId = device.deviceNetworkId
    def playerName = device.name.replaceAll(" ","%20")
	def currentRepeat = device.currentValue("playbackRepeatMode")
    def currentVolume = device.currentValue("level")
    def currentLength = device.currentValue("playlistLength").toInteger() ?: 0
    def currentStatus = device.currentValue("status")
    def actions = []
    if (currentLength  > 0) {
    	actions << [type:"path", data: "p0=playlist&p1=save&p2=${playerName}-ST01list", player: playerId]
    	actions << [type:"delay", data: "200"]
    }
    actions << [type:"path", data: "p0=playlist&p1=repeat&p2=0", player: playerId]
    actions << [type:"delay", data: "200"]
    def playURI = uri.replaceFirst("^https","http")
   	actions << [type:"path", data: "p0=playlist&p1=play&p2=${playURI}&p3=Play%20Track%20Request", player: playerId]
	if (volume) {actions << [type:"path", data: "p0=mixer&p1=volume&p2=${volume}", player: playerId]}     
    def durationDelay = Math.round((duration.toInteger() * 1000) + 5000)
//    log.debug "DELAY : ${durationDelay}"
    actions << [type:"delay", data: durationDelay]
//    log.debug "${playerName} ${currentStatus}"
	actions = restorePlayer(actions, playerId, currentRepeat, currentVolume)
    if (currentLength  > 0) {
		actions << [type:"path", data: "p0=playlist&p1=resume&p2=${playerName}-ST01list", player: playerId]
   	    actions << [type:"delay", data: "100"]
		actions << [type:"path", data: "p0=stop", player: playerId]        
	} else {
       	actions = stopAndClear(actions, playerId)
    }
    
    
    if (playURI) {parent.multiHubAction(actions)} else {log.debug "Play Track and Restore Failed"}

	log.debug actions
}

def custom(path_cmd) {

	def playerId = device.deviceNetworkId
   	parent.makeLANcall(path_cmd, playerId, null)

}


def setTimer() {

	runEvery1Minute(refresh)

}

def refresh() {

	def playerId = device.deviceNetworkId
    def path_data = "status"
   	parent.makeLANcall(path_data, playerId, null)
    
}

def updatePlayer(playerInfo) {

//	log.debug "Inside player update"

	switch(playerInfo.playerStatus) {
    	case "play" :
        	log.debug "REPORTED PLAYING"

            	sendEvent(name: "status", value: "playing")

        	break
        case "pause" :
        	log.debug "REPORTED PAUSED"

            	sendEvent(name: "status", value: "paused")

        	break
        case ["stop", null] :
        	log.debug "REPORTED STOPPED OR OFF"

            	sendEvent(name: "status", value: "stopped")

        	break
        default:
    		log.debug "UNKNOWN PLAYER STATUS"
    }
    log.debug "PLAYER STATUS: ${device.currentValue("status")}"
    
    sendEvent(name: "trackDescription", value: playerInfo.longInfo)
    
    if (playerInfo.playerVol != null) {
    	sendEvent(name: "level", value: playerInfo.playerVol)
    }

    if (playerInfo.shuffle != null) {
//  		log.debug "REPORTED SHUFFLE ${playerInfo.shuffle}"
       	sendEvent(name: "playbackShuffle", value: playerInfo.shuffle)
       
    }

    if (playerInfo.repeat != null) {
//  		log.debug "REPORTED Repeat ${playerInfo.repeat}"
       	sendEvent(name: "playbackRepeatMode", value: playerInfo.repeat)
       
    }
//    log.debug "UPDATE PLAYLIST LENGTH ${playerInfo.playlistLength}"
    if  (playerInfo.playlistLength != null) {
       	sendEvent(name: "playlistLength", value: playerInfo.playlistLength)
    }

}

def stopAndClear(actions, playerId) {
//	log.debug "IN STOP AND CLEAR"
	actions << [type:"path", data: "p0=stop", player: playerId]
   	actions << [type:"delay", data: "200"]
	actions << [type:"path", data: "p0=playlist&p1=clear", player: playerId]

	return actions

}

def restorePlayer(actions, playerId, currentRepeat, currentVolume) {

	if (currentVolume) {
    	actions << [type:"path", data: "p0=mixer&p1=volume&p2=${currentVolume}", player: playerId]
		actions << [type:"delay", data: "200"]
    }
    actions << [type:"path", data: "p0=playlist&p1=repeat&p2=${currentRepeat}", player: playerId]
   	actions << [type:"delay", data: "200"]
    
	return actions
}

def getSound(msg) {

	def myVoice = speechVoice ?: "Salli(en-us)"
	myVoice = myVoice.replace("(en-us)","")
   	myVoice = myVoice.replace("(en-gb)","")
   	myVoice = myVoice.replace("(es-us)","")
    
    if (!msg) {msg = "You have requested a text to speech event, but have failed to supply the appropriate message." }
    def sound = textToSpeech(msg, myVoice)
//    log.debug "SOUND URI ${sound.uri}"
    if (sound.duration.toInteger() < 5  && squeezeLite == "yes") {
		msg = "The text you entered is too short for the SqueezeLite Player, please enter a longer phrase"
        sound = textToSpeech(msg, myVoice)
    }
    
    return sound

}

def button1() {

	sendEvent(name: "buttonOne", value: "on", isStateChange: true, displayed: false)
	def playerId = device.deviceNetworkId
    def path_data = button1Command
    if (path_data) {
		if (button1Extra == "shuffle" || button1Extra == "both") {setPlaybackShuffle("1")} else (setPlaybackShuffle("0"))
		if (button1Extra == "repeat" || button1Extra == "both") {setPlaybackRepeatMode("2")} else (setPlaybackRepeatMode("0"))   
	   	parent.makeLANcall(path_data, playerId, null)
    }
	sendEvent(name: "buttonOne", value: "off", isStateChange: true, displayed: false)
    
}

def button2() {

	sendEvent(name: "buttonTwo", value: "on", isStateChange: true, displayed: false)
	def playerId = device.deviceNetworkId
    def path_data = button2Command
    if (path_data) {
		if (button2Extra == "shuffle" || button2Extra == "both") {setPlaybackShuffle("1")} else (setPlaybackShuffle("0"))
		if (button2Extra == "repeat" || button2Extra == "both") {setPlaybackRepeatMode("2")} else (setPlaybackRepeatMode("0"))     
		parent.makeLANcall(path_data, playerId, null)
	}
	sendEvent(name: "buttonTwo", value: "off", isStateChange: true, displayed: false)
    
}

def button3() {

	sendEvent(name: "buttonThree", value: "on", isStateChange: true, displayed: false)
	def playerId = device.deviceNetworkId
    def path_data = button3Command
    if (path_data) {
		if (button3Extra == "shuffle" || button3Extra == "both") {setPlaybackShuffle("1")} else (setPlaybackShuffle("0"))
		if (button3Extra == "repeat" || button3Extra == "both") {setPlaybackRepeatMode("2")} else (setPlaybackRepeatMode("0"))     
   		parent.makeLANcall(path_data, playerId, null)
    }
	sendEvent(name: "buttonThree", value: "off", isStateChange: true, displayed: false)
    
}