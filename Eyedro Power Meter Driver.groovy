/**
 *	Eyedro Electricity Monitors (local)
 *
 *	Copyright(C) 2020 - Kevin Summers
 *
 *	Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *	in compliance with the License. You may obtain a copy of the License at:
 *
 *			http://www.apache.org/licenses/LICENSE-2.0
 *
 *	Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *	for the specific language governing permissions and limitations under the License.
 *
 * version 0.0.1 - Nov, 2020 - Initial release
 * version 0.0.2 - Feb, 2021 - Fixed: Polling not always running
 *
 */

static String version() {
	return "0.0.2"
}

preferences {
    input("ipAddress", "string", title:"Eyedro Local IP Address", required: true, displayDuringSetup: true)
    input("port", "number", title:"TCP Port (8080 by default)",defaultValue:"8080", required: true, displayDuringSetup: true)
    input("sensors", "number", title:"Number of Power Sensors (2 or 4)", required: true, range: "2..4", displayDuringSetup: true)
    input("pollInterval", "number", title:"Polling Interval in Seconds (between 5 and 60)", defaultValue:"10", range: "5..60", required: true, displayDuringSetup: true)
    input("debugEnable", "bool", title: "Enable debug logging?", defaultValue: true, displayDuringSetup: true)
}

metadata {
    definition (name: "Eyedro Power Meter", namespace: "KISTech", author: "Kevin Summers") {
    capability "Sensor"
    capability "Refresh"
    attribute "currentVolts", "number"
    attribute "currentAmps", "number"
    attribute "currentWatts", "number"
    attribute "solarVolts", "number"
    attribute "solarAmps", "number"
    attribute "solarWatts", "number"
    }
}

void initialize() {
    if (ipAddress == null) {
        info "Device init: Please set device preferences"
    } else {
        updated()
    }
}

void updated() {
    unschedule(debugOff)
    if (debugEnable == true) {
        runIn(60 * 60, debugOff)
    }
    startPoll()
}

void debugOff() {
    debug "Disabling debug logging after 1 hour"
    debugEnable = false
}

void startPoll() {
    unschedule(getData)
    info("Eyedro - Polling every ${pollInterval} seconds started")
    runIn(pollInterval, getData)
}

void refresh() {
    getData()
}

void getData() {
    unschedule(getData)
    debug("Requesting data from Eyedro")
    sendHubCommand(new hubitat.device.HubAction([
	method: "GET",
	path: "/getdata",
	headers: [HOST:ipAddress+":"+port]
    ], null, [callback: dataCallback])
    )
    runIn(pollInterval, getData)
}

void dataCallback(hubitat.device.HubResponse msg) {
    def data = msg.json.toString()
    info("new data: ${data}")
    int a = data.indexOf("]", 9)
    int b = 0
    int c = 0
    List sensorA = []
    List sensorB = []
    List sensorC = []
    List sensorD = []
    String Data = data.substring(8, a)
    int A = 0
    int B = 0
    for (int i = 0; i < 3; i++) {
        B = Data.indexOf(",", A)
        sensorA.add(Data.substring(A, B))
        A = B + 2
    }
    sensorA.add(Data.substring(A, Data.length()))
    b = data.indexOf("]", a + 3)
    Data = data.substring(a + 4, b)
    A = 0
    B = 0
    for (int i = 0; i < 3; i++) {
        B = Data.indexOf(",", A)
        sensorB.add(Data.substring(A, B))
        A = B + 2
    }
    sensorB.add(Data.substring(A, Data.length()))
    if (settings.sensors > 2) {
        c = data.indexOf("]", b + 3)
        Data = data.substring(b + 4, c)
        A = 0
        B = 0
        for (int i = 0; i < 3; i++) {
            B = Data.indexOf(",", A)
            sensorC.add(Data.substring(A, B))
            A = B + 2
        }
        sensorC.add(Data.substring(A, Data.length()))
    }
    if (settings.sensors > 3) {
        int d = data.indexOf("]", c + 3)
        Data = data.substring(c + 4, d)
        A = 0
        B = 0
        for (int i = 0; i < 3; i++) {
            B = Data.indexOf(",", A)
            sensorD.add(Data.substring(A, B))
            A = B + 2
        }
        sensorD.add(Data.substring(A, Data.length()))
    }
    debug("A - ${sensorA} - B - ${sensorB} - C - ${sensorC} - D - ${sensorD}")
    float current_Volts = (sensorA[1].toInteger() + sensorB[1].toInteger()) / 100
    float current_Amps = (sensorA[2].toInteger() + sensorB[2].toInteger()) / 1000
    float current_Watts = (sensorA[3].toInteger() + sensorB[3].toInteger()) / 1000
    float solar_Volts = 0
    float solar_Amps = 0
    float solar_Watts = 0
    debug("Consumption : Volts : ${current_Volts} - Amps : ${current_Amps} - kiloWatts : ${current_Watts}")
    if (settings.sensors > 2) {
        solar_Volts = (sensorC[1].toInteger() + sensorD[1].toInteger()) / 100
        solar_Amps = (sensorC[2].toInteger() + sensorD[2].toInteger()) / 1000
        solar_Watts = (sensorC[3].toInteger() + sensorD[3].toInteger()) / 1000
        debug("Generation : Volts : ${solar_Volts} - Amps : ${solar_Amps} - kiloWatts : ${solar_Watts}")
    }
    sendEvent(name: "currentVolts", value: current_Volts, descriptionText: "Current Voltage (V)")
    sendEvent(name: "currentAmps", value: current_Amps, descriptionText: "Current Amps (A)")
    sendEvent(name: "currentWatts", value: current_Watts, descriptionText: "Current Watts (kW)")
    sendEvent(name: "solarVolts", value: solar_Volts, descriptionText: "Solar Voltage (V)")
    sendEvent(name: "solarAmps", value: solar_Amps, descriptiontext: "Solar Amps (A)")
    sendEvent(name: "solarWatts", value: solar_Watts, descriptionText: "Solar Watts (kW)")
}

void debug(String msg) {
    if (debugEnable == true) {
        log.debug msg
    }
}

void info(String msg) {
	log.info msg
}
