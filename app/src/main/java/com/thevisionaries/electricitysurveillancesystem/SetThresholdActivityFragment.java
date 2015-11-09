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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
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
public class SetThresholdActivityFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {

    private final int startDialogId = 0;
    private final int endDialogId = 1;

    EditText thresholdLimitEditText;
    EditText startDateEditText;
    EditText startHourEditText;
    EditText endDateEditText;
    EditText endHourEditText;

    final String R_THRESHOLD_LIMIT = "threshold_limit";
    final String R_START_DATE = "start_date";
    final String R_START_HOUR = "start_hour";
    final String R_END_DATE = "end_date";
    final String R_END_HOUR = "end_hour";

    Calendar mcurrentDate = Calendar.getInstance();
    private int year = mcurrentDate.get(Calendar.YEAR);
    private int month = mcurrentDate.get(Calendar.MONTH);
    private int day = mcurrentDate.get(Calendar.DAY_OF_MONTH);
    private int hour = mcurrentDate.get(Calendar.HOUR_OF_DAY);
    private int minute = 0;

    public SetThresholdActivityFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_set_threshold, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        } else if (id == R.id.action_set_threshold) {
            final String threshold_limit = thresholdLimitEditText.getText().toString();
            final String start_date = startDateEditText.getText().toString();
            final String start_hour = startHourEditText.getText().toString();
            final String end_date = endDateEditText.getText().toString();
            final String end_hour = endHourEditText.getText().toString();

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    HttpURLConnection urlConnection = null;
                    try {

                        Uri builturi = Uri.parse(getActivity().getString(R.string.base_url))
                                .buildUpon()
                                .appendPath("android-web-service")
                                .appendPath("handler.php")
                                .appendQueryParameter("query", "setThreshold")
                                .appendQueryParameter("threshold_limit", threshold_limit)
                                .appendQueryParameter("start_date", start_date)
                                .appendQueryParameter("start_hour", start_hour)
                                .appendQueryParameter("end_date", end_date)
                                .appendQueryParameter("end_hour", end_hour)
                                .build();
                        URL url = new URL(builturi.toString());
                        Log.d("url", "url=" + url.toString());

                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.connect();

                        InputStream input = urlConnection.getInputStream();

                    } catch (IOException e) {
                        Log.e("Main Activity Fragment", "error", e);
                    } finally {
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                    }
                    return null;

                }
            }.execute();

//            getActivity().onBackPressed();
            getActivity().finish();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_threshold, container, false);
        thresholdLimitEditText = (EditText) rootView.findViewById(R.id.threshold_limit_editText);
        startDateEditText = (EditText) rootView.findViewById(R.id.threshold_start_date_editText);
        startHourEditText = (EditText) rootView.findViewById(R.id.threshold_start_hour_editText);
        endDateEditText = (EditText) rootView.findViewById(R.id.threshold_end_date_editText);
        endHourEditText = (EditText) rootView.findViewById(R.id.threshold_end_hour_editText);

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

        getThreshold();

        return rootView;
    }

    void getThreshold() {
        new AsyncTask<Void, Void, HashMap>() {
            @Override
            protected void onPostExecute(HashMap map) {
                thresholdLimitEditText.setText(map.get(R_THRESHOLD_LIMIT).toString());
                startDateEditText.setText(map.get(R_START_DATE).toString());
                startHourEditText.setText(map.get(R_START_HOUR).toString());
                endDateEditText.setText(map.get(R_END_DATE).toString());
                endHourEditText.setText(map.get(R_END_HOUR).toString());
            }

            @Override
            protected HashMap doInBackground(Void... voids) {
                HttpURLConnection urlConnection = null;
                BufferedReader reader = null;
                String response = null;
                try {

                    Uri builturi = Uri.parse(getActivity().getString(R.string.base_url))
                            .buildUpon()
                            .appendPath("android-web-service")
                            .appendPath("handler.php")
                            .appendQueryParameter("query", "getThreshold")
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
                        return getThresholdFromJson(response);
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

    HashMap<String, String> getThresholdFromJson(String thresholdJsonResponse) throws JSONException {
        Log.d("response", "response=" + thresholdJsonResponse);

        JSONObject jsonThresholdObject = new JSONObject(thresholdJsonResponse);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(R_THRESHOLD_LIMIT, jsonThresholdObject.getString("ThresholdLimit"));
        map.put(R_START_DATE, jsonThresholdObject.getString("thresh_start_date"));
        map.put(R_START_HOUR, jsonThresholdObject.getString("thresh_start_hour"));
        map.put(R_END_DATE, jsonThresholdObject.getString("thresh_end_date"));
        map.put(R_END_HOUR, jsonThresholdObject.getString("thresh_end_hour"));

        return map;
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
            case R.id.threshold_start_date_editText:
                showDatePickerDialog(startDialogId);
                break;
            case R.id.threshold_start_hour_editText:
                showTimePickerDialog(startDialogId);
                break;
            case R.id.threshold_end_date_editText:
                showDatePickerDialog(endDialogId);
                break;
            case R.id.threshold_end_hour_editText:
                showTimePickerDialog(endDialogId);
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b) {
            switch (view.getId()) {
                case R.id.threshold_start_date_editText:
                    showDatePickerDialog(startDialogId);
                    break;
                case R.id.threshold_start_hour_editText:
                    showTimePickerDialog(startDialogId);
                    break;
                case R.id.threshold_end_date_editText:
                    showDatePickerDialog(endDialogId);
                    break;
                case R.id.threshold_end_hour_editText:
                    showTimePickerDialog(endDialogId);
                    break;
            }
        }
    }
}
