package com.example.pierpaolo.arduinoledcontroller;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class Decoder {



    // *******************************************************************************************************************
    //                                     CONSTRUCTOR
    // ********************************************************************************************************************
    public Decoder() {
    }



    // ***************************************************************************************************************************************
    //****************************************************************************************************************************************
    //****************************************************************************************************************************************
    //*************************************DECODER*******************************************************************************************
    // ***************************************************************************************************************************************
    //****************************************************************************************************************************************
    //****************************************************************************************************************************************

    private MediaExtractor mediaExtractor = new MediaExtractor();
    private MediaCodec mediaCodec;
    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();


    // **********************************************************************************************************************
    //                STATE METHOD: CONFIGURE THE DECODER AND THE EXTRACTOR (must be called every time a new track has to be played)
    // **********************************************************************************************************************
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void prepare() {

        Log.d( "DECODER", "****************prepare()***************** " );

        if (!musicPlayerInfo.isDecoderPrepared) {

            if ( mediaExtractor == null){
                Log.d( "DECODER", "*****************Media Extractor is null: ERROR***************** " );
                return;
            }

            // Set the path
            try {
                mediaExtractor.setDataSource( musicPlayerInfo.getCurrentPath() );
            } catch (IOException e) {
                Log.d( "DECODER", "*****************Media Extractor can't set the data source: ERROR***************** " );
                e.printStackTrace();
                return;
            }

            // Get the format
            MediaFormat format = mediaExtractor.getTrackFormat( 0 ); // i give directly the path of the song

            // Save information from the format
            String mime = format.getString( MediaFormat.KEY_MIME );
            musicPlayerInfo.numberOfChannel = format.getInteger( MediaFormat.KEY_CHANNEL_COUNT );
            musicPlayerInfo.samplingFrequency = format.getInteger( MediaFormat.KEY_SAMPLE_RATE );
            musicPlayerInfo.durationMs =  (int) ( (double) (format.getLong( MediaFormat.KEY_DURATION ) )/1000);

            // select the track of the extractor and create the mediaCodec of the correct type
            if (mime.startsWith( "audio/" )) {
                mediaExtractor.selectTrack( 0 );
                try {
                    mediaCodec = MediaCodec.createDecoderByType( mime );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaCodec.configure( format, null, null, 0 );
            }

            // Last Check
            if (mediaCodec == null) {
                Log.d( "DECODER", "*****************Media Codec is null: ERROR***************** " );
                return;
            }

            // start Decoder
            mediaCodec.start();

            // do at least on "decode" to store some data in the musicPayerInfo
            musicPlayerInfo.isDecoderPrepared = true;
            short data[][] = null;
            while (data == null) {
                data = decode();
            }

            mediaExtractor.seekTo( musicPlayerInfo.currentTimeUs , MediaExtractor.SEEK_TO_CLOSEST_SYNC );
            Log.d("DECODER","*********************Decoder is prepared*******************");
        }
    }



    // *************************************************************************************************************************
    //                  METHOD TO DECODE A SINGLE FRAME WITH THE MEDIA CODEC
    // *************************************************************************************************************************
    // This method must be call after the method prepare(), it returns the bytebuffer with the decoded information or null if they are not available
    private short[][] decode() {

        // ***********READ INFORMATION FROM THE MP3 FILE AND GIVE IT TO THE DECODER**********
        if (musicPlayerInfo.isDecoderPrepared) {
            // get the index of the input buffer
            int inputIndex = mediaCodec.dequeueInputBuffer( 20000 );
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer( inputIndex );

            // Create the returned bytebuffer
            short data[][] = null;

            // Get Buffer Inf
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            // check if the index is admissible
            if (inputIndex >= 0) {

                // fill the input buffer with information and get the presentation time
                int frameSize = 0;
                if (inputBuffer != null) {
                    frameSize = mediaExtractor.readSampleData( inputBuffer, 0 );
                }
                long inputFrameTimeUs = mediaExtractor.getSampleTime();

                // check the sample-size to determine if the input streaming must end
                if (frameSize >= 0) {
                    // if it's OK give the input buffer at the codec and advance the extractor
                    mediaCodec.queueInputBuffer( inputIndex, 0, frameSize, inputFrameTimeUs, 0 );
                    mediaExtractor.advance();
                } else {
                    // if the size is negative we pass the end of stream signal to the output buffer
                    Log.d( "DECODER", "*****************InputBuffer BUFFER_FLAG_END_OF_STREAM*****************" );
                    mediaCodec.queueInputBuffer( inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM );
                }
            }

            // ************TAKE INFORMATION FROM THE DECODER OUTPUT**************
            // get the index of the output buffer
            int outputIndex = mediaCodec.dequeueOutputBuffer( bufferInfo, 20000 );

            // handle some cases
            switch (outputIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    Log.d( "DECODER", "*****************New format " + mediaCodec.getOutputFormat() + "*****************" );
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    Log.d( "DECODER", "*****************dequeueOutputBuffer timed out!*****************" );
                    break;
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    break;
                default: // default is the case that it runs without any problem

                    // get the returned bytebuffer
                    ByteBuffer outputBuffer = mediaCodec.getOutputBuffer( outputIndex );
                    data = handleNumberOfChannel( outputBuffer );

                    if (outputBuffer != null) {
                        // get the information on the output buffer
                        musicPlayerInfo.outputBufferLimit = outputBuffer.limit();

                        // clear and release the output buffer
                        outputBuffer.clear();
                    }
                    mediaCodec.releaseOutputBuffer( outputIndex, true );
            }

            // All decoded frames have been rendered, we can stop playing now
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d( "DECODER", "*****************OutputBuffer BUFFER_FLAG_END_OF_STREAM*****************" );
            }
            return data;
        }
        else{
            Log.d( "DECODER","*****************Decode function called when decoder is not prepared: ERROR*******************" );
            return null;
        }
    }



    // ********************************************************************************************************************
    //                  METHOD TO DECODE A GIVEN PORTION INDEPENDENTLY FROM THE MAIN PLAYBACK DECODER
    // ********************************************************************************************************************
    // frame number is the number of MP3 frame to decode
    public double[][] decodeSection(int frameNumber) {

        int dim = musicPlayerInfo.getNumberOfSampleForFrame() * frameNumber;
        double[][] data = new double[2][dim];
        int count = 0;

        // for the number of frames to be decoded
        for (int i = 0; i < frameNumber; i++) {
            short[][] newPCM = decode();
            if (newPCM != null) {
                for (int j = 0; j < newPCM[0].length; j ++) {
                    data[0][count] = ((double) newPCM[0][j])/32767  ;
                    data[1][count] = ((double) newPCM[1][j])/32767  ;
                    count = count + 1;
                }
            }
        }

        double max = 0;
        for (int k = 0; k<data[0].length; k++){
            if ( max < data[0][k]){
                max = data[0][k];
            }
        }
        return data;
    }



    // *****************************************************************************************************************************
    //                STATE METHOD: STOP ALL THE DECODING ELEMENT AND REFRESH
    // ******************************************************************************************************************************
    public void stop() {
        Log.d( "DECODER", "*****************stop()******************" );
        if (musicPlayerInfo.isDecoderPrepared) {

            // STOP the MediaExtractor
            mediaExtractor.release();
            mediaExtractor = new MediaExtractor();
            Log.d( "DECODER", "*****************Killed Media Extractor*****************" );

            // STOP the MediaCodec
            mediaCodec.stop();
            mediaCodec.release();
            Log.d( "DECODER", "*****************Killed Media Codec***************** " );

            // Change prepared status
            musicPlayerInfo.isDecoderPrepared = false;
        }
    }



    // ***************************************************************************************************************************************
    //****************************************************************************************************************************************
    //****************************************************************************************************************************************
    //*************************************OTHER METHODS FOR PCM EXTRACTION*******************************************************************
    // ***************************************************************************************************************************************
    //****************************************************************************************************************************************
    //****************************************************************************************************************************************



    // ********************************************************************************************************************
    //                  METHOD TO GET THE SHORT PCM FROM A BYTEBUFFER
    // ********************************************************************************************************************
    private short[] getSamplesForChannel(ByteBuffer outputBuffer, int channelIx) {
        ShortBuffer samples = outputBuffer.order( ByteOrder.nativeOrder() ).asShortBuffer();
        if (channelIx < 0 || channelIx >= musicPlayerInfo.numberOfChannel) {
            return null;
        }
        short[] res = new short[samples.remaining() / musicPlayerInfo.numberOfChannel];
        for (int i = 0; i < res.length; ++i) {
            res[i] = samples.get( i * musicPlayerInfo.numberOfChannel + channelIx );
        }
        return res;
    }



    // ********************************************************************************************************************
    //                  METHOD TO HANDLE DIFFERENT NUMBER OF CHANNELS
    // ********************************************************************************************************************
    private short[][] handleNumberOfChannel(ByteBuffer buffer) {

        // output variable
        short[][] data = new short[2][musicPlayerInfo.getNumberOfSampleForFrame()];

        if ( data.length > 0) {
            if (buffer != null) {
                // Handle different case (MONO/STEREO)
                switch (musicPlayerInfo.numberOfChannel) {
                    case 1:
                        // Audio Mono not supported
                        data = null;
                        break;
                    case 2:
                        // get the PCM
                        data[0] = getSamplesForChannel( buffer, 0 );
                        data[1] = getSamplesForChannel( buffer, 1 );
                        break;
                    default:
                        data = null;
                        Log.d( "DECODER", "*****************Channel not Correctly Defined : ERROR*****************" );
                        break;
                }
            }
        }
        else{
            Log.d( "DECODER", "*****************numberOfSamplesForFrame is 0 : ERROR*****************" );
            data = null;
        }
        return data;
    }
}
