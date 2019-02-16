package com.example.pierpaolo.arduinoledcontroller;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Created by pierpaolo on 08/04/18.
 */

/*
   Class to manage the write/read information on file to memorize it, for each information there are two methods, one for writing and one for reading
   the data on a file
 */



public class DataManager {

    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();



    // ********************************************************************************************************************
    //                       WRITE INFO
    // ********************************************************************************************************************
    public void writeInfo(Context context){
        writeList( context );
        writeInt( context );
        writeBoolean( context );
        writeDouble( context );
    }



    // ********************************************************************************************************************
    //                       WRITE INFO
    // ********************************************************************************************************************
    public void readInfo(Context context){
        readList( context );
        readInt( context );
        readBoolean( context );
        readDouble( context );
    }



    // ********************************************************************************************************************
    //                       WRITE THE LIST INFORMATION ON FILE
    // ********************************************************************************************************************
    private void writeList( Context context) {
        FileOutputStream fout;
        try {
            fout = context.openFileOutput("MusicList", Context.MODE_PRIVATE );
            ObjectOutputStream oos = new ObjectOutputStream( fout );
            oos.writeObject(musicPlayerInfo.list);
            fout.close();
            Log.d("DATA MANAGER","****************LIST Information written on file*******************");
        } catch (IOException e) {
            Log.d("DATA MANAGER","****************Can't write LIST information on file: ERROR*******************");
            e.printStackTrace();
        }
    }



    // ********************************************************************************************************************
    //                       READ LIST INFORMATION FROM FILE
    // ********************************************************************************************************************
    private void readList(Context context) {
        FileInputStream fin;
        ObjectInputStream ois;
        try {
            fin = context.openFileInput("MusicList");
            ois = new ObjectInputStream(fin);
            musicPlayerInfo.list = (ArrayList<String>) ois.readObject();
            fin.close();
            Log.d("DATA MANAGER","****************LIST Information read from file*******************");
        } catch (IOException | ClassNotFoundException e) {
            Log.d("DATA MANAGER","****************Can't read LIST information from file: ERROR*******************");
            e.printStackTrace();
        }
    }



    // ********************************************************************************************************************
    //                       WRITE THE PARAMETER INFORMATION ON FILE
    // ********************************************************************************************************************
    private void writeInt( Context context) {
        FileOutputStream fout;
        try {
            int[] info = {musicPlayerInfo.spanForDecoder,
                    musicPlayerInfo.speedFlash,
                    musicPlayerInfo.speedFade};
            fout = context.openFileOutput("intInfo", Context.MODE_PRIVATE );
            ObjectOutputStream oos = new ObjectOutputStream( fout );
            oos.writeObject(info);
            fout.close();
            Log.d("DATA MANAGER","****************Integer Information written on file*******************");
        } catch (IOException e) {
            Log.d("DATA MANAGER","****************Can't write Integer information on file: ERROR*******************");
            e.printStackTrace();
        }
    }



    // ********************************************************************************************************************
    //                       READ LIST INFORMATION FROM FILE
    // ********************************************************************************************************************
    private void readInt(Context context) {
        FileInputStream fin;
        ObjectInputStream ois;
        try {
            fin = context.openFileInput("intInfo");
            ois = new ObjectInputStream(fin);
            int[] info = (int[]) ois.readObject();
            musicPlayerInfo.spanForDecoder = info[0];
            musicPlayerInfo.speedFlash = info[1];
            musicPlayerInfo.speedFade = info[2];
            fin.close();
            Log.d("DATA MANAGER","****************Integer Information read from file*******************");
        } catch (IOException | ClassNotFoundException e) {
            Log.d("DATA MANAGER","****************Can't read Integer information from file: ERROR*******************");
            e.printStackTrace();
        }
    }

    // ********************************************************************************************************************
    //                       WRITE THE PARAMETER INFORMATION ON FILE
    // ********************************************************************************************************************
    private void writeBoolean( Context context) {
        FileOutputStream fout;
        try {
            boolean[] info = {musicPlayerInfo.isFade,
                    musicPlayerInfo.isFlash,
                    musicPlayerInfo.manualControlEnable,
                    musicPlayerInfo.musicControlEnable};
            fout = context.openFileOutput("booleanInfo", Context.MODE_PRIVATE );
            ObjectOutputStream oos = new ObjectOutputStream( fout );
            oos.writeObject(info);
            fout.close();
            Log.d("DATA MANAGER","****************Boolean Information written on file*******************");
        } catch (IOException e) {
            Log.d("DATA MANAGER","****************Can't write Boolean information on file: ERROR*******************");
            e.printStackTrace();
        }
    }



    // ********************************************************************************************************************
    //                       READ LIST INFORMATION FROM FILE
    // ********************************************************************************************************************
    private void readBoolean(Context context) {
        FileInputStream fin;
        ObjectInputStream ois;
        try {
            fin = context.openFileInput("booleanInfo");
            ois = new ObjectInputStream(fin);
            boolean[] info = (boolean[]) ois.readObject();
            musicPlayerInfo.isFade = info[0];
            musicPlayerInfo.isFlash = info[1];
            musicPlayerInfo.manualControlEnable = info[2];
            musicPlayerInfo.musicControlEnable = info[3];
            fin.close();
            Log.d("DATA MANAGER","****************Boolean Information read from file*******************");
        } catch (IOException | ClassNotFoundException e) {
            Log.d("DATA MANAGER","****************Can't read Boolean information from file: ERROR*******************");
            e.printStackTrace();
        }
    }


    // ********************************************************************************************************************
    //                       WRITE THE PARAMETER INFORMATION ON FILE
    // ********************************************************************************************************************
    private void writeDouble( Context context) {
        FileOutputStream fout;
        try {
            double[] info = {musicPlayerInfo.colorIntensity};
            fout = context.openFileOutput("doubleInfo", Context.MODE_PRIVATE );
            ObjectOutputStream oos = new ObjectOutputStream( fout );
            oos.writeObject(info);
            fout.close();
            Log.d("DATA MANAGER","****************Double Information written on file*******************");
        } catch (IOException e) {
            Log.d("DATA MANAGER","****************Can't write Double information on file: ERROR*******************");
            e.printStackTrace();
        }
    }



    // ********************************************************************************************************************
    //                       READ LIST INFORMATION FROM FILE
    // ********************************************************************************************************************
    private void readDouble(Context context) {
        FileInputStream fin;
        ObjectInputStream ois;
        try {
            fin = context.openFileInput("doubleInfo");
            ois = new ObjectInputStream(fin);
            double[] info = (double[]) ois.readObject();
            musicPlayerInfo.colorIntensity = info[0];
            fin.close();
            Log.d("DATA MANAGER","****************Double Information read from file*******************");
        } catch (IOException | ClassNotFoundException e) {
            Log.d("DATA MANAGER","****************Can't read Double information from file: ERROR*******************");
            e.printStackTrace();
        }
    }

}