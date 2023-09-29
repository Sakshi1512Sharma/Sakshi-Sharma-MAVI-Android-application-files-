package com.example.sakshisharmaiitdelhimaviproject;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sakshisharmaiitdelhimaviproject.Services.BluetoothSendService;

import java.io.IOException;
import java.util.UUID;


public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private static UUID MY_UUID = UUID.fromString("446118f0-8b1e-11e2-9e96-0800200c9a66");
    private final String objDetectionType;
    private final String faceDetectionType;
    private final Context c;
    public ConnectedThread connected;
    public static BluetoothSend bluetoothSend;
    private Handler mHandler;

    public ConnectThread(BluetoothDevice device, Handler handler, Context receive,
                         String objDetectionType, String faceDetectionType) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.

            // Added Permission Check
            if (ContextCompat.checkSelfPermission(receive, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }

        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;
        c = receive;
        mHandler = handler;
        this.objDetectionType = objDetectionType;
        this.faceDetectionType = faceDetectionType;
    }

//    public ConnectThread(BluetoothDevice device, Handler handler, Context receive,
//                         String objDetectionType, String faceDetectionType) throws IOException {
//        // Use a temporary object that is later assigned to mmSocket
//        // because mmSocket is final.
//        BluetoothSocket tmp = null;
//        mmDevice = device;
//
//        try {
//            // Get a BluetoothSocket to connect with the given BluetoothDevice.
//            // MY_UUID is the app's UUID string, also used in the server code.
//            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//        } catch (IOException e) {
//            Log.e(TAG, "Socket's create() method failed", e);
//            throw e; // re-throw the exception to be handled by the caller
//        }
//
//        if (tmp == null) {
//            // Handle the failure case gracefully by throwing an exception or returning a failure result
//            throw new IOException("Failed to create Bluetooth socket");
//        }
//
//        mmSocket = tmp;
//        c = receive;
//        mHandler = handler;
//        this.objDetectionType = objDetectionType;
//        this.faceDetectionType = faceDetectionType;
//    }

    public void run() {
        // Cancel discovery helps conserve battery life and improves overall Bluetooth performance
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (ActivityCompat.checkSelfPermission(c, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            bluetoothAdapter.cancelDiscovery();
        }

        try {
            mmSocket.connect();
        } catch (IOException connectException) {
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Scanning failed", closeException);
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        Log.e(TAG, "Connected to Raspberry pi");
        //toast("Connected to Raspberry Pi");
        connected = new ConnectedThread(mmSocket, mHandler, c, objDetectionType);
        connected.start();
        Log.d("ConnectThread","new bluetoothsend object attached");
        BluetoothSendService.c = c;
        BluetoothSendService.objDetectionType = objDetectionType;
        BluetoothSendService.faceDetectionType = faceDetectionType;
        BluetoothSendService.setSocket(mmSocket);
        Intent bluetoothSend = new Intent(c, BluetoothSendService.class);
        c.startService(bluetoothSend);
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
    }
}
