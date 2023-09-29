package com.example.sakshisharmaiitdelhimaviproject.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.sakshisharmaiitdelhimaviproject.GlobalClass;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PiSyncService extends Service {

    private final static String TAG = "PiSyncService";

    public static Context ctxt;
    public static Context thiss;
    public static String ip, path;
    public static int port;
    private static Lock aLock;
    private static Condition readyCondition;
    private static Socket socket;
    private static DataInputStream din;
    private static PrintStream pout;
    public static Handler mainHandler;
    private static Boolean activatedHotspot;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        path = Environment.getExternalStorageDirectory().getPath();
        path += "/MAVI";
        new File(path).mkdirs();
        ctxt = getApplication();
        ip = null;port = -1;
        aLock = new ReentrantLock();
        readyCondition = aLock.newCondition();
        mainHandler = new Handler(getMainLooper());
        activatedHotspot = false;
        thiss = this;
        if(!BluetoothSendService.running){
            Toast.makeText(getApplicationContext(),"Controller not connected to bluetooth",Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }
//        createHotspot();
        createThread();
        switchOffThreads();
        switchSyncThread("n");
        GlobalClass mapp = (GlobalClass)ctxt;
        Log.d("GlobalClass","SyncThread off");
        mapp.updateVariable("syncThread","f");
    }

    public static void sendConfirmation(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("for", "syncThread");
            obj.put("status","success");
        }catch(org.json.JSONException e){}
        BluetoothSendService.pushToQueue(obj.toString());
    }

    public static void moveFiles(){
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        String folderpath = path + "/" + formatter.format(ts);
        String filename;
        File old,target;
        // create timestamped folder
        new File(folderpath).mkdirs();
        new File(folderpath,"recordPhotos").mkdirs();
        filename = "records.log";
        old = new File(path,filename);
        target = new File(folderpath,filename);
        old.renameTo(target);
        filename = "recordsMobile.log";
        old = new File(path,filename);
        target = new File(folderpath,filename);
        old.renameTo(target);
        File file = new File(path, "filenames.txt");
        String imagespathold = path + "/recordPhotos";
        String imagespathnew = folderpath + "/recordPhotos";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                old = new File(imagespathold,line);
                target = new File(imagespathnew,line);
                old.renameTo(target);
            }
            br.close();
        } catch (Exception e) {
            Log.e(TAG,"exception", e);
        }
        new File(imagespathold).delete();
        new File(path,"filenames.txt").delete();
        toast("Files received and moved to MAVI/" + formatter.format(ts));
    }


    private void createThread(){
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

    private void tRun() {
        Log.d(TAG, "tRun");
        aLock.lock();
        try {
            while(ip==null) {
                try {
                    toast("Waiting for WiFi connection");
                    readyCondition.await();
                } catch (InterruptedException e) { }
            }
        } finally {
            aLock.unlock();
        }
        // IP and port are available get Files
        connect();

        getFile("records.log", path);
        getFile("recordsMobile.log", path);
        getFile("filenames.txt", path);
        moveFiles();
        //sendConfirmation();
        //handleHotspot();
        stopSelf();
    }



    public static void toast(final String str){
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                // Do your stuff here related to UI, e.g. show toast
                Toast.makeText(ctxt, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void toast(final String str, boolean isLong){
        if(isLong){
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Do your stuff here related to UI, e.g. show toast
                    Toast.makeText(ctxt, str, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            toast(str);
        }
    }

    public void connect(){
        Log.d(TAG, "connect: "+ip+":"+port);
        try{
            socket = new Socket(ip, port);
            din = new DataInputStream(socket.getInputStream());
            pout = new PrintStream(socket.getOutputStream());
            toast("Connection successful! Transferring files\nConnected to : " + ip,true);
        }catch (Exception e){
            Log.e(TAG, "exception", e);
            toast("Connection not successful",true);
            stopSelf();
        }
    }

    public static void getFile(String filename, String path){
        Log.d(TAG, "getFile");
        try {
            File file = new File(path, filename);
            // Create new file if it does not exist
            // Then request the file from server
            if(!file.exists()){
                System.out.println(filename + " does not exist");
                file.createNewFile();
                System.out.println("Created New File: " + filename);
            }
            FileOutputStream fos = new FileOutputStream(file);

            byte[] buffer = new byte[8192];
            din.read(buffer,0,9);
            String str = new String(buffer,0,9);
            int numBytes= Integer.parseInt(str);
            int toRead = numBytes;
            while(toRead>0){
                int temp = din.read(buffer, 0, Math.min(buffer.length,toRead));
                toRead -= temp;
                fos.write(buffer,0,temp);
            }
            Log.d(TAG,"NumBytes: " + numBytes);
            fos.flush();
            fos.close();
            pout.print("1");
        } catch (Exception e) {
            Log.e(TAG, "exception", e);
        }
    }

    public static void onSyncDetected(JSONObject reader){
        try{
            Log.d(TAG,"onSyncDetected");
            ip = reader.getString("ip");
            Log.d("IP Address : ", ip);
            port = reader.getInt("port");
            aLock.lock();
            readyCondition.signalAll();
            Log.d(TAG,"Ready to Send");
            aLock.unlock();
        }catch(Exception e){
            Log.e(TAG, "unexpected JSON exception", e);
        }
    }

    private static void switchSyncThread(String on){
        GlobalClass mapp = (GlobalClass)ctxt;
        if(mapp.updateVariable("syncThread",on)){
            Log.d(TAG,"syncThread: " + on);
            String str = mapp.variablesToString("movidius", "movidius");
            BluetoothSendService.pushToQueue(str);
        }
    }

    private static void switchOffThreads(){
        Log.d(TAG,"switchOffThreads");
        GlobalClass mapp = (GlobalClass)ctxt;
        if(mapp.updateVariable("mobileView","f")){
            Log.d(TAG,"Turn off mobileView");
            String str = mapp.variablesToString("movidius", "movidius");
            BluetoothSendService.pushToQueue(str);
        }
        if(mapp.updateVariable("mobileRecord","f")){
            Log.d(TAG,"Turn off mobileRecord");
            String str = mapp.variablesToString("movidius", "movidius");
            BluetoothSendService.pushToQueue(str);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null;}
}
