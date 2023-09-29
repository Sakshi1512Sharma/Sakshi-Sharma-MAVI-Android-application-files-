package com.example.sakshisharmaiitdelhimaviproject;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;


public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    // private final OutputStream mmOutStream;
    private Handler mHandler;
    private Context c;
    private String objDetectionType;

    public ConnectedThread(BluetoothSocket socket, Handler handler, Context receive, String objDetectionType) {
        this.objDetectionType = objDetectionType;
        mmSocket = socket;
        InputStream tempIn = null;
        // OutputStream tempOut = null;
        mHandler = handler;
        try {
            tempIn = socket.getInputStream();
            // tempOut = socket.getOutputStream();
        } catch (IOException e) {

        }

        mmInStream = tempIn;
        // mmOutStream = tempOut;
        c = receive;
    }

    // private void sendConfig(){
    //     try {
    //         JSONObject obj = new JSONObject();
    //         obj.put("objectDetection", objDetectionType);
    //         String writeString = obj.toString();
    //         Log.d("ConnectedThread", writeString);
    //         byte[] buf = writeString.getBytes();
    //         // mmOutStream.write(buf, 0, buf.length);
    //     }catch(Exception e){
    //         Log.d("ConnectedThread",e.toString());
    //     }
    // }

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
            obj.put("faceDetection", (check.getFaceDetectStatus()));
            obj.put("signboardDetection", (check.getSignboardDetectStatus()));
            obj.put("power", check.getPiVariable());
        }catch(org.json.JSONException e){}
        return obj;
    }

    public void run() {
        sendConnectionMessage(true);
        byte[] buffer = new byte[1024];
        int bytes;
        Boolean flag = true;

        while(flag) {
            GlobalClass check = (GlobalClass) c.getApplicationContext();
            try {
                Log.d("ConnectedThread", "Waiting to receive");
                bytes = mmInStream.read(buffer);
                Log.d("ConnectedThread", "Received");
                String jsonString = new String(buffer, 0, bytes);
                Log.i("Connected Thread", jsonString);
                JSONObject reader;
                int count = 0;
                while(true){
                    count += 1;
                    Log.d("Sakshi",""+count);
                    try {
                        reader = new JSONObject(jsonString);
                        Log.i("Connected Thread reader",""+reader);
                    }catch(Exception e){
//                        buffer = new byte[1024];
                        bytes = mmInStream.read(buffer);
                        jsonString += new String(buffer, 0, bytes);
                        Log.i("Connected Thread 2", jsonString);
                        continue;
                    }
                    break;
                }
                Log.d("ConnectedThread", String.format("Num of loops: %d",count));

                if (!jsonString.equals(null) && !jsonString.equals("")) {
                    Message msgObj = mHandler.obtainMessage();
                    Bundle b = new Bundle();
                    b.putString("message", jsonString);
                    msgObj.setData(b);
                    mHandler.sendMessage(msgObj);


                } else {
                    Log.d("ConnectedThread", "Received null");
                }

            } catch (IOException e) {
                break;
            }
        }
        sendConnectionMessage(false);
        Log.d("ConnectedThread", "Exit reading buffer");
    }

    // public void run2() {
    //     sendConnectionMessage(true);
    //     byte[] buffer = new byte[1024];
    //     int bytes;
    //     Boolean flag = true;

    //     while(flag) {
    //         GlobalClass check = (GlobalClass) c.getApplicationContext();
    //         try {
    //             Log.d("ConnectedThread", "Waiting to receive");
    //             bytes = mmInStream.read(buffer);
    //             Log.d("ConnectedThread", "Received");
    //             String jsonString = new String(buffer, 0, bytes);
    //             JSONObject reader;
    //             while(true){
    //                 Log.d("ConnectedThread", "inWhile");
    //                 try {
    //                     reader = new JSONObject(jsonString);
    //                 }catch(Exception e){
    //                     bytes = mmInStream.read(buffer);
    //                     jsonString += new String(buffer, 0, bytes);
    //                     continue;
    //                 }
    //                 break;
    //             }
    //             Log.d("ConnectedThread", "JsonString: " + jsonString.toString());
    //             if (!jsonString.equals(null) && !jsonString.equals("")) {
    //                 Message msgObj = mHandler.obtainMessage();
    //                 Bundle b = new Bundle();
    //                 b.putString("message", jsonString);
    //                 msgObj.setData(b);
    //                 mHandler.sendMessage(msgObj);


    //             } else {
    //                 Log.d("ConnectedThread", "Received null");
    //             }

    //             //Writing "off" after receiving stuff
    //             try {
    //                 JSONObject obj = getJSONObject(check);
    //                 String writeString = obj.toString() + "\n";
    //                 byte[] buf = writeString.getBytes();
    //                 mmOutStream.write(buf, 0, buf.length);
    //                 if(!obj.getString("power").equals("n")){
    //                     flag = false;
    //                 }
    //             } catch(Exception e) {
    //                 break;
    //             }
    //         } catch (IOException e) {
    //             break;
    //         }
    //     }
    //     sendConnectionMessage(false);
    //     Log.d("ConnectedThread", "Exit reading buffer");
    // }

    public void cancel() {
        Log.d("ConnectedThread","cancel() this thread");
        try {
            mmSocket.close();
        } catch (IOException e) {

        }
    }
}
