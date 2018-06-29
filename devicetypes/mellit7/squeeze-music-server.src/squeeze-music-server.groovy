/**
 *  Squeeze Music Server
 *
 *  Version 1.2 June 29, 2018
 *
 *  Written by Melinda Little 2018
 *
 *  Smartthings control for Logictech Media Server also known as Squeezebox.  Many thanks to the Smartthings community for the numerous 
 *  code snippets and problem solving solutions that they have shared
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
    
	section("Server Information"){
			input "internal_ip", "text", title: "Server IP, should be a static address", required: true, displayDuringSetup: true
			input "internal_port", "text", title: "Server Port (usually 9000)", required: true, displayDuringSetup: true

	
	}
 	section("Player Information"){
        	input "player_mac1", "text", title: "Player 1 MAC ID", required: false, displayDuringSetup: true  
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
        
        command "makeLANcall", ["string", "string", "string"]
 
        attribute "numPlayers", "string"
        attribute "playerCount", "number"
        attribute "buildingPlayer", "string"
	}

	tiles(scale: 2) {
	
		valueTile("DeviceLabel", "device.currentActivity", width: 6, height: 2, decoration: "flat") {
			state "currentActivity", label:'${currentValue}'
        }    
		valueTile("Players", "device.numPlayers", width: 6, height: 2, decoration: "flat") {
			state "numPlayers", label:'${currentValue}'
        }    
		valueTile("PlayerNum", "device.playerCount", width: 6, height: 2, decoration: "flat", canChangeIcon: true) {
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

/*     def headerMap = msg.headers
	  headerMap.each { key, value ->
	   		log.debug "KEYVAL:  $key VALCONTENTS  $value"
      } */

    def body = msg.body
//   log.debug "body: ${body}"
	finishParse(body)

} /* End of Parse */


def makeLANcall(path_cmd, playerMAC, callHandler) {

	def port = internal_port ?: 9000
	if (callHandler == null) {callHandler = "standardHandler"}
    if (path_cmd == "status") {
	 	path_cmd = "/status.html?player=${playerMAC}"
    } else {
    	path_cmd = "/status.html?${path_cmd}&player=${playerMAC}"
     
    }
//     log.debug  "Path Command '${path_cmd}'"
	def result = [delayAction(200), new physicalgraph.device.HubAction(
				method: "GET",
				path: "${path_cmd}",
				headers: [
					HOST: "${internal_ip}:${port}"
				], null, [callback: "${callHandler}"]
				)]
	sendHubCommand(result)

	log.debug result

}

def refresh(playerMAC) {

    def path_data = "status"
    makeLANcall(path_data, playerMAC, null)

}

def playerBuild(index) {
		sendEvent(name: "buildingPlayer", value: index)
//	def playerMAC = null
    def playerList = [player_mac1, player_mac2, player_mac3, player_mac4, player_mac5]
    def playerMAC = playerList[index-1]

	log.debug "Player ${index} mac: ${playerMAC}"
    
	if (playerMAC != null) {
    	def path_data = "status"
//		def buildHandler = "buildHandler${index}"
//     	log.debug "HANDLER: ${buildHandler}"
		makeLANcall(path_data, playerMAC, "buildHandler")
	}
    else{
    	if (index < 5) {
        	playerBuild(index+1)
        }
    
    }
}

def buildHandler(description) {
	def currentPlayer = device.currentValue("buildingPlayer").toInteger()
//	log.debug " ***IN buildhandler ${currentPlayer}"
    def body = description.body
	finishParse(body)
   	if (currentPlayer < 5) {playerBuild(currentPlayer+1)}

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
//   	log.debug "In Check Found Child: ${foundChild}"
   	if (foundChild == null){buildPlayer(playerMAC, playerName)}  // Player doesn't already exist

//Reload child list to update with new device

    children = getChildDevices()
    numChildren = children.size()
//    log.debug "Num Children: ${numChildren}"
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

	log.debug "IN Build Player"
 
	log.debug "New Player:  ${playerMAC} ${playerName}"
                
	addChildDevice("Mellit7", "Squeeze Music Player", playerMAC, null,
                            [completedSetup: true, isComponent: false, name: playerName])
	
}

def initialize() {

    def hexID = setNetworkID()
	def displayText = "Logitech Media Server at ${internal_ip}:${internal_port}\n${hexID}"
	sendEvent(name: "currentActivity", value: displayText)

//	createPlayers()
    playerBuild(1)

}

def setNetworkID() {

    def deviceHexID = "${convertIPtoHex(internal_ip)}:${convertPortToHex(internal_port)}"
    device.deviceNetworkId = deviceHexID
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

def standardHandler(description) {
//	log.debug "----In Standard Handler"
    def body = description.body
	finishParse(body)

}

def finishParse(body) {

    def split1 = body.split('d value="')
//   log.debug " FIRST SPLIT : ${split1.size()}"

    def split2 = split1[1].split('">')
    def playerId = split2[0]
    def playerInfo = [playerId : split2[0]]
    log.debug "PLAYER???  : ${playerId}"

	def playerName = split2[1].split('<')[0]
    playerInfo.playerName = split2[1].split('<')[0]
    log.debug "Player Name: ${playerName}"
    
    def newBody = split1[1]
//    log.debug newBody

    split2 = newBody.split('playingStatus">')
//   log.debug "SPLIT 2  ${split2[1]}"

	def probeStatus = split2[1].split('Now ')[1]
//    def playerStatus
    
    if (probeStatus?.startsWith("stopped")) {
//    	log.debug "Player status Stopped"
//        playerStatus = "stop"
        playerInfo.playerStatus = "stop"
    }
	else {
    	if (probeStatus?.startsWith("paused")) {
// 			 log.debug "Player status paused"
//             playerStatus = "pause"
             playerInfo.playerStatus = "pause"
       }
    	else {
        	if (probeStatus?.startsWith("Playing")){
//        		log.debug "Player status playing"
//                playerStatus = "play"
		        playerInfo.playerStatus = "play"                
        	}
            else {
            	log.debug "Player Status Unknown"
            }
        
        }
    
    }
	def findPlaylist = probeStatus.split("playingSong")[0]
    findPlaylist = findPlaylist.split("of ")
    def playlistLength
    if (findPlaylist.size() > 1) {
    	playlistLength = findPlaylist[1].split(":")[0].toInteger()
    } else {playlistLength = 0}
//    log.debug "playlist Length : ${playlistLength} ${playerInfo.playerName}"
	playerInfo.playlistLength = playlistLength
    newBody = probeStatus.split("playingSong")[1]

	def trackName = newBody.split('browser">')[1].split('</a')[0].replaceAll("\\s+", " ")
   	def album
    def artist    
//    log.debug "Track: ${trackName}"

  	if (trackName.length() > 0) {
   		newBody = probeStatus.split("from <")

        if (newBody.size() > 1) {
			album = newBody[1].split('browser">')[1].split('</a')[0]
        } else {album = null}
//		log.debug album

    	newBody = probeStatus.split("by <a")
        if (newBody.size() > 1) {
			artist = newBody[1].split('inline">')[1].split('</span')[0]
        } else {artist = null}
//    	log.debug artist
        
    } 
    else {
    	trackName = 'Nothing'
        album = null
        artist = null
    
    }
 
    newBody = probeStatus.split("Repeat")[1]
	def repeatWord = newBody.split('<b>')[1].split('</b')[0]
//    log.debug "Repeat: ${repeatWord}"
    def repeat 
    
    switch (repeatWord) {
    		case "off":
        		repeat = 0
        		break
    		case "one":
       			repeat = 1
        		break
    		case "all":
        		repeat = 2
                break    
       		default:
        		repeat = 0
        }  
	playerInfo.repeat = repeat        

    newBody = probeStatus.split("Shuffle")[1]
	def shuffleWord = newBody.split('<b>')[1].split('</b')[0]
//    log.debug "Shuffle: ${shuffleWord}"
    def shuffle 
    
    switch (shuffleWord) {
    		case "off":
        		shuffle = 0
        		break
    		case "songs":
       			shuffle = 1
        		break
    		case "albums":
        		shuffle = 2
                break    
       		default:
        		shuffle = 0
        }
    playerInfo.shuffle = shuffle
    
    newBody = probeStatus.split("Volume")[1]
	def playerVol = newBody.split('<b>')[1].split('</b')[0].toInteger()
//    log.debug "Volume: ${playerVol}"
    playerVol = (playerVol-1) * 10
//    log.debug "Volume: ${playerVol}"
    playerInfo.playerVol = playerVol

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

	playerInfo.longInfo = longInfo

/*	  playerInfo.each { key, value ->
	   		log.debug "$key : $value"
      } */

//Update the Player

    if (childPlayer) {
//    	log.debug "Updating Player ${playerName}"
        childPlayer.updatePlayer(playerInfo)
    }

//Update number of players on Server    
	def children = getChildDevices()
    def numChildren = children.size()
    def playersLabel = "Number of Players: ${numChildren}"
    sendEvent(name: "numPlayers", value: playersLabel)
    sendEvent(name: "playerCount", value: numChildren)


} //End Finish Parse

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay ${time}")
}

def multiHubAction(actions) {  //Process multiple hub actions as a group

	def port = internal_port ?: 9000
	def hubactions = []
	for (i in 0..(actions.size()-1)) {
    	switch (actions[i].type) {
        	case "delay":
        		hubactions[i] = new physicalgraph.device.HubAction("delay ${actions[i].data}")
        		break;
        	case "path":
       			hubactions[i] = new physicalgraph.device.HubAction(method: "GET",path: "/status.html?${actions[i].data}&player=${actions[i].player}",headers: [HOST: "${internal_ip}:${port}"], null, [callback: standardHandler])
        		break
        	case "status":
       			hubactions[i] = new physicalgraph.device.HubAction(method: "GET",path: "/status.html?player=${actions[i].player}",headers: [HOST: "${internal_ip}:${port}"], null, [callback: standardHandler])
        		break        
       		 default:
        		log.debug "BAD MULTI HUB COMMAND"
        
        }
	}

//	log.debug hubactions
   	sendHubCommand(hubactions)

}
