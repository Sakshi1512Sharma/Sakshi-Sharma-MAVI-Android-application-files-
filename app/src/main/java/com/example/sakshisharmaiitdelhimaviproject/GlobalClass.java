 package com.example.sakshisharmaiitdelhimaviproject;

 import android.app.Application;
 import android.util.Log;

 import org.json.JSONObject;

 import java.util.Hashtable;

 public class GlobalClass extends Application {
     private String powerStatus;
     private String piStatus;
     private String objectDetectStatus;
     private String faceDetectStatus;
     private String signboardDetectStatus;
     private Hashtable<String, String> h;

     public GlobalClass() {
         powerStatus = "n";
         piStatus = "n";
         objectDetectStatus = "f";
         faceDetectStatus = "f";
         signboardDetectStatus = "f";
         h = new Hashtable<String, String>();
         h.put("power","n");
         h.put("objectDetection","f");
         h.put("faceDetection","f");
         h.put("signboardDetection","f");
     }

     public Boolean updateVariable(String key, String val){
         Log.d("GlobalClass", key);
         Log.d("GlobalClass", val);
         if( ! h.get(key).equals(val) ) {
             h.put(key, val);
             return true;
         }
         return false;
     }

     public String variablesToString(String objDetectionType, String faceDetectionType){
         JSONObject obj = new JSONObject();
         try {
             obj.put("for", "main");

             if(objDetectionType.equals(("cpu"))){
                 obj.put("objectDetectionCPU", h.get("objectDetection"));
             }else{
                 obj.put("objectDetection", h.get("objectDetection"));
             }

             if(objDetectionType.equals(("cpu"))){
                 obj.put("faceDetectionCPU", h.get("faceDetection"));
             }else{
                 obj.put("faceDetection", h.get("faceDetection"));
             }

             obj.put("signboardDetection", h.get("signboardDetection"));

             obj.put("power", h.get("power"));
         }

         catch(org.json.JSONException e){}
         Log.d("GlobalClass",obj.toString());
         return obj.toString();
     }

     public void setPowerVariable(String someVariable) {
         this.powerStatus = someVariable;
     }
     public String getPowerVariable() {
         return powerStatus;
     }

     public void setPiVariable(String someVariable) {
         this.piStatus = someVariable;
     }
     public String getPiVariable() {
         return piStatus;
     }

     public void setFaceDetectStatus(String someVariable) {
         this.faceDetectStatus = someVariable;
     }
     public String getFaceDetectStatus() {
         return faceDetectStatus;
     }

     public void setObjectDetectStatus(String someVariable) { this.objectDetectStatus = someVariable; }
     public String getObjectDetectStatus() {
         return objectDetectStatus;
     }

     public void setSignboardDetectStatus(String someVariable) { this.signboardDetectStatus = someVariable; }
     public String getSignboardDetectStatus() {
         return signboardDetectStatus;
     }
 }
