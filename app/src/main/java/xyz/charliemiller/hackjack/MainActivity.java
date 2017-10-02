package xyz.charliemiller.hackjack;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.CommonStatusCodes;
import java.util.HashMap;
import java.util.Map;

/**
 *  This activity is used to send submitted receipt codes to our remote site.
 *  If we receive a
 *
 */
public class MainActivity extends Activity implements View.OnClickListener,
        Response.Listener<String>, Response.ErrorListener{

    private static final int RC_OCR_CAPTURE = 9003;

    // Remote site. TODO Should probably move to some sort of resource file.
    private static final String url = "https://sheltered-thicket-46039.herokuapp.com/";

    // POST request param.
    private static final String receipt_post_name = "receipt";

    // Tag for logging.
    private static final String TAG = "MainActivity";

    // Request we'll post to the remote site.
    private StringRequest stringRequest;

    private RequestQueue requestQueue;

    // UI receipt input.
    private EditText receiptText;

    private ProgressDialog progressDialog;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve UI elements.
        receiptText = (EditText) findViewById(R.id.input_receipt);

        // Set click listeners for ui elements.
        findViewById(R.id.redeem_receipt).setOnClickListener(this);
        findViewById(R.id.start_ocr).setOnClickListener(this);

        // We'll need to send data to our remote site.
        requestQueue = Volley.newRequestQueue(this);
    }

    /**
     *
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.redeem_receipt:
                // The only valid values are 14 digit long numbers.
                if(receiptText.getText().toString().matches("^[0-9]{14}$"))
                {
                    // We implement both onResponse and onErrorResponse.
                    stringRequest = new StringRequest(Request.Method.POST, url, this, this)
                    {
                        // Set POST data.
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put(receipt_post_name, receiptText.getText().toString());
                            return params;
                        }

                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            HashMap<String, String> headers = new HashMap<String, String>();
                            headers.put("Content-Type","application/x-www-form-urlencoded");
                            return headers;
                        }

                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded";
                        }
                    };

                    // Set timeout to 12 seconds.
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(12000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                    // Make POST request.
                    requestQueue.add(stringRequest);

                    // Start dialog, to inform the user that the process is being carried out.
                    progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(getString(R.string.redeem_receipt_dialog));
                    progressDialog.show();
                }
                else
                {
                    receiptText.setError("Receipt must be a 14 digit code.");
                }
            break;

            // The camera icon was selected.
            case R.id.start_ocr:
                // Start OCR Activity.
                Intent intent = new Intent(this, OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                intent.putExtra(OcrCaptureActivity.UseFlash, false);
                startActivityForResult(intent, RC_OCR_CAPTURE);
            break;
        }

    }

    /**
     * Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned, and any additional
     * data from it.  The <var>resultCode</var> will be
     * {@link #RESULT_CANCELED} if the activity explicitly returned that,
     * didn't return any result, or crashed during its operation.
     * <p/>
     * <p>You will receive this call immediately before onResume() when your
     * activity is re-starting.
     * <p/>
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    receiptText.setText(text.replaceAll("[^\\d]", ""));
                }
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Cancel requestQueue like the docs recommend.
     */
    @Override
    protected void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }

    /**
     *
     * @param response
     */
    @Override
    public void onResponse(String response)
    {
        progressDialog.dismiss();
        Intent receiptIntent = new Intent(this, ReceiptActivity.class);
        receiptIntent.putExtra(ReceiptActivity.VALID_CODE_EXTRA, response);
        startActivity(receiptIntent);
    }

    /**
     *  TODO: Once I add more
     * @param error
     */
    @Override
    public void onErrorResponse(VolleyError error)
    {
        progressDialog.dismiss();
        Toast.makeText(this, "Something went wrong. Check receipt code and try again.",
                Toast.LENGTH_SHORT).show();
    }
}
