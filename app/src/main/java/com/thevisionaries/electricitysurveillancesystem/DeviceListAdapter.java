package com.thevisionaries.electricitysurveillancesystem;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Sunny on 01-11-2015.
 */
public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListAdapterViewHolder> {

    final private Context mContext;
    int[] mDeviceNo, mDeviceStatus;
    String[] mDeviceNames;

    public DeviceListAdapter(Context context) {
        mContext = context;
        mDeviceNo = new int[1];
        mDeviceNames = new String[1];
        mDeviceStatus = new int[1];
    }

    public DeviceListAdapter(Context context, int[] deviceNo, String[] deviceNames, int[] deviceStatus) {
        mContext = context;
        mDeviceNo = deviceNo;
        mDeviceNames = deviceNames;
        mDeviceStatus = deviceStatus;
    }

    @Override
    public DeviceListAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.device_list_item, parent, false);
        DeviceListAdapterViewHolder viewHolder = new DeviceListAdapterViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DeviceListAdapterViewHolder holder, int position) {
        holder.deviceNameTextView.setText(mDeviceNames[position]);
        holder.deviceNo = mDeviceNo[position];
        boolean state = mDeviceStatus[position] == 0 ? false : true;
        holder.deviceStateSwitch.setChecked(state);
    }

    @Override
    public int getItemCount() {
        return mDeviceNo.length;
    }

    public class DeviceListAdapterViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
        public final TextView deviceNameTextView;
        public final SwitchCompat deviceStateSwitch;
        public int deviceNo = -1;
        protected View mRootView;

        public DeviceListAdapterViewHolder(View view) {
            super(view);
            deviceNameTextView = (TextView) view.findViewById(R.id.device_name_textview);
            deviceStateSwitch = (SwitchCompat) view.findViewById(R.id.device_state_switch);
            mRootView = view;
            deviceStateSwitch.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
            int state = isChecked ? 1 : 0;
            int position = getAdapterPosition();
//            if (mDeviceStatus[position] != state) {
                new SetDeviceStateTask().execute(state, position,mDeviceNo[position]);
//            }
        }
    }

    class SetDeviceStateTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            try {

                Uri builturi = Uri.parse(mContext.getString(R.string.base_url))
                        .buildUpon()
                        .appendPath("android-web-service")
                        .appendPath("setDeviceState.php")
                        .appendQueryParameter("state", String.valueOf(params[0]))
                        .appendQueryParameter("device_no", String.valueOf(params[2]))
                        .build();
                URL url = new URL(builturi.toString());
                Log.d("url", "url=" + url.toString());

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();

                mDeviceStatus[params[1]]=params[0];

            } catch (IOException e) {
                Log.e("Main Activity Fragment", "error", e);
            }
            return null;
        }
    }
}
