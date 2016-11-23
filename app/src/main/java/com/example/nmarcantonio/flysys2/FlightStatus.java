package com.example.nmarcantonio.flysys2;

import java.io.Serializable;

/**
 * Created by traie_000 on 17/11/2016.
 */

public class FlightStatus implements Serializable {
    int id;
    int number;
    AirlineInfo airline;
    String status;
    FlightInfo arrival;
    FlightInfo departure;

    FlightStatus(int id, int number, AirlineInfo airline, String status, FlightInfo arrival, FlightInfo departure) {
        this.id = id;
        this.number = number;
        this.airline = airline;
        this.status = status;
        this.arrival = arrival;
        this.departure = departure;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(this.getClass())) {
            FlightStatus other = (FlightStatus) obj;
            return other.number == this.number && this.airline.id.equals(other.airline.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (String.valueOf(number) + "|" +airline.id).hashCode();
    }
}
