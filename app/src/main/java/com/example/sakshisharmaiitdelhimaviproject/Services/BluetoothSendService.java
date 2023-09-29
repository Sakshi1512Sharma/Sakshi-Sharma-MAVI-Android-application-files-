package com.example.sakshisharmaiitdelhimaviproject.Services;

import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.sakshisharmaiitdelhimaviproject.GlobalClass;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BluetoothSendService extends Service {

    private final static String TAG = "BluetoothSendService";
    private static Lock aLock;
    private static Condition queueCondition;
    private static Queue<String> q;
    private static BluetoothSocket mmSocket;
    private static OutputStream mmOutStream;
    private static Handler mHandler;
    public static Boolean running = false;
    public static String objDetectionType, faceDetectionType;
    public static Context c;

    @Override
    public void onCreate(){
        Log.d(TAG, "onCreate");
        aLock = new ReentrantLock();
        queueCondition = aLock.newCondition();
        q = new LinkedList<String>();
        running = true;
        createThread();
        sendConfig();


        try{
            GlobalClass mapp = (GlobalClass)c.getApplicationContext();
            pushToQueue(mapp.variablesToString(objDetectionType,faceDetectionType));

        }catch (Exception e ){
            //TODO
        }finally {
            //TODO
        }

    }

    private static void createThread(){
        Log.d(TAG, "createThread");
        Runnable r = new Runnable() {
            @Override
            public void run() {
                tRun();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private static void tRun(){
        Log.d(TAG, "tRun");
        while(running){
            try {
                Log.d(TAG, "Waiting to send");
                String writeString = null;
                aLock.lock();
                try {
                    while(q.size()==0) {
                        try {
                            queueCondition.await();
                        } catch (InterruptedException e) { }
                    }
                    writeString = q.remove();
                } finally {
                    aLock.unlock();
                }
                Log.d(TAG, writeString);
                byte[] buf = writeString.getBytes();
                mmOutStream.write(buf, 0, buf.length);
            } catch (IOException e) {
                break;
            }
        }
    }

    private static void sendConfig(){
        try {
            JSONObject obj = new JSONObject();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            obj.put("objectDetection", prefs.getBoolean("objectDetection", true));
            obj.put("faceDetection", prefs.getBoolean("faceDetection", false));

            String writeString = obj.toString();
            pushToQueue(writeString);
        }catch(Exception e){
            Log.d("ConnectedThread", e.toString());
        }
    }

    public static void setSocket(BluetoothSocket mmSocket) {
        BluetoothSendService.mmSocket = mmSocket;
        try {
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            mmOutStream = null;
        }
    }

    public static void setHandler(Handler mHandler) {
        BluetoothSendService.mHandler = mHandler;
    }

    public static void pushToQueue(String str){
        Log.d(TAG,str);
        aLock.lock();
        q.add(str);
        queueCondition.signalAll();
        Log.d(TAG,"1..SignalAll");
        aLock.unlock();
        Log.d(TAG,"2..Unlocked");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
