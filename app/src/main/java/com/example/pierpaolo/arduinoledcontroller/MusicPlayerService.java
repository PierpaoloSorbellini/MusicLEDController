package com.example.pierpaolo.arduinoledcontroller;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MusicPlayerService extends Service {

    private static MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private static Player player = new Player();
    private final IBinder musicBind = new MusicBinder();



    public class MusicBinder extends Binder {
        MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }



    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }



    // ********************************************************************************************************
    //                STATE METHOD ON CREATE
    // ********************************************************************************************************
    @Override
    public void onCreate() {
        Log.d( "MUSIC PLAYER SERVICE", "*****************onCreate()*****************" );
        super.onCreate();
        // player.mediaPlayer.setWakeMode( getApplicationContext(),PowerManager.PARTIAL_WAKE_LOCK ); //TODO is it necessary?
    }



    // ********************************************************************************************************
    //                STATE METHOD ON DESTORY
    // ********************************************************************************************************
    @Override
    public void onDestroy() {
        Log.d( "MUSIC PLAYER SERVICE", "*****************onDestroy*****************" );
        player.release();
        super.onDestroy();

    }



    // ********************************************************************************************************
    //                BUTTON PLAY METHOD
    // ********************************************************************************************************
    public void playButton() {
        if (player.mediaPlayer.isPlaying()) {
            player.stop();
        }
        else {
            player.play();
        }
    }



    // ********************************************************************************************************
    //               BUTTON NEXT METHOD
    // ********************************************************************************************************
    public void nextButton() {
        player.stop();
        if (musicPlayerInfo.list.size() == musicPlayerInfo.currentTrack + 1) {
            musicPlayerInfo.currentTrack = 0;
        } else {
            musicPlayerInfo.currentTrack = musicPlayerInfo.currentTrack + 1;
        }
        musicPlayerInfo.currentTimeUs = 0;
        player.play();
    }



    // ********************************************************************************************************
    //                BUTTON PREVIOUS METHOD
    // ********************************************************************************************************
    public void previousButton() {
        player.stop();
        if (musicPlayerInfo.currentTrack == 0) {
            musicPlayerInfo.currentTrack = musicPlayerInfo.list.size() - 1;
        } else {
            musicPlayerInfo.currentTrack = musicPlayerInfo.currentTrack - 1;
        }
        musicPlayerInfo.currentTimeUs = 0;
        player.play();
    }



    // ********************************************************************************************************
    //                ON ITEM CLICKED METHOD
    // ********************************************************************************************************
    public void itemSelected(int i) {
        player.stop();
        musicPlayerInfo.currentTrack = i;
        musicPlayerInfo.currentTimeUs = 0;
        player.play();
    }



    // ********************************************************************************************************
    //                METHOD FOR RELOAD THE PLAYER WHEN OPEN A NEW LIST
    // ********************************************************************************************************
    public void reload() {
        // it is available only when a new list is loaded
        if (musicPlayerInfo.isListChanged) {
            musicPlayerInfo.isListChanged = false;
            Log.d( "MUSIC PLAYER SERVICE", "*****************IS LIST CHANGED = FALSE*****************" );
            stop();
            musicPlayerInfo.currentTrack = 0;
            musicPlayerInfo.currentTimeUs = 0;
        }
    }



    // ********************************************************************************************************
    //               STOP
    // ********************************************************************************************************
    public void stop() {
        player.stop();
    }



    // ********************************************************************************************************
    //               STOP
    // ********************************************************************************************************
    public void play() {
        player.play();
    }



    // ***************************************************************************************************************************************
    //****************************************************************************************************************************************
    //****************************************************************************************************************************************
    //*************************************PLAYER*******************************************************************************************
    // ***************************************************************************************************************************************
    //****************************************************************************************************************************************
    //****************************************************************************************************************************************
    private static class Player {

        private MediaPlayer mediaPlayer = new MediaPlayer();
        private Decoder decoder = new Decoder();
        private int counter1;
        private ScheduledExecutorService scheduler;
        private BluetoothComunication bluetoothComunication = BluetoothComunication.getBluetoothComunication();


        // ********************************************************************************************************
        //                CONSTRUCTOR
        // ********************************************************************************************************
        public Player() {

            //**************************ON COMPLETION LISTENER***********************
            mediaPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    Log.d( "PLAYER", "*****************onCompletion()*****************" );
                    if (musicPlayerInfo.isPlayerPrepared) {
                        stop();
                        if (musicPlayerInfo.list.size() - 1 != musicPlayerInfo.currentTrack) {
                            musicPlayerInfo.currentTrack = musicPlayerInfo.currentTrack + 1;
                        } else {
                            musicPlayerInfo.currentTrack = 0;
                        }
                        musicPlayerInfo.currentTimeUs = 0;
                        play();
                    }

                }
            } );

            //**************************ON PREPARED LISTENER***********************
            mediaPlayer.setOnPreparedListener( new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer playerM) {
                    Log.d( "PLAYER", "*****************onPrepared()*****************" );
                    startAnalyzingThread();
                    playerM.start();
                    mediaPlayer.seekTo( musicPlayerInfo.currentTimeUs / 1000 );
                    musicPlayerInfo.currentTimeUs = (int) Objects.requireNonNull( mediaPlayer.getTimestamp() ).getAnchorMediaTimeUs();
                    musicPlayerInfo.isPlayerPrepared = true;
                }
            } );

            //**************************ON ERROR LISTENER***********************
            mediaPlayer.setOnErrorListener( new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                    stop();
                    return false;
                }
            } );
        }



        // ********************************************************************************************************************
        //                  METHOD TO ANALYZE THE TRACK EVERY X SECONDS
        // ********************************************************************************************************************
        private static double[][] PCMData;
        private static beatRecognitionMADFAT MADFAT;
        private void startAnalyzingThread() {
            Log.d( "PLAYER", "*****************startAnalyzingThread()*****************" );

            decoder.stop();
            decoder.prepare();
            counter1 = 0;
            MADFAT = new beatRecognitionMADFAT();
            PCMData = decoder.decodeSection( musicPlayerInfo.spanForDecoder );
            MADFAT.displayColor( PCMData );

            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate( new Runnable() {
                public void run() {
                    try {
                        if (counter1 == musicPlayerInfo.spanForDecoder - 1) {
                            PCMData = decoder.decodeSection( musicPlayerInfo.spanForDecoder );
                            MADFAT.displayColor( PCMData );
                            counter1 = 0;
                        } else {
                            counter1 = counter1 + 1;
                        }
                        musicPlayerInfo.currentTimeUs = musicPlayerInfo.currentTimeUs + (int) musicPlayerInfo.getFrameDurationUs();
                    } catch (Exception e) {
                        //Log.d( "PLAYER", "*************thread analyzer Exception : ERROR***************" );
                    }
                }
            }, 0, musicPlayerInfo.getFrameDurationUs(), TimeUnit.MICROSECONDS );
        }


        // ********************************************************************************************************************
        //                  START PLAYING THE TRACK (runnable after the service is prepared)
        // ********************************************************************************************************************
        private void play() {
                Log.d( "PLAYER", "*****************play()*****************" );
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource( musicPlayerInfo.getCurrentPath() );
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    Log.d( "PLAYER", "*****************can't prepare the player: ERROR*****************" );
                    e.printStackTrace();
                }
        }



        // ********************************************************************************************************************
        //                        STOP THE REPRODUCTION
        // ********************************************************************************************************************
        private void stop() {
            Log.d( "PLAYER", "*****************stop()*****************" );
            decoder.stop();
            if (musicPlayerInfo.isPlayerPrepared) {
                musicPlayerInfo.currentTimeUs = (int) Objects.requireNonNull( mediaPlayer.getTimestamp() ).getAnchorMediaTimeUs();
                mediaPlayer.stop();
                musicPlayerInfo.isPlayerPrepared = false;
                bluetoothComunication.switchOff();
                if (scheduler != null) {
                    scheduler.shutdown();
                    Log.d( "PLAYER", "*****************Killed Scheduler*****************" );
                }
            }
        }



        // ********************************************************************************************************************
        //                         METHOD RELEASE THE PLAYER WHEN IT IS CALLED (SERVICE CLOSED)
        // ********************************************************************************************************************
        private void release() {
            Log.d( "PLAYER", "*****************release()*****************" );
            stop();
            mediaPlayer.release();
            if (musicPlayerInfo.bluetoothSocket != null) {
                try {
                    bluetoothComunication.switchOff();
                    musicPlayerInfo.bluetoothSocket.close();
                    Log.d( "PLAYER", "*****************Close the Bluetooth Socket*****************" );
                } catch (IOException e) {
                    Log.d( "PLAYER", "*****************Can't close the bluetooth Socket: ERROR*****************" );
                    e.printStackTrace();
                }
            }
        }
    }
}