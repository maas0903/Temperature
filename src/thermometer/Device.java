/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermometer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 *
 * @author marius
 */
public class Device
{
    public String Name;
    public Float Temperature;
    public String Directory;

    public Device(String Directory)
    {
        this.Directory = Directory;
        String[] splitted = Directory.split("/");
        Name = splitted[splitted.length - 2];
    }
}
