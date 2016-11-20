package com.example.nmarcantonio.flysys2;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by traies on 20/11/16.
 */

public class GetCommentsTask extends AsyncTask<String, Void, String> {
    View commentView;
    Activity context;

    GetCommentsTask(View commentView, Activity context) {
        super();
        this.commentView = commentView;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection conn = null;
        String ret = null;
        try {
            Uri uri = new Uri.Builder()
                    .scheme("http")
                    .authority("hci.it.itba.edu.ar")
                    .appendPath("v1")
                    .appendPath("api")
                    .appendPath("review.groovy")
                    .appendQueryParameter("method", "getairlinereviews")
                    .appendQueryParameter("airline_id", strings[0])
                    .appendQueryParameter("flight_number", strings[1])
                    .appendQueryParameter("sort_key", "rating")
                    .appendQueryParameter("sort_order", "desc")
                    .appendQueryParameter("page_size", "5")
                    .build();
            conn = (HttpURLConnection) new URL(uri.toString()).openConnection();

            InputStream in = new BufferedInputStream(conn.getInputStream());
            ret = readStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return ret;
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject obj = new JSONObject(result);
            if (!obj.has("reviews")) {
                return;
            }
            else {
                Gson gson = new Gson();
                Type listType = new TypeToken<CommentInfo>() {}.getType();
                CommentInfo ci = gson.fromJson(obj.toString(), listType);
                Comment[] comments = new Comment[ci.page_size];
                for (int i = 0; i < ci.page_size; i++) {
                    CommentInfo.ReviewInfo ri = ci.reviews[i];
                    comments[i] = new Comment(
                            Html.fromHtml(Uri.decode(ri.comments)).toString(),
                            ri.yes_recommend,
                            ri.rating.overall,
                            ri.rating.friendliness,
                            ri.rating.food,
                            ri.rating.punctuality,
                            ri.rating.mileage_program,
                            ri.rating.comfort,
                            ri.rating.quality_price
                    );
                }
                CommentArrayAdapter adapter = new CommentArrayAdapter(context, comments);
                final ListView listView = (ListView) commentView.findViewById(R.id.flight_comments_list_view);
                listView.setAdapter(adapter);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String readStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int i = inputStream.read();
            while(i != -1 ) {
                outputStream.write(i);
                i = inputStream.read();
            }
            return outputStream.toString();
        } catch(IOException e) {
            e.printStackTrace();
            Log.d("err", "fallo la conexion");
            return null;
        }
    }

    private static void setText(Activity activity, int id, String str) {
        if (str == null) {
            str = "-";
        }
        ((TextView) activity.findViewById(id))
                .setText(str);
    }
}
