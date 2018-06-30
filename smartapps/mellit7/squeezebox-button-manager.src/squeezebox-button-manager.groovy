/**
 *
 *  Squeezebox Button Manager
 *
 *  Version 1.0
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
    name: "Squeezebox Button Manager",
    namespace: "mellit7",
    author: "Melinda Little",
    description: "Manage buttons for play, pause, stop, buttons 1-3, and potentially other buttons for Squeezebox player devices",
    category: "Convenience",
    iconUrl: "http://cdn.device-icons.smartthings.com/Entertainment/entertainment2-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Entertainment/entertainment2-icn@2x.png"
)

preferences {

	section("Select the Squeezebox player you wish to control") {
		input "thePlayer", "capability.musicPlayer", multiple: false, required: true, title: "Player?"

	}
	section("Select the button to be used to start playing") {
		input "playControl", "capability.Momentary", multiple: false, required: false, title: "Play Control Button?"
	}

	section("Select the button to be used to pause") {
        input "pauseControl", "capability.Momentary", multiple: false, required: false, title: "Pause Control Button?"
	}   

	section("Select the button to be used to stop playing") {
		input "stopControl", "capability.Momentary", multiple: false, required: false, title: "Stop Control Button?"
	}
    
	section("Select the button to be used to load Preset 1") {
		input "button1Control", "capability.Momentary", multiple: false, required: false, title: "Preset 1 Control Button?"
	}
        
	section("Select the button to be used to load Preset 2") {
		input "button2Control", "capability.Momentary", multiple: false, required: false, title: "Preset 2 Control Button?"
	}
    
	section("Select the button to be used to load Preset 3") {
		input "button3Control", "capability.Momentary", multiple: false, required: false, title: "Preset 3 Control Button?"
	}
}

def installed()
{
	setupsubscriptions()

}

def updated()
{
	unsubscribe()
	setupsubscriptions()

}

def playSwitchOnHandler(evt) {

	log.debug "Play Switch On Handler: $evt"
	thePlayer.play()
}

def stopSwitchOnHandler(evt) {
	log.debug "Stop Switch On Handler: $evt"
	thePlayer.stop()
}

def pauseSwitchOHandler(evt) {
	log.debug "Pause Switch On Handler: $evt"
	thePlayer.pause()
}

def button1SwitchOnHandler(evt) {
	log.debug "Button 1 Switch On Handler: $evt"
	thePlayer.button1()
}

def button2SwitchOnHandler(evt) {
	log.debug "Button 2 Switch On Handler: $evt"
	thePlayer.button2()
}

def button3SwitchOnHandler(evt) {
	log.debug "Button 3 Switch On Handler: $evt"
	thePlayer.button3()
}

def setupsubscriptions() {
	log.debug "SUBSCRIBE ${button1Control}"
	if (playControl) {subscribe(playControl, "switch.on", playSwitchOnHandler)}
	if (pauseControl) {subscribe(pauseControl, "switch.on", pauseSwitchOHandler)}
	if (stopControl) {subscribe(stopControl, "switch.on", stopSwitchOnHandler)}
   	if (button1Control) {subscribe(button1Control, "switch.on", button1SwitchOnHandler)}
   	if (button2Control) {subscribe(button2Control, "switch.on", button2SwitchOnHandler)}
   	if (button3Control) {subscribe(button3Control, "switch.on", button3SwitchOnHandler)}

}