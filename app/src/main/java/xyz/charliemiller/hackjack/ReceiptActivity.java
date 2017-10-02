package xyz.charliemiller.hackjack;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 *  This activity is used to display the receipt validation code.
 *
 * Created by charlie on 10/1/2017.
 */
public class ReceiptActivity extends AppCompatActivity
{
    public static final String VALID_CODE_EXTRA = "valid_code";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        try
        {
            // Set text to
            ((TextView) findViewById(R.id.validation_code)).setText(
                    getIntent().getExtras().getString(VALID_CODE_EXTRA));
        }
        catch (NullPointerException e){}
    }
}
