/**
 *  Toggimmer
 *  Version 1.0.0 - 07/01/16
 *
 *  1.0.0 - Initial release
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
 
 
definition(
	name: "Toggimmer",
	namespace: "ericvitale",
	author: "ericvitale@gmail.com",
	description: "...",
	category: "My Apps",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience%402x.png"
)

/*preferences {
	section("Dimmers") {
		input "dimmers", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
    }
   	
    section("Lights") {
	    input "lights", "capability.switchLevel", title: "Lights", multiple: true, required: false
	}
    
	section("Options") {
		label(title: "Assign a name", required: false)
        input "logging", "text", title: "Log Level", required: false, defaultValue: "DEBUG"
    }
}*/

preferences {
	page name: "mainPage"
}

def mainPage() {
	dynamicPage(name: "mainPage", title: "", install: true, uninstall: true) {
    
    	section("Dimmers") {
			input "dimmers", "capability.switchLevel", title: "Dimmers", multiple: true, required: false
    	}
   	
    	section("Lights") {
	        input "lights", "capability.switchLevel", title: "Lights", multiple: true, required: false
		}
    
	    section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false)
            input "logging", "text", title: "Log Level", required: false, defaultValue: "DEBUG"
    	}
    
	}
}

def determineLogLevel(data) {
	if(data.toUpperCase() == "TRACE") {
    	return 0
    } else if(data.toUpperCase() == "DEBUG") {
    	return 1
    } else if(data.toUpperCase() == "INFO") {
    	return 2
    } else if(data.toUpperCase() == "WARN") {
    	return 3
    } else {
    	return 4
    }
}

def log(data, type) {
    
    data = "Toggimmer -- " + data
    
    try {
        if(determineLogLevel(type) >= determineLogLevel(logging)) {
            if(type.toUpperCase() == "TRACE") {
                log.trace "${data}"
            } else if(type.toUpperCase() == "DEBUG") {
                log.debug "${data}"
            } else if(type.toUpperCase() == "INFO") {
                log.info "${data}"
            } else if(type.toUpperCase() == "WARN") {
                log.warn "${data}"
            } else if(type.toUpperCase() == "ERROR") {
                log.error "${data}"
            } else {
                log.error "Toggimmer -- Invalid Log Setting"
            }
        }
    } catch(e) {
    	log.error ${e}
    }
}

def installed() {   
	log("Begin installed.", "DEBUG")
	initialize() 
    log("End installed.", "DEBUG")
}

def updated(){
	log("Begin updated().", "DEBUG")
	unsubscribe()
    initalization()
    log("End updated().", "DEBUG")
}

def initalization() {
	log("Begin intialization().", "DEBUG")

	subscribe(dimmers, "switch", switchHandler)
    log("switchHandler subscribed!", "DEBUG")
    subscribe(dimmers, "level", levelHandler)
    log("levelHandler subscribed!", "DEBUG")

	//state.l = [:]

	//Store the values of the dimmers
    dimmers.each { it->
    	log("+++label = xxx${it.label}xxx", "DEBUG")
    	state[it.label] = it.currentValue("level")
    }
    
    dimmers.each { it->
    	log("---${it.label} = ${state['it.label']}", "DEBUG")
    }
    
    log("End initialization().", "DEBUG")
}

def switchHandler(evt) {
	log("Begin switchHandler(evt).", "DEBUG")
	lights.each { it->
    	log("name = ${it.name}", "DEBUG")
    	if(it.currentValue("switch") == "on") {
        	it.off()
        } else {
        	it.on()
        }
    }
	log("End switchHandler(evt).", "DEBUG")
}

def levelHandler(evt) {
	log("Begin levelHandler(evt).", "DEBUG")
    log("evt = ${evt.value}", "DEBUG")
    log("evt.displayName = xxx${evt.displayName}xxx", "DEBUG")
    log(state['"${evt.displayName}"'], "DEBUG")
    log("old Value = ${state[evt.displayName]}", "DEBUG")
    
    if(compareValue(evt.value, "${state[evt.displayName]}")) {
    	log("UP", "DEBUG")
        //state[evt.displayName] = state[evt.displayName] + 10
        state[evt.displayName] = setDimmers(10)
    } else {
    	log("DOWN", "DEBUG")
        //state[evt.displayName] = state[evt.displayName] - 10
        state[evt.displayName] = setDimmers(-10)
    }
    
	log("End levelHandler(evt).", "DEBUG")
}

def setDimmers(val) {
	log("Begin setDimmers(val)", "DEBUG")
    def value = val	
    lights.each { it->
		value = val
        def currentVal = it.currentValue("level")
        log("Current Level = ${value}", "DEBUG")
        
        if(currentVal < 25 && value < 0) {
        	value = -5
        }
        
        if(currentVal + value > 100) {
        	it.setLevel(100)
            log("End setDimmers(val)", "DEBUG")
            return 100
        } else if(currentVal + value < 0) {
        	it.setLevel(0)
            log("End setDimmers(val)", "DEBUG")
            return 0
        } else {
	        it.setLevel(currentVal + value)
            log("End setDimmers(val)", "DEBUG")
            return currentVal + value
		}
    }
    log("End setDimmers(val)", "DEBUG")
}

def compareValue(newVal, oldVal) {
	log("New ${newVal}", "DEBUG")
	log("Old ${oldVal}", "DEBUG")
	return newVal > oldVal
}