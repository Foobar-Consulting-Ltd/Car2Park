package first.alexander.com.car2park;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import info.hoang8f.widget.FButton;


public class LoginActivity extends AppCompatActivity {

    EditText etEmail;

    final private int JSON_TIME_OUT = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasLoggedIn = prefs.getBoolean("hasLoggedIn", false);

        if (hasLoggedIn) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            Toast.makeText(getApplicationContext(), "USER HAVE LOGIN WITH KEY: " + prefs.getString("cookie_key", "NULL"), Toast.LENGTH_LONG).show();
            startActivity(intent);
            finish();
        }

        etEmail = (EditText) findViewById(R.id.etEmail);

        FButton btnFindPath = (FButton) findViewById(R.id.btnSubmitEmail);
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                if (etEmail.getText().length() < 1) {
                    etEmail.setError("Please Enter a valid Email Address");
                } else {
                    etEmail.setError(null);
                    sendEmailJSONRequest();
                }
            }
        });


        final AlertDialog alertDialog = new AlertDialog.Builder(
                LoginActivity.this).create();
        alertDialog.setTitle("Info Disclaimer");
        alertDialog.setMessage("Thank you for choosing Car2Park. To get started, " +
                "please enter a valid email address so we can provide access to our services.");
        alertDialog.setIcon(R.drawable.ic_logo);

        alertDialog.setButton(Dialog.BUTTON_POSITIVE,"OK",new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }


    private void sendEmailJSONRequest() {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Please wait",
                "Authenticating Email...", true);

        String server_request_url = "https://dry-shore-37281.herokuapp.com/login";

        StringRequest StringR = new StringRequest
                (Request.Method.POST, server_request_url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putBoolean("hasLoggedIn", true);
                        editor.putString("cookie_key", response);
                        editor.commit();
                        System.out.println("FINISH COMMIT WITH RESPONSE KEY: " + response);
                        progressDialog.dismiss();
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(intent);
                        finish();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");

                        // Handle network related Errors
                        if (error.networkResponse == null) {

                            // Handle network Timeout error
                            if (error.getClass().equals(TimeoutError.class)) {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Request Timeout Error!", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                // Handle no internet network error
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Network Error. No Internet Connection", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                }) {


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;

            }

            @Override
            public byte[] getBody() {

                JSONObject json_email = new JSONObject();

                try {
                    json_email.put("user_email", etEmail.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String email = json_email.toString();
                System.out.println("THE SEND DATA EMAIL: " + email);
                return email.getBytes();
            }
        };

        StringR.setRetryPolicy(new DefaultRetryPolicy(JSON_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add to JSON request Queue
        JSONVolleyController.getInstance().addToRequestQueue(StringR);

    }

}
