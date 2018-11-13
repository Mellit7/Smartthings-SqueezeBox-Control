/**
 *
 *  Squeezebox Player Installation Helper
 *
 *  Version 1.0 November 13, 2018
 *
 *  Author: Melinda Little 2018
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

definition(
    name: "Squeezebox Player Install",
    namespace: "mellit7",
    author: "Melinda Little",
    description: "Automate installation of players for a Logitech Media Server (Squeezebox)",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Electronics/electronics14-icn@2x.png"
)

preferences {
	page(name: "pageOne", title: "Player Installation", uninstall: true, nextPage: "selectPlayers") {
//    href(name: "discover",title: "Player Installation",required: false,page: "selectPlayers"){
		section("Select the Music Server") {
			input "theServer", "device.SqueezeMusicServer", multiple: false, required: true, title: "Server?"
//    	href(name: "discover",title: "Player Installation",required: false,page: "selectPlayers", description: "tap to find players")
    paragraph "Press Next to select players"
		}
	}
    page(name: "selectPlayers", title: "Select Players to Install")
    page(name: "finished", title: "Players Installed")    
}

def installed()
{
	log.debug "INSTALLED"
 
}

def updated()
{
	log.debug "UPDATED"

}

def selectPlayers() {
	def currentActivity = theServer.currentValue("currentActivity")

    def findURI = currentActivity.split(" ")
	if (!state?.serverChecked) {
    	state.playersFound = true
       	state.playersList = []
        state.installedPlayers = []
		def commandString = "{\"id\":1,\"method\":\"slim.request\",\"params\":[\"-\",[\"players\",0,99]]}"
		def result = new physicalgraph.device.HubAction(
				method: "POST",
				path: "/jsonrpc.js",
                body: "${commandString}",
				headers: [
					HOST: "${findURI[4]}",
                    "Content-Type" : 'application/json'
				], null, [callback: "JSONhandler"]
				)
		sendHubCommand(result)
		state.serverChecked = true
		log.debug result
    
	}
    def existingText = "No players are currently installed"
    if (state.installedPlayers.size() > 0) {
      	existingText = "The following players are already installed"
    	for (i in 0..(state.installedPlayers.size()-1)) {
            	existingText = "${existingText}\n   ${state.installedPlayers[i].playerName}"
        }
    }
    def availablePlayers = []
    availablePlayers = state?.playersList?.playerName ?: []
    if (availablePlayers?.size() > 0) {

    	return dynamicPage(name: "selectPlayers", nextPage: "finished") {
     	   section {
	            input(name: "players", type: "enum", title: "${availablePlayers.size()} Available player(s) found", required: true, multiple:true, options: availablePlayers)
	        }
            section {
                paragraph "Press Next to install selected players"
            } 
            section {title: "Existing Players"
        		paragraph "${existingText}"
        	}
	    }
    } else {
    	if (state.playersFound) {
	    	return dynamicPage(name: "selectPlayers", refreshInterval:4) {
	        	section {
	        		paragraph "Looking for players"
	        	}
	    	}
        }
        else {
	        return dynamicPage(name: "selectPlayers") {
	        	section {
	        		paragraph "No Players Found"
	        	}
	    	}
        }
	}
}

def finished() {
   	def installedPlayers = theServer.currentValue("playerList").split(",")
    def existingPlayers = []
    def newPlayers = []
    def badPlayers = []
	if (players?.size() > 0 ) {
    	for (i in 0..(players.size()-1)) {
	    	def playerIndex = state.playersList.playerName.findIndexOf { it == players[i]}

    		def playerMAC = (playerIndex >-1) ? state.playersList.playerMAC[playerIndex] : null
            def child
            if (playerMAC) {
            	child = installedPlayers.find { it == playerMAC}
                if (child) {  //player already exists
	            	existingPlayers << players[i]
    	        }
        	    else {  //new player needs creating
                	theServer.buildPlayer(playerMAC,players[i])
            		newPlayers << players[i]
            	}
            } else {badPlayers << players[i]}
		}        

		def installedText = "No new players were installed"
        if (newPlayers.size() > 0) {
        	installedText = "The following players were installed"
            for (i in 0..(newPlayers.size()-1)) {
            	installedText = "${installedText}\n   ${newPlayers[i]}"
            }
        }
        def existingText = "No players were previously installed"
        if (existingPlayers.size() > 0) {
        	existingText = "The following players were already installed"
            for (i in 0..(existingPlayers.size()-1)) {
            	existingText = "${existingText}\n   ${existingPlayers[i]}"
            }
        }
        def badText = "No players failed to install"
        if (badPlayers.size() > 0) {
        	badText = "The following players could not be installed"
            for (i in 0..(badPlayers.size()-1)) {
            	badText = "${badText}\n   ${badPlayers[i]}"
            }
        }
		return dynamicPage(name: "finished", nextPage:"", install:true, uninstall: true) {
        	section {title: "Installed Players"
        		paragraph "${installedText}"
        	}
            section {title: "Existing Players"
        		paragraph "${existingText}"
        	}
            section {title: "Bad Players"
        		paragraph "${badText}"
        	}
    	}
    
    }
    else {
 	   return dynamicPage(name: "finished", nextPage:"", install:false, uninstall: true) {
        	section {
        		paragraph "No Players Selected"
        	}
    	}
    }
	state.serverChecked = false
}

def JSONhandler(msg) {
	
//    log.debug "*****IN JSON HANDLER"

//    def json = msg.json
    def body = msg.body
    def bodyJSON = new groovy.json.JsonSlurper().parseText(body)
  
//    log.debug "JSON Body result  ${bodyJSON.result}" 
    def numberPlayers = bodyJSON.result.count.toInteger()
   	def installedPlayers = theServer.currentValue("playerList").split(",")
    if (numberPlayers > 0) {
		def playerMAC
        for (i in 0..(numberPlayers-1)) {
    		playerMAC = bodyJSON.result.players_loop[i].playerid
            def child
            if (playerMAC) {
            	child = installedPlayers.find { it == playerMAC}
                if (child) {  //player already installed
	            	state.installedPlayers << [playerMAC : bodyJSON.result.players_loop[i].playerid, playerName : bodyJSON.result.players_loop[i].name]
    	        }
        	    else {  //uninstalled player 
		           	state.playersList << [playerMAC : bodyJSON.result.players_loop[i].playerid, playerName : bodyJSON.result.players_loop[i].name]
            	}
			}
        }
//      log.debug "State stored players${state.players}" 
    } 
    else {
    	state.playersFound = false
    }

}
