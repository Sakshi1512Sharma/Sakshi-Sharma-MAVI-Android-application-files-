package com.example.sakshisharmaiitdelhimaviproject;

/*Created by SAKSHI SHARMA in 2023*/

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sakshisharmaiitdelhimaviproject.Adapter.BTDeviceAdapter;
import com.example.sakshisharmaiitdelhimaviproject.Model.DeviceList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PairedDeviceInfo extends AppCompatActivity {

    private static final String TAG = PairedDeviceInfo.class.getName();
    private static final int REQUEST_ENABLE_BT = 1;

    private List<DeviceList> mPairedDeviceList;

    private BTDeviceAdapter mBTAdapter;
    private BTDeviceAdapter mPairedAdapter;
    private RecyclerView pairedDevicesRv;
    private RecyclerView.LayoutManager layoutManager;
    private BluetoothAdapter mAdapter;
    private Button ScanBTN;

    private TextToSpeech tts;
    private int MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_device_info);

        getSupportActionBar().hide();

        // Initialize the TTS engine
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "TTS language is not supported");
                    } else {
                        String activityName = getString(R.string.content2);
                        //String activityName = "Your activity name";
                        //tts.speak("You are in " + activityName, TextToSpeech.QUEUE_FLUSH, null, null);
                        tts.speak(activityName, TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                } else {
                    Log.e(TAG, "TTS initialization failed");
                }
            }
        });

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mAdapter != null) {

            ScanBTN = (Button) findViewById(R.id.BTScan);
            ScanBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    connect(view);
                }
            });


            pairedDevicesRv = (RecyclerView) findViewById(R.id.RVBTPairedDevices);
            pairedDevicesRv.setHasFixedSize(false);
            pairedDevicesRv.setLayoutManager(new LinearLayoutManager(this));


            mPairedDeviceList = new ArrayList<>();
            mPairedAdapter = new BTDeviceAdapter(this, mPairedDeviceList);
            pairedDevicesRv.setAdapter(mPairedAdapter);

            if (mAdapter != null && mAdapter.isEnabled())
                // Bluetooth is enabled, proceed with your code
                populatePairedView(mAdapter);

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(bReciever, filter);
        } else {
            // Bluetooth is not enabled, request enablement
            ensureBluetoothEnabled();
        }

    }

    private void ensureBluetoothEnabled() {
        if (mAdapter != null && !mAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // Bluetooth was successfully enabled by the user
                // Proceed with your Bluetooth-related operations
                populatePairedView(mAdapter);
            } else {
                // Bluetooth enablement was canceled by the user or failed
                // You can display a message or take appropriate action
                Toast.makeText(this, "Bluetooth enablement canceled or failed", Toast.LENGTH_SHORT).show();
            }
        }
    }



    //@SuppressLint("NotifyDataSetChanged")
    private void populatePairedView(BluetoothAdapter mAdapter) {
        Log.d("DEVICELIST", "Super called for DeviceListFragment onCreate\n");

        if (Build.VERSION.SDK_INT < 31 || ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED) {
            Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    DeviceList newDevice = new DeviceList(device.getName(), device.getAddress(), true);
                    Log.i(TAG, "Paired: " + device.getName() + device.getAddress());

                    //mPairedDeviceList.add(newDevice);
                    if(newDevice.getName().startsWith("rasp")){
                        mPairedDeviceList.add(newDevice);
                    }
                }
                mPairedAdapter.notifyDataSetChanged();
            }
        }
        else {
            ActivityCompat.requestPermissions(this, MainActivity.PERMISSIONS_12, 15);
        }
    }

    public void OnThBlueTooth() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void connect(View view) {
        if (mAdapter.isEnabled()) {
            mBTAdapter.notifyDataSetChanged();

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(bReciever, filter);
        } else {
            // Enable the bluetooth
            OnThBlueTooth();
            connect(view);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onDestroy() {
        //super.onDestroy();
        this.unregisterReceiver(bReciever);
        // Before canceling discovery
        if (ActivityCompat.checkSelfPermission(PairedDeviceInfo.this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(PairedDeviceInfo.this, MainActivity.PERMISSIONS_12, MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN);
        } else {
            // Permission is already granted, proceed with the Bluetooth operation
            mAdapter.cancelDiscovery();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with the Bluetooth operation
                try {
                    mAdapter.cancelDiscovery();
                } catch (SecurityException e) {
                    showToast("Try again");
                }
            } else {
                showToast("Scanning of device unsuccessful");
            }
        }
        else if (requestCode == 15) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please provide permission to user", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            populatePairedView(mAdapter);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private final BroadcastReceiver bReciever = new BroadcastReceiver() {
        @SuppressLint("NotifyDataSetChanged")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Create a new device item
                try {
                    // Execute the code that requires the permission
                    DeviceList newDevice = new DeviceList(device.getName(), device.getAddress(), false);
                    // Add it to our adapter
                    Log.i("Device IDs: ","" + newDevice.getName() + " --> "+ newDevice.getDeviceID());
                    if(!mBTAdapter.hasDeviceItem(newDevice)){
                        mBTAdapter.notifyDataSetChanged();
                    }else{
                        Log.d(TAG,"duplicated Data");
                    }
                } catch (SecurityException e) {
                    // Handle the exception
                    Log.d("Some problem", e.toString());
                }
            }
        }
    };

}
