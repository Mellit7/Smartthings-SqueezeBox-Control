/**
 *  Squeeze Music Player
 *
 *  Version 2.2 December 16, 2019
 *
 *  Written by Melinda Little 2018
 *
 *  Smartthings control for Logitech Media players.  This is a child device handler that will only function when 
 *  created as part of the Squeeze Music Server. Many thanks to the Smartthings community for the numerous 
 *  code snippets and problem solving solutions that they have shared.
 *
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
 
	input name: "speechVoice", type: "enum", title: "Voice for Speech", options: availableVoices(), description: "Select voice to use for speech. Defaults to Salli", 
    	defaultValue: "Salli(en-us)" , required: no
    input name: "speechVolume", type: "string", title: "Volume for Speech", description: "Desired volume for Speech Requests", required: no
	input name: "shuffleOff", type: "enum", title: "Turn off Shuffle/Repeat", options: ["shuffle", "repeat", "both", "none"], description: "Turn off shuffle and/or repeat with stop command? Defaults to both", 
    	defaultValue: "both" ,required: no
    input name: "squeezeLite", type: "enum", title: "SqueezeLite", options: ["yes", "no"], description: "Is this a SqueezeLite player? (not Chromecast)? Defaults to no", 
    	defaultValue: "no" ,required: no
    input name: "button1Command", type: "string", title: "Preset 1 Command", description: "Server command for Preset 1", required: no
    input name: "button1Extra", type: "enum", title: "Preset 1 Extra Commands", options: ["shuffle", "repeat", "both", "none"], description: "Add shuffle and/or repeat to Preset 1 Command? Defaults to none", 
    	defaultValue: "none" ,required: no
	input name: "button2Command", type: "string", title: "Preset 2 Command", description: "Server command for Preset 2", required: no  
    input name: "button2Extra", type: "enum", title: "Preset 2 Extra Commands", options: ["shuffle", "repeat", "both", "none"], description: "Add shuffle and/or repeat to Preset 2 Command? Defaults to none", 
    	defaultValue: "none" ,required: no
	input name: "button3Command", type: "string", title: "Preset 3 Command", description: "Server command for Preset 3", required: no
    input name: "button3Extra", type: "enum", title: "Preset 3 Extra Commands", options: ["shuffle", "repeat", "both", "none"], description: "Add shuffle and/or repeat to Preset 3 Command? Defaults to none", 
    	defaultValue: "none" , required: no

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
        capability "Media Playback"
        capability "Media Playback Shuffle"
        capability "Media Playback Repeat"
        capability "Speech Synthesis"
        capability "Audio Volume"
        capability "Audio Mute"
        capability "Audio Track Data"
        capability "Audio Notification"
        capability "Health Check"
        
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
		standardTile("stop", "device.status", width: 2, height: 2) {
			state "stopped", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#cf6567"
			state "playing", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
   			state "paused", label:'', action:"music Player.stop", icon:"st.sonos.stop-btn", backgroundColor:"#ffffff"
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
        
        standardTile("power", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.samsung.da.RC_ic_power", backgroundColor: "#cf6567", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.samsung.da.RC_ic_power", backgroundColor: "#79b821", nextState: "off"
		}

		main "mediaMulti"
		details(["mediaMulti","stop","shuffle","repeat", "button1", "button2", "button3","power"])
	}
}

def installed() {

	sendEvent(name: "level", value: 20)
	sendEvent(name: "mute", value: "unmuted")
	sendEvent(name: "status", value: "stopped")
	sendEvent(name: "trackDescription", value: "Stopped")

    setHealth()
	setTimer()

}

def updated() {
	
    unschedule()
	setHealth()
    setTimer()

}

def parse(description) {


} /* End of Parse */

def on() {

	def playerId = device.deviceNetworkId
	def params = '"power",1'
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def off() {

	def playerId = device.deviceNetworkId
	def params = '"power",0'
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def play() {

	def playerId = device.deviceNetworkId
   	def params = '"play"'
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def pause() {

	def playerId = device.deviceNetworkId
   	def params = '"pause",1'
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def stop() {

  	def playerId = device.deviceNetworkId
    def path_data
	if (shuffleOff == null || shuffleOff == "shuffle" || shuffleOff == "both") {setPlaybackShuffle("0")}
	if (shuffleOff == null || shuffleOff == "repeat" || shuffleOff == "both") {setPlaybackRepeatMode("0")}
   	def params = '"stop"'
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def previousTrack() {
	
  	def playerId = device.deviceNetworkId
   	def params = '"playlist","index","-1"'
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def nextTrack() {

  	def playerId = device.deviceNetworkId
   	def params = '"playlist","index","+1"'
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def mute() {

	def playerId = device.deviceNetworkId
   	def params = '"mixer","muting",1'
	parent.makeJSONcall(params, playerId, "JSONhandler")
   	sendEvent(name: "mute", value: "muted")
}

def unmute() {

	def playerId = device.deviceNetworkId 
   	def params = '"mixer","muting",0'
	parent.makeJSONcall(params, playerId, "JSONhandler")
   	sendEvent(name: "mute", value: "unmuted")

}

def setLevel(level) {

	def playerId = device.deviceNetworkId
   	def params = "\"mixer\",\"volume\",${level}"
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def setVolume(volume) {

	setLevel(volume)

}

def volumeUp() {

	def playerId = device.deviceNetworkId
    def params = "\"mixer\",\"volume\",\"+2\""
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def volumeDown() {

	def playerId = device.deviceNetworkId
    def params = "\"mixer\",\"volume\",\"-2\""
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def setMute(muteState) {

	if (muteStated == "muted") {mute()}
	if (muteStated == "unmuted") {unmute()}

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
   	def params = "\"playlist\",\"shuffle\",${controlValue}"
	parent.makeJSONcall(params, playerId, "JSONhandler")

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

	def playerId = device.deviceNetworkId
   	def params = "\"playlist\",\"repeat\",${controlValue}"
   	parent.makeJSONcall(params, playerId, "JSONhandler")

}


def speak(msg, inVoice=null) {

  	def playerId = device.deviceNetworkId
   	def sound = getSound(msg, inVoice)
    def playURI = sound.uri?.replaceFirst("^https","http")
    def durationDelay = Math.round((sound.duration.toInteger() * 1000) + 5000)
	def volume = speechVolume ?: null
//	log.debug playURI
    def params = statusCommand
    params = "${params},\"action:speak\",\"playURI: ${playURI}\",\"durationDelay:${durationDelay}\",\"volume:${volume}\""
//	log.debug params
	parent.makeJSONcall(params, playerId, "JSONhandler")
 
}

def playTrack(trackToPlay) {

	def playerId = device.deviceNetworkId
   	def params = "\"playlist\",\"play\",\"${trackToPlay}\""
    parent.makeJSONcall(params, playerId, "JSONhandler")

}

def playTrackAndResume(uri, duration, volume=null) {

  	def playerId = device.deviceNetworkId
    def playURI = uri.replaceFirst("^https","http")
   	def durationDelay = Math.round((duration.toInteger() * 1000) + 5000)
    def params = statusCommand
    params = "${params},\"action:resume\",\"playURI: ${playURI}\",\"durationDelay:${durationDelay}\",\"volume:${volume}\""    
//    def params = "\"status\",\"-\",1,\"tags:al\",\"action:resume\",\"playURI: ${playURI}\",\"durationDelay:${durationDelay}\",\"volume:${volume}\""
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def playTrackAndRestore(uri, duration, volume=null) {

  	def playerId = device.deviceNetworkId
	def playURI = uri.replaceFirst("^https","http")
    def durationDelay = Math.round((duration.toInteger() * 1000) + 5000)    
    def params = statusCommand
    params = "${params},\"action:restore\",\"playURI: ${playURI}\",\"durationDelay:${durationDelay}\",\"volume:${volume}\""     
//    def params = "\"status\",\"-\",1,\"tags:al\",\"action:restore\",\"playURI: ${playURI}\",\"durationDelay:${durationDelay}\",\"volume:${volume}\""
	parent.makeJSONcall(params, playerId, "JSONhandler")

}

def custom(params) {

	def playerId = device.deviceNetworkId
    parent.makeJSONcall(params, playerId, "JSONhandler")

}


def setTimer() {

	runEvery1Minute(refresh)

}

def refresh() {

	def playerId = device.deviceNetworkId
    def params = statusCommand
	parent.makeJSONcall(params, playerId, "JSONhandler")
   
}

def updatePlayer(playerInfo) {

//	log.debug "Inside player update"

	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	switch(playerInfo.playerStatus) {
    	case "play" :
        	log.debug "REPORTED PLAYING"

            	sendEvent(name: "status", value: "playing")
				setPlaybackStatus(playerInfo.playerStatus)
        	break
        case "pause" :
        	log.debug "REPORTED PAUSED"

            	sendEvent(name: "status", value: "paused")
				setPlaybackStatus(playerInfo.playerStatus)
        	break
        case ["stop", null] :
        	log.debug "REPORTED STOPPED OR OFF"

            	sendEvent(name: "status", value: "stopped")
				setPlaybackStatus("stop")
        	break
        default:
    		log.debug "UNKNOWN PLAYER STATUS"
    }
    
    
    log.debug "PLAYER STATUS: ${device.currentValue("status")}"
//    log.debug "TRACK DESC ${playerInfo.longInfo}"
    sendEvent(name: "trackDescription", value: playerInfo.longInfo)
    
    if (playerInfo.playerVol != null) {
    	sendEvent(name: "level", value: playerInfo.playerVol)
        sendEvent(name: "volume", value: playerInfo.playerVol)
		if (playerInfo.playerVol >= 0) {
           	sendEvent(name: "mute", value: "unmuted")
        } else {
           	sendEvent(name: "mute", value: "muted")       
        }
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

    if  (playerInfo.power == 1) {sendEvent(name: "switch", value: "on")} 
    else {sendEvent(name: "switch", value: "off")}

}

def restorePlayer(actions, playerId, currentRepeat, currentVolume) {

	def playerCommand
	if (currentVolume) {
      	playerCommand = "\"mixer\",\"volume\",${currentVolume}"
        actions << [type:"JSON", data: playerCommand, player: playerId, handler: "noStatus"] 
		actions << [type:"delay", data: "200"]
    }
   	playerCommand = "\"playlist\",\"repeat\",${currentRepeat}"
    actions << [type:"JSON", data: playerCommand, player: playerId, handler: "noStatus"] 
   	actions << [type:"delay", data: "200"]
    
	return actions
}

def getSound(msg, inVoice) {

	def myVoice = inVoice ?: speechVoice ?: "Salli(en-us)"
    def splitVoice = myVoice.split("\\(")
	myVoice = splitVoice[0]

    def sound = [:]
    if (!msg) {msg = "You have requested a text to speech event, but have failed to supply the appropriate message." }
    try {
    	sound = textToSpeech(msg, myVoice)
    }
    catch (e) {
    	sound.uri = "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3"
        sound.duration = 10
    	log.error "An error occurred in the text to speech request:\n $e"
	}
//    log.debug "SOUND URI ${sound.uri}"
    if (sound.duration.toInteger() < 5  && squeezeLite == "yes") {
		msg = "The text you entered is too short for the SqueezeLite Player, please enter a longer phrase"
        try {
        	sound = textToSpeech(msg, myVoice)
        }
        catch (e) {
    		sound.uri = "http://s3.amazonaws.com/smartapp-media/sonos/bell1.mp3"
        	sound.duration = 10
    		log.error "An error occurred in the text to speech request:\n $e"
		}
    }
    
    return sound

}

def restoreNeeded(playURI, durationDelay, volume, source) {

	def resume = (source == "restore") ? false : true
  	def playerId = device.deviceNetworkId
    def currentLength = device.currentValue("playlistLength").toInteger() ?: 0
    def currentStatus = device.currentValue("status")
    def playerName = device.name         
	def currentRepeat = device.currentValue("playbackRepeatMode")
    def currentVolume = device.currentValue("level")

    def actions = []
	def playerCommand
        
    if (currentLength  > 0) {
       	playerCommand = "\"playlist\",\"save\",\"${playerName}-ST01list\""
        actions << [type:"JSON", data: playerCommand, player: playerId, handler: "noStatus"]
    	actions << [type:"delay", data: "200"]
    }
	actions << [type:"JSON", data: "\"playlist\",\"repeat\",0", player: playerId, handler: "noStatus"]
    actions << [type:"delay", data: "200"]
   	playerCommand = "\"playlist\",\"play\",\"${playURI}\",\"Speech%20Notification\""
    actions << [type:"JSON", data: playerCommand, player: playerId, handler: "JSONhandler"]
   	if (volume) { 
      	playerCommand = "\"mixer\",\"volume\",${volume}"
        actions << [type:"JSON", data: playerCommand, player: playerId, handler: "JSONhandler"]    
	}
    actions << [type:"delay", data: durationDelay]
   	actions = restorePlayer(actions, playerId, currentRepeat, currentVolume)
    if (currentLength  > 0) {
		if (currentStatus == "playing" && resume) {
        	playerCommand = "\"playlist\",\"resume\",\"${playerName}-ST01list\",\"noplay:0\""
        } else {
        	playerCommand = "\"playlist\",\"resume\",\"${playerName}-ST01list\",\"noplay:1\""
        }
        actions << [type:"JSON", data: playerCommand, player: playerId, handler: "JSONhandler"]
	    actions << [type:"delay", data: "500"]    
        playerCommand = "\"playlists\",0,999,\"list:${playerName}-ST01list\",\"action:delete\""
        actions << [type:"JSON", data: playerCommand, player: "-", handler: "JSONhandler"]
    } else {
        actions << [type:"JSON", data: '"playlist","clear"', player: playerId, handler: "JSONhandler"]
    }
    
    if (playURI) {parent.multiHubAction(actions)}  else {log.debug "${action} Failed"}

}

def button1() {

	sendEvent(name: "buttonOne", value: "on", isStateChange: true, displayed: false)
	def playerId = device.deviceNetworkId
    def params = button1Command
    if (params) {
		if (button1Extra == "shuffle" || button1Extra == "both") {setPlaybackShuffle("1")} else (setPlaybackShuffle("0"))
		if (button1Extra == "repeat" || button1Extra == "both") {setPlaybackRepeatMode("2")} else (setPlaybackRepeatMode("0"))   
	    parent.makeJSONcall(params, playerId, "JSONhandler")
    }
	sendEvent(name: "buttonOne", value: "off", isStateChange: true, displayed: false)
    
}

def button2() {

	sendEvent(name: "buttonTwo", value: "on", isStateChange: true, displayed: false)
	def playerId = device.deviceNetworkId
    def params = button2Command
    if (params) {
		if (button2Extra == "shuffle" || button2Extra == "both") {setPlaybackShuffle("1")} else (setPlaybackShuffle("0"))
		if (button2Extra == "repeat" || button2Extra == "both") {setPlaybackRepeatMode("2")} else (setPlaybackRepeatMode("0"))     
	    parent.makeJSONcall(params, playerId, "JSONhandler")
	}
	sendEvent(name: "buttonTwo", value: "off", isStateChange: true, displayed: false)
    
}

def button3() {

	sendEvent(name: "buttonThree", value: "on", isStateChange: true, displayed: false)
	def playerId = device.deviceNetworkId
    def params = button3Command
    if (params) {
		if (button3Extra == "shuffle" || button3Extra == "both") {setPlaybackShuffle("1")} else (setPlaybackShuffle("0"))
		if (button3Extra == "repeat" || button3Extra == "both") {setPlaybackRepeatMode("2")} else (setPlaybackRepeatMode("0"))     
	    parent.makeJSONcall(params, playerId, "JSONhandler")
    }
	sendEvent(name: "buttonThree", value: "off", isStateChange: true, displayed: false)
    
}

def getStatusCommand() {
	return '"status","-",1,"tags:al"'
}

def setPlaybackStatus(status) {

   	sendEvent(name: "playbackStatus", value: status)

}

//  Device Health related code

def setHealth() {
//	log.debug device.hub.id
	sendEvent(name: "DeviceWatch-DeviceStatus", value: "online")
	sendEvent(name: "healthStatus", value: "online")
	sendEvent(name: "DeviceWatch-Enroll", value: "{\"protocol\": \"LAN\", \"scheme\":\"untracked\",  \"hubHardwareId\": \"${device.hub.hardwareID}\"}", displayed: false)

}
 // ************
 
private availableVoices() {

["Ivy(en-us)","Joanna(en-us)","Joey(en-us)","Justin(en-us)","Kendra(en-us)","Kimberly(en-us)","Salli(en-us)",
                "Amy(en-gb)","Brian(en-gb)","Emma(en-gb)","Miguel(es-us)","Penelope(es-us)","Ruben(nl-NL)","Lotte(nl-NL)","Mads(da-DK)","Naja(da-DK)","Russell(en-AU)","Nicole(en-AU)",
                "Aditi(en-IN hi-IN)","Raveena(en-IN)","Geraint(en-GB-WLS)","Mathieu(fr-FR)","Celine(fr-FR)","Léa(fr-FR)","Chantal (fr-CA)","Hans(de-DE)","Marlene(de-DE)","Vicki(de-DE)",
                "Karl(is-IS)","Dora(is-IS)","Giorgio(it-IT)","Carla(it-IT)","Takumi(ja-JP)","Mizuki(ja-JP)","Seoyeon(ko-KR)","Liv(nb-NO)","Jacek(pl-PL)","Jan(pl-PL)","Ewa(pl-PL)","Maja(pl-PL)",
                "Ricardo (pt-BR)","Vitoria (pt-BR)","Cristiano(pt-PT)","Ines(pt-PT)","Carmen(ro-RO)","Maxim(ru-RU)","Tatyana(ru-RU)","Enrique(es-ES)","Conchita(es-ES)","Astrid(sv-SE)",
                "Filiz(tr-TR)","Gwyneth(cy-GB)","Zhiyu(cmn-CN)"]

}