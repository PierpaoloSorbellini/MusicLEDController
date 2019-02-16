package com.example.pierpaolo.arduinoledcontroller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by pierpaolo on 08/03/18.
 */


/*
  Search in the file of the smartphone looking for song to be played
 */

public class ActivitySearch extends BaseActivity {

    private ItemAdapter itemAdapter;
    private FileManager fileManager = new FileManager();
    private ArrayList<String> nameList = new ArrayList<>();
    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private DataManager dataManager = new DataManager();



    // ********************************************************************************************************
    //                ON CREATE
    // ********************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( "SEARCH ACTIVITY:", "*****************on Create*****************" );
        super.onCreate( savedInstanceState );

        //*******************Preparation****************************
        ITEM_SELECTED = 1;
        super.setContentView( R.layout.activity_search );
        super.changeToolbarTitle( "Search:" );

        // if permission guarantee go on else, stop everything
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            //*****************Initialize default path***********************
            fileManager.currentPath = Environment.getExternalStorageDirectory().toString();
            super.changeToolbarTitle( "Search: " + fileManager.currentPath );

            //*******************Reference View Object*******************
            final ListView listView = findViewById( R.id.search_list );

            //*****************Setting list view of the files****************
            nameList = fileManager.open();
            itemAdapter = new ItemAdapter( this, nameList );
            listView.setAdapter( itemAdapter );
            listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                    ArrayList arrayList = new ArrayList();
                    if (fileManager.isFolder( nameList.get( position ) )) {
                        fileManager.addPath( nameList.get( position ) );
                        changeToolbarTitle( "Search: " + fileManager.currentPath );
                        arrayList = fileManager.open();
                        if (arrayList != null) {
                            nameList = arrayList;
                            itemAdapter.notifyDataSetChanged();
                        } else {
                            fileManager.back();
                        }
                    } else if (fileManager.isMp3( nameList.get( position ) )) {
                        String path = fileManager.currentPath + File.separator + nameList.get( position );
                        musicPlayerInfo.setListElement( path );
                        newListLoaded();
                        Intent intentMusicPlayer = new Intent( getApplicationContext(), ActivityMusicPlayer.class );
                        startActivity( intentMusicPlayer );
                    }
                }
            } );

            //******************Context menu**************************
            registerForContextMenu( listView );
        }
    }



    // ********************************************************************************************************
    //                ON PAUSE
    // ********************************************************************************************************
    @Override
    protected void onPause() {
        Log.d( "SEARCH ACTIVITY:", "*****************on Pause*****************" );
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            dataManager.writeInfo( getApplicationContext() );
        }
        super.onPause();
    }



    // ********************************************************************************************************
    //                METHOD TO HANDLE THE BACK PRESSED
    // ********************************************************************************************************
    @Override
    public void onBackPressed() {
        int e = fileManager.back();
        if (e == -1) {
            super.onBackPressed();
        }
        fileManager.open();
        changeToolbarTitle( "Search: " + fileManager.currentPath );
        itemAdapter.notifyDataSetChanged();
    }



    // ********************************************************************************************************
    //                METHOD FOR TO CREATE THE CONTEXT MENU
    // ********************************************************************************************************
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu( menu, v, menuInfo );
        getMenuInflater().inflate( R.menu.search_longpress_menu, menu );
    }



    // ********************************************************************************************************
    //                METHOD FOR THE CONTEXT MENU ACTION
    // ********************************************************************************************************
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        switch (item.getItemId()) {
            case R.id.search_longpress_menu_play:
                if (fileManager.isFolder( nameList.get( position ) )) {
                    fileManager.addPath( nameList.get( position ) );
                    musicPlayerInfo.setList( fileManager.openMp3() );
                    Intent intentMusicPlayer = new Intent( getApplicationContext(), ActivityMusicPlayer.class );
                    if (musicPlayerInfo.list.size() == 0) {
                        Toast.makeText( getApplicationContext(), "No mp3 Inside", Toast.LENGTH_SHORT ).show();
                        fileManager.back();
                    } else {
                        newListLoaded();
                        startActivity( intentMusicPlayer );
                    }
                }
                return true;
            case R.id.search_longpress_menu_cancel:
                return true;
        }
        return false;
    }



    // ********************************************************************************************************
    //                METHOD CALLED EVERY TIME A NEW LIST IS LOADED
    // ********************************************************************************************************
    private void newListLoaded() {
        musicPlayerInfo.isListChanged = true;
        Log.d( "STATUS:", "*****************IS LIST CHANGED == TRUE*****************" );
        musicPlayerInfo.currentTrack = 0;
    }

}






