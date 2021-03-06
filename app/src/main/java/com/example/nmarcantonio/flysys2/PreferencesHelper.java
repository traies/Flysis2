package com.example.nmarcantonio.flysys2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;

/**
 * Created by traie_000 on 22-Nov-16.
 */

public class PreferencesHelper {
    private static final String TAG = "PreferencesHelper";

    public static synchronized ArrayList<FlightStatus> getFlights(Context context) {

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<FlightStatus>>() {
        }.getType();

        SharedPreferences sharedPreferences = context.getSharedPreferences("flights", Context.MODE_PRIVATE);
        String flightsString;
        if ( (flightsString = sharedPreferences.getString("flights", null)) == null) {
            flightsString = gson.toJson(new ArrayList<FlightStatus>(), listType);
            SharedPreferences.Editor e = sharedPreferences.edit();
            e.putString("flights", flightsString);
            e.apply();
        }
        ArrayList<FlightStatus> flights = gson.fromJson(flightsString, listType);
        return flights;
    }

    public static synchronized void updatePreferences(ArrayList<FlightStatus> list, Context context) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<FlightStatus>>() {
        }.getType();
        SharedPreferences sharedPreferences = context.getSharedPreferences("flights", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String flightsString = gson.toJson(list, listType);
        editor.putString("flights", flightsString);
        editor.apply();
    }

    public static synchronized void updatePreferences(FlightStatus flightStatus, Context context) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<FlightStatus>>() {
        }.getType();
        SharedPreferences sharedPreferences = context.getSharedPreferences("flights", Context.MODE_PRIVATE);
        String flightsString = sharedPreferences.getString("flights", null);
        ArrayList<FlightStatus> flights;
        if (flightsString == null) {
            flights = new ArrayList<>();
            flights.add(flightStatus);
        }
        else {
            flights = gson.fromJson(flightsString, listType);
            int index = flights.indexOf(flightStatus);
            if (index >= 0) {
                flights.set(index, flightStatus);
            }
        }
        flightsString = gson.toJson(flights);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("flights", flightsString);
        editor.apply();
    }

    public static synchronized FlightStatus getFlight(FlightStatus flightStatus, Context context) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<FlightStatus>>() {
        }.getType();
        SharedPreferences sharedPreferences = context.getSharedPreferences("flights", Context.MODE_PRIVATE);

        String flightStatusOut = sharedPreferences.getString("flights", null);
        if (flightStatusOut == null) {
            return null;
        }

        ArrayList<FlightStatus> flightStatusArrayList = gson.fromJson(flightStatusOut, listType);
        int index = flightStatusArrayList.indexOf(flightStatus);
        if (index >= 0) {
            FlightStatus flight = flightStatusArrayList.get(index);
            return flight;
        }
        return null;
    }

    public static synchronized Boolean notificationsActive(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean("notifications_active", true);
    }

    public static synchronized long notificationsInterval(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String stringValue = sharedPreferences.getString("interval_list","1m");
        long interval;
        switch(stringValue) {
            case "1h":
                interval = 3600000;
                break;
            case "6h":
                interval = 21600000;
                break;
            case "12h":
                interval = 43200000;
                break;
            case "24h":
                interval = 86400000;
                break;
            case "5m":
                interval = 300000;
                break;
            default:
                interval = 60000;
                break;
        }
        return interval;
    }
}
