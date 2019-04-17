/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermometer;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

/**
 *
 * @author marius
 */
public class Device
{

    public String Name;
    public Float Temperature;
    public String Directory;
    public String UpperLimit = "22.5";
    public String LowerLimit = "21.5";
    public Float Upper;
    public Float Lower;
    public String GPIOPin;
    public RaspiPin Pin;
    GpioPinDigitalOutput GPIOpin;
    public boolean Enabled;
    public boolean goingUp = true;
    public long cycle = 0;
    public int DebugPin = 0;

    public Device(String Directory, boolean Debug, int DebugNo)
    {
        if (Debug)
        {
            Name = "Debug"+DebugNo;
        } else
        {
            this.Directory = Directory;
            String[] splitted = Directory.split("/");
            Name = splitted[splitted.length - 2];
        }
    }
}
