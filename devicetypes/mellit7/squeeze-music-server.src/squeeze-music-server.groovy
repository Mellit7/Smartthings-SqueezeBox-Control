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
    
	section("Server Information"){
        paragraph "Input the Logictech Media Server (Squeezebox) IP address and communication port.  By default it is 9000."
			input "internal_ip", "text", title: "Server IP, should be a static address", required: true, displayDuringSetup: true
			input "internal_port", "text", title: "Server Port (usually 9000)", required: true, displayDuringSetup: true

	
	}
 	section("Player Information"){
	    paragraph "Input the MAC Id for up to 5 players, include colons.  IP address can be used, but should be a static address."
        	input "player_mac1", "text", title: "Player 1 MAC ID", required: true, displayDuringSetup: true  
        	input "player_mac2", "text", title: "Player 2 MAC ID", required: false, displayDuringSetup: true
        	input "player_mac3", "text", title: "Player 3 MAC ID", required: false, displayDuringSetup: true
        	input "player_mac4", "text", title: "Player 4 MAC ID", required: false, displayDuringSetup: true
           	input "player_mac5", "text", title: "Player 5 MAC ID", required: false, displayDuringSetup: true
    }
    
}

metadata {
	definition (
		name: "Squeeze Music Server",
		namespace: "Mellit7",
		author: "Melinda Little") {

		capability "Actuator"
		capability "Switch"
		capability "Refresh"
		capability "Sensor"
		capability "Media Controller"
//   		capability "Music Player"
        
        command "makeLANcall", ["string", "string"]
//        command "createPlayers"
 
        attribute "numPlayers", "string"
        attribute "playerCount", "number"
	}

	tiles(scale: 2) {
	
		valueTile("DeviceLabel", "device.currentActivity", width: 6, height: 2, decoration: "flat") {
			state "currentActivity", label:'${currentValue}'
        }    
		valueTile("Players", "device.numPlayers", width: 6, height: 2, decoration: "flat") {
			state "numPlayers", label:'${currentValue}'
        }    
		valueTile("PlayerNum", "device.playerCount", width: 6, height: 2, decoration: "flat") {
			state "playCount", label:'${currentValue}'
		}
          
		main "PlayerNum"
		details("DeviceLabel", "Players")
	}
}

def installed() {
	log.debug "INSTALLED"
    initialize()

}

def updated() {

	def lastUpdateDate = (state.updatedDate != null) ? state.updatedDate : 0
	if ( (Calendar.getInstance().getTimeInMillis() - lastUpdateDate) < 5000 ) return
	log.debug "UPDATED"

    initialize()
    
	state.updatedDate = Calendar.getInstance().getTimeInMillis()

}

def parse(description) {

//   log.debug "Entering Parse"

	def msg = parseLanMessage(description)

//	 log.debug "MSG: ---- ${msg}"
//   def headerString = msg.header
//	 log.debug headerString

     def headerMap = msg.headers
/*	  headerMap.each { key, value ->
	   		log.debug "KEYVAL:  $key VALCONTENTS  $value"
      } */


//   def body = msg.body
//   log.debug "body: ${body}"
//   log.debug  "Description: ${description}"


// Get Info of interest
    def artist = headerMap['x-playerartist']
    def trackName = headerMap['x-playertitle']
    def playerStatus = headerMap['x-playermode']
    def volume = headerMap['x-playervolume']
   	def playerName  = headerMap['x-playername']
   	def playerId  = headerMap['x-player']
   	def repeat  = headerMap['x-playerrepeat']
	def shuffle = headerMap['x-playershuffle']
    def album = headerMap['x-playeralbum']

//	log.debug "SHUFFLE : ${shuffle}"

//Find or build the player
	def childPlayer
	if (playerId != null) {
    	childPlayer = checkPlayer(playerId, playerName)
    } 
    
//	log.debug "After player check ${childPlayer}"
    
 // Build Track Description

	def longInfo    
 	if (album && trackName) {
    
   		longInfo = "${album} - ${trackName}"                           
	
    } else {  //At least one element Missing
    
    	if (trackName) { //Use only track name

			longInfo =  "${trackName}"
            
        } else {  //No track info

    		longInfo =  "Empty"
		}
	}
    if (artist !=null) {
        longInfo = "${artist} : ${longInfo}"
    }

//    if (playerStatus == "stop") { //Override if player is stopped
//    	longInfo =  "Stopped"
//    }
//    log.debug "Track Description: ${longInfo}"

//  Set Volume

//    log.debug "VOLUME -- ${volume}"
	def playerVol = (volume) ? volume.toInteger() : null  

//Update the Player

    if (childPlayer) {
//    	log.debug "Updating Player ${playerName}"
        childPlayer.updatePlayer(playerStatus,longInfo,playerVol,shuffle)
    }

//Update number of players on Server    
	def children = getChildDevices()
    def numChildren = children.size()
    def playersLabel = "Number of Players: ${numChildren}"
    sendEvent(name: "numPlayers", value: playersLabel)
    sendEvent(name: "playerCount", value: numChildren)

} /* End of Parse */


def makeLANcall(path_cmd, playerMAC) {

//    sendEvent(name: "pName", value: "")
	def port
	if (internal_port){
			port = "${internal_port}"
	} 
    else {
			port = 9000
	}
     if (path_cmd == "status") {
	 	path_cmd = "/status.txt?player=${playerMAC}"
     } else {
    	path_cmd = "/status.txt?${path_cmd}&player=${playerMAC}"
     
     }
//     log.debug  "Path Command '${path_cmd}'"
	def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "${path_cmd}",
				headers: [
					HOST: "${internal_ip}:${port}"
				]
				)
	sendHubCommand(result)

	log.debug result

}

def refresh(playerMAC) {

    def path_data = "status"
    makeLANcall(path_data, playerMAC)

}

def createPlayers() {

	log.debug "IN CREATE PLAYERS"
    
    def playerMAC 
    
    for (i in 1..5) {
    	switch (i) {
    		case 1:
        		playerMAC = player_mac1
        		break
    		case 2:
       			playerMAC = player_mac2
        		break
    		case 3:
        		playerMAC = player_mac3
        		break    
    		case 4:
        		playerMAC = player_mac4
        		break
    		case 5:
        		playerMAC = player_mac5
        		break    
    		default:
        		playerMAC = null
        }
		log.debug "Player ${i} mac: ${playerMAC}"
        
        if (playerMAC !=null) {
			refresh(playerMAC)           
		}
	}
}
def checkPlayer(playerMAC, playerName) {

//	log.debug "Check Player"
//    log.debug "CHECK ${playerMAC} :: ${playerName}"
    
    def children = getChildDevices()

    def numChildren = children.size()

//    log.debug "Check Number of Children: ${numChildren}"
    
    def foundChild     
   	if (numChildren > 0) {
   		foundChild = children.deviceNetworkId.find { it == playerMAC}
 
    }
    else {
       	foundChild = null
    } 
   	log.debug "Found Child: ${foundChild}"
   	if (foundChild == null){    // Player doesn't already exist

        buildPlayer(playerMAC, playerName)
    }


//Reload child list to update with new device

    children = getChildDevices()
    numChildren = children.size()
    log.debug "Num Children: ${numChildren}"
    def foundChildIndex  
    if (children.size() > 0) {
    	foundChildIndex = children.deviceNetworkId.findIndexOf { it == playerMAC}
    }
    else {
    	foundChildIndex = null
    } 
//    log.debug "foundChildIndex: ${foundChildIndex}"
    
    def childPlayer
    
	if (foundChildIndex != null) {

        childPlayer = children[foundChildIndex]
    }
    else{
    	log.debug "FOUND INDEX FAILED"
        childPlayer = null
    }
    
    log.debug "CHILD PLAYER :: ${childPlayer}"
    return childPlayer
}

def buildPlayer(playerMAC, playerName) {

//	log.debug "IN Build Player"
 
	log.debug "New Player:  ${playerMAC} ${playerName}"
                
	addChildDevice("MLittle", "Squeeze Music Player", playerMAC, null,
                            [completedSetup: true, isComponent: false, name: playerName])
            
        
	
}

def initialize() {

    def hexID = setNetworkID()
	def displayText = "Logitech Media Server at ${internal_ip}:${internal_port}\n${hexID}"
	sendEvent(name: "currentActivity", value: displayText)

	createPlayers()

}

def setNetworkID() {

	def hexIP = convertIPtoHex(internal_ip)
    def hexPort = convertPortToHex(internal_port)
    def deviceHexID = "${hexIP}:${hexPort}"
//    device.deviceNetworkId = "C0A8010D:2328"
    device.deviceNetworkId = deviceHexID
//    log.debug "HEX ID:  ${deviceHexID}"
	return deviceHexID
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
//    log.debug "IP address entered is $ipAddress and the converted hex code is $hex"
    return hex
}

private String convertPortToHex(port) {
    String hexport = port.toString().format( '%04X', port.toInteger() )
//    log.debug hexport
    return hexport
}