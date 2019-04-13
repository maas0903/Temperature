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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import thermometer.Mappers.DeviceGPIOToRaspiGPIOMapper;

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
    protected static final String CONFIGFILE = "thermometer.config.properties";
    private static final GpioController GPIO = GpioFactory.getInstance();

    private static final String DEBUGFILECONTENT = "66 01 4b 46 7f ff 0a 10 2d : crc=2d YES\n"
            + "66 01 4b 46 7f ff 0a 10 2d t=22375";
    private static final int NUMBEROFDEBUGDEVICES = 2;
    private static final boolean DEBUG = true;

    private static void SetProperties()
    {
        Properties prop = new Properties();
        OutputStream output = null;

        try
        {
            output = new FileOutputStream(CONFIGFILE);
            prop.setProperty("DefaultUpperLimit", DefaultUpperLimit);
            prop.setProperty("DefaultLowerLimit", DefaultLowerLimit);
            DefaultUpper = Float.parseFloat(DefaultUpperLimit);
            DefaultLower = Float.parseFloat(DefaultLowerLimit);

            for (Device device : devices)
            {
                if (device.GPIOPin == null || device.GPIOPin.equals(""))
                {
                    device.Enabled = false;
                    device.GPIOPin = "GPIO_XX";
                    prop.setProperty(device.Name + "Enabled", "false");
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

                prop.setProperty(device.Name + "_GPIOPin", device.GPIOPin);
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

        File file = new File(CONFIGFILE);

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
                    device.GPIOPin = LoadProperty(prop, device.Name + "_GPIOPin", "GPIO_XX");
                    String Enabled = LoadProperty(prop, device.Name + "Enabled", "false");

                    if (device.GPIOPin == null || device.GPIOPin.equals("GPIO_XX"))
                    {
                        device.Enabled = false;
                    } else
                    {
                        device.Enabled = true;

                        if (DEBUG)
                        {
                            if (!Files.exists(Paths.get(device.Name)))
                            {
                                Files.write(Paths.get(device.Name), DEBUGFILECONTENT.getBytes());
                            }
                        } else
                        {
                            if (device.GPIOpin == null)
                            {
                                try
                                {
                                    device.GPIOpin = GPIO.provisionDigitalOutputPin(DeviceGPIOToRaspiGPIOMapper.Map(device.GPIOPin), "PinOut", PinState.LOW);
                                } catch (Exception e)
                                {
                                    device.Enabled = false;
                                }
                                device.GPIOpin.setShutdownOptions(true, PinState.LOW);
                            }
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

        if (DEBUG)
        {
            for (int i = 0; i < NUMBEROFDEBUGDEVICES; i++)
            {
                result.add("Debug" + i);
            }

        } else
        {
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
        }
        return result;
    }

    static String readFile(String path, Charset encoding) throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static Float GetDeviceTemperature(Device device) throws IOException
    {
        Float result;
        String cont;

        if (DEBUG)
        {
            cont = readFile(device.Name, Charset.defaultCharset());
        } else
        {
            cont = readFile(device.Directory, Charset.defaultCharset());
        }

        if (cont.contains(" YES"))
        {
            result = Float.parseFloat(cont.substring(cont.indexOf("t=") + 2)) / 1000;
        } else
        {
            result = Float.parseFloat("1000");
        }

        return result;
    }

    private static List<Device> SetupDevices(List<String> DS18B20Directories)
    {
        List<Device> Devices = new ArrayList<>();

        int DebugNo = 0;
        for (String dir : DS18B20Directories)
        {
            Devices.add(new Device(dir, DEBUG, DebugNo));
            DebugNo = DebugNo + 1;
        }

        return Devices;
    }

    private static void SetPin(Device device, int i)
    {
        if (DEBUG)
        {
            device.DebugPin = i;
        } else
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
    }

    private static void RunACycle()
    {
        try
        {
            if (!GetProperties())
            {
                Path currentRelativePath = Paths.get("");
                String appPath = currentRelativePath.toAbsolutePath().toString();

                System.out.println("Please setup the configuration in " + appPath + "/" + CONFIGFILE);
                System.exit(0);
            }

            for (Device device : devices)
            {
                device.cycle = device.cycle + 1;
                device.Temperature = GetDeviceTemperature(device);
                if (device.goingUp)
                {
                    if (device.Temperature > device.Upper)
                    {
                        device.goingUp = false;
                        System.out.println("Cycle = " + device.cycle + " - " + device.Name + " - Phase change from going up to going down");
                    } else
                    {
                        SetPin(device, 1);
                        System.out.println("Cycle = " + device.cycle + " - " + device.Name + " - going up - " + device.Temperature + " < (Upper) " + device.Upper + " Pad is ON");
                    }
                } else
                {
                    if (device.Temperature < device.Lower)
                    {
                        System.out.println("Cycle = " + device.cycle + " - " + device.Name + " - Phase change from going down to going up");
                        device.goingUp = true;
                    } else
                    {
                        SetPin(device, 0);
                        System.out.println("Cycle = " + device.cycle + " - " + device.Name + " - going down - " + device.Temperature + " > (Lower)" + device.Lower + " Pad is OFF");
                    }
                }
            }
        } catch (IOException ex)
        {
        }
    }

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        devices = SetupDevices(GetDS18B20Directories());

        if (DEBUG)
        {
            int DebugCycles = 10;
            for (int i = 0; i < DebugCycles; i++)
            {
                RunACycle();
            }
        } else
        {
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    RunACycle();
                }
            },
                    0, Interval
            );
        }

        //TODO gpio.shutdown(); put somewhere else to run at shutdown only
    }
}
