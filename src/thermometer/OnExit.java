/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermometer;

import com.pi4j.io.gpio.GpioController;

/**
 *
 * @author marius
 */
// a class that extends thread that is to be called when program is exiting
public class OnExit extends Thread
{

    GpioController localgpio;
    public OnExit(GpioController gpio)
    {
        localgpio = gpio;
    }
    
    public void run()
    {
        System.out.println("Shutting down");
        localgpio.shutdown();
    }
}
