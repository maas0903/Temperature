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
    public String UpperLimit;
    public String LowerLimit;
    public Float Upper;
    public Float Lower;
    public  String GPIOPin;
    public  RaspiPin Pin;
    GpioPinDigitalOutput GPIOpin;
    public boolean Enabled;

    public Device(String Directory)
    {
        this.Directory = Directory;
        String[] splitted = Directory.split("/");
        Name = splitted[splitted.length - 2];
    }
}
