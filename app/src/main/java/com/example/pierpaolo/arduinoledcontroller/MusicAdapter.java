package com.example.pierpaolo.arduinoledcontroller;

import android.annotation.SuppressLint;
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




public class MusicAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private ArrayList<String> items = new ArrayList<String>();
    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private int color;



    // ********************************************************************************************************************
    //                         CONSTRUCTOR
    // ********************************************************************************************************************
    public MusicAdapter(Context c) {
        color = ContextCompat.getColor(c, R.color.primaryColor);
        if (musicPlayerInfo.list.size() != 0) {
            items = convertList( musicPlayerInfo.list );
        }
        layoutinflater = (LayoutInflater) c.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
    }



    // ********************************************************************************************************************
    //                         CONVERT PATH LIST IN NAME LIST
    // ********************************************************************************************************************
    private ArrayList<String> convertList(ArrayList<String> list) {
        ArrayList<String> nameList = new ArrayList<>();
        String name;
        for (String element : list) {
            name = element.substring( element.lastIndexOf( "/" ) + 1, element.lastIndexOf( "." ) );
            nameList.add( name );
        }
        return nameList;
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



    // ********************************************************************************************************************
    //                         GET VIEW
    // ********************************************************************************************************************
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        @SuppressLint("ViewHolder") View v = layoutinflater.inflate( R.layout.activity_musicplayer_detail, null );

        // Set the text of list item which are not selected
        TextView textView = v.findViewById( R.id.musicplayer_detail_text );
        ImageView imageView = v.findViewById( R.id.musicplayer_detail_icon );
        textView.setText( items.get( i ) );

        // Set the selected item characteristic
        if (i == musicPlayerInfo.currentTrack) {
            textView.setTextColor( color );
            imageView.setColorFilter( color );
        }
        return v;
    }


}