/**
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
    input name: "shuffleOff", type: "enum", title: "Turn off Shuffle", options: ["yes", "no"], description: "Turn off shuffle with stop command?", required: no
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
        
        command "updatePlayer", ["string","string","string","string"]
        command "custom", ["string"]

     
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "mediaMulti", type:"mediaPlayer", width:6, height:4, canChangeIcon: true) {
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
        
/*        standardTile("repeat", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Repeat', action: "switch.on", icon: "st.secondary.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'Repeat', action: "switch.off", icon: "st.illuminance.illuminance.light", backgroundColor: "#79b821", nextState: "off"
		}  */

		main "mediaMulti"
		details(["mediaMulti","stop","shuffle"/*,"repeat"*/])
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
	parent.makeLANcall(path_data, playerId)  

	log.debug "Executing PLAY Player: $playerId" 

//	sendEvent(name: "status", value: "playing")

}

def pause() {

	def playerId = device.deviceNetworkId
    def path_data = "p0=pause&p1=1"
    
	parent.makeLANcall(path_data, playerId)
	log.debug "Executing PAUSE Player: $playerId" 
    
//    sendEvent(name: "status", value: "paused")

}

def stop() {

  	def playerId = device.deviceNetworkId
    def path_data
	if (shuffleOff == null || shuffleOff == "yes") { 
	   	path_data = "p0=playlist&p1=shuffle&p2=0"  //Turn off Shuffle
		parent.makeLANcall(path_data, playerId)
    }
    path_data = "p0=stop"
   	parent.makeLANcall(path_data, playerId)
   	log.debug "Executing STOP Player: $playerId" 
    
//    sendEvent(name: "status", value: "stopped")

}

def previousTrack() {
	
  	def playerId = device.deviceNetworkId
    def path_cmd = "p0=playlist&p1=jump&p2=-1"
    
	parent.makeLANcall(path_cmd, playerId)
//    log.debug "Executing Previous Track Player: $playerId" 

}

def nextTrack() {

  	def playerId = device.deviceNetworkId
    def path_cmd = "p0=playlist&p1=jump&p2=%2b1"
	parent.makeLANcall(path_cmd, playerId)


}

def mute() {

	def playerId = device.deviceNetworkId
	def path_cmd = "p0=button&p1=muting"
	parent.makeLANcall(path_cmd, playerId)
//   	sendEvent(name: "mute", value: "muted")
}

def unmute() {

	def playerId = device.deviceNetworkId 
	def path_cmd = "p0=button&p1=muting"
	parent.makeLANcall(path_cmd, playerId)
   	sendEvent(name: "mute", value: "unmuted")

}

def setLevel(level) {

	def playerId = device.deviceNetworkId
	def path_cmd = "p0=mixer&p1=volume&p2=${level}"
   	parent.makeLANcall(path_cmd, playerId)

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
	parent.makeLANcall(path_data, playerId)

}

def custom(path_cmd) {

	def playerId = device.deviceNetworkId
   	parent.makeLANcall(path_cmd, playerId)

}


def setTimer() {

//	schedule("19 0/1 * * * ?", refresh)
	runEvery1Minute(refresh)

}

def refresh() {

	def playerId = device.deviceNetworkId
    def path_data = "status"
   	parent.makeLANcall(path_data, playerId)
    
}

def updatePlayer(playerStatus,longInfo,playerVol,shuffle) {

//	log.debug "Inside player update"

	switch(playerStatus) {
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
    
    sendEvent(name: "trackDescription", value: longInfo)
    
    if (playerVol != null) {
    	sendEvent(name: "level", value: playerVol)
    }

    if (shuffle != null) {
//  		log.debug "REPORTED SHUFFLE ${shuffle}"
       	sendEvent(name: "playbackShuffle", value: shuffle)
       
    }



}