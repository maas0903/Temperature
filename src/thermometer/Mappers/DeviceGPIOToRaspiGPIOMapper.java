    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermometer.Mappers;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 *
 * @author marius
 */
public class DeviceGPIOToRaspiGPIOMapper
{

    public static Pin Map(String gpio)
    {
        if (gpio.equals("GPIO_00"))
        {
            return RaspiPin.GPIO_00;
        }
        if (gpio.equals("GPIO_01"))
        {
            return RaspiPin.GPIO_01;
        }
        if (gpio.equals("GPIO_02"))
        {
            return RaspiPin.GPIO_02;
        }
        if (gpio.equals("GPIO_03"))
        {
            return RaspiPin.GPIO_03;
        }
        if (gpio.equals("GPIO_04"))
        {
            return RaspiPin.GPIO_04;
        }
        if (gpio.equals("GPIO_05"))
        {
            return RaspiPin.GPIO_05;
        }
        if (gpio.equals("GPIO_06"))
        {
            return RaspiPin.GPIO_06;
        }
        if (gpio.equals("GPIO_07"))
        {
            return RaspiPin.GPIO_07;
        }
        if (gpio.equals("GPIO_08"))
        {
            return RaspiPin.GPIO_08;
        }
        if (gpio.equals("GPIO_09"))
        {
            return RaspiPin.GPIO_09;
        }
        if (gpio.equals("GPIO_10"))
        {
            return RaspiPin.GPIO_10;
        }
        if (gpio.equals("GPIO_11"))
        {
            return RaspiPin.GPIO_11;
        }
        if (gpio.equals("GPIO_12"))
        {
            return RaspiPin.GPIO_12;
        }
        if (gpio.equals("GPIO_13"))
        {
            return RaspiPin.GPIO_13;
        }
        if (gpio.equals("GPIO_14"))
        {
            return RaspiPin.GPIO_14;
        }
        if (gpio.equals("GPIO_15"))
        {
            return RaspiPin.GPIO_15;
        }
        if (gpio.equals("GPIO_16"))
        {
            return RaspiPin.GPIO_16;
        }
        if (gpio.equals("GPIO_17"))
        {
            return RaspiPin.GPIO_17;
        }
        if (gpio.equals("GPIO_18"))
        {
            return RaspiPin.GPIO_18;
        }
        if (gpio.equals("GPIO_19"))
        {
            return RaspiPin.GPIO_19;
        }
        if (gpio.equals("GPIO_20"))
        {
            return RaspiPin.GPIO_20;
        }
        return null;
    }

}
