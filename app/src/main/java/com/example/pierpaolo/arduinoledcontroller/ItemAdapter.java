package com.example.pierpaolo.arduinoledcontroller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by pierpaolo on 08/03/18.
 */

public class ItemAdapter extends BaseAdapter {

    private LayoutInflater layoutinflater;
    private ArrayList<String> items;



    // *******************************************************************************************************************
    //                                     CONSTRUCTOR
    // ********************************************************************************************************************
    public ItemAdapter(Context c, ArrayList<String> i) {
        items = i;
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



    // *******************************************************************************************************************
    //                                     GET VIEW
    // ********************************************************************************************************************
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = layoutinflater.inflate( R.layout.activity_search_detail, null );

        // Set the text of list item
        TextView fileName = v.findViewById( R.id.search_file_name );
        fileName.setText( items.get( i ) );

        // Set the image of the item
        ImageView fileImage = v.findViewById( R.id.search_file_icon );
        setImage( items.get( i ), fileImage );
        return v;
    }



    // *******************************************************************************************************************
    //                                     METHOD TO ASSOCIATE EACH OBJECT WITH THE ICON
    // ********************************************************************************************************************
    private void setImage(String name, ImageView fileImage) {
        String extension = getExtension( name );
        if (extension.contains( "." )) {
            switch (extension) {
                case ".jpg":
                case ".png":
                    fileImage.setImageResource( R.drawable.icon_image );
                    break;
                case ".mp3":
                    fileImage.setImageResource( R.drawable.icon_song );
                    break;
                default:
                    fileImage.setImageResource( R.drawable.icon_unknow_extension );
            }
        } else {
            fileImage.setImageResource( R.drawable.icon_folder );
        }
    }



    // *******************************************************************************************************************
    //                                     GET EXTENSION FROM THE FILE NAME
    // ********************************************************************************************************************
    private String getExtension(String Name) {
        String extension;
        if (Name.length() > 3) {
            extension = Name.substring( Name.length() - 4, Name.length() );
        } else {
            extension = "";
        }
        return extension;
    }


}
