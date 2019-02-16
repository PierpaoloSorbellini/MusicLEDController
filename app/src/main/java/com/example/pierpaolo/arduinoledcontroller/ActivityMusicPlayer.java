package com.example.pierpaolo.arduinoledcontroller;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by pierpaolo on 09/03/18.
 */


public class ActivityMusicPlayer extends BaseActivity {

    // Layout object
    private ImageButton playButton;
    private SeekBar timeBar;
    private TextView timeText1;private int itemSelected = 0;
    private TextView timeText2;
    private SeekBar volumeBar;
    private MusicPlayerService musicPlayerService = new MusicPlayerService();
    private Intent playIntent = null;
    private AudioManager audioManager;
    private MusicAdapter musicAdapter;
    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();



    // ********************************************************************************************************
    //                CONNECT THE SERVICE
    // ********************************************************************************************************
    public ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicPlayerService.MusicBinder binder = (MusicPlayerService.MusicBinder) service;
            musicPlayerService = binder.getService();
            Log.d( "ACTIVITY MUSICPLAYER:", "*****************Service Connected*****************" );
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public ActivityMusicPlayer() {

    }


    // ********************************************************************************************************
    //                HANDLE FOCUS
    // ********************************************************************************************************
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    musicPlayerInfo.isAudioFocusObtained = true;
                    Log.d( "ACTIVITY MUSIC PLAYER", "*********************Audio Focus Gain****************" );
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    musicPlayerService.stop();
                    musicPlayerInfo.isAudioFocusObtained = false;
                    Log.d( "ACTIVITY MUSIC PLAYER", "*********************Audio Focus Loss****************" );
                    break;
            }
        }
    };


    // ********************************************************************************************************
    //                                 ON CREATE
    // ********************************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d( "ACTIVITY MUSICPLAYER:", "*****************onCreate*****************" );
        super.onCreate( savedInstanceState );

        //************************Preparation***************************************
        ITEM_SELECTED = 2;
        super.setContentView( R.layout.activity_musicplayer );
        super.changeToolbarTitle( "Music Player" );

        //********************* Set reference to all the view element ***************************
        playButton = findViewById( R.id.musicplayer_play_button );
        ImageButton nextButton = findViewById( R.id.musicplayer_next_button );
        ImageButton previousButton = findViewById( R.id.musicplayer_previous_button );
        timeBar = findViewById( R.id.musicplayer_time_bar );
        timeText1 = findViewById( R.id.musicplayer_time_text1 );
        timeText2 = findViewById( R.id.musicplayer_time_text2 );
        ListView songList = findViewById( R.id.musicplayer_list );
        // ImageView volumeUp = findViewById( R.id.musicplayer_volume_up );
        // ImageView volumeDown = findViewById( R.id.musicplayer_volume_down );
        volumeBar = findViewById( R.id.musicplayer_volume_bar );

        // ************************** Start the service *****************************************
        if (playIntent == null) {
            playIntent = new Intent( this, MusicPlayerService.class );
            bindService( playIntent, musicConnection, BIND_AUTO_CREATE );
            startService( playIntent );
            Log.d( "ACTIVITY MUSICPLAYER:", "*****************Start the Service*****************" );
        }

        // *************************List*************************************
        if (musicPlayerInfo.list.size() != 0) {
            musicPlayerInfo.isListLoaded = true;
            Log.d( "STATUS:", "*****************LIST IS LOADED = TRUE*****************" );
            musicPlayerService.reload();
        } else {
            musicPlayerInfo.isListLoaded = false;
            Log.d( "STATUS:", "*****************LIST IS LOADED = FALSE*****************" );
        }
        musicAdapter = new MusicAdapter( this );
        songList.setAdapter( musicAdapter );
        songList.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                musicPlayerService.itemSelected( position );
            }
        } );

        // **************************** Play Button ************************************************
        playButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                if (musicPlayerInfo.isListLoaded) {
                    musicPlayerService.playButton();
                    setViewObjects();
                }
            }
        } );

        //********************************* Next Button*********************************************
        nextButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                if (musicPlayerInfo.isListLoaded) {
                    musicPlayerService.nextButton();
                    setViewObjects();
                }
            }
        } );

        //***************************** Previous Button *********************************************
        previousButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                if (musicPlayerInfo.isListLoaded) {
                    musicPlayerService.previousButton();
                    setViewObjects();
                }
            }
        } );

        // *************************************Time Bar *******************************************
        timeBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b && musicPlayerInfo.isListLoaded) {
                    timeBar.setProgress( i );
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (musicPlayerInfo.isListLoaded) {
                    musicPlayerService.stop();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (musicPlayerInfo.isListLoaded) {
                    musicPlayerInfo.currentTimeUs = seekBar.getProgress() * 1000;
                    musicPlayerService.play();
                }
            }
        } );

        //*********************************** Volume Bar********************************************
        audioManager = (AudioManager) getSystemService( Context.AUDIO_SERVICE );
        if (audioManager != null) {
            volumeBar.setMax( audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC ) );
            volumeBar.setProgress( audioManager.getStreamVolume( AudioManager.STREAM_MUSIC ) );
        }
        volumeBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                audioManager.setStreamVolume( AudioManager.STREAM_MUSIC, i, 0 );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        } );
    }


    // ********************************************************************************************************
    //                                         ON START
    // ********************************************************************************************************
    @Override
    protected void onStart() {
        Log.d( "ACTIVITY MUSICPLAYER:", "*****************onStart*****************" );
        super.onStart();

        // Upload main information when restarting after a delay
        Runnable r = new Runnable() {
            @Override
            public void run() {
                // Upload Information
                if (musicPlayerInfo.isPlayerPrepared) {
                    timeBar.setMax( musicPlayerInfo.durationMs );
                    timeBar.setProgress( musicPlayerInfo.currentTimeUs / 1000 );
                    if (musicPlayerInfo.currentTimeUs / 1000 > 1) {
                        timeBar.setProgress( musicPlayerInfo.currentTimeUs / 1000 );
                        String text1 = createTimeLabel( musicPlayerInfo.currentTimeUs / 1000 );
                        timeText1.setText( text1 );
                        String text2 = createTimeLabel( musicPlayerInfo.durationMs );
                        timeText2.setText( text2 );
                    }
                }
                setTimeObjects( true );
            }
        };
        Handler h = new Handler();
        h.postDelayed( r, 100 );

        //***********************Handle Audio Focus**************************************
        if (!musicPlayerInfo.isAudioFocusObtained) {
            audioManager = (AudioManager) getSystemService( Context.AUDIO_SERVICE );
            int result = 0;
            if (audioManager != null) {
                result = audioManager.requestAudioFocus( afChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN );
            }
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                musicPlayerInfo.isAudioFocusObtained = true;
                Log.d( "ACTIVITY MUSIC PLAYER", "*********************Audio Focus Request Granted****************" );
            }
        }
    }


    // ********************************************************************************************************
    //                                 ON PAUSE
    // ********************************************************************************************************
    @Override
    protected void onPause() {
        Log.d( "ACTIVITY MUSICPLAYER:", "*****************onPause*****************" );
        setTimeObjects( false );
        super.onPause();
    }


    // ********************************************************************************************************
    //                                 ON DESTROY
    // ********************************************************************************************************
    @Override
    protected void onDestroy() {
        Log.d( "ACTIVITY MUSICPLAYER:", "*****************onDestroy*****************" );
        stopService( playIntent );
        musicPlayerService = null;
        audioManager.abandonAudioFocus( afChangeListener );
        musicPlayerInfo.isAudioFocusObtained = false;
        Log.d( "ACTIVITY MUSIC PLAYER", "*********************Audio Focus Abbandoned****************" );
        super.onDestroy();
    }


    // ********************************************************************************************************
    //                METHOD FOR TIME OBJECT REFRESH
    // ********************************************************************************************************
    public void setTimeObjects(final boolean killThread) {
        @SuppressLint("HandlerLeak") final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                if (timeBar.getMax() != musicPlayerInfo.durationMs) {
                    timeBar.setMax( musicPlayerInfo.durationMs );
                }

                setViewObjects();

                int currentPosition = msg.what;
                if (currentPosition > 1 && currentPosition < musicPlayerInfo.durationMs) {
                    timeBar.setProgress( currentPosition );
                    String text1 = createTimeLabel( currentPosition );
                    timeText1.setText( text1 );
                    String text2 = createTimeLabel( musicPlayerInfo.durationMs );
                    timeText2.setText( text2 );
                }
            }
        };

        new Thread( new Runnable() {
            @Override
            public void run() {
                while (killThread) {
                    Message msg = new Message();
                    msg.what = musicPlayerInfo.currentTimeUs / 1000;
                    handler.sendMessage( msg );
                    try {
                        Thread.sleep( 500 );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } ).start();
    }


    // ********************************************************************************************************
    //                METHOD FOR THE PLAY BUTTON AND THE ITEM SELECTION CHANGE IN THE VIEW
    // ********************************************************************************************************
    public void setViewObjects() {
        if (musicPlayerInfo.isPlayerPrepared) {
            playButton.setImageResource( R.drawable.icon_pause );
        } else {
            playButton.setImageResource( R.drawable.icon_play );
        }
        if (musicPlayerInfo.currentTrack != itemSelected) {
            musicAdapter.notifyDataSetChanged();
            itemSelected = musicPlayerInfo.currentTrack;
        }
    }


    // ********************************************************************************************************
    //                METHOD FOR THE TIME LABEL //TODO verify if this method can be deleted
    // ********************************************************************************************************
    public String createTimeLabel(int i) {
        String timeLabel;
        int min = i / 1000 / 60;
        int sec = i / 1000 % 60;
        timeLabel = min + ":";
        if (sec < 10) timeLabel += "0";
        timeLabel += sec;
        return timeLabel;
    }


    // ********************************************************************************************************
    //                METHOD FOR THE VOLUME BUTTON
    // ********************************************************************************************************
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    volumeBar.setProgress( audioManager.getStreamVolume( AudioManager.STREAM_MUSIC ) );
                }
                return super.dispatchKeyEvent( event );
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    volumeBar.setProgress( audioManager.getStreamVolume( AudioManager.STREAM_MUSIC ) );
                }
                return super.dispatchKeyEvent( event );
            default:
                return super.dispatchKeyEvent( event );
        }
    }
}