# Eyedro-Power-Meter-for-Hubitat
A driver and app to gather, store and display readings from an Eyedro Power Meter with either 2 or 4 sensors.

I made this to monitor power generation from our new Solar panel system and our consumption to ensure we are being billed properly.
I also wanted to make cool little graphs to show guests and friends how the solar panels have impacted our electric bill.

So far only the DRIVER is complete and functional.

## Driver Installation

1. With the Hubitat web page up, click on "Drivers Code"
2. In the upper right, click on "New Driver"
3. Paste the RAW code from the github
4. Click on Save
..and then..
5. On the Hubitat menu click on Devices
6. In the upper right, click on "Add Virtual Device"
7. Under Device Name enter "Eyedro Power Meter (local)" or whatever you would like to call it
8. In the "type" dropdown, scroll to the bottom and find the "Eyedro Power Meter (local)" and click on it
9. Click on the "Save Device" button

You will then need to enter your Eyedro's IP address and Port.
The default port is 8080 so you probably won't need to change that.
Then enter how many sensors you have. There are typically 2, other versions of the Eyedro have 4. 2 are for monitoring your power consumption from the mains, and 2 are for monitoring the power you are generating from the Solar mains.
Enter (in seconds) how often you want to poll the Eyedro for it's data. I settled on a default of 10 seconds.
Click on "Save Preferences"

At the moment the attributes you can subscribe to are,

Consumption - Adds the values together from sensors A and B
* currentVolts (in Volts)
* currentAmps (in Amps)
* currentWatts (in kilowatts)

Generation - Add the values together from sensors C and D
* solarVolts (in Volts)
* solarAmps (in Amps)
* solarWatts (in kilowatts)

More to come soon...
