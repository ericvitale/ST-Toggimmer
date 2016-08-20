/**
 *  Toggimmer v2
 *  Version 1.0.0 - 07/07/16
 *
 *  2.0.1 - Added icon.
 *  2.0.0 - Parent Child App
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
 *  Toggimmer is a SmartApp designed to work with wireless dimmers like the Cooper RF9500 (link to device handler below) 
 *  that operater wirelessly only and are not wired into your lights. Toggimmer allows you to select 1 to many dimmers and 
 *  control 1 to many dimmable lights without having to worry about keeping these dimmers in sync with each other or the lights. 
 *  You can 100% replicate the functionality of this SmartApp with something like CoRE. The reason for this apps existance is 
 *  that I felt something as powerful as CoRE was overkill for this kind of function.
 *
 *  You can find this smart app @ https://github.com/ericvitale/ST-Toggimmer
 *  You can find the reference Cooper RF9500 Beast device handler @ https://github.com/ericvitale/ST-CooperRF9500Beast
 *  You can find my other device handlers & SmartApps @ https://github.com/ericvitale
 *
 */
 
 
definition(
    name: "${appName()}",
    namespace: "ericvitale",
    author: "Eric Vitale",
    description: "Toogle (On / Off) & dim wireless dimmers like the Cooper RF9500 that operater wirelessly only and are not wired into your lights.",
    category: "",
    iconUrl: "https://s3.amazonaws.com/ev-public/st-images/toggimmer-1x.png",
    iconX2Url: "https://s3.amazonaws.com/ev-public/st-images/toggimmer-2x.png",
    iconX3Url: "https://s3.amazonaws.com/ev-public/st-images/toggimmer-3x.png")


preferences {
    page(name: "startPage")
    page(name: "parentPage")
    page(name: "childStartPage")
}

def startPage() {
    if (parent) {
        childStartPage()
    } else {
        parentPage()
    }
}

def parentPage() {
	return dynamicPage(name: "parentPage", title: "", nextPage: "", install: false, uninstall: true) {
        section("Create a new child app.") {
            app(name: "childApps", appName: appName(), namespace: "ericvitale", title: "New Roku Automation", multiple: true)
        }
    }
}
 
def childStartPage() {
	return dynamicPage(name: "childStartPage", title: "", install: true, uninstall: true) {
    
    	section("Dimmers") {
			input "dimmers", "capability.switchLevel", title: "Dimmers", multiple: true, required: true
    	}
   	
    	section("Lights") {
	        input "lights", "capability.switchLevel", title: "Lights", multiple: true, required: true
		}
    
	    section([mobileOnly:true], "Options") {
			label(title: "Assign a name", required: false)
            input "logging", "enum", title: "Log Level", required: true, defaultValue: "INFO", options: ["TRACE", "DEBUG", "INFO", "WARN", "ERROR"]
    	}
	}
}

private def appName() { return "${parent ? "Toggimmer Configuration" : "Toggimmer v2"}" }

private determineLogLevel(data) {
    switch (data?.toUpperCase()) {
        case "TRACE":
            return 0
            break
        case "DEBUG":
            return 1
            break
        case "INFO":
            return 2
            break
        case "WARN":
            return 3
            break
        case "ERROR":
        	return 4
            break
        default:
            return 1
    }
}

def log(data, type) {
    data = "TG -- ${data ?: ''}"
        
    if (determineLogLevel(type) >= determineLogLevel(settings?.logging ?: "INFO")) {
        switch (type?.toUpperCase()) {
            case "TRACE":
                log.trace "${data}"
                break
            case "DEBUG":
                log.debug "${data}"
                break
            case "INFO":
                log.info "${data}"
                break
            case "WARN":
                log.warn "${data}"
                break
            case "ERROR":
                log.error "${data}"
                break
            default:
                log.error "TG -- Invalid Log Setting"
        }
    }
}

def installed() {
	log("Begin installed.", "DEBUG")
	initialization() 
    log("End installed.", "DEBUG")
}

def updated() {
	log("Begin updated().", "DEBUG")
	unsubscribe()
    unschedule()
	initialization()
    log("End updated().", "DEBUG")
}

def initialization() {
	log.debug "Begin initialization()."
    
    if(parent) { 
    	initChild() 
    } else {
    	initParent() 
    }
    
    log.debug "End initialization()."
}

def initParent() {
	log.debug "initParent()"
}

def initChild() {
	log("Begin intialization().", "DEBUG")
    
	subscribe(dimmers, "switch", switchHandler)
    subscribe(dimmers, "level", levelHandler)
    
    state.sw = [:]
    
    dimmers.each { it->
    	state.sw[it.label] = it.currentValue('level')
        log("Level = ${it.currentValue('level')}.", "DEBUG")
    }
    
    log("End initialization().", "DEBUG")
}

def switchHandler(evt) {
	log("Begin switchHandler(evt).", "DEBUG")
	lights.each { it->
    	if(it.currentValue("switch") == "on") {
        	it.off()
            log("${it.label} -- Turned off.", "INFO")
        } else {
        	it.on()
            log("${it.label} -- Turned on.", "INFO")
        }
    }
	log("End switchHandler(evt).", "DEBUG")
}

def levelHandler(evt) {
	log("Begin levelHandler(evt).", "DEBUG")
    
    if(compareValue(evt.value, "${state.sw[evt.displayName]}")) {
    	log("UP", "INFO")
        state.sw[evt.displayName] = evt.value
        setDimmers("UP")
    } else {
    	log("DOWN", "INFO")
        state.sw[evt.displayName] = evt.value
        setDimmers("DOWN")
    }
    
	log("End levelHandler(evt).", "DEBUG")
}

def setDimmers(direction) {
	log("Begin setDimmers(val)", "DEBUG")
    
    lights.each { it->
        def currentVal = it.currentValue("level")
        def newVal = getNextValue(direction, currentVal)
        it.setLevel(newVal.toInteger())
    }
    
    log("End setDimmers(val)", "DEBUG")
}

def compareValue(newVal, oldVal) {
	return newVal > oldVal
}

def getNextValue(direction, currentValue) {
	log("Begin getNextValue()", "DEBUG")
    
	def result = ""
    
	if(direction.toUpperCase() == "DOWN") {
    	switch(currentValue.toInteger()) {
        	case 100..86:
            	result = "80"
                break
            case 85..66:
            	result = "60"
                break
            case 65..46:
            	result = "40"
                break
            case 45..26:
            	result = "25"
                break
            case 25..21:
            	result = "20"
              	break
            case 20..16:
            	result = "15"
            	break
            case 15..11:
            	result = "10"
            	break
            case 10..6:
            	result = "5"
                break
            case 5..0:
            	result = "0"
                break
            
            default: 
            	result = "0"
        }
        return result
    } else {
    	switch(currentValue.toInteger()) {
        	case 100..80:
            	result = "100"
                break
            case 79..60:
            	result = "80"
                break
            case 59..40:
            	result = "60"
                break
            case 39..25:
            	result = "40"
                break
            case 24..20:
            	result = "25"
              	break
            case 19..15:
            	result = "20"
            	break
            case 14..10:
            	result = "15"
            	break
            case 9..5:
            	result = "10"
                break
            case 4..0:
            	result = "5"
                break
            
            default: 
            	result = "100"
        }
        return result
    }
    
    log("End getNextValue()", "DEBUG")
}