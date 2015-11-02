package com.thevisionaries.electricitysurveillancesystem;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONArray;
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
public class DeviceSwitchActivityFragment extends Fragment {

    DeviceListAdapter mDeviceListAdapter;
    private RecyclerView mRecyclerView;
    int[] deviceNo, deviceStatus;
    String[] deviceNames;
//    public final String BASE_URL = "http://thevisionaries.binhoster.com/";
    ProgressBar deviceProgressBar = null;
    int deviceCount = 0;

    public DeviceSwitchActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device_switch, container, false);

        deviceProgressBar = (ProgressBar) rootView.findViewById(R.id.devices_progressBar);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.devices_recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);

        mDeviceListAdapter = new DeviceListAdapter(getActivity());
        mRecyclerView.setAdapter(mDeviceListAdapter);

        refreshDeviceList();

        return rootView;
    }

    void refreshDeviceList(){
        new FetchDevicesTask().execute();
    }

    class FetchDevicesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            mRecyclerView.setVisibility(View.GONE);
            deviceProgressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mDeviceListAdapter = new DeviceListAdapter(getActivity(),deviceNo,deviceNames,deviceStatus);
            mRecyclerView.setAdapter(mDeviceListAdapter);
            deviceProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String devicesJsonResponse = null;

            try {

                Uri builturi = Uri.parse(getString(R.string.base_url))
                        .buildUpon()
                        .appendPath("android-web-service")
                        .appendPath("getDevices.php").build();
                URL url = new URL(builturi.toString());
                Log.d("url","url="+url.toString());

                devicesJsonResponse = getJsonResponse(url);

            } catch (IOException e) {
                Log.e("Main Activity Fragment", "error", e);
            }

            try {
                getDevicesFromJson(devicesJsonResponse);
            } catch (JSONException e) {
                Log.e("Main Activity Fragment", "Json result error", e);
            }
            return null;
        }

        private String getJsonResponse(URL url) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String response = null;

            try {
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
            return response;
        }

        private void getDevicesFromJson(String devicesJsonResponse) throws JSONException {
            Log.d("response","response="+devicesJsonResponse);

            JSONObject jsonDevicesObject = new JSONObject(devicesJsonResponse);

            final String R_DEVICES = "devices";
            final String R_DEVICE_NO = "device_no";
            final String R_DEVICE_NAME = "device_name";
            final String R_DEVICE_STATUS = "device_status";

            JSONArray jsonDevicesArray = jsonDevicesObject.getJSONArray(R_DEVICES);
            deviceCount = jsonDevicesArray.length();
            deviceNo = new int[deviceCount];
            deviceStatus = new int[deviceCount];
            deviceNames = new String[deviceCount];

            for (int i = 0; i < deviceCount; i++) {
                JSONObject tempObject = jsonDevicesArray.getJSONObject(i);
                deviceNo[i] = tempObject.getInt(R_DEVICE_NO);
                deviceNames[i] = tempObject.getString(R_DEVICE_NAME);
                deviceStatus[i] = tempObject.getInt(R_DEVICE_STATUS);
            }
        }
    }
}
