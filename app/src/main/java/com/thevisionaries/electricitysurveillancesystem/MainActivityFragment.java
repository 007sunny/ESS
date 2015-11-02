package com.thevisionaries.electricitysurveillancesystem;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnClickListener {

    Activity activity;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_main, container, false);
        activity=getActivity();
        Button onOffButton=(Button)rootView.findViewById(R.id.on_off_button);
        onOffButton.setOnClickListener(this);
        Button setThresholdButton=(Button)rootView.findViewById(R.id.set_threshold_button);
        setThresholdButton.setOnClickListener(this);
        Button checkConsumptionButton=(Button)rootView.findViewById(R.id.check_consumption_button);
        checkConsumptionButton.setOnClickListener(this);

        return rootView;
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
                startActivity(intent, options.toBundle());
                break;
            default:
                Toast.makeText(activity, "Error", Toast.LENGTH_SHORT).show();
        }
    }
}
