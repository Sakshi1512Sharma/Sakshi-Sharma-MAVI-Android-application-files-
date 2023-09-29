package com.example.sakshisharmaiitdelhimaviproject;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sakshisharmaiitdelhimaviproject.Services.BluetoothSendService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;


@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;

    private static final int REQUEST_PAIRING = 123;
    private static final int MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT = 2;
    private static final int REQUEST_PERMISSIONS = 1;

    public static final String[] PERMISSIONS_11 = {
            Manifest.permission.BLUETOOTH,
//            Manifest.permission.ACCESS_FINE_LOCATION,
//            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    public static final String[] PERMISSIONS_12 = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
    };
    private static final String TAG = "Bluetooth permission";
    int PERMISSION_ALL = 1;

    TextView mStatusBlueTv;
    ImageView mBlueIv;
    Button mOnBtn;
    Button mOffBtn;
    Button mConnectBtn;
    Button piRebootBtn;
    Button piOffBtn;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    FloatingActionButton fab;

    BluetoothAdapter mBlueAdapter;
    private GlobalClass mApp;
    private Context context;

    @Override
    protected void onResume(){
        super.onResume();

        Log.d(TAG,"Bluetooth Adapter State: " + mBlueAdapter.getState());
        //if(mBlueAdapter.getState() != BluetoothAdapter.STATE_ON)
        setUpBluetooth();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApp = new GlobalClass(); // Or initialize it using the appropriate constructor
        //mApp = ((GlobalClass) getApplicationContext());
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Objects.requireNonNull(getSupportActionBar()).hide();

        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mBlueIv = findViewById(R.id.bluetoothIv);
        mOnBtn = findViewById(R.id.onBtn);
        mOffBtn = findViewById(R.id.offBtn);
        mConnectBtn = findViewById(R.id.connectBtn);
        piRebootBtn = findViewById(R.id.piReboot);
        piOffBtn = findViewById(R.id.piOff);

        fab = findViewById(R.id.fab);

        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBlueAdapter == null) {
            // Device does not support Bluetooth
            showToast("Why using such an old phone mate,buddy,bud,bub :((((");
            return;
        }

        ViewGroup rootView  =   findViewById(android.R.id.content);
        rootView.setContentDescription("");

        //set image according to bluetooth status(on/off)
        if (mBlueAdapter.isEnabled()) {
            mBlueIv.setImageResource(R.drawable.ic_action_on);
        } else {
            mBlueIv.setImageResource(R.drawable.ic_action_off);
        }

        // Check if Bluetooth permission is granted
        if (hasBluetoothPermission()) {
            fab.setEnabled(true); // Enable the FAB if permission is granted
        } else {
            // Request Bluetooth permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_BLUETOOTH_PERMISSION);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform the action you want when the FAB is clicked
            }
        });


//        mOnBtn.setOnClickListener(new View.OnClickListener(){
//            //on btn click
//            public void onClick(View v) {
//                if (!mBlueAdapter.isEnabled()){
//
//                    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//
//                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
//                                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//                            mBlueIv.setImageResource(R.drawable.ic_action_on);
//                        }
//                        else {
//                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSIONS);
//                            mBlueIv.setImageResource(R.drawable.ic_action_off);
//                        }
//                    }
//
//                    else{
//                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//                        mBlueIv.setImageResource(R.drawable.ic_action_on);
//                    }
//
//                } else {
//                    mBlueIv.setImageResource(R.drawable.ic_action_on);
//                    showToast("Bluetooth is already on");
//                }
//            }
//        });

        // Connect to Raspberry pi device
        mConnectBtn.setOnClickListener(v -> {
            if (hasPermissionAndBluetoothEnabled_11()) {
                Intent intent1 = new Intent(MainActivity.this, PairedDeviceInfo.class);
                startActivityForResult(intent1, REQUEST_PAIRING);
            } else if (hasPermissionAndBluetoothEnabled_12()) {
                Intent intent2 = new Intent(MainActivity.this, PairedDeviceInfo.class);
                startActivityForResult(intent2, REQUEST_PAIRING);
            } else {
                // Bluetooth is off so can't get paired devices
                mBlueIv.setImageResource(R.drawable.ic_action_off);
                showToast("Turn on bluetooth to get paired devices");
                setUpBluetooth();
                //showToast("Turn on Bluetooth and grant necessary permissions to get paired devices");
            }
        });

        //off btn click
//        mOffBtn.setOnClickListener(v -> {
//            if (mBlueAdapter.isEnabled()) {
//                // Check if the necessary permissions are granted
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
//                    // Request the permission at runtime for Android 11 and lower
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSIONS);
//                } else {
//                    // Permission is already granted, proceed with disabling Bluetooth
//                    mBlueAdapter.disable();
//                    showToast("Turning Bluetooth Off");
//                    mBlueIv.setImageResource(R.drawable.ic_action_off);
//                }
//            } else {
//                showToast("Bluetooth is already off");
//            }
//        });

        piRebootBtn.setOnClickListener(v -> {
            mApp.setPiVariable("r");
            if (mApp.updateVariable("power", "r")) {
                if (BluetoothSendService.running) {
                    String str = mApp.variablesToString(getDetectionType("objectDetection"), getDetectionType("faceDetection"));
                    BluetoothSendService.pushToQueue(str);
                }
            }
        });

        piOffBtn.setOnClickListener(v -> {
            mApp.setPiVariable("f");
            if (mApp.updateVariable("power", "f")) {
                if (BluetoothSendService.running) {
                    String str = mApp.variablesToString(getDetectionType("objectDetection"), getDetectionType("faceDetection"));
                    BluetoothSendService.pushToQueue(str);
                }
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final VibrationEffect vibrationEffect3;

                // this type of vibration requires API 29
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                    // create vibrator effect with the constant EFFECT_DOUBLE_CLICK
                    vibrationEffect3 = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK);

                    // it is safe to cancel other vibrations currently taking place
                    vibrator.cancel();

                    vibrator.vibrate(vibrationEffect3);
                }
            }

        });

        //setUpBluetooth();

    }//onCreate ends

    // Check if Bluetooth permission is granted
    private boolean hasBluetoothPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
    }

    // Permission check still visible even after abruptly closing the app - causes relaunch issues
    private static final String BLUETOOTH_PERMISSION_REQUESTED_KEY = "bluetooth_permission_requested";
    private static final int MAX_PERMISSION_ATTEMPTS = 6;
    private int permissionAttempts = 0;

    private void setUpBluetooth(){
        if (!mBlueAdapter.isEnabled()){
            // BLUETOOTH is OFF
            //added
            mBlueIv.setImageResource(R.drawable.ic_action_off);
            //showToast("Bluetooth is OFF");
            //***

            // Check if you have already requested Bluetooth permission in this session
            SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            boolean permissionRequested = preferences.getBoolean(BLUETOOTH_PERMISSION_REQUESTED_KEY, false);


            if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//
//                    if (!permissionRequested) {
//
//                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//                        //mBlueIv.setImageResource(R.drawable.ic_action_on);
//                        // Update the flag to indicate that permission has been requested
//                        preferences.edit().putBoolean(BLUETOOTH_PERMISSION_REQUESTED_KEY, true).apply();
//                    }
                    //added
                    if(!permissionRequested || permissionAttempts < MAX_PERMISSION_ATTEMPTS){
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        preferences.edit().putBoolean(BLUETOOTH_PERMISSION_REQUESTED_KEY, true).apply();
                        permissionAttempts++;
                    }
                    else if (permissionAttempts == MAX_PERMISSION_ATTEMPTS) {
                        showToast("I'm tired of asking for permission! Please enable Bluetooth manually now from your settings to use the app.");
                    }
                    //****
                }
                else {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSIONS);
                    //mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
            }

            else{
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                //mBlueIv.setImageResource(R.drawable.ic_action_on);
            }

        } else {
            //mBlueIv.setImageResource(R.drawable.ic_action_on);
            //showToast("Bluetooth is already on");
            // BLUETOOTH is ON
            mBlueIv.setImageResource(R.drawable.ic_action_on);
            //showToast("Bluetooth is ON");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth is now enabled
                //added
                mBlueIv.setImageResource(R.drawable.ic_action_on);
                showToast("Bluetooth enabled");
                //****
                // You can perform further Bluetooth operations here
//                if (mBlueAdapter.getState() != mBlueAdapter.STATE_ON) {
//                    showToast("Bluetooth enabled");
//                    mBlueIv.setImageResource(R.drawable.ic_action_on);
//                }
//                else{
//                    setUpBluetooth();
//                }
            }else {
                //mBlueIv.setImageResource(R.drawable.ic_action_off);
                // User chose not to enable Bluetooth or an error occurred
                // Handle it as per your app's requirement
                //setUpBluetooth();
//                showToast("Bluetooth NOT enabled. Please Allow.");
                //addedd
                mBlueIv.setImageResource(R.drawable.ic_action_off);
                showToast("Bluetooth NOT enabled. Please Allow.");
                //***
            }
        }
    }

    public String getDetectionType(String detectionType){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isMovidius = prefs.getBoolean(detectionType, true);
        if(isMovidius){
            return "movidius";
        }
        return "cpu";
    }

    private boolean hasPermissionAndBluetoothEnabled_11() {
        return mBlueAdapter.getState() == BluetoothAdapter.STATE_ON && hasPermissions(this,PERMISSIONS_11);
    }

    private boolean hasPermissionAndBluetoothEnabled_12() {
        //Static member 'android.bluetooth.BluetoothAdapter.STATE_ON' accessed via instance reference
        //Access via BluetoothAdapter reference
        return mBlueAdapter.getState() == BluetoothAdapter.STATE_ON && hasPermissions(this,PERMISSIONS_12);
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable the FAB
                fab.setEnabled(true);
            }
        }
        if(requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT){
            if(hasPermissions(this,PERMISSIONS_12)){
                setUpBluetooth();
                Toast.makeText(this, "Permission Granted ! Good to go", Toast.LENGTH_SHORT).show();
            }
            else {
                mBlueIv.setImageResource(R.drawable.ic_action_off);
                Log.d(TAG, "Permission not granted ask again ");
                // Permission is denied (this is the first time, when "Allow" is not checked) so ask again explaining the usage of permission
                // Show the dialog or snackbar saying its necessary and try again
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_CONNECT)||
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.BLUETOOTH_ADMIN)) {
                    showDialogOK("MAVI app needs Bluetooth permission. Please Grant it.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            setUpBluetooth();
                                            //ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_12, PERMISSION_ALL);
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // Proceed with logic by disabling the related features or quit the app
                                            setUpBluetooth();
                                            break;
                                    }
                                }
                            });
                }
                else{
                    setUpBluetooth();
                }
            }

//            if has all the permission but not not bluetooth enable then Switch it on
//            if(hasPermissions(this,PERMISSIONS) && mAdapter.getState()!=mAdapter.STATE_ON)
//                switchOnThBlueTooth();

        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }

//    @SuppressLint("MissingPermission")
//    @Override
//    //Not annotated parameter overrides @NonNull parameter
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (MY_PERMISSIONS_REQUEST_BLUETOOTH_CONNECT == requestCode) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with the Bluetooth operation
//                startActivity(new Intent(MainActivity.this, PairedDeviceInfo.class));
//            } else {
//                showToast("Scanning of device unsuccessful");
//            }
//        }
//        else if (requestCode == REQUEST_PERMISSIONS) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with the Bluetooth operation
//                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            } else {
//                showToast("Please allow Bluetooth permission");
//            }
//        }
//    }


    //toast message function
    public void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}