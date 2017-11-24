package first.alexander.com.car2park;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import dmax.dialog.SpotsDialog;
import info.hoang8f.widget.FButton;


public class LoginActivity extends AppCompatActivity {

    private EditText etEmail;

    private String Key;

    final private int JSON_TIME_OUT = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasLoggedIn = prefs.getBoolean("hasLoggedIn", false);

        if (hasLoggedIn) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            TastyToast.makeText(getApplicationContext(), "Login Successfully",
                    TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
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
                    etEmail.setError("Please enter a valid email address");
                } else {
                    etEmail.setError(null);
                    sendEmailJSONRequest();
                }
            }
        });


        final AlertDialog alertDialog = new AlertDialog.Builder(
                LoginActivity.this).create();
        alertDialog.setTitle("Info Disclaimer");
        alertDialog.setMessage("Thank you for choosing Car2Park.\n To get started, " +
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

        final SpotsDialog progressValidate = new
                SpotsDialog(LoginActivity.this, R.style.ProgressValidateEmail);
        progressValidate.show();

        String server_request_url = "https://dry-shore-37281.herokuapp.com/login";

        StringRequest StringR = new StringRequest
                (Request.Method.POST, server_request_url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        System.out.println("RESPONSE KEY: " + response);
                        Key = response;
                        progressValidate.dismiss();

                        // Start Verification Dialog Menu
                        showVerificationDialog();

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");

                        progressValidate.dismiss();

                        // Handle network related Errors
                        if (error.networkResponse == null) {

                            // Handle network Timeout error
                            if (error.getClass().equals(TimeoutError.class)) {
                                Toast.makeText(getApplicationContext(),
                                        "Request Timeout Error!", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                // Handle no internet network error
                                TastyToast.makeText(getApplicationContext(), "No Internet Connection",
                                        TastyToast.LENGTH_LONG, TastyToast.ERROR);
                            }
                        }
                        else{

                            // Need to catch 400 error code for invalid email address
                            int error_code = error.networkResponse.statusCode;
                            if(error_code == 400){
                                TastyToast.makeText(getApplicationContext(), "Its not a valid email address",
                                        TastyToast.LENGTH_LONG, TastyToast.ERROR);
                            }
                            else{
                                TastyToast.makeText(getApplicationContext(),
                                        "HTTP Error. Error Code: "  + error_code,
                                        TastyToast.LENGTH_LONG, TastyToast.ERROR);
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

    private void sendVerificationJSONRequest() {

        String server_request_url = "https://dry-shore-37281.herokuapp.com/parkingspots?lat=49.2624&lng=-123.2433";

        final SweetAlertDialog CheckingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        CheckingDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        CheckingDialog.setTitleText("Checking verification...");
        CheckingDialog.setCancelable(false);
        CheckingDialog.show();

        JsonObjectRequest JsonObjectR = new JsonObjectRequest
                (Request.Method.GET, server_request_url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        CheckingDialog.setTitleText("Email Verified")
                                .setContentText("Enjoy our app :)")
                                .setConfirmText("Ok")
                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);

                        CheckingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                // Set confirmed user key and go to Main Activity
                                setUserKey();
                            }
                        });

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");

                      CheckingDialog.setTitleText("Error")
                                .setConfirmText("Ok")
                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);

                        CheckingDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        });

                        // Handle network related Errors
                        if (error.networkResponse == null) {

                            // Handle network Timeout error
                            if (error.getClass().equals(TimeoutError.class)) {
                                CheckingDialog.setContentText("Request Timeout Error!");
                            } else {
                                // Handle no internet network error
                                CheckingDialog.setContentText("Network Error. No Internet Connection");
                            }
                        }
                        else{

                            // Need to catch 401 unauthorized access error code
                            int error_code = error.networkResponse.statusCode;
                            if(error_code == 401){
                                CheckingDialog.setContentText("Email has not been verified. \n Please try again.");

                            }
                            else{
                                CheckingDialog.setContentText("HTTP Error. Error Code: " + error_code);
                            }
                        }
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                System.out.println("Sending KEY: " + Key);

                params.put("Cookie", "auth=" + Key);
                return params;
            }
        };

        JsonObjectR.setRetryPolicy(new DefaultRetryPolicy(JSON_TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add to JSON request Queue
        JSONVolleyController.getInstance().addToRequestQueue(JsonObjectR);
    }

    private void showVerificationDialog(){
        
        SweetAlertDialog VerificationDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        VerificationDialog.setTitleText("Email Verification")
        .setContentText("We have sent an email verification to: " + etEmail.getText()
                + "\n Press Confirm when you have verified your email.")
                .setConfirmText("Confirm Verification")
                .setCancelText("Cancel")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        sendVerificationJSONRequest();

                    }
                })
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })

                .show();

    }


    private void setUserKey(){

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();

        // Set Key as user Key
        editor.putBoolean("hasLoggedIn", true);
        editor.putString("cookie_key", Key);
        editor.commit();

        // Login to Main Activity
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
        finish();

    }

}
