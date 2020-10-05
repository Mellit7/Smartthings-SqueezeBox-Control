/**
 *  Squeeze Music Server
 *
 *  Version 2.1 November 13, 2018
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
		author: "Melinda Little", mnmn: "SmartThingsCommunity", vid: "e6e4fce5-1753-3b59-890c-53d5352d8228", ocfDeviceType: 'oic.d.networkaudio') {

//		capability "Actuator"
		capability "Switch"
		capability "Refresh"
//		capability "Sensor"
//		capability "Media Controller"
        capability "wanderwater41919.playerCount"
        capability "wanderwater41919.serverActivity"
        
        command "makeLANcall", ["string", "string", "string"]
        command "buildPlayer", ["string", "string"]
 
        attribute "numPlayers", "string"
//        attribute "playerCount", "number"
        attribute "buildingPlayer", "string"
        attribute "playerList", "string"
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

   log.debug "Entering Parse"

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
	def result = [delayAction(200), new physicalgraph.device.HubAction(
				method: "GET",
				path: "${path_cmd}",
				headers: [
					HOST: "${internal_ip}:${port}"
				], null, [callback: "${callHandler}"]
				)]
	sendHubCommand(result)

//	log.debug result

}

def makeJSONcall(params, playerMAC, callHandler) {

	def port = internal_port ?: 9000
	if (callHandler == null) {callHandler = "JSONhandler"}
    if(params == null) {params = statusCommand}
	def commandString = buildJSON(params, playerMAC)

	def result = [delayAction(200), new physicalgraph.device.HubAction(
				method: "POST",
				path: "/jsonrpc.js",
                body: "${commandString}",
				headers: [
					HOST: "${internal_ip}:${port}",
                    "Content-Type" : 'application/json'
				], null, [callback: "${callHandler}"]
				)]
	sendHubCommand(result)

//	log.debug result

}

def refresh(playerMAC) {

	def params = statusCommand
//    log.debug "REFRESH PARAMS ${params}"
	makeJSONcall(params, playerMAC, "JSONhandler")

}

def loadPlaylists() {

	def params = '"playlists",0,25'
	makeJSONcall(params, "-", "listHandler")

}

def listHandler(msg) {

    def body = msg.body
    def bodyJSON = new groovy.json.JsonSlurper().parseText(body)
//   	log.debug " ***IN listHandler ${bodyJSON}"
    def favsList = []
    if (bodyJSON.result.count.toInteger() > 0) {
    	for (int i = 0; i < bodyJSON.result.count.toInteger();i++) {
	    	favsList[i] = "{\"id\":\"${bodyJSON.result.playlists_loop[i].id}\",\"name\":\"${bodyJSON.result.playlists_loop[i].playlist}\"}"
    	}
    }
    def children = getChildDevices()
    for (int i = 0; i < children.size();i++) {
	    children[i].playlists(favsList)
	}
}

def playerBuild(index) {

	sendEvent(name: "buildingPlayer", value: index)

    def playerList = [player_mac1, player_mac2, player_mac3, player_mac4, player_mac5]
    def playerMAC = playerList[index-1]

	log.debug "Player ${index} mac: ${playerMAC}"
    
	if (playerMAC != null) {
		def params = statusCommand
		makeJSONcall(params, playerMAC, "buildHandler")

	}
    else{
    	if (index < 5) {
        	playerBuild(index+1)
        }
    
    }
}

def buildHandler(msg) {

    def playerList = [player_mac1, player_mac2, player_mac3, player_mac4, player_mac5]
	def currentPlayer = device.currentValue("buildingPlayer").toInteger()
    def playerMAC = playerList[currentPlayer-1]

    def body = msg.body
    def bodyJSON = new groovy.json.JsonSlurper().parseText(body)
   	log.debug " ***IN buildhandler ${currentPlayer}  REQUESTED ${playerMAC} FOUND ${bodyJSON.params[0]}"
    parseJSONstatus(bodyJSON)
   	if (currentPlayer < 5) {playerBuild(currentPlayer+1)}

}

def checkPlayer(playerMAC, playerName) {

//    log.debug "CHECK ${playerMAC} :: ${playerName}"
    def foundChild = findChild(playerMAC)   

   	if (foundChild == null){  // Player doesn't exist
    	buildPlayer(playerMAC, playerName)
        foundChild = findChild(playerMAC)  //Reload child list to update with new device
    }  

	log.debug "CHILD PLAYER :: ${foundChild}"
    return foundChild
}

def findChild(playerMAC) {

    def children = getChildDevices()
//    log.debug "Found Number of Children: ${children.size()}"
    def foundChildIndex  
    if (children.size() > 0) {
    	foundChildIndex = children.deviceNetworkId.findIndexOf { it == playerMAC}
    }
    else {
    	foundChildIndex = -1
    } 
//    log.debug "Found Child Index: ${foundChildIndex}"
    
    def childPlayer
    
	if (foundChildIndex > -1) {

        childPlayer = children[foundChildIndex]
    }
    else{
    	log.debug "Player ${playerMAC} Not Found"
        childPlayer = null
    }
	return childPlayer
}

def buildPlayer(playerMAC, playerName) {

	log.debug "New Player:  ${playerMAC} ${playerName}"
                
	addChildDevice("Mellit7", "Squeeze Music Player", playerMAC, null,
                            [completedSetup: true, isComponent: false, name: playerName, label: playerName])
	
}

def initialize() {

    def hexID = setNetworkID()
	def displayText = "${internal_ip}:${internal_port}\n${hexID}"
	sendEvent(name: "currentActivity", value: displayText)
	loadPlaylists()
    runEvery15Minutes(loadPlaylists)
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

	log.debug "OLD LAN COMMAND NEEDS UPDATING"
    def split1 = body.split('d value="')
//   log.debug " FIRST SPLIT : ${split1.size()}"

    def split2 = split1[1].split('">')
    def playerId = split2[0]

    log.debug "PARSE PLAYER : ${playerId}"

// Make JSON Status update call
	if (playerId != null) {
		def params = statusCommand
		makeJSONcall(params, playerId, "JSONhandler")
    }

} //End Finish Parse

def JSONhandler(msg) {
	
//    log.debug "*****IN JSON HANDLER"

//    def json = msg.json
    def body = msg.body
    def bodyJSON = new groovy.json.JsonSlurper().parseText(body)
    def responseType = bodyJSON.params[1][0]
    
//  log.debug "JSON Body result  ${bodyJSON.result}" 

/* 	bodyJSON.each { key, value ->
	   		log.debug " @@@@@@@ $key :  $value"	
	} */
	def playerInfo = [:]
    switch (responseType) {
    		case "status":
	            playerInfo = parseJSONstatus(bodyJSON)
            	if (bodyJSON.params[1].size()>4) {
//                	log.debug "====IN JSON PARSE STATUS ${bodyJSON.params[1][4]}"
                	def returned = [:]
                	for (i in 4..(bodyJSON.params[1].size()-1)) {
                    	returned << [(bodyJSON.params[1][i].split(":")[0]) : (bodyJSON.params[1][i].split(":",2)[1])]
					}
				    switch (returned.action) {
                    	case ["speak","resume","restore"]:
                        	def child = findChild(bodyJSON.params[0])
	                        if (child) {child.restoreNeeded(returned.playURI, returned.durationDelay, returned.volume, returned.action)}
	                        break
                    	default:
	                        break
                    }
                
				} 
        		break
       		case "playlists": 
            	if (bodyJSON.params[1].size()>3) {
                	def returned = [:]
                	for (i in 3..(bodyJSON.params[1].size()-1)) {
                    	returned << [(bodyJSON.params[1][i].split(":")[0]) : (bodyJSON.params[1][i].split(":",2)[1])]
					}
                    if (returned.action == "delete"){
                    	if (bodyJSON.result.count.toInteger() > 0) {
                   	    	def listIndex = bodyJSON.result.playlists_loop.playlist.findIndexOf { it == returned.list}
                            if (listIndex > -1) {
	                    		def params = "\"playlists\",\"delete\",\"playlist_id:${bodyJSON.result.playlists_loop[listIndex].id}\""
								makeJSONcall(params, "-", "JSONhandler")
//								log.debug "PLAYLIST SEARCH  ${bodyJSON.result.playlists_loop[listIndex].id} ${returned.list} ${bodyJSON.result.playlists_loop[listIndex].playlist} "
                            } else {log.debug "COULD NOT FIND PLAYLIST TO DELETE"}
                    	} else {log.debug "NO PLAYLISTS FOUND"}
                    }
                }    
                break
            default:
	            def params = statusCommand
				makeJSONcall(params, bodyJSON.params[0], "JSONhandler")
        		break
        }
       
/*	  playerInfo.each { key, value ->
	   		log.debug "JSON $key : $value"
      } */
   

}

def noStatus(msg) {
	// Empty handler when no parse action is required
}

def parseJSONstatus(bodyJSON) {

//	log.debug "IN JSON STATUS"

    def playerInfo = [playerId : bodyJSON.params[0]]
    playerInfo.playerName = bodyJSON.result.player_name
    log.debug "Player ${playerInfo.playerName} : ${playerInfo.playerId}"
	playerInfo.playerStatus = bodyJSON.result.mode
    def trackName
    def album
    def artist
    def coverId
    if (bodyJSON.result.playlist_loop) {
    	trackName = bodyJSON.result.playlist_loop[0]?.title ?: "Nothing"
     	album = bodyJSON.result.playlist_loop[0]?.album ?: bodyJSON.result.playlist_loop[0]?.remote_title ?: null
       	artist = bodyJSON.result.playlist_loop[0]?.artist ?: null
        coverId = bodyJSON.result.playlist_loop[0]?.coverid ?: bodyJSON.result.playlist_loop[0].id ?: null
        playerInfo.type = bodyJSON.result?.playlist_loop[0]?.type ?: "LMS"
        playerInfo.artURL = bodyJSON.result?.playlist_loop[0]?.artwork_url ?: null
    } else {
    	trackName = bodyJSON.result?.remoteMeta?.title ?: "Nothing"
        album = bodyJSON.result?.remoteMeta?.album ?: bodyJSON.result?.remoteMeta?.remote_title ?: null
        artist = bodyJSON.result?.remoteMeta?.artist ?: null
        coverId = bodyJSON.result?.remoteMeta?.coverid ?: bodyJSON.result?.remoteMeta?.id ?: null
        playerInfo.type = "UNKNOWN"
        playerInfo.artURL = null
    }
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
    if (artist !=null) {longInfo = "${artist} : ${longInfo}"}

//    if (playerStatus == "stop") { //Override if player is stopped
//    	longInfo =  longInfo = "Stopped : ${longInfo}"
//    }
//    log.debug "Track Description: ${longInfo}"

	playerInfo.longInfo = longInfo

//  *** New for Track Data

	playerInfo.album = album
   	playerInfo.artist = artist
	playerInfo.trackName = trackName    
	playerInfo.coverId = coverId
    playerInfo.ip = internal_ip
    playerInfo.port = internal_port ?: 9000

//  *** End new Track Data
    
    playerInfo.repeat =  bodyJSON.result['playlist repeat']
    playerInfo.shuffle =  bodyJSON.result['playlist shuffle']
//    log.debug "JSON REPEAT AND SHUFFLE ${playerInfo.repeat} ${playerInfo.shuffle}"
    
    playerInfo.playlistLength = bodyJSON.result.playlist_tracks ?: 0
    playerInfo.playerVol = bodyJSON.result['mixer volume']
    playerInfo.power = bodyJSON.result.power

/*    playerInfo.each { key, value ->
   		log.debug "$key : $value"
     }  */
     
           
//Find or build the player
	def childPlayer
	if (playerInfo.playerId != null) {
    	childPlayer = checkPlayer(playerInfo.playerId, playerInfo.playerName)
    } 
    
//	log.debug "After JSON player check ${childPlayer}"    

//Update the Player

    if (childPlayer) {
//    	log.debug "JSON Updating Player ${childPlayer}"
        childPlayer.updatePlayer(playerInfo)
    }
      
//Update number of players on Server    
	def children = getChildDevices()
    def numChildren = children.size()
    if (numChildren > 0) {sendEvent(name: "playerList", value: children.deviceNetworkId.join(","))} else {sendEvent(name: "playerList", value: "")}
    def playersLabel = "Number of Players: ${numChildren}"
    sendEvent(name: "numPlayers", value: playersLabel)
    sendEvent(name: "playerCount", value: numChildren)
     
	return playerInfo      

}

def buildJSON(params, playerMAC) {
	def commandString = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"${playerMAC}\",[${params}]]}"
    return commandString
}

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
           	case "JSON":
	            def handler = actions[i].handler ?: "JSONhandler"
            	def commandString = buildJSON(actions[i].data, actions[i].player)
       			hubactions[i] = new physicalgraph.device.HubAction(method: "POST",path: "/jsonrpc.js",body: "${commandString}",headers: [HOST: "${internal_ip}:${port}","Content-Type" : 'application/json'], null, [callback: "${handler}"])
//        		log.debug "JSON HUBACTION : ${hubactions[i]}"
                break     
       		 default:
        		log.debug "BAD MULTI HUB COMMAND"
        
        }
	}

//	log.debug hubactions
   	sendHubCommand(hubactions)

}

def getStatusCommand() {
	return '"status","-",1,"tags:alcKoN"'
}
