package com.example.kunah.mybluetoothspp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.widget.Adapter;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.TieBean;
import com.dou361.dialogui.listener.DialogUIItemListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BtDevWidget  {
    private List<BluetoothDevice> mDevItems = new ArrayList<>();;
    private DialogUIItemListener mListener;

    BtDevWidget(DialogUIItemListener listener) {
        mListener = listener;
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (bondedDevices != null)
            mDevItems.addAll(bondedDevices);
    }

    public BluetoothDevice findDevByAddress(String address) {
        String devAddress;
        for (int i = 0; i < mDevItems.size(); i++) {
            devAddress = mDevItems.get(i).getAddress();
            if (devAddress.equals(address))
                return mDevItems.get(i);
        }
        return null;
    }

    public void addItem(BluetoothDevice dev){
        if (mDevItems.contains(dev))
            return;
        mDevItems.add(dev);
        //Log.i("addItems", "#####" + dev.getName());
        //notifyDataSetChanged();
    }

    public void show (Context context) {
        List<TieBean> devNames = new ArrayList<>();
        BluetoothDevice dev;

        for (int i = 0; i < mDevItems.size(); i++) {
            dev = mDevItems.get(i);
                devNames.add(new TieBean(dev.getName() + "\n"+ dev.getAddress()));
                //devNames.add(new TieBean(mDevItems.get(i).getAddress()));
            Log.i("mDevItems", "#####" + mDevItems.get(i).getName());
        }
//        devNames.add(new TieBean("1"));
//        devNames.add(new TieBean("2"));
//        devNames.add(new TieBean("3"));
        DialogUIUtils.showSheet(context, devNames, "", Gravity.CENTER, true, true, mListener).show();
    }
}
