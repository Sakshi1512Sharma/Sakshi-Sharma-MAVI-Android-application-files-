package com.example.sakshisharmaiitdelhimaviproject;

/*Created by SAKSHI SHARMA in 2023*/

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.sakshisharmaiitdelhimaviproject.Model.DeviceList;
import com.example.sakshisharmaiitdelhimaviproject.Services.BluetoothSendService;
import com.example.sakshisharmaiitdelhimaviproject.Services.PiSyncService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DetectionModule extends AppCompatActivity {

    private BluetoothAdapter myAdapter;
    private ConnectThread connect;
    private Handler myHandler;

    public TextView commonTextBoxCtr;
    private SwitchCompat objectSwitch;
    private SwitchCompat signboardSwitch;
    private Button connect_global_status;

    Boolean last_objectmodel_loaded_speak = false;
    Boolean last_facerecogmodel_loaded_speak = false;
    private String[] textViewString;
    private int numOfDetectionTypes = 3;
    private TextToSpeech tts;
    private TextToSpeech tts_hin;
    private int utteranceCount = 0;
    private int utteranceCount_hin = 0;
    private DeviceList LastPairedDevice;

    private Context context;
    private GlobalClass mApp;
    public static final String[] PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    };
    int PERMISSION_ALL = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_DSC = 3;

    private MediaPlayer mediaPlayer;
    private boolean isConnected = false;

    Locale defaultLocale = Locale.getDefault();
    String languageCode = defaultLocale.getLanguage();

    FloatingActionButton fab2;
    private TextToSpeech textToSpeech;
    //private BreakIterator objectDetectionStatus;

    private AccessibilityManager accessibilityManager;

    @Override
    protected void onResume() {
        super.onResume();

        // permission granted & bluetooth on
        Log.d(TAG, "Bluetooth Adapter State: " + myAdapter.getState());
        if (myAdapter.getState() != BluetoothAdapter.STATE_ON) {
            getLastPairedDevice();

            if (hasPermissionAndBluetoothEnabled())
                connect();
            else
                requestPermissionAndBluetooth();
            //connect();
        }

    }

    private boolean hasPermissionAndBluetoothEnabled() {
        return myAdapter.getState() == myAdapter.STATE_ON && hasPermissions(context, PERMISSIONS);
    }

    private void requestPermissionAndBluetooth() {
        // requesting all the permissions, if it does not have
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        // if has all the permissions but bluetooth is not enabled then Switch it on
        if (hasPermissions(this, PERMISSIONS) && myAdapter.getState() != myAdapter.STATE_ON)
            switchOnThBlueTooth();
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

    public void switchOnThBlueTooth() {
        // Added Permission Check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Overriding method should call `super.onActivityResult`
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    //mAdapter is enabled
                    if (myAdapter.getState() == myAdapter.STATE_ON) {
                        //'setConnectionButtonText(int)' in 'com.example.signboardmodule.TabsModule' cannot be applied to '(java.lang.String)'
                        //setConnectionButtonText("Bluetooth Enabled!");
                        setConnectionButtonText(R.string.bt_enable);
                        getLastPairedDevice();
                        connect();
                    } else {
                        //mAdapter is not enabled
                        //setConnectionButtonText("Bluetooth Not Enabled!");
                        setConnectionButtonText(R.string.bt_notenabled);
                    }

                } else {
                    //mAdapter is not enabled
                    //setConnectionButtonText("Bluetooth Not Enabled!");
                    setConnectionButtonText(R.string.bt_notenabled);
                }
                break;

            case REQUEST_ENABLE_DSC:
                // Added Permission Check
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (myAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        commonTextBoxCtr.setText("Status: Enabled and Discoverable");
                    } else {
                        commonTextBoxCtr.setText("Status: Enabled but not Discoverable");
                    }
                }

                break;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detection_module);

        // Initialize the TextToSpeech engine
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // TTS engine is successfully initialized
                } else {
                    // Handle initialization failure
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        getSupportActionBar().hide();

        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);


        mApp = ((GlobalClass) getApplicationContext());

        Intent intent = getIntent();

        //connect_global_status = globalButtonView.findViewById(R.id.connect_global);
        //connect_global_status.setBackgroundColor(getResources().getColor(R.color.green));
        connect_global_status   =   findViewById(R.id.connect_global);
        commonTextBoxCtr        =   findViewById(R.id.textBox);
        objectSwitch            =   findViewById(R.id.objectDetectSwitch);
        signboardSwitch         =   findViewById(R.id.signboardDetectSwitch);

        fab2                    =   findViewById(R.id.fab2);
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        myAdapter = BluetoothAdapter.getDefaultAdapter();

        setSwitchListeners();

        myHandler = new Handler(Looper.getMainLooper()) {
            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void handleMessage(Message message) {
                if (message.getData().containsKey("connection")) {
                    Boolean connectionStatus = message.getData().getBoolean("connection");
                    handleConnection(connectionStatus);
                }
                if (message.getData().containsKey("message")) {
                    String str = message.getData().getString("message");
                    if (str != null && !str.equals("")) {
                        handle(str);
                    }
                }
            }
        };

        textViewString = new String[numOfDetectionTypes];
        for (int i = 0; i < numOfDetectionTypes; i++) {
            textViewString[i] = "";
        }

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                //tts.speak("My name is Sakshi",TextToSpeech.QUEUE_ADD,null);
                Log.d(TAG, "tts onInit");
                Log.d(TAG, "tts: " + status + ", " + TextToSpeech.SUCCESS);
                if (status != TextToSpeech.ERROR) {
                    Log.d(TAG, "tts no error");
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            if (!nextPossibletts.equals("")) {
                                Handler refresh = new Handler(Looper.getMainLooper());
                                refresh.post(new Runnable() {
                                    public void run() {
                                        handleTTS(nextPossibletts, nextPossibleviewStr, nextInd);
                                    }
                                });
                            } else {
                                fixInd = -1;
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                        }

                        @Override
                        public void onStart(String utteranceId) {
                        }
                    });
                    tts.setLanguage(Locale.UK);
//                    try {
//                        onObjectDetected(new JSONObject("{\n" +
//                                "  \"noOfAnimals\": 3,\n" +
//                                "  \"animalArray\": [\n" +
//                                "    \"dog\",\n" +
//                                "    \"dog\",\n" +
//                                "    \"dog\"\n" +
//                                "  ],\n" +
//                                "  \"directionArray\":[\n" +
//                                "    \"left\",\n" +
//                                "    \"left\",\n" +
//                                "    \"left\"\n" +
//                                "  ]\n" +
//                                "}"));
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        });

        tts_hin = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d(TAG, "tts onInit");
                Log.d(TAG, "tts: " + status + ", " + TextToSpeech.SUCCESS);
                if (status != TextToSpeech.ERROR) {
                    Log.d(TAG, "tts no error");
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            if (!nextPossibletts.equals("")) {
                                Handler refresh = new Handler(Looper.getMainLooper());
                                refresh.post(new Runnable() {
                                    public void run() {
                                        handleTTS(nextPossibletts, nextPossibleviewStr, nextInd);
                                    }
                                });
                            } else {
                                fixInd = -1;
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                        }

                        @Override
                        public void onStart(String utteranceId) {
                        }
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts_hin.setLanguage(new Locale("hi", "IN"));
                    }
                }
            }
        });


        getLastPairedDevice();
        onReceive(this,intent);

        //Test Json
//        try {
//            onObjectDetected(new JSONObject("{\n" +
//                    "  \"noOfAnimals\": 3,\n" +
//                    "  \"animalArray\": [\n" +
//                    "    \"dog\",\n" +
//                    "    \"dog\",\n" +
//                    "    \"dog\"\n" +
//                    "  ],\n" +
//                    "  \"directionArray\":[\n" +
//                    "    \"left\",\n" +
//                    "    \"left\",\n" +
//                    "    \"left\"\n" +
//                    "  ]\n" +
//                    "}"));
//        } catch (JSONException e) {
//            e.printStackTrace();
//            Log.e(TAG, "test json", e);
//        }
        //tts.speak("My name is Sakshi",TextToSpeech.QUEUE_ADD,null);
//        try {
//            onSignboardDetected(new JSONObject(""));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        fab2.setOnClickListener(new View.OnClickListener() {
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



    }//oncreate ends


    // Create the BroadcastReceiver
    public void onReceive(Context context, Intent intent) {

        String macaddress = intent.getStringExtra("macaddress");
        String btname = intent.getStringExtra("btname");

        // Raspberry pi
        Toast.makeText(context, "Selected device: " + btname, Toast.LENGTH_SHORT).show();

        LastPairedDevice = new DeviceList(btname,macaddress,true);

        connect_global_status.setText("Connecting "+ btname);
        // save it in sharedpreferences as lastPairedDevicesMac and lastPairedDeviceName
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("lastPairedDevicesMac", macaddress);
        editor.putString("lastPairedDeviceName", btname);
        editor.apply();

        // connect automatically
        connect();
    }

    public void connect() {

        if(LastPairedDevice!=null && LastPairedDevice.getDeviceID()!=null) {
            Log.d(TAG, "This is the Last Paired Device"+LastPairedDevice);
            Log.d(TAG, "This is the Device ID"+LastPairedDevice.getDeviceID());
            String lastPairedMac = LastPairedDevice.getDeviceID();
            String lastPairedName = LastPairedDevice.getName();
            String lastPairedNamecon = getString(R.string.lastPairedName);

            Log.d(TAG,"Connecting .... with "+ lastPairedName+":"+lastPairedMac);

            BluetoothDevice bt_dev = myAdapter.getRemoteDevice(lastPairedMac);

            //setConnectionButtonText("Connecting .." + lastPairedName);
            String connectingText = "Connecting .. " + lastPairedName;
            int resourceId = getResources().getIdentifier("lastPairedName", "string", getPackageName());
            setConnectionButtonText(resourceId);

            connect = new ConnectThread(bt_dev, myHandler, this,
                    getDetectionType("objectDetection"), getDetectionType("faceDetection"));

            connect.start();
        }
        else {
            //setConnectionButtonText("Click on + to add");
            Log.d(TAG,"Bluetooth Connection Not Established! Click + to add");
        }
    }

    private void getLastPairedDevice() {
        // get sharedpreferences lastPairedDevices variable and set the text in Last Paired TextView
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String lastPairedDevicesMac = prefs.getString("lastPairedDevicesMac", "");
        String lastPairedDeviceName = prefs.getString("lastPairedDeviceName", "");

        if(!lastPairedDevicesMac.equals("")){
            Log.d(TAG,lastPairedDeviceName+":"+lastPairedDevicesMac);

            LastPairedDevice = new DeviceList(lastPairedDeviceName,lastPairedDevicesMac,true);
        }else{
            // not found last paired device
            connect_global_status.setText("Click on + button");
        }
    }

    public String getDetectionType(String detectionType){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean isMovidius = prefs.getBoolean(detectionType, true);
        if(isMovidius){
            return "movidius";
        }
        return "cpu";
    }


    @RequiresApi(api = Build.VERSION_CODES.S)
    public void handleConnection(Boolean connectionStatus){
        String ttsString;
        if(connectionStatus){
            ttsString = "Bluetooth Connected";
            connect_global_status.announceForAccessibility(getString(R.string.connected));
            Log.d(TAG,"Connected To Raspberry Pi - Green");
            //connect_global_status.setBackgroundColor(getResources().getColor(R.color.green));
            //setConnectionButtonText("Connected");
            setConnectionButtonText(R.string.connected);
            //commonTextBoxCtr.setText("Connected! Waiting for results.");
            commonTextBoxCtr.setText(R.string.connected_waiting);
        }else{
            //connect_global_status.setBackgroundColor(getResources().getColor(R.color.red));
            ttsString = "Bluetooth Disconnected";
            //setConnectionButtonText("Disconnected");
            setConnectionButtonText(R.string.disconnected);
            commonTextBoxCtr.setText("Not Connected to the Device");
            // To re-connect you have to go back to this Activity
            // I changed from PairedDeviceInfo to MainActivity because when Bluetooth is turned off from MainActivity then
            // going to PairedDeviceInfo activity was not the right step
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        try {
            // Say Bluetooth Connected/Disconnected
            if (!tts.isSpeaking()) {
                HashMap<String, String> params = new HashMap<>();
                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceCount + "");
                tts.speak(ttsString, TextToSpeech.QUEUE_ADD, params);
                utteranceCount++;
            }
        } catch (Exception e) {
        }
    }

    //'setConnectionButtonText(java.lang.String)' in 'com.example.signboardmodule.TabsModule' cannot be applied to '(int)'
//    public void setConnectionButtonText(String data){
//        connect_global_status.setText(data);
//    }

    public void setConnectionButtonText(int resourceId) {
        connect_global_status.setText(resourceId);
        if (resourceId == R.string.connected) {
            isConnected = true;
            //connect_global_status.announceForAccessibility(getString(R.string.connected));
            connect_global_status.setBackgroundColor(getResources().getColor(R.color.green));
            //connect_global_status.announceForAccessibility("Connected");

        } else {
            isConnected = false;
            connect_global_status.setBackgroundColor(getResources().getColor(R.color.red));
            //connect_global_status.announceForAccessibility("Disonnected");
            connect_global_status.announceForAccessibility(getString(R.string.disconnected));

        }
    }

    public void setSwitchListeners() {
        objectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean on) {
                String temp = "f";
                if (on){
                    temp = "n";

                mApp.setObjectDetectStatus(temp);
                if (mApp.updateVariable("objectDetection", temp)) {
                    if (BluetoothSendService.running) {
                        //objectDetectionStatus.setText(R.string.modelLoading);
                        String str = mApp.variablesToString(getDetectionType("objectDetection"), getDetectionType("faceDetection"));
                        //String str = mApp.variablesToString(getDetectionType(getContext(), "objectDetection"), getDetectionType(getContext(), "faceDetection"));
                        BluetoothSendService.pushToQueue(str);
                     }
                  }
                    // Show a temporary pop-up view
                    showTemporaryMessage(" Please wait I am loading the details in 10 seconds        ");
                }
            }
        });

        signboardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean on) {
                String temp = "f";
                if(on) {
                    temp = "n";

                    // Show a temporary pop-up view
                    showTemporaryMessage(" Signboard reading starts in 5 seconds        ");

                    mApp.setSignboardDetectStatus(temp);
                    if (mApp.updateVariable("signboardDetection", temp)) {
                        if (BluetoothSendService.running) {
                            //signboardReadingStatus.setText(R.string.modelLoading);
                            String str = mApp.variablesToString(getDetectionType("objectDetection"), getDetectionType("faceDetection"));
                            BluetoothSendService.pushToQueue(str);
                        }
                    }
                }
            }
        });
    }

    private void showTemporaryMessage(String message) {
        View rootView = findViewById(android.R.id.content); // Get the root view
        final Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE);


        //snackbar.setAction("Dismiss", new View.OnClickListener()
          snackbar.setAction("", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        // Show the Snackbar
        snackbar.show();

        // Set a timer to hide the Snackbar after a few seconds (adjust the timing as needed)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                snackbar.dismiss();
            }
        }, 6000); // Delay of 6 seconds
    }

    public void handle(String jsonMessage) {
        Log.i("RaspberryPiData", jsonMessage);
        try {
            JSONObject reader = new JSONObject(jsonMessage);
            Log.i(TAG, "Running ...");

            boolean objectDetected, faceDetected, signboardDetected, syncDetected, statusDetected;

            objectDetected = faceDetected = signboardDetected = syncDetected = statusDetected = false;

            JSONObject objectJSON, faceJSON, signboardJSON, syncJSON, statusJSON;

            objectJSON = faceJSON = signboardJSON = syncJSON = statusJSON = null;

            try{
                objectJSON = new JSONObject(reader.getString("animalDetectionString"));
                objectDetected = objectJSON.getInt("noOfAnimals")>0;
            }catch(JSONException e){}


            try{
                faceJSON =  new JSONObject(reader.getString("faceDetectionString"));
                faceDetected = faceJSON.getBoolean("isPerson");
            }catch(JSONException e){}

            try{
                signboardJSON = new JSONObject(reader.getString("signBoardString"));
                signboardDetected = signboardJSON.getBoolean("isSignBoardDetected");
            }catch(JSONException e){}

            try{
                syncJSON = new JSONObject(reader.getString("syncThread"));
                Log.d(TAG,"syncThread");
                syncDetected = true;
            }catch(JSONException e){}

            try{
                statusJSON = new JSONObject(reader.getString("statusThread"));
                statusDetected = true;
            }catch(JSONException e){}

            if(objectDetected)
                onObjectDetected(objectJSON);
            else if(faceDetected)
                onFaceDetected(faceJSON);
            else if(signboardDetected)
                onSignboardDetected(signboardJSON);
            else if(syncDetected){
                PiSyncService.onSyncDetected(syncJSON);
            }else if(statusDetected){
                onStatusDetected(statusJSON);
            }
        } catch (JSONException e) {
            Log.e("MYAPP", "unexpected JSON exception", e);
        }
    }

    private boolean isMessagePlayed = false; // To keep track of whether the message has been played or not

    //    private void onReplayButtonClick(View view) {
//        replayMessage();
//    }
//
//    private void replayMessage() {
//        if (isMessagePlayed) {
//            setTextView();
//            handleTTS(ttsString, objectString, 0);
//        }
//    }


    private String ttsString;
    private String objectString;
    private String previousTTSString = "";
    private String previousObjectString = "";

    public void onObjectDetected(JSONObject reader){
        try {
            ttsString = ""; // Initialize instance variable
            objectString = "" + reader.getInt("noOfAnimals") + " detected\n";
            org.json.JSONArray objects = reader.getJSONArray("animalArray");
            org.json.JSONArray directions = reader.getJSONArray("directionArray");
            Log.i("Object data", String.valueOf(reader));

            // Flag to indicate if a dog was detected
            boolean dogDetected = false;

            String previousString = "" ;

            for (int i = 0; i < objects.length(); i++) {
                String object = objects.getString(i);
                String direction = directions.getString(i);
                String objectVernac = null;

                if (object.equals("dog")) {
                    objectVernac = getString(R.string.dog);
                } else if (object.equals("cow")) {
                    objectVernac = getString(R.string.cow);
                } else if (object.equals("signboard")) {
                    objectVernac = getString(R.string.signboard);
                } else {
                    objectVernac = object;
                }

                objectVernac += " ";

                if (direction.equals("left")) {
                    objectVernac += getString(R.string.left);
                } else if (direction.equals("right")) {
                    objectVernac += getString(R.string.right);
                } else if (direction.equals("center")) {
                    objectVernac += getString(R.string.center);
                } else {
                    objectVernac += direction;
                }

                if (!objectVernac.equalsIgnoreCase(previousString) ) {
                    Log.i("object string ", objectString);
                    if(TextUtils.isEmpty(objectString.trim())) {
                        objectString = objectVernac;
                    }
                    else {
                        objectString += "," + objectVernac;
                    }
                    ttsString += " " + objectVernac;
                }
                previousString = objectVernac;

//                if (i != objects.length() - 1)
//                    objectString += ", ";

                // Check if a dog was detected
                if (objects.getString(i).equalsIgnoreCase("dog")) {
                    dogDetected = true;
                }
            }

            if (0 != fixInd)
                textViewString[0] = objectString;

            // Compare with previous information
            if (!ttsString.equals(previousTTSString) || !objectString.equals(previousObjectString)) {
                setTextView();
                handleTTS(ttsString, objectString, 0);

                if (dogDetected) {
                    MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.beep_sound);
                    mediaPlayer.start();
                }

                // Update previous information
                previousTTSString = ttsString;
                previousObjectString = objectString;
            }

        }catch(Exception e){
            Log.e(TAG, "unexpected JSON exception", e);
        }
    }

    public void onFaceDetected(JSONObject reader){
        String faceString;
        faceString = getString(R.string.faceString);

//        String[] directionStrings = {"left", "right", "center"}; // English directions
//        String[] directionStringsHindi = {"बाएँ", "दाएँ", "मध्य"}; // Hindi directions

        String person_direction = "";
        String crowds = "";
        String ttsString;
        try{
//            String person_direction = reader.getString("person_direction");
//            int directionIndex = Arrays.asList(directionStrings).indexOf(person_direction);

//            // check if direction exists in the array
//            if(directionIndex != -1) {
//                String directionInHindi = directionStringsHindi[directionIndex];
//            }

            person_direction += reader.getString("person_direction");
            crowds += reader.getString("crowd_direction");
            faceString += person_direction;
            if(!crowds.equals("")){
                faceString += " Crowd at ";
                faceString += crowds;
            }

            if(1!=fixInd)
                textViewString[1] = faceString;
            setTextView();
            ttsString = faceString;
            handleTTS(ttsString,faceString,1);
        }catch(Exception e){
            Log.e(TAG, "unexpected JSON exception", e);
        }
    }

    private String replayString = ""; // Variable to store the old string information for replay

    public void onSignboardDetected(JSONObject reader){
        try{
            String ttsString = "";
            String signboardString = "";
            signboardString += reader.getString("data");
            Log.i("Signboard data", ""+reader);

            textViewString[2] = signboardString;
            //textViewString[2] += signboardString;
            setTextView();

            // Get the current phone's language
            String phoneLanguage = Locale.getDefault().getLanguage();

            // Filter the signboardString based on the phone's language
            String filteredString = new String();
            String filteredStringEng = "";
            String filteredStringHin = null;
            if (phoneLanguage.equals("en")) {
                // Only English words are read
                filteredStringEng = filterEnglishWords(textViewString[2]);
                Log.i("English filtered string",filteredStringEng);
            }
            else if (phoneLanguage.equals("hi")) {
                // Only Hindi words are read
                filteredStringHin = filterHindiWords(textViewString[2]);
                Log.i("Hindi filtered string",filteredStringHin);
            }
            else {
                // Unknown language, read the complete string
                filteredString = textViewString[2];
                Log.i("Random words",filteredString);
            }

            // Speak the filteredString if the signboardDetectSwitch is checked
            if (signboardSwitch.isChecked()) {
                String spokenMessage;
                if (phoneLanguage.equals("en")) {
                    //spokenMessage = "Signboard Text is " + filteredString;
                    spokenMessage = " " + filteredStringEng;
                    handleTTS(spokenMessage, filteredStringEng, 2);
                } else if (phoneLanguage.equals("hi")) {
                    //spokenMessage = "साइनबोर्ड का पाठ है " + filteredString;
                    spokenMessage = " " + filteredStringHin;
                    handleTTS(spokenMessage, filteredStringHin, 2);
                }
                else {
                    spokenMessage = " " + filteredString;
                    handleTTS(spokenMessage, filteredString, 2);
                }
            }
        }
        catch(Exception e){
            Log.e(TAG, "unexpected JSON exception", e);
        }
    }


    // Filter the English words from the input string
    private String filterEnglishWords(String input) {
        StringBuilder filteredString = new StringBuilder();

        // Split the input string into individual words
        String[] words = input.split("\\s+");

        for (String word : words) {
            // Check if the word contains only English alphabets
            if (word.matches("[a-zA-Z]+")) {
                // If word is CYCLESTAND or CAFEETERIA, TTS engine is spelling it instead of speaking; IIT should be spoken as IIT and not iit
//                if (word.length() >= 4) {
//                    filteredString.append(word.toLowerCase()).append(" ");
//                } else {
//                    filteredString.append(word).append(" ");
//                }
                // Convert the word to lowercase before appending it
                filteredString.append(word.toLowerCase()).append(" ");
            }
        }
        return filteredString.toString().trim();
    }

    // Filter the Hindi words from the input string
    private String filterHindiWords(String input) {
        // Implement your logic to extract and filter Hindi words from the input string
        StringBuilder filteredString = new StringBuilder();

        // Split the input string into individual words
        String[] words = input.split("\\s+");

        // Iterate over each word
        for (String word : words) {
            // Check if the word contains any Devanagari characters (indicating Hindi)
            if (word.matches(".*\\p{IsDevanagari}.*")) {
                filteredString.append(word).append(" ");
            }
        }

        return filteredString.toString().trim(); // Return the filtered Hindi words as a new string
    }

    public void onStatusDetected(JSONObject statusJSON) {
        try{
            Log.d(TAG,statusJSON.toString());

            // status_sent for object detection
            try{
                Boolean is_objdetmodel_loaded = Boolean.valueOf(statusJSON.getString("is_odmodel_loaded"));
                if (is_objdetmodel_loaded){
                    //objectDetectionStatus.setText(R.string.modelLoaded);

                    String ttsString ="Object Detection Model Loaded";
                    if(last_objectmodel_loaded_speak!=is_objdetmodel_loaded){
                        try {
                            // Say Bluetooth Connected/Disconnected
                            if(!tts.isSpeaking()){

                                HashMap<String, String> params = new HashMap<>();
                                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceCount + "");
                                tts.speak(ttsString, TextToSpeech.QUEUE_ADD, params);
                                utteranceCount++;
                            }
                            last_objectmodel_loaded_speak = true;
                        }catch (Exception e){}
                    }

                }else{
                    //objectDetectionStatus.setText(R.string.modelNotLoaded);
                    last_objectmodel_loaded_speak = false;
                }
                Log.d(TAG,"Model Loaded.");
            }catch (Exception e){
                Log.d(TAG,"Error in setting OD Status");
                //objectDetectionStatus.setText(R.string.error);
            }

            // status_sent for face recognition
            try{
                Boolean is_facerecogmodel_loaded = Boolean.valueOf(statusJSON.getString("is_frmodel_loaded"));
                if (is_facerecogmodel_loaded){
                    //faceRecognitionStatus.setText(R.string.modelLoaded);
                    String ttsString ="Pedestrian Detection Model Loaded";
                    if(last_facerecogmodel_loaded_speak!=is_facerecogmodel_loaded ){
                        try {
                            // Say Bluetooth Connected/Disconnected
                            if(!tts.isSpeaking()){

                                HashMap<String, String> params = new HashMap<>();
                                params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceCount + "");
                                tts.speak(ttsString, TextToSpeech.QUEUE_ADD, params);
                                utteranceCount++;
                            }
                            last_facerecogmodel_loaded_speak = true;
                        }catch (Exception e){}
                    }

                }else{
                    //faceRecognitionStatus.setText(R.string.modelNotLoaded);
                    last_facerecogmodel_loaded_speak = false;
                }
                Log.d(TAG,"Model Loaded.");
            }catch (Exception e){
                Log.d(TAG,"Error in setting OD Status");
                //faceRecognitionStatus.setText(R.string.error);
            }

            try{
                //signboardReadingStatus
                Boolean is_textrecog_model_loaded = Boolean.valueOf(statusJSON.getString("is_trmodel_loaded"));
                //if (is_textrecog_model_loaded)signboardReadingStatus.setText(R.string.modelLoaded);
                //else signboardReadingStatus.setText(R.string.modelNotLoaded);
            }
            catch (Exception e){
                Log.d(TAG,"Error in Signboard Status");
            }

            String last_time;
            last_time = statusJSON.getString("last_time");

            if(last_time.equals(""))
                last_time = "Unknown Error.";

            String statusStr = String.format(last_time);
            Log.d(TAG,statusStr);
            //commonTextView.setText(statusStr);

            // cleaning memory:
            last_time=null;


        }catch(Exception e){
            Log.e(TAG, "unexpected JSON exception", e);
        }

    }


    String lastSpokenString = "";
    String nextPossibletts = "";
    String nextPossibleviewStr = "";
    int fixInd = -1;
    int nextInd = -1;

    public void handleTTS(String ttsString, String viewString, int ind){
        Log.d(TAG,"handleTTS: " + ttsString + " " + viewString + " " + ind);
//        !tts.isSpeaking() && !lastSpokenString.equals(ttsString)
        if(!tts.isSpeaking()){
            if (languageCode.equals("hi")) {
                // Set  language and locale to Hindi
                tts.setLanguage(new Locale("hi", "IN"));

                // Set voice for Hindi
                // Check available voices and set voice using tts.setVoice()
            } else {
                tts.setLanguage(new Locale("en", "GB"));
            }

            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceCount + "");
            lastSpokenString = ttsString;
            fixInd = ind;
            //Log.d(TAG,"handleTTS: !speaking: " + ttsString + " " + viewString + " " + ind);
            textViewString[fixInd] = viewString;
            nextPossibletts = ""; nextPossibleviewStr = ""; nextInd = -1;
            setTextView();
            tts.speak(ttsString, TextToSpeech.QUEUE_ADD, params);
            utteranceCount++;
        }else{
            if(!lastSpokenString.equals(ttsString)){
                nextPossibletts = ttsString;
                nextPossibleviewStr = viewString;
                nextInd = ind;
            }
        }
        Log.d(TAG,"handleTTS: " + nextPossibletts+ " " + nextPossibleviewStr+ " " + nextInd);
    }

    String lastSpokenString_hin = "", nextPossibletts_hin = "", nextPossibleviewStr_hin = "";
    int fixInd_hin = -1, nextInd_hin = -1;
    public void handleTTS_hin(String ttsString, String viewString, int ind){
        Log.d(TAG,"handleTTS hin: " + ttsString + " " + viewString + " " + ind);
//        !tts.isSpeaking() && !lastSpokenString.equals(ttsString)
        if(!tts_hin.isSpeaking()){
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceCount_hin + "");
            lastSpokenString_hin = ttsString;
            fixInd_hin = ind;
            Log.d(TAG,"handleTTS hin: !speaking: " + ttsString + " " + viewString + " " + ind);
            try {
                textViewString[fixInd_hin] = viewString;
                nextPossibletts_hin = "";
                nextPossibleviewStr_hin = "";
                nextInd_hin = -1;
                setTextView();
                tts_hin.speak(ttsString, TextToSpeech.QUEUE_ADD, params);
                utteranceCount_hin++;
            } catch (UnknownError e){
                Log.d(TAG, "Exception in Hindi TTS " + e);

            }
        }else{
            if(!lastSpokenString_hin.equals(ttsString)){
                nextPossibletts_hin = ttsString;
                nextPossibleviewStr_hin = viewString;
                nextInd_hin = ind;
            }
        }
        Log.d(TAG,"handleTTS: " + nextPossibletts_hin+ " " + nextPossibleviewStr_hin+ " " + nextInd_hin);
    }

    public void setTextView(){
        Log.d(TAG,"setTextView: " + textViewString[0]);
        //String[] prefixes = {"Object: ", "Pedestrian: ", "Signboard: "};
        String[] prefixes;
        prefixes = new String[] {
                getResources().getString(R.string.object_prefix),
                getResources().getString(R.string.pedestrian_prefix),
                getResources().getString(R.string.signboard_prefix)
        };

        String temp = "";
        for(int i=0;i<numOfDetectionTypes;i++){
            temp += "<b>" + prefixes[i] + "</b>" + textViewString[i];
            if(i!=numOfDetectionTypes-1)
                temp += "\n";
        }
        String temp2 = "";
        for(String s: temp.split("\n")){
            temp2 += s + "<br>";
        }
        commonTextBoxCtr.setText(Html.fromHtml(temp2));
    }

}