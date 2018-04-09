package com.example.mitac.bcrreadersample;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mitac.lib.bcr.McBcrConnection;
import com.mitac.lib.bcr.MiBcrListener;
import com.mitac.lib.bcr.utils.BARCODE;
import com.mitac.lib.bcr.utils.BcrStatus;

public class ReaderActivity extends AppCompatActivity implements View.OnClickListener, MiBcrListener {
    private static final String TAG = ReaderActivity.class.getName();
    private McBcrConnection bcrConnection;
    private TextView tvBarcodeType, tvBarcodeLength, tvBarcodeValue;
    private ImageView ivTrigger;
    private Button btnScan, btnSettings;
    private boolean isContinuousScan = false;

    //abc

    /**
     * onCreate event handler
     * @param savedInstanceState
     */
   // @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 0317. Fix Jira HRA-320. Keep screen on when BCR Reader is in foreground.
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        setContentView(R.layout.activity_reader);
        // Initialize BCR Service connection
        bcrConnection = new McBcrConnection(this);
        bcrConnection.setListener(this);

        // Intialize UI elements...
        tvBarcodeType = (TextView)findViewById(R.id.textViewType);
        tvBarcodeType.setMovementMethod(new ScrollingMovementMethod());

        tvBarcodeLength = (TextView)findViewById(R.id.textViewLength);

        tvBarcodeValue = (TextView)findViewById(R.id.textViewValue);

        ivTrigger = (ImageView)findViewById(R.id.imageViewTriggerLight);

        btnScan = (Button) findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this);
        btnSettings = (Button) findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(this);

        //Wayne, 0202
        btnScan.setEnabled(true);
        if (bcrConnection != null) {

            ivTrigger.setVisibility(View.VISIBLE);
            //Start scan...
            bcrConnection.scan(true);
        }
    }

    /**
     * onClick event handler
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnScan:
                if (bcrConnection != null){
                    tvBarcodeValue.setText("");
                    tvBarcodeLength.setText("");
                    tvBarcodeType.setText(R.string.status_scanning);
                    ivTrigger.setVisibility(View.VISIBLE);
                    //Start scan...
                    bcrConnection.scan(true);
                }
                break;
            case R.id.btnSettings:
                // Felix 0317, Stop scanning before exiting reader app. => keep scanning...
                //bcrConnection.scan(false);
                // Felix --
                bcrConnection.startSettingsActivity();
                break;
        }
    }

    /**
     * onResume event handler
     */
   // @Override
    protected void onResume() {
        super.onResume();
        this.bcrConnection.bind();
    }

    /**
     * onStop event handler
     */
   // @Override
    protected void onStop() {
        this.bcrConnection.unbind();
        Log.i(TAG, "BCR Service has been unbound.");

        super.onStop();
    }

    /**
     * BCR scanned event handler
     * @param barcodeType
     * @param decodedData
     * @param length
     */
  //  @Override
    public void onScanned(final BARCODE.TYPE barcodeType, final String decodedData, final int length) {
        ivTrigger.setVisibility(View.INVISIBLE);
        tvBarcodeValue.setText(decodedData);

        final String typeText = String.format(this.getString(R.string.format_barcode_type_result), barcodeType.name(this));
        tvBarcodeType.setText(typeText);

        final String lengthText = String.format(this.getString(R.string.format_barcode_length_result), length);
        tvBarcodeLength.setText(lengthText);



        Intent Home = new Intent(Intent.ACTION_MAIN);
        Home.setClassName("com.example.wayne_mac.wayne_food_management","com.example.wayne_mac.wayne_food_management.MainActivity");

        Home.putExtra("MESSAGE",decodedData);
        setResult(RESULT_OK,Home);
        finish();//finishing activity



    }

    /**
     * BCR status changed event handler
     * @param status
     */
   // @Override
    public void onStatusChanged(int status) {
        Log.i(TAG, "BCR status : " + status);
        switch (status){
            case BcrStatus.Status_Ready:
                //Denny, 0314
                btnScan.setEnabled(true);
                //-----
                tvBarcodeType.setText(R.string.status_ready_to_scan);
                tvBarcodeLength.setText(R.string.comment_press_scan_button);
                break;
            //Denny, 0314
            case BcrStatus.Status_Closed:
                // Felix, 0316
                tvBarcodeType.setText("BCR is NOT ready");
                tvBarcodeLength.setText("");
                // Felix --
                btnScan.setEnabled(false);
                break;
            case BcrStatus.Status_ScanStopped:
                // Felix, 0321.
                btnScan.setEnabled(true);
                // Felix --
                ivTrigger.setVisibility(View.INVISIBLE);
                tvBarcodeType.setText(R.string.status_ready_to_scan);
                tvBarcodeLength.setText(R.string.comment_press_scan_button);
                break;
            //-----
            default:
                // Felix, 0316
                btnScan.setEnabled(false);
                tvBarcodeType.setText("BCR is NOT ready");
                tvBarcodeLength.setText("");
                // Felix --
                break;
        }
    }
}
