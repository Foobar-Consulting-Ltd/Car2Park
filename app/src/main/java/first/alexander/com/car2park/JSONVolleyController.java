package first.alexander.com.car2park_mvp;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * JSONVolleyController.java - a singleton class for the JSON Volley Object Request.
 * Ensuring only one JSON Object Request instance will be created and used.
 *
 * @author Alexander Julianto (no131614)
 */
public class JSONVolleyController extends Application {

    public static final String TAG = JSONVolleyController.class.getSimpleName();

    private RequestQueue RequestQ;

    // Private static variable of JSONVolleyController (singleton criteria)
    private static JSONVolleyController JSONVolleyInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        JSONVolleyInstance = this;
    }

    /**
     * Static method to get JSON Volley Instance (singleton criteria)
     * @return JSONVolleyController - the JSON volley instance
     */
    public static synchronized JSONVolleyController getInstance() {
        return JSONVolleyInstance;
    }


    /**
     * Method to get the JSON Request Queue
     * @return RequestQueue - the JSON volley request queue
     */
    public RequestQueue getRequestQueue() {
        if (RequestQ == null) {
            RequestQ = Volley.newRequestQueue(getApplicationContext());
        }

        return RequestQ;
    }

    /**
     * Add JSON Requests to Request Queue with tag indicated
     * @param req - the JSON Request
     * @param tag - String tag of the request
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    /**
     * Add JSON Requests to Request Queue
     * @param req - the JSON Request
     */
    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    /**
     * Cancel pending JSON Requests on Request Queue
     * @param tag - tag of the request
     */
    public void cancelPendingRequests(Object tag) {
        if (RequestQ != null) {
            RequestQ.cancelAll(tag);
        }
    }
}

