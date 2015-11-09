package com.thevisionaries.electricitysurveillancesystem;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener {

    Activity activity;
    TextView threshold_exceeded_textview;
    ProgressBar thresholdProgressBar;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        activity = getActivity();
        Button onOffButton = (Button) rootView.findViewById(R.id.on_off_button);
        onOffButton.setOnClickListener(this);
        Button setThresholdButton = (Button) rootView.findViewById(R.id.set_threshold_button);
        setThresholdButton.setOnClickListener(this);
        Button checkConsumptionButton = (Button) rootView.findViewById(R.id.check_consumption_button);
        checkConsumptionButton.setOnClickListener(this);

        thresholdProgressBar = (ProgressBar) rootView.findViewById(R.id.threshold_progressBar);

        threshold_exceeded_textview = (TextView) rootView.findViewById(R.id.threshold_exceeded_textview);
        getThresholdStatus();

        return rootView;
    }

    void getThresholdStatus() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                threshold_exceeded_textview.setVisibility(View.GONE);
                thresholdProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                if (integer == 0) {
                    threshold_exceeded_textview.setText("No");
                } else {
                    threshold_exceeded_textview.setText("Yes");
                }
                threshold_exceeded_textview.setVisibility(View.VISIBLE);
                thresholdProgressBar.setVisibility(View.GONE);
            }

            @Override
            protected Integer doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String response = null;
                try {

                    Uri builturi = Uri.parse(getActivity().getString(R.string.base_url))
                            .buildUpon()
                            .appendPath("android-web-service")
                            .appendPath("handler.php")
                            .appendQueryParameter("query", "isThresholdExceeded")
                            .build();
                    URL url = new URL(builturi.toString());
                    Log.d("url", "url=" + url.toString());

                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.connect();

                    InputStream input = urlConnection.getInputStream();

                    if (input == null) {
                        return null;
                    }

                    reader = new BufferedReader(new InputStreamReader(input));
                    StringBuffer buffer = new StringBuffer();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        return null;
                    }
                    response = buffer.toString();
                    if (response == null) {
                        return null;
                    }

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        return Integer.parseInt(jsonObject.getString("threshold_exceeded"));
                    } catch (JSONException e) {
                        Log.e("Main Activity Fragment", "Json result error", e);
                    }

                } catch (IOException e) {
                    Log.e("Main Activity Fragment", "error", e);
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            Log.e("Main Activity Fragment", "Error closing stream", e);
                        }

                    }
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onClick(View view) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
        Intent intent;
        switch (view.getId()) {
            case R.id.on_off_button:
                intent = new Intent(activity, DeviceSwitchActivity.class);
                startActivity(intent, options.toBundle());
                break;
            case R.id.check_consumption_button:
                intent = new Intent(activity, CheckConsumptionActivity.class);
                startActivity(intent, options.toBundle());
                break;
            case R.id.set_threshold_button:
                intent = new Intent(activity, SetThresholdActivity.class);
                startActivityForResult(intent, 100, options.toBundle());
                break;
            default:
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            getThresholdStatus();
        }
    }
}
