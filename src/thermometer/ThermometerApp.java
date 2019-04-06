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
    private static String DefaultUpperLimit = "22.5";
    private static String DefaultLowerLimit = "21.5";
    private static Float DefaultUpper;
    private static Float DefaultLower;
    private static List<Device> devices;
    private static String ConfigFile = "thermometer.config.properties";
    private static GpioController gpio = GpioFactory.getInstance();
    //private static GpioPinDigitalOutput GPIOpin;

    private static void SetProperties()
    {
        Properties prop = new Properties();
        OutputStream output = null;

        try
        {
            output = new FileOutputStream(ConfigFile);
            prop.setProperty("DefaultUpperLimit", DefaultUpperLimit);
            prop.setProperty("DefaultLowerLimit", DefaultLowerLimit);
            DefaultUpper = Float.parseFloat(DefaultUpperLimit);
            DefaultLower = Float.parseFloat(DefaultLowerLimit);

            for (Device device : devices)
            {
                if (device.GPIOPin.equals(""))
                {
                    device.Enabled = false;
                    device.GPIOPin = "GPIO_XX";
                } else
                {
                    device.Enabled = true;
                }

                if (device.UpperLimit.equals(""))
                {
                    device.UpperLimit = DefaultUpperLimit;
                }

                if (device.LowerLimit.equals(""))
                {
                    device.LowerLimit = DefaultLowerLimit;
                }

                prop.setProperty(device.Name + "UpperLimit", device.UpperLimit);
                prop.setProperty(device.Name + "LowerLimit", device.LowerLimit);
            }

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

    private static boolean GetProperties()
    {
        Properties prop = new Properties();
        InputStream input = null;

        File file = new File(ConfigFile);

        if (!file.exists())
        {
            SetProperties();
            return false;
        } else
        {
            try
            {
                input = new FileInputStream(file);
                prop.load(input);

                DefaultUpperLimit = LoadProperty(prop, "DefaultUpperLimit", "22.5");
                DefaultLowerLimit = LoadProperty(prop, "DefaultLowerLimit", "21.5");
                DefaultUpper = Float.parseFloat(DefaultUpperLimit);
                DefaultLower = Float.parseFloat(DefaultLowerLimit);

                for (Device device : devices)
                {
                    device.Upper = Float.parseFloat(LoadProperty(prop, device.Name + "UpperLimit", DefaultUpperLimit));
                    device.Lower = Float.parseFloat(LoadProperty(prop, device.Name + "LowerLimit", DefaultLowerLimit));

                    if (device.GPIOPin.equals("GPIO_XX"))
                    {
                        device.Enabled = false;
                    } else
                    {
                        device.Enabled = true;
                        if (device.GPIOpin.equals(null))
                        {
                            device.GPIOpin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "PinOut", PinState.LOW);
                            device.GPIOpin.setShutdownOptions(true, PinState.LOW);
                        }
                    }
                }

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
            return true;
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
        devices = SetupDevices(GetDS18B20Directories());
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                try
                {
                    if (!GetProperties())
                    {
                        Path currentRelativePath = Paths.get("");
                        String appPath = currentRelativePath.toAbsolutePath().toString();

                        System.out.println("Please setup the configuration in " + appPath + "/" + ConfigFile);
                        System.exit(0);
                    };

                    for (Device device : devices)
                    {
                        device.Temperature = GetDeviceTemperature(device.Directory);
                        if (device.Temperature < DefaultLower)
                        {
                            System.out.println(device.Name + " - " + device.Temperature + " < " + DefaultLower + " Pad is ON");
                            if (device.Enabled)
                            {
                                SetPin(device, 1);
                            } else
                            {
                                System.out.println(device.Name + " GPIOPin is not setup");
                            }

                        } else if (device.Temperature > DefaultUpper)
                        {
                            System.out.println(device.Name + " - " + device.Temperature + " > " + DefaultUpper + " Pad is OFF");
                            if (device.Enabled)
                            {
                                SetPin(device, 0);
                            }
                        }
                    }
                } catch (IOException ex)
                {
                }
            }

            private void SetPin(Device device, int i)
            {
                try
                {
                    if (i == 1)
                    {

                        device.GPIOpin.high();
                    } else
                    {
                        device.GPIOpin.low();
                    }

                } catch (Exception e)
                {
                    System.out.println(device.Name + " GPIOPin is not setup");
                }
            }
        },
                 0, Interval
        );

        //TODO gpio.shutdown(); put somewhere else to run at shutdown only
    }
}
