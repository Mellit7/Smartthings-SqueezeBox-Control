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

//   def headerString = msg.header
//	 log.debug headerString

//     def headerMap = msg.headers
/*	  headerMap.each { key, value ->
	   		log.debug "KEYVAL:  $key VALCONTENTS  $value"
      } */


    def body = msg.body
//   log.debug "body: ${body}"
	finishParse(body)

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
	 	path_cmd = "/status.html?player=${playerMAC}"
     } else {
    	path_cmd = "/status.html?${path_cmd}&player=${playerMAC}"
     
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
    
/*    def playerMAC 
    
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
	} */
    
    playerBuild(1)
}

def playerBuild(index) {

	def playerMAC

    switch (index) {
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
	log.debug "Player ${index} mac: ${playerMAC}"
	if (playerMAC != null) {
		def buildHandler = "buildHandler${index}"
    	log.debug buildHandler
		def port
		if (internal_port){
			port = "${internal_port}"
		} 
   		else {
			port = 9000
		}

		def path_cmd = "/status.html?player=${playerMAC}"
      
//     log.debug  "Path Command '${path_cmd}'"
		def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "${path_cmd}",
				headers: [
					HOST: "${internal_ip}:${port}"
				], null,
                [callback: "${buildHandler}"]
				)
		sendHubCommand(result)

		log.debug result
	}
    else{
    	if (index < 5) {
        	playerBuild(index+1)
        }
    
    }
}

def buildHandler1(physicalgraph.device.HubResponse description) {

	log.debug "buildhandler1"
    def body = description.body
	finishParse(body)
   	playerBuild(2)

}

def buildHandler2(description) {

    def body = description.body
	finishParse(body)
   	playerBuild(3)

}

def buildHandler3(description) {

    def body = description.body
	finishParse(body)
   	playerBuild(4)

}

def buildHandler4(description) {

    def body = description.body
	finishParse(body)
   	playerBuild(5)

}

def buildHandler5(description) {

    def body = description.body
	finishParse(body)

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
    log.debug "foundChildIndex: ${foundChildIndex}"
    
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

def finishParse(body) {

   def split1 = body.split('d value="')
//   log.debug " FIRST SPLIT : ${split1.size()}"

   def split2 = split1[1].split('">')
   def playerId = split2[0]
    log.debug "PLAYER???  : ${playerId}"

	def playerName = split2[1].split('<')[0]
    log.debug "Player Name: ${playerName}"
    
    def newBody = split1[1]
//    log.debug newBody

    split2 = newBody.split('playingStatus">')
//   log.debug "SPLIT 2  ${split2[1]}"

	def probeStatus = split2[1].split('Now ')[1]
    def playerStatus
    
    if (probeStatus?.startsWith("stopped")) {
//    	log.debug "Player status Stopped"
        playerStatus = "stop"
    }
	else {
    	if (probeStatus?.startsWith("paused")) {
// 			 log.debug "Player status paused"
             playerStatus = "pause"
    	}
    	else {
        	if (probeStatus?.startsWith("Playing")){
//        		log.debug "Player status playing"
                playerStatus = "play"
        	}
            else {
            	log.debug "Player Status Unknown"
            }
        
        }
    
    }

    newBody = probeStatus.split("playingSong")[1]

	def trackName = newBody.split('browser">')[1].split('</a')[0]
   	def album
    def artist    
//    log.debug "Track: ${trackName}"

  	if (trackName.length() > 0) {
   		newBody = probeStatus.split("from")[1]
		album = newBody.split('browser">')[1].split('</a')[0]
//    	log.debug album
    
    	newBody = probeStatus.split("by <a")[1]
		artist = newBody.split('inline">')[1].split('</span')[0]
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
        
    newBody = probeStatus.split("Volume")[1]
	def playerVol = newBody.split('<b>')[1].split('</b')[0].toInteger()
//    log.debug "Volume: ${playerVol}"
    playerVol = playerVol * 9
//    log.debug "Volume: ${playerVol}"
    

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


}