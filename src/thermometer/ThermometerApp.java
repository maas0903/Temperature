/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermometer;

/**
 *
 * @author marius To run: cd '/home/pi/usbdrv/NetBeansProjects//Thermometer';
 * '/usr/lib/jvm/java-9-openjdk-armhf/bin/java' -Dfile.encoding=UTF-8 -jar
 * /home/pi/usbdrv/NetBeansProjects//Thermometer/dist/Thermometer.jar
 *
 */
import static com.melektro.Tools.LoadProperty.LoadProperty;
import com.melektro.Tools.MyWget;
import static com.melektro.Tools.MyWget.MyWget;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.sun.corba.se.impl.protocol.giopmsgheaders.Message;
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
//import java.util.Timer;
//import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.naming.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pi4j.io.gpio.PinMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import static javafx.scene.input.KeyCode.T;

public class ThermometerApp
{

    static String baseDir = "/sys/bus/w1/devices/";
    static String searchPattern = "28";
    static String deviceFile = "/w1_slave";
    static int Interval = 5000;
    private static String UpperLimit = "22.5";
    private static String LowerLimit = "21.5";
    private static Float Upper;
    private static Float Lower;
    private static boolean goingUp = true;
    private static long cycle = 0;
    private static LocalDate dateNow = LocalDate.MIN;

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
        
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:m:s a");        
    LocalTime l =LocalTime.from(timeFormatter.parse("4:43:38 AM"));

        
        SunRiseSet sunRiseSet=null;

        final GpioController gpio = GpioFactory.getInstance();
        //Runtime.getRuntime().addShutdownHook(new OnExit(gpio));
        final GpioPinDigitalOutput GPIOpin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "PinOut", PinState.HIGH);
        GPIOpin.setShutdownOptions(true, PinState.HIGH);

        List<Device> devices = SetupDevices(GetDS18B20Directories());

        for (;;)
        {
            try
            {
                if (cycle == 4)
                {
//                        System.out.println("Debug exit");
//                        gpio.shutdown();
//                        System.exit(0);
                }
                if (cycle < Long.MAX_VALUE)
                {
                    cycle = cycle + 1;
                } else
                {
                    cycle = 1;
                }

                if (GetNewSunriseSunSet())
                {
                    sunRiseSet = GetSunRiseSunSet();
                    dateNow = LocalDate.now();
                }

                GetProperties();
                for (Device device : devices)
                {
                    device.Temperature = GetDeviceTemperature(device.Directory);
                    if (!TimeToSleep(sunRiseSet, 1))
                    {

                        if (goingUp)
                        {
                            if (device.Temperature >= Upper)
                            {
                                goingUp = false;
                                System.out.println("Cycle = " + cycle + " - " + device.Name + " - Phase change from going up to going down");
                            } else
                            {
                                GPIOpin.low();
                                System.out.println("Cycle = " + cycle + " - " + device.Name + " - going up - " + device.Temperature + " < (Upper) " + Upper + " Pad is ON");
                            }
                        } else
                        {
                            if (device.Temperature <= Lower)
                            {
                                System.out.println("Cycle = " + cycle + " - " + device.Name + " - Phase change from going down to going up");
                                goingUp = true;
                            } else
                            {
                                GPIOpin.high();
                                System.out.println("Cycle = " + cycle + " - " + device.Name + " - going down - " + device.Temperature + " > (Lower)" + Lower + " Pad is OFF");
                            }
                        }
                    }
                    else
                    {
                        GPIOpin.high();
                        System.out.println("Cycle = " + cycle + " - " + device.Name + " Sleeping at " + device.Temperature + " deg C");
                    }
                }

            } catch (IOException ex)
            {
            }
            Thread.sleep(Interval);
        }
    }

    private static SunRiseSet GetSunRiseSunSet() throws IOException
    {
        //https://sunrise-sunset.org/api
        String riseSet = MyWget("https://api.sunrise-sunset.org/json?lat=50.918769&lng=4.698409", "", "")
                .replace('[', ' ')
                .replace(']', ' ');
         //String riseSet =  "{\"results\":{\"sunrise\":\"4:43:38 AM\",\"sunset\":\"3:0:0 PM\",\"solar_noon\":\"11:40:57 AM\",\"day_length\":\"13:54:39\",\"civil_twilight_begin\":\"4:08:36 AM\",\"civil_twilight_end\":\"7:13:18 PM\",\"nautical_twilight_begin\":\"3:25:03 AM\",\"nautical_twilight_end\":\"7:56:51 PM\",\"astronomical_twilight_begin\":\"2:36:08 AM\",\"astronomical_twilight_end\":\"8:45:46 PM\"},\"status\":\"OK\"}" ;
         
        Gson gson = new Gson();

        return gson.fromJson(riseSet, SunRiseSet.class);
    }

    private static boolean TimeToSleep(SunRiseSet sunRiseSet, int i)
    {
        LocalTime sunRise = sunRiseSet.getResults().getSunrise();
        LocalDateTime sunRiseDT = LocalDateTime.of(LocalDate.now(), sunRise);
        LocalTime sunSet = sunRiseSet.getResults().getSunset();
        LocalDateTime sunSetDT = LocalDateTime.of(LocalDate.now(), sunSet);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(sunRiseDT))
        {
           sunRiseDT = sunRiseDT.plusDays(1); 
        }
        return (now.isAfter(sunSetDT.plusHours(i)) && now.isBefore(sunRiseDT.plusHours(i)));
    }

    private static boolean GetNewSunriseSunSet()
    {
        LocalDate localNow = LocalDate.now();
        return !dateNow.isEqual(localNow);
    }

    private static boolean AfterMidnight(LocalDateTime now)
    {
        return now.isAfter(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT));
    }
}
