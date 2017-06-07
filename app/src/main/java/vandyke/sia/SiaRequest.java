package vandyke.sia;

import android.util.Base64;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class SiaRequest extends StringRequest {

    private HashMap<String, String> headers;
    private HashMap<String, String> params;

    public SiaRequest(int method, String command, final VolleyCallback callback) {
        super(method, "http://" + MainActivity.prefs.getString("address", "10.0.0.2:9980") + command, new Response.Listener<String>() {
            public void onResponse(String response) {
                try {
                    callback.onSuccess(new JSONObject(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                try {
                    if (error.networkResponse != null) {
                        System.out.println(new String(error.networkResponse.data, "utf-8"));
//                        if (json.getString("message").contains("wallet must be unlocked before it can be used"))
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        headers = new HashMap<>();
        headers.put("User-agent", "Sia-Agent");
        headers.put("Authorization", "Basic " + Base64.encodeToString((":" + MainActivity.prefs.getString("apiPass", "")).getBytes(), 0));
        params = new HashMap<>();
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }

    public SiaRequest addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public SiaRequest addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public void send() {
        MainActivity.requestQueue.add(this);
    }

    public interface VolleyCallback {
        void onSuccess(JSONObject response);
//        void onError(JSONObject error);
    }
}
