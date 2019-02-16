package com.example.pierpaolo.arduinoledcontroller;

import java.io.IOException;
import java.io.OutputStream;

public class BluetoothComunication {

    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    static private BluetoothComunication bluetoothComunication = null;

    private BluetoothComunication(){
    }

    public static synchronized BluetoothComunication getBluetoothComunication() {
        if (bluetoothComunication == null) {
            bluetoothComunication = new BluetoothComunication();
        }
        return bluetoothComunication;
    }

    public void write(int[] color){
        if ( musicPlayerInfo.bluetoothSocket != null) {
            if (musicPlayerInfo.bluetoothSocket.isConnected()) {
                byte b;
                byte[] colorData = new byte[color.length + 1];
                for (int k = 0; k < color.length; k++) {
                    if (color[k] == 254) {
                        color[k] = 255;
                    }
                    if (color[k] > 255) {
                        color[k] = 0;
                    }
                    if (color[k] < 0) {
                        color[k] = 0;
                    }
                    b = (byte) color[k];
                    colorData[k] = b;
                }
                colorData[colorData.length - 1] = (byte) 254;
                try {
                    OutputStream outputStream = musicPlayerInfo.bluetoothSocket.getOutputStream();
                    outputStream.write( colorData );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void switchOff(){
        int[] off = new int[3];
        write( off );
    }

}
