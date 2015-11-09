package com.thevisionaries.electricitysurveillancesystem;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class CheckConsumptionActivityFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {

    private final int startDialogId = 0;
    private final int endDialogId = 1;

    EditText startDateEditText;
    EditText startHourEditText;
    EditText endDateEditText;
    EditText endHourEditText;
    Button checkButton;
    TextView unitsConsumedTextView;
    TextView expectedBillTextView;

    final String R_MIN_UNITS = "minUnitsConsumed";
    final String R_MAX_UNITS = "maxUnitsConsumed";
    final String R_MIN_BILL = "minExpectedBill";
    final String R_MAX_BILL = "maxExpectedBill";

    Calendar mcurrentDate = Calendar.getInstance();
    private int year = mcurrentDate.get(Calendar.YEAR);
    private int month = mcurrentDate.get(Calendar.MONTH);
    private int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);
    private int hour = mcurrentDate.get(Calendar.HOUR_OF_DAY);
    private int minute = 0;

    ProgressBar progressBar;
    GridLayout consumptionGridLayout;

    public CheckConsumptionActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_check_consumption, container, false);
        startDateEditText = (EditText) rootView.findViewById(R.id.consumption_start_date_editText);
        startHourEditText = (EditText) rootView.findViewById(R.id.consumption_start_hour_editText);
        endDateEditText = (EditText) rootView.findViewById(R.id.consumption_end_date_editText);
        endHourEditText = (EditText) rootView.findViewById(R.id.consumption_end_hour_editText);
        checkButton = (Button) rootView.findViewById(R.id.consumption_check_button);
        unitsConsumedTextView = (TextView) rootView.findViewById(R.id.units_consumed_textview);
        expectedBillTextView = (TextView) rootView.findViewById(R.id.expected_bill_textview);
        progressBar = (ProgressBar) rootView.findViewById(R.id.consumption_progressBar);
        consumptionGridLayout = (GridLayout) rootView.findViewById(R.id.consumption_gridLayout);

        startDateEditText.setInputType(InputType.TYPE_NULL);
        startHourEditText.setInputType(InputType.TYPE_NULL);
        endDateEditText.setInputType(InputType.TYPE_NULL);
        endHourEditText.setInputType(InputType.TYPE_NULL);

        startDateEditText.setOnClickListener(this);
        startDateEditText.setOnFocusChangeListener(this);
        startHourEditText.setOnClickListener(this);
        startHourEditText.setOnFocusChangeListener(this);
        endDateEditText.setOnClickListener(this);
        endDateEditText.setOnFocusChangeListener(this);
        endHourEditText.setOnClickListener(this);
        endHourEditText.setOnFocusChangeListener(this);
        checkButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void checkConsumption() {
        new CheckConsumptionAsyncTask().execute(
                startDateEditText.getText().toString(),
                startHourEditText.getText().toString(),
                endDateEditText.getText().toString(),
                endHourEditText.getText().toString()
        );
    }

    class CheckConsumptionAsyncTask extends AsyncTask<String, Void, HashMap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            consumptionGridLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(HashMap hashMap) {
            super.onPostExecute(hashMap);
            unitsConsumedTextView.setText("" + hashMap.get(R_MIN_UNITS) + " to " + hashMap.get(R_MAX_UNITS));
            expectedBillTextView.setText("₹" + hashMap.get(R_MIN_BILL) + " to ₹" + hashMap.get(R_MAX_BILL));
            progressBar.setVisibility(View.GONE);
            consumptionGridLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected HashMap doInBackground(String... params) {
            String consumptionJsonResponse = null;

            try {

                Uri builturi = Uri.parse(getString(R.string.base_url))
                        .buildUpon()
                        .appendPath("android-web-service")
                        .appendPath("handler.php")
                        .appendQueryParameter("query", "checkConsumption")
                        .appendQueryParameter("start_date", params[0])
                        .appendQueryParameter("start_hour", params[1])
                        .appendQueryParameter("end_date", params[2])
                        .appendQueryParameter("end_hour", params[3])
                        .build();
                URL url = new URL(builturi.toString());
                Log.d("url", "url=" + url.toString());

                consumptionJsonResponse = getJsonResponse(url);

            } catch (IOException e) {
                Log.e("Main Activity Fragment", "error", e);
            }

            try {
                return getConsumptionFromJson(consumptionJsonResponse);
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

        private HashMap getConsumptionFromJson(String consumptionJsonResponse) throws JSONException {
            Log.d("response", "response=" + consumptionJsonResponse);

            JSONObject jsonConsumptionObject = new JSONObject(consumptionJsonResponse);


            HashMap<String, String> map = new HashMap<String, String>();
            map.put(R_MIN_UNITS, jsonConsumptionObject.getString(R_MIN_UNITS));
            map.put(R_MAX_UNITS, jsonConsumptionObject.getString(R_MAX_UNITS));
            map.put(R_MIN_BILL, jsonConsumptionObject.getString(R_MIN_BILL));
            map.put(R_MAX_BILL, jsonConsumptionObject.getString(R_MAX_BILL));
            return map;
        }
    }

    public void showDatePickerDialog(int dialogId) {
        if (dialogId == startDialogId) {
            new DatePickerDialog(getActivity(), startDateListener, year, month, day).show();
        } else {
            new DatePickerDialog(getActivity(), endDateListener, year, month, day).show();
        }
    }

    private DatePickerDialog.OnDateSetListener startDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // arg1 = year
            // arg2 = month
            // arg3 = day
            setDate(arg1, arg2 + 1, arg3, startDialogId);
        }
    };

    private DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // arg1 = year
            // arg2 = month
            // arg3 = day
            setDate(arg1, arg2 + 1, arg3, endDialogId);
        }
    };

    private void setDate(int year, int month, int day, int dialogId) {
        if (dialogId == startDialogId) {
            startDateEditText.setText(new StringBuilder()
                    .append(year).append("-").append(month).append("-").append(day));
        } else {
            endDateEditText.setText(new StringBuilder()
                    .append(year).append("-").append(month).append("-").append(day));
        }

    }

    public void showTimePickerDialog(int dialogId) {
        if (dialogId == startDialogId) {
            new TimePickerDialog(getActivity(), startTimeListener, hour, minute, true).show();
        } else {
            new TimePickerDialog(getActivity(), endTimeListener, hour, minute, true).show();
        }
    }

    private TimePickerDialog.OnTimeSetListener startTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            setTime(i, i1, startDialogId);
        }
    };
    private TimePickerDialog.OnTimeSetListener endTimeListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            setTime(i, i1, endDialogId);
        }
    };

    private void setTime(int hour, int minute, int dialogId) {
        if (dialogId == startDialogId) {
            startHourEditText.setText(new StringBuilder().append(hour));
        } else {
            endHourEditText.setText(new StringBuilder().append(hour));
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.consumption_start_date_editText:
                showDatePickerDialog(startDialogId);
                break;
            case R.id.consumption_start_hour_editText:
                showTimePickerDialog(startDialogId);
                break;
            case R.id.consumption_end_date_editText:
                showDatePickerDialog(endDialogId);
                break;
            case R.id.consumption_end_hour_editText:
                showTimePickerDialog(endDialogId);
                break;
            case R.id.consumption_check_button:
                checkConsumption();
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            switch (view.getId()) {
                case R.id.consumption_start_date_editText:
                    showDatePickerDialog(startDialogId);
                    break;
                case R.id.consumption_start_hour_editText:
                    showTimePickerDialog(startDialogId);
                    break;
                case R.id.consumption_end_date_editText:
                    showDatePickerDialog(endDialogId);
                    break;
                case R.id.consumption_end_hour_editText:
                    showTimePickerDialog(endDialogId);
                    break;
            }
        }
    }

}
