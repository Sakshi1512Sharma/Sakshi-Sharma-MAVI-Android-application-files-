package com.example.sakshisharmaiitdelhimaviproject;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class BluetoothSend extends Thread {
    private final BluetoothSocket mmSocket;
    private final OutputStream mmOutStream;
    private Handler mHandler;
    private Context c;
    private String objDetectionType, faceDetectionType;
    private Condition queueCondition;
    private Lock aLock;
    private Queue<String> q;

    public BluetoothSend(BluetoothSocket socket, Handler handler, Context receive, String objDetectionType) {
        aLock = new ReentrantLock();
        queueCondition = aLock.newCondition();
        q = new LinkedList<String>();
        this.objDetectionType = objDetectionType;
        mmSocket = socket;
        OutputStream tempOut = null;
        mHandler = handler;

        try {
            tempOut = socket.getOutputStream();
        } catch (IOException e) {}

        mmOutStream = tempOut;
        c = receive;
        sendConfig();
    }

    private void sendConfig(){
        try {
            JSONObject obj = new JSONObject();
            obj.put("objectDetection", objDetectionType);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
            obj.put("sendImage",prefs.getBoolean("pushToServer", false));
            String writeString = obj.toString();
            pushToQueue(writeString);
        }catch(Exception e){
            Log.d("ConnectedThread", e.toString());
        }
    }

    public void sendConnectionMessage(Boolean connectionStatus){
        Message msgObj = mHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putBoolean("connection",connectionStatus);
        msgObj.setData(b);
        mHandler.sendMessage(msgObj);
    }

    public JSONObject getJSONObject(GlobalClass check){
        JSONObject obj = new JSONObject();
        try {
            if(objDetectionType.equals(("cpu"))){
                obj.put("objectDetectionCPU", (check.getObjectDetectStatus()));
            }else{
                obj.put("objectDetection", (check.getObjectDetectStatus()));
            }
            if(faceDetectionType.equals(("cpu"))){
                obj.put("faceDetectionCPU", (check.getFaceDetectStatus()));
            }else{
                obj.put("faceDetection", (check.getFaceDetectStatus()));
            }
            obj.put("signboardDetection", (check.getSignboardDetectStatus()));
            obj.put("power", check.getPiVariable());
        }catch(org.json.JSONException e){}
        return obj;
    }

    public void pushToQueue(String str){
        Log.d("pushToQueue",str);
        aLock.lock();
        q.add(str);
        queueCondition.signalAll();
        Log.d("pushToQueue","1..SignalAll");
        aLock.unlock();
        Log.d("pushToQueue","2..Unlocked");
    }

    public void run() {
        while(true){
            try {
                Log.d("pushToQueue", "Waiting to send");
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
                Log.d("ConnectedThread", writeString);
                byte[] buf = writeString.getBytes();
                mmOutStream.write(buf, 0, buf.length);
            } catch (IOException e) {
                break;
            }
        }
    }

    public void cancel() {
        Log.d("ConnectedThread","cancel() this thread");
        try {
            mmSocket.close();
        } catch (IOException e) {

        }
    }
}
