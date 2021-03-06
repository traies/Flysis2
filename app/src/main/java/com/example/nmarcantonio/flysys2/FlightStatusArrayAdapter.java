package com.example.nmarcantonio.flysys2;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.ArraySwipeAdapter;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.R.attr.id;
import static android.R.attr.singleUser;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.L;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Created by traie_000 on 23-Nov-16.
 */

public class FlightStatusArrayAdapter extends BaseSwipeAdapter{
    private Context context;
    private List<FlightStatus> flights;
    private BaseSwipeAdapter adapter = this;
    private SwipeRefreshLayout swiping;
    public FlightStatusArrayAdapter(Context context, List<FlightStatus> objects, SwipeRefreshLayout swiping) {
        this.flights=objects;
        this.context = context;
        this.swiping = swiping;
    }

    @Override
    public int getCount() {
        return flights.size();
    }

    @Override
    public Object getItem(int position) {
        return flights.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int position, ViewGroup parent){
        return (SwipeLayout)LayoutInflater.from(context).inflate(R.layout.flight_status_item_swipeable,parent,false);
    }

    @Override
    public void fillValues(final int position, View convertView){
        final FlightStatus flightStatus = (FlightStatus)getItem(position);
        final SwipeLayout swipeLayout = (SwipeLayout)convertView;
        final FlightStatusHolder holder;

        holder = new FlightStatusHolder(flightStatus.airline.id,flightStatus.number,
                flightStatus.airline,flightStatus.departure.airport,flightStatus.arrival.airport,flightStatus);
        holder.header = (TextView) swipeLayout.findViewById(R.id.flights_card_header);
        holder.origin = (TextView) swipeLayout.findViewById(R.id.flights_card_origin);
        holder.destintation = (TextView) swipeLayout.findViewById(R.id.flights_card_destination);
        holder.description = (TextView) swipeLayout.findViewById(R.id.flights_card_description);
        holder.logo = (ImageView)convertView.findViewById((R.id.logo));
        holder.state = (TextView) swipeLayout.findViewById(R.id.flight_card_state);
        swipeLayout.setTag(holder);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {

            @Override
            public void onClose(SwipeLayout layout) {
                //when the SurfaceView totally cover the BottomView.
                //;
            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                //you are swiping.
            }

            @Override
            public void onStartOpen(SwipeLayout layout) {
                //
            }

            @Override
            public void onOpen(SwipeLayout layout) {
                //when the BottomView totally show.
                FlightStatusHolder holder = (FlightStatusHolder)layout.getTag();
                String s = holder.getId()+"-"+holder.getNumber();
                int i;
                for(i = 0; i < flights.size(); i++) {
                    FlightStatus f = flights.get(i);
                    if (f.airline.id.equals(holder.getId()) && f.number == holder.getNumber()) {
                        flights.remove(i);
                        break;
                    }
                }
                if(flights.size()==0){
                    ((Activity)context).findViewById(R.id.flights_empty_error).setVisibility(View.VISIBLE);
                }
                notifyDataSetChanged();
                PreferencesHelper.updatePreferences((ArrayList) flights, context);
                if(BinPreferencesHelper.recycleFlight(new FlightShort(holder.getId(),holder.getNumber(),
                        holder.getAirlineInfo(),holder.getDeparture(),holder.getArrival(),holder.getStatus()),context)){
                    Toast.makeText(context,s+" "+context.getString(R.string.toBin), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onStartClose(SwipeLayout layout) {
            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {
                //when user's hand released.
            }
        });

        ViewTreeObserver.OnGlobalLayoutListener swipeGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                swipeLayout.close(false);
            }
        };

        swipeLayout.getViewTreeObserver().addOnGlobalLayoutListener(swipeGlobalLayoutListener);

        swipeLayout.getSurfaceView().setOnClickListener(new SwipeLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlightStatus status = flights.get(position);
                Intent intent = new Intent(context, FlightActivity.class);
                intent.putExtra("id",status.airline.id);
                intent.putExtra("number",new Integer(status.number).toString());

                PendingIntent pendingIntent =
                        TaskStackBuilder.create(context)
                                // add all of DetailsActivity's parents to the stack,
                                // followed by DetailsActivity itself
                                .addNextIntentWithParentStack(intent)
                                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
                builder.setContentIntent(pendingIntent);
                context.startActivity(intent);
            }
        });

        holder.header.setText(context.getString(R.string.flight) + " " + flightStatus.number);
        holder.origin.setText(flightStatus.departure.airport.id);
        holder.destintation.setText(flightStatus.arrival.airport.id);
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .showImageOnLoading(R.drawable.ic_loading)
                .showImageOnFail(R.drawable.ic_error)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(defaultOptions)
                .build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        if(!imageLoader.isInited())
            imageLoader.init(config);

        imageLoader.loadImage(flightStatus.airline.getLogo(), new SimpleImageLoadingListener() {

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                holder.logo.setImageBitmap(loadedImage);
            }

        });
        flightStatus.setDescription(context);
        if (flightStatus.flightStatusDescription != null) {
            holder.description.setText(flightStatus.flightStatusDescription.buildDescription(new Date(), context));
            String statusString;
            int statusColor;

            switch (flightStatus.flightStatusDescription.state) {
                case SCHEDULED:
                    statusString = context.getString(R.string.flight_info_status_scheduled);
                    statusColor = context.getResources().getColor(R.color.colorGreen);
                    break;
                case BOARDING:
                    statusString = context.getString(R.string.flight_info_status_boarding);
                    statusColor = context.getResources().getColor(R.color.colorGreen);
                    break;
                case FLYING:
                    statusString = context.getString(R.string.flight_info_status_flying);
                    statusColor = context.getResources().getColor(R.color.colorGreen);
                    break;
                case DIVERT:
                    statusString = context.getString(R.string.flight_info_status_divert);
                    statusColor = context.getResources().getColor(R.color.colorRed);
                    break;
                case CANCELLED:
                    statusString = context.getString(R.string.flight_info_status_cancelled);
                    statusColor = context.getResources().getColor(R.color.colorRed);
                    break;
                case LANDED:
                    statusString = context.getString(R.string.flight_info_status_landed);
                    statusColor = context.getResources().getColor(R.color.colorGreen);
                    break;
                default:
                    statusString = context.getString(R.string.flight_info_status_unknown);
                    statusColor = context.getResources().getColor(R.color.colorRed);
                    break;
            }
            holder.state.setText(statusString);
            holder.state.setTextColor(statusColor);
        }
    }
}
