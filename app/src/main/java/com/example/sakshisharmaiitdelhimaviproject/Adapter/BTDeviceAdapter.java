package com.example.sakshisharmaiitdelhimaviproject.Adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.sakshisharmaiitdelhimaviproject.DetectionModule;
import com.example.sakshisharmaiitdelhimaviproject.Model.DeviceList;
import com.example.sakshisharmaiitdelhimaviproject.R;

import java.util.Iterator;
import java.util.List;

public class BTDeviceAdapter extends RecyclerView.Adapter<BTDeviceAdapter.MyViewHolder> {

    private List<DeviceList> mBTDevices;
    private Context ctx;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BTDeviceAdapter(Context ctx, List<DeviceList> myDataset) {
        this.ctx= ctx;
        this.mBTDevices = myDataset;
    }

    public boolean hasDeviceItem(DeviceList Dd){
        Iterator itr = mBTDevices.iterator();

        while(itr.hasNext()){
            DeviceList  d= (DeviceList) itr.next();
            if(d.getDeviceID().equals(Dd.getDeviceID()) && d.getName().equals(Dd.getName()) ){
                return true;
            }
        }
        return false;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.btdevice_list_view, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String name = mBTDevices.get(position).getName();

        holder.itemView.setContentDescription(ctx.getString(R.string.btdeviceadaptermsg));

        holder.Name.setText(name);
        holder.MACaddr.setText(mBTDevices.get(position).getDeviceID());
    }

    @Override
    public int getItemCount() {
        return mBTDevices.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView Name;
        public TextView MACaddr;
        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.Name=(TextView)view.findViewById(R.id.name);
            this.MACaddr=(TextView)view.findViewById(R.id.macaddr);
        }

        @Override
        public void onClick(View view) {
         Log.i("MyViewHolder","onclick: "+getPosition()+Name.getText().toString()+":"+MACaddr.getText().toString());

            Log.i("MyViewHolder","onclick: "+getPosition()+Name.getText().toString()+":"+MACaddr.getText().toString());

//            Intent intent = new Intent("custom-message");
//            intent.putExtra("macaddress",MACaddr.getText().toString());
//            intent.putExtra("btname",Name.getText().toString());
//            LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);
//
//            ((Activity)view.getContext()).finish();


            Intent intent = new Intent((Activity)view.getContext(), DetectionModule.class);
            intent.putExtra("macaddress",MACaddr.getText().toString());
            intent.putExtra("btname",Name.getText().toString());
            //LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(intent);

            ((Activity)view.getContext()).startActivity(intent);

            ((Activity)view.getContext()).finish();
        }
    }
}
