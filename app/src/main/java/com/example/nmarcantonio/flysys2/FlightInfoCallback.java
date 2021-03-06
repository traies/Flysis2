package com.example.nmarcantonio.flysys2;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.example.nmarcantonio.flysys2.FlightStatus.FlightStatusState.CANCELLED;

/**
 * Created by traie_000 on 22-Nov-16.
 */

public class FlightInfoCallback implements TaskCallback, OnMapReadyCallback {
    private static final String TAG = "FlightInfoCallback";
    View flightView;
    GoogleMap map;
    Condition mapCond;
    boolean mapInit;
    Lock lock;
    Context context;
    ArrayList<FlightStatus> storedFlights;

    public FlightInfoCallback(View flightView, MapFragment mapFragment, Context context, ArrayList<FlightStatus> storedFlights) {
        this.flightView = flightView;
        mapFragment.getMapAsync(this);
        lock = new ReentrantLock();
        mapCond = lock.newCondition();
        mapInit = false;
        this.context = context;
        this.storedFlights = storedFlights;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        lock.lock();
        this.map = map;
        map.getUiSettings().setAllGesturesEnabled(false);
        mapInit = true;
        mapCond.signal();
        lock.unlock();
    }

    @Override
    public void callback(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            if (!obj.has("status")) {
                FrameLayout frameLayout = (FrameLayout) flightView.findViewById(R.id.flight_info_layout_inflate);
                ((Activity) context).getLayoutInflater().inflate(R.layout.flight_info_not_found, frameLayout);
                return;
            }
            else {

                FrameLayout frameLayout = (FrameLayout) flightView.findViewById(R.id.flight_info_layout_inflate);
                ((Activity) context).getLayoutInflater().inflate(R.layout.flight_info_body, frameLayout);

                Gson gson = new Gson();
                Type listType = new TypeToken<FlightStatus>() {
                }.getType();

                String jsonFragment = obj.getString("status");
                final FlightStatus fi = gson.fromJson(jsonFragment, listType);
                ((TextView)flightView.findViewById(R.id.flight_info_number)).setText(context.getString(R.string.flight) + " " + fi.number);
                ((TextView)flightView.findViewById(R.id.flight_origin_content)).setText(fi.departure.airport.city.name.split(",")[0]);
                ((TextView)flightView.findViewById(R.id.flight_destination_content_2)).setText(fi.arrival.airport.city.name.split(",")[0]);
                String statusString;
                String statusDescription = "";
                int statusColor;
                Date arrivalTime = null;
                Date currentTime = new Date();
                String stringHeader = null;
                fi.setDescription(context);
                FlightStatusDescription fd = fi.flightStatusDescription;
                switch (fd.state) {
                    case SCHEDULED:
                        statusString = context.getString(R.string.flight_info_status_scheduled);
                        statusColor = flightView.getResources().getColor(R.color.colorGreen);
                        break;
                    case BOARDING:
                        statusString = context.getString(R.string.flight_info_status_boarding);
                        statusColor = flightView.getResources().getColor(R.color.colorGreen);
                        break;
                    case FLYING:
                        statusString = context.getString(R.string.flight_info_status_flying);
                        statusColor = flightView.getResources().getColor(R.color.colorGreen);
                        break;
                    case DIVERT:
                        statusString = context.getString(R.string.flight_info_status_divert);
                        statusColor = flightView.getResources().getColor(R.color.colorRed);
                        break;
                    case CANCELLED:
                        statusString = context.getString(R.string.flight_info_status_cancelled);
                        statusColor = flightView.getResources().getColor(R.color.colorRed);
                        break;
                    case LANDED:
                        statusString = context.getString(R.string.flight_info_status_landed);
                        statusColor = flightView.getResources().getColor(R.color.colorGreen);
                        break;
                    default:
                        statusString = context.getString(R.string.flight_info_status_unknown);
                        statusColor = flightView.getResources().getColor(R.color.colorRed);
                        break;
                }

                ((TextView)flightView.findViewById(R.id.flight_info_status)).setTextColor(statusColor);
                ((TextView)flightView.findViewById(R.id.flight_info_status)).setText(statusString);
                TextView dateText = (TextView) flightView.findViewById(R.id.flight_info_status_description);
                if (fi.flightStatusDescription.state != CANCELLED) {
                    dateText.setText(fi.flightStatusDescription.buildDescription(new Date(), context));
                }

                View detailView = flightView.findViewById(R.id.flight_info_detail);
                detailView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, FlightInfoDetailActivity.class);

                        PendingIntent pendingIntent =
                                TaskStackBuilder.create(context)
                                        .addNextIntentWithParentStack(intent)
                                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setContentIntent(pendingIntent);
                        intent.putExtra("flight_info", fi);
                        context.startActivity(intent);
                    }
                });

                View commentView = flightView.findViewById(R.id.flight_comments);
                commentView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, FlightCommentsActivity.class);

                        PendingIntent pendingIntent =
                                TaskStackBuilder.create(context)
                                        .addNextIntentWithParentStack(intent)
                                        .getPendingIntent(1, PendingIntent.FLAG_UPDATE_CURRENT);

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                        builder.setContentIntent(pendingIntent);
                        intent.putExtra("flight_info", fi);
                        context.startActivity(intent);
                    }
                });

                View thumbsUpButton = flightView.findViewById(R.id.flight_comments_thumbs_up);

                thumbsUpButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        ((FlightActivity) context).showPostCommentDialog(true, fi.airline.id, String.valueOf(fi.number));
                    }
                });

                View thumbsDownButton = flightView.findViewById(R.id.flight_comments_thumbs_down);

                thumbsDownButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        ((FlightActivity) context).showPostCommentDialog(false, fi.airline.id, String.valueOf(fi.number));
                    }
                });

                new GetFlightCommentsPreviewTask(flightView.findViewById(R.id.flight_comments_preview_list), context).execute(fi.airline.getId(), String.valueOf(fi.number));

                lock.lock();
                while (!mapInit) {
                    try {
                        mapCond.await();
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                lock.unlock();
                LatLng departure = new LatLng(Double.valueOf(fi.departure.airport.latitude), Double.valueOf(fi.departure.airport.longitude));
                LatLng arrival = new LatLng(Double.valueOf(fi.arrival.airport.latitude), Double.valueOf(fi.arrival.airport.longitude));

                Drawable plane = context.getResources().getDrawable(R.drawable.ic_flight_black_24dp);
                Drawable arrow = context.getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp);
                BitmapDescriptor bd = getMarkerIconFromDrawable(plane);
                BitmapDescriptor ad = getMarkerIconFromDrawable(arrow);

                Marker marker = map.addMarker(new MarkerOptions().position(departure).icon(bd));
                Marker arrowTip = map.addMarker(new MarkerOptions().position(arrival).icon(ad));
                arrowTip.setAnchor(0.5f, 0.5f);
                arrowTip.setRotation((float) SphericalUtil.computeHeading(departure, arrival));
                marker.setAnchor(0.5f, 0.5f);
                marker.setRotation((float) SphericalUtil.computeHeading(departure, arrival));
                map.addPolyline(new PolylineOptions().color(context.getResources().getColor(R.color.colorRed)).add(departure).add(arrival).width(5));
                //marker.setIcon(bd);
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(departure);
                builder.include(arrival);
                LatLngBounds bounds = builder.build();
                //marker.setPosition(arrival);
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));

                MarkerAnimation.animateMarkerToHC(marker, arrival, new LatLngInterpolator.LinearFixed());
                final FloatingActionButton fab = (FloatingActionButton) flightView.findViewById(R.id.add_flight_fab);
                fab.setOnClickListener(new AddFlightOnClickListener(storedFlights, fi, fab, context));

            }
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.d("err", "fallo la conexion");

        }
    }

    private BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
