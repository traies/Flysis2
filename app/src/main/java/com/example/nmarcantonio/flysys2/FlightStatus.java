package com.example.nmarcantonio.flysys2;

/**
 * Created by traie_000 on 17/11/2016.
 */

public class FlightStatus {
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
}