package com.example.pierpaolo.arduinoledcontroller;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by pierpaolo on 10/03/18.
 */




public class MyBluetoothAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private ArrayList<String> items = new ArrayList<String>();
    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private int color;



    // ********************************************************************************************************
    //                CONSTRUCTOR
    // ********************************************************************************************************
    public MyBluetoothAdapter(Context c) {
        color = ContextCompat.getColor(c, R.color.primaryColor);
        items = musicPlayerInfo.getBluetoothDeviceList();
        layoutinflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }



    @Override
    public int getCount() {
        return items.size();
    }



    @Override
    public Object getItem(int i) {
        return items.get( i );
    }



    @Override
    public long getItemId(int i) {
        return i;
    }



    // ********************************************************************************************************
    //                GET VIEW
    // ********************************************************************************************************
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = layoutinflater.inflate( R.layout.activity_bluetooth_detail, null );

        // Set the text of list item which are not selected
        TextView textView = v.findViewById( R.id.bluetooth_detail_text );
        ImageView imageView = v.findViewById( R.id.bluetooth_detail_icon );
        textView.setText( items.get( i ) );

        // Get the itemselected from textfile if present otherwise get default
        int selectedItem = musicPlayerInfo.currentDeviceConnected;

        // Set the selected item characteristic
        if (i == selectedItem) {
            textView.setTextColor( color );
            imageView.setColorFilter( color );
        }
        return v;
    }


}