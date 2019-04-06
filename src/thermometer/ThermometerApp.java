/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermometer;

/**
 *
 * @author marius
 */
import static com.melektro.Tools.LoadProperty.LoadProperty;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ThermometerApp
{

    static String baseDir = "/sys/bus/w1/devices/";
    static String searchPattern = "28";
    static String deviceFile = "/w1_slave";
    static int Interval = 3000;
    private static String UpperLimit = "22.5";
    private static String LowerLimit = "21.5";
    private static Float Upper;
    private static Float Lower;

    private static void SetProperties()
    {
        Properties prop = new Properties();
        OutputStream output = null;

        try
        {
            output = new FileOutputStream("thermometer.config.properties");
            prop.setProperty("UpperLimit", UpperLimit);
            prop.setProperty("LowerLimit", LowerLimit);
            Upper = Float.parseFloat(UpperLimit);
            Lower = Float.parseFloat(LowerLimit);
            prop.store(output, null);
        } catch (IOException e)
        {
        } finally
        {
            if (output != null)
            {
                try
                {
                    output.close();
                } catch (IOException e)
                {
                }
            }
        }
    }

    private static void GetProperties()
    {
        Properties prop = new Properties();
        InputStream input = null;

        File file = new File("thermometer.config.properties");

        if (!file.exists())
        {
            SetProperties();
        } else
        {

            try
            {
                input = new FileInputStream(file);
                prop.load(input);

                UpperLimit = LoadProperty(prop, "UpperLimit", "22.5");
                LowerLimit = LoadProperty(prop, "LowerLimit", "21.5");
                Upper = Float.parseFloat(UpperLimit);
                Lower = Float.parseFloat(LowerLimit);

            } catch (IOException e)
            {
            } finally
            {
                if (input != null)
                {
                    try
                    {
                        input.close();
                    } catch (IOException e)
                    {
                    }
                }
            }
        }
    }

    public static List GetDS18B20Directories()
    {
        List<String> result = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(Paths.get(baseDir)))
        {
            List<String> devicesDir = walk
                    .filter(Files::isDirectory)
                    .map(x -> x.toString())
                    .collect(Collectors.toList());

            devicesDir.stream().filter((device) -> (device.contains("/28"))).forEachOrdered((device) ->
            {
                result.add(device + deviceFile);
            });
        } catch (IOException e)
        {

        }
        return result;
    }

    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static Float GetDeviceTemperature(String dir) throws IOException
    {
        String cont = readFile(dir, Charset.defaultCharset());
        Float result;
        if (cont.contains(" YES"))
        {
            result = Float.parseFloat(cont.substring(cont.indexOf("t=") + 2)) / 1000;
        } else
        {
            result = Float.parseFloat("-1000");
        }
        return result;
    }

    private static List<Device> SetupDevices(List<String> DS18B20Directories)
    {
        List<Device> devices = new ArrayList<>();
        DS18B20Directories.forEach((dir) ->
        {
            devices.add(new Device(dir));
        });

        return devices;
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput GPIOpin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "PinOut", PinState.LOW);
        GPIOpin.setShutdownOptions(true, PinState.LOW);

        List<Device> devices = SetupDevices(GetDS18B20Directories());
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    GetProperties();
                    for (Device device : devices)
                    {
                        device.Temperature = GetDeviceTemperature(device.Directory);
                        if (device.Temperature < Lower)
                        {
                            System.out.println(device.Name + " - " + device.Temperature + " < " + Lower + " Pad is ON");
                            GPIOpin.high();
                        } else if (device.Temperature > Upper)
                        {
                            System.out.println(device.Name + " - " + device.Temperature + " > " + Upper + " Pad is OFF");
                            GPIOpin.low();
                        }
                    }
                } catch (IOException ex)
                {
                }
            }
        }, 0, Interval);
        
        //TODO gpio.shutdown(); put somewhere else to run at shutdown only
    }
}
