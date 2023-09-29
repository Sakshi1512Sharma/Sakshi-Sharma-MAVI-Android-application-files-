package com.example.sakshisharmaiitdelhimaviproject;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.sakshisharmaiitdelhimaviproject.Services.BluetoothSendService;

import java.io.IOException;
import java.util.UUID;

public class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private Handler mHandler;
    private static UUID MY_UUID = UUID.fromString("446118f0-8b1e-11e2-9e96-0800200c9a66");
    private Context c;
    private String objDetectionType;
    private String faceDetectionType;
    public ConnectedThread connected;
    public static BluetoothSend bluetoothSend;

    public AcceptThread(BluetoothAdapter mBtAdapter, Handler handler, Context receive,
                        String objDetectionType, String faceDetectionType) {
        mHandler = handler;
        BluetoothServerSocket temp = null;
        try {
            // Added Permission Check
            if (ActivityCompat.checkSelfPermission(receive, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                temp = mBtAdapter.listenUsingRfcommWithServiceRecord("MyService", MY_UUID);
            }
        } catch (IOException e) {

        }
        mmServerSocket = temp;
        c = receive;
        this.objDetectionType = objDetectionType;
        this.faceDetectionType = faceDetectionType;
        connected = null;
        bluetoothSend = null;
    }

    public void run() {
        BluetoothSocket socket = null;
        while(true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }

            if(socket != null) {
                connected = new ConnectedThread(socket, mHandler, c, objDetectionType);
                connected.start();
                Log.d("AcceptThread","new bluetoothsend object attached");
                BluetoothSendService.c = c;
                BluetoothSendService.objDetectionType = objDetectionType;
                BluetoothSendService.faceDetectionType = faceDetectionType;
                BluetoothSendService.setSocket(socket);
                Intent bluetoothSend = new Intent(c, BluetoothSendService.class);
                c.startService(bluetoothSend);
//                AcceptThread.bluetoothSend = new BluetoothSend(socket, mHandler, c, objDetectionType);
//                AcceptThread.bluetoothSend.start();
                try {
                    mmServerSocket.close();
                } catch(IOException e) {


                }
                break;
            }
        }
        Log.d("AcceptThread", "Exit while loop");
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch(IOException e) {

        }
    }
}
