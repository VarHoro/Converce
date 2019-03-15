package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.myapplication.Constants.API_URL;
import static com.example.myapplication.Constants.COURSE_NAME;
import static com.example.myapplication.Constants.COURSE_PLACE;
import static com.example.myapplication.Constants.MESSAGE_NETWORK;
import static com.example.myapplication.Constants.RUB;
import static com.example.myapplication.Constants.TITLE;
import static com.example.myapplication.Constants.USD;

public class MainActivity extends AppCompatActivity {

    EditText amountTV;
    TextView resultTV;
    TextView courseTV;
    double course;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        amountTV = findViewById(R.id.value_text);
        resultTV = findViewById(R.id.result_text);
        courseTV = findViewById(R.id.curs_now);

        TextView firstStr = findViewById(R.id.text_now);
        String str = getResources().getString(R.string.text_curs);
        firstStr.setText(String.format(str, USD, RUB));

        //checking for internet connection
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new ProgressTask().execute();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(TITLE);
            builder.setMessage(MESSAGE_NETWORK);
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            builder.create().show();
        }

        ImageButton btn = findViewById(R.id.eur_rub_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v) {
                if (course >= 0){
                    String var = amountTV.getText().toString();
                    if (!"".equals(var)){
                        resultTV.setText(String.format("%.2f", course*(Double.valueOf(var))));
                    } else {
                        amountTV.setHint(R.string.error_val);
                        amountTV.setHintTextColor(getResources().getColor(R.color.errorHint));
                    }
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class ProgressTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;
            try {
                URL url = new URL(API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection(); //opening URL
                InputStream in = new BufferedInputStream(urlConnection.getInputStream()); //reading stream for the opened URL
                result = inputStreamToString(in); //saving result
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //trying to get JSONObject
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONObject object = jsonObject.getJSONObject(COURSE_PLACE);
                course = object.getDouble(COURSE_NAME);
                courseTV.setText(String.format("%.2f", course));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        private String inputStreamToString(InputStream is) {
            String rLine;
            StringBuilder answer = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader rd = new BufferedReader(isr);

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return answer.toString();
        }
    }
}
