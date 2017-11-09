package first.alexander.com.car2park;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
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


public class LoginActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnSubmit;

    final private int JSON_TIME_OUT = 6000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean hasLoggedIn = prefs.getBoolean("hasLoggedIn", false);

        if (hasLoggedIn) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            Toast.makeText(getApplicationContext(), "USER HAVE LOGIN", Toast.LENGTH_LONG).show();
            System.out.println("USER HAS LOGIN BEFORE AND ALREADY HAVE KEY" + prefs.getString("cookie_key", "NULL"));
            startActivity(intent);
            finish();
        }

        etEmail = (EditText) findViewById(R.id.etEmail);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailJSONRequest();
            }
        });

    }


    private void sendEmailJSONRequest() {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();
        String server_request_url = "https://dry-shore-37281.herokuapp.com/login";

        StringRequest StringR = new StringRequest
                (Request.Method.POST, server_request_url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        editor.putBoolean("hasLoggedIn", true);
                        editor.putString("cookie_key", response);
                        editor.commit();
                        System.out.println("FINISH COMMIT WITH RESPONSE KEY: " + response);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VOLLEY", "ERROR");

                        // Handle network related Errors
                        if (error.networkResponse == null) {

                            // Handle network Timeout error
                            if (error.getClass().equals(TimeoutError.class)) {
                                //progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(),
                                        "Request Timeout Error!", Toast.LENGTH_LONG)
                                        .show();
                            } else {
                                // Handle no internet network error
                                //progressDialog.dismiss();
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
