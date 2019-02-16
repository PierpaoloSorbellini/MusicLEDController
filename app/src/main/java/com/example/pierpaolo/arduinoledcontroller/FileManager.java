package com.example.pierpaolo.arduinoledcontroller;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by pierpaolo on 13/03/18.
 */

/*
  Class to manage the file navigation, once created an object "FileManager" it can be used to access the file in the memory.
  -The file variable is used to access the list of the file.
  -The variable "List" contains the name of the file to be visualized in the file manager.
  -The variable "mp3List" is used to get the song list.
  -The variable "currentPath" is used to navigate in the various folders.
  it is composed of various method that allows to open the current path, get the name of the file or fill the mp3list.
  to move back and forward in the folders there are the methods back and addPath, as well as the method setPath.
  other  method allows to get information about the kind of element (getExtension isFolder isMp3).
 */

public class FileManager {

    String currentPath;
    private ArrayList<String> List = new ArrayList<String>();
    private ArrayList<String> mp3List = new ArrayList<String>();



    // *******************************************************************************************************************
    //                             METHOD TO ADD SUFFIX AT THE PATH
    // ********************************************************************************************************************
    public void addPath(String p) {
        currentPath = currentPath + File.separator + p;
    }



    // *******************************************************************************************************************
    //                             METHOD TO DELETE THE LAST FOLDER IN THE PATH
    // ********************************************************************************************************************
    public int back() {
        if (currentPath.equals( Environment.getExternalStorageDirectory().toString() )) {
            return -1;
        }
        currentPath = currentPath.substring( 0, currentPath.lastIndexOf( "/" ) );
        return 0;
    }



    // *******************************************************************************************************************
    //       METHOD TO OPEN THE PATH AND RETURN THE LIST OF THE NAME OF THE FILES IN IT IN ALPHABETICAL ORDER
    // ********************************************************************************************************************
    public ArrayList<String> open() {
        File[] file = new File( currentPath ).listFiles();
        if (file != null) {
            convertFilesToList( file );
            return List;
        } else return null;
    }



    // *******************************************************************************************************************
    //                       METHOD TO EXTRAPOLATE FROM THE FILE OBJECT ONLY NAME OF THE FILELIST
    // ********************************************************************************************************************
    private void convertFilesToList(File[] files) {
        List.clear();
        String Name;
        for (File file : files) {
            Name = file.toString();
            Name = Name.substring( Name.lastIndexOf( "/" ) + 1, Name.length() );
            List.add( Name );
        }
        orderList( List );
    }



    // *******************************************************************************************************************
    //                          METHOD TO ORDER THE LIST IN ALPHABETICAL ORDER
    // ********************************************************************************************************************
    private void orderList(ArrayList<String> list) {
        Collections.sort( list, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo( s2 );
            }
        } );
    }



    // *******************************************************************************************************************
    //                METHOD TO TAKE THE PATH OF THE MP3 INSIDE A FOLDER
    // ********************************************************************************************************************
    public ArrayList<String> openMp3() {
        mp3List.clear();
        File[] files = new File( currentPath ).listFiles();
        String Name;
        for (File file : files) {
            Name = file.toString();
            String extension = getExtension( Name );
            if (extension.equals( ".mp3" )) {
                mp3List.add( Name );
            }
        }
        return mp3List;
    }



    // *******************************************************************************************************************
    //                         METHOD TO TAKE THE EXTENSION FROM THE FILE NAME
    // ********************************************************************************************************************
    private String getExtension(String s) {
        String extension;
        if (s.length() > 3) {
            extension = s.substring( s.length() - 4, s.length() );
        } else extension = s;
        return extension;
    }



    // *******************************************************************************************************************
    //                            METHOD THAT RETURNS TRUE IF IT IS A FOLDER
    // ********************************************************************************************************************
    public boolean isFolder(String s) {
        String ex = getExtension( s );
        return !ex.contains( "." );
    }



    // *******************************************************************************************************************
    //                            METHOD THAT RETURNS TRUE IF IT IS AN MP3
    // ********************************************************************************************************************
    public boolean isMp3(String s) {
        String ex = getExtension( s );
        return ex.contains( ".mp3" );
    }


}
