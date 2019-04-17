/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package thermometer;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author marius
 */
public class Results
{

    @SerializedName("sunrise")
    @Expose
    private String sunrise;
    @SerializedName("sunset")
    @Expose
    private String sunset;
    @SerializedName("solar_noon")
    @Expose
    private String solarNoon;
    @SerializedName("day_length")
    @Expose
    private String dayLength;
    @SerializedName("civil_twilight_begin")
    @Expose
    private String civilTwilightBegin;
    @SerializedName("civil_twilight_end")
    @Expose
    private String civilTwilightEnd;
    @SerializedName("nautical_twilight_begin")
    @Expose
    private String nauticalTwilightBegin;
    @SerializedName("nautical_twilight_end")
    @Expose
    private String nauticalTwilightEnd;
    @SerializedName("astronomical_twilight_begin")
    @Expose
    private String astronomicalTwilightBegin;
    @SerializedName("astronomical_twilight_end")
    @Expose
    private String astronomicalTwilightEnd;

    private LocalTime sunRise;
    private LocalTime sunSet;

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:m:s a");
    
    public LocalTime getSunrise()
    {
        LocalTime l =LocalTime.from(timeFormatter.parse(sunrise));
        
        return  LocalTime.from(ZonedDateTime
                .of(LocalDateTime
                        .of(LocalDate.now(), l), ZoneId.of("Z"))
                .withZoneSameInstant(ZoneId.of("Europe/Brussels")));
    }
    
    

    public String getSunriseString()
    {
        return sunrise;
    }

    public void setSunrise(String sunrise)
    {
        this.sunrise = sunrise;
    }

    public LocalTime getSunset()
    {
        return  LocalTime.from(ZonedDateTime
                .of(LocalDateTime
                        .of(LocalDate.now(), LocalTime.from(timeFormatter.parse(sunset))), ZoneId.of("Z"))
                .withZoneSameInstant(ZoneId.of("Europe/Brussels")));
    }

    public String getSunsetString()
    {
        return sunset;
    }

    public void setSunset(String sunset)
    {
        this.sunset = sunset;
    }

    public String getSolarNoon()
    {
        return solarNoon;
    }

    public void setSolarNoon(String solarNoon)
    {
        this.solarNoon = solarNoon;
    }

    public String getDayLength()
    {
        return dayLength;
    }

    public void setDayLength(String dayLength)
    {
        this.dayLength = dayLength;
    }

    public String getCivilTwilightBegin()
    {
        return civilTwilightBegin;
    }

    public void setCivilTwilightBegin(String civilTwilightBegin)
    {
        this.civilTwilightBegin = civilTwilightBegin;
    }

    public String getCivilTwilightEnd()
    {
        return civilTwilightEnd;
    }

    public void setCivilTwilightEnd(String civilTwilightEnd)
    {
        this.civilTwilightEnd = civilTwilightEnd;
    }

    public String getNauticalTwilightBegin()
    {
        return nauticalTwilightBegin;
    }

    public void setNauticalTwilightBegin(String nauticalTwilightBegin)
    {
        this.nauticalTwilightBegin = nauticalTwilightBegin;
    }

    public String getNauticalTwilightEnd()
    {
        return nauticalTwilightEnd;
    }

    public void setNauticalTwilightEnd(String nauticalTwilightEnd)
    {
        this.nauticalTwilightEnd = nauticalTwilightEnd;
    }

    public String getAstronomicalTwilightBegin()
    {
        return astronomicalTwilightBegin;
    }

    public void setAstronomicalTwilightBegin(String astronomicalTwilightBegin)
    {
        this.astronomicalTwilightBegin = astronomicalTwilightBegin;
    }

    public String getAstronomicalTwilightEnd()
    {
        return astronomicalTwilightEnd;
    }

    public void setAstronomicalTwilightEnd(String astronomicalTwilightEnd)
    {
        this.astronomicalTwilightEnd = astronomicalTwilightEnd;
    }
}
