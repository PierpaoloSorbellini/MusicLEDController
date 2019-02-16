package com.example.pierpaolo.arduinoledcontroller;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.util.ArrayList;

public class MusicPlayerInfo {



    // ***********************************SINGLETON ACCESS************************************
    private static MusicPlayerInfo musicPlayerInfo = null;

    private MusicPlayerInfo() {
    }


    public static synchronized MusicPlayerInfo getMusicPlayerInfo() {
        if (musicPlayerInfo == null) {
            musicPlayerInfo = new MusicPlayerInfo();
        }
        return musicPlayerInfo;
    }

    //**********************************GLOBAL***********************************************
    public boolean readAndWritePermission = false;

    // set to true on the ActivitySearch when a the intent to open the ActivityMusicPlayer is activated,
    // it enables the function reload of the MusicService, after reload is called isListChanged is set
    // automatically to false.
    boolean isListChanged = true;

    // set to true when the list is non-empty on the onStart of the ActivityMusicPlayer,
    // when is on true the button of the player are enabled
    boolean isListLoaded = false;

    // variable to control the Arduino comand
    boolean manualControlEnable = false;
    boolean musicControlEnable = false;

    // ***********************************LIST AND CURRENT TRACK************************************************
    ArrayList<String> list = new ArrayList<String>(); // list to store the path of the track
    int currentTrack = -1; // current track selected
    private String currentPath; //current path to return the path of the song to be played



    public void setList(ArrayList<String> s) {
        list.clear();
        list.addAll( s );
    }



    public void setListElement(String s) {
        list.clear();
        list.add( s );
    }



    public String getCurrentPath() {
        currentPath = list.get( currentTrack );
        return currentPath;
    }



    //***********************************PLAYER************************************
    public boolean isPlayerPrepared = false; // variable to verify if the player is prepared
    public int currentTimeUs = 0;
    public int durationMs = 0;
    public boolean isAudioFocusObtained = false;



    //*********************************ANALYZER************************************
    int spanForDecoder = 3; // frame span to be decoded by the secondary decoder for the track analysis (TUNABLE)


    //*********************************COLOR CONTROLLER****************************

    // manual controller
    boolean isFade = false;
    boolean isFlash = false;
    int speedFlash = 30; // increment or decrement the flash speed in manual controller mode (TUNABLE)
    int speedFade = 30; // increment or decrement the fade speed in manual controller mode (TUNABLE)
    double colorIntensity = 1; // increment or decrement the color intensity in manual controller mode (TUNABLE)
    int[] manualColor = {0,0,0};


    // ***********************************DECODER************************************
    int samplingFrequency = 44100; // sampling frequency of the current track
    int numberOfChannel = 2; // number of channel used by the MP3
    int outputBufferLimit = 4096; //limit of the output buffer of the music decoder
    private int NumberOfSamplesForFrame = 1; //variable to store the number of samples decoded at each step
    private long frameDurationUs = 12000; //duration of a single frame
    boolean isDecoderPrepared = false;

    public int getNumberOfSampleForFrame() {
        NumberOfSamplesForFrame = outputBufferLimit / (numberOfChannel * 2);
        return NumberOfSamplesForFrame;
    }

    public long getFrameDurationUs() {
        double a = (double) getNumberOfSampleForFrame();
        frameDurationUs = (long) (a / (samplingFrequency) * 1000 * 1000);
        return frameDurationUs;
    }


    //***********************************BLUETOOTH************************************
    ArrayList<String> bluetoothDevice = new ArrayList<String>(); //Array list of the bluetooth device
    int currentDeviceConnected = -1; // current bluetooth device selected from the list
    BluetoothDevice device; // current bluetooth device selected
    BluetoothSocket bluetoothSocket; // socket for the bluetooth connection

    public ArrayList<String> getBluetoothDeviceList() {
        return bluetoothDevice;
    }



}


