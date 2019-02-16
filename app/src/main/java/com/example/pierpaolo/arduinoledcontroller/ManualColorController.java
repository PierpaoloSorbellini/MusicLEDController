package com.example.pierpaolo.arduinoledcontroller;

public class ManualColorController {
    private static MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private static BluetoothComunication bluetoothComunication = BluetoothComunication.getBluetoothComunication();
    private static int[] off = {0,0,0};


    public void setColor(){
        if (musicPlayerInfo.manualControlEnable) {
            int[] color = new int[3];
            color[0] = (int) (musicPlayerInfo.manualColor[0] * musicPlayerInfo.colorIntensity);
            color[1] = (int) (musicPlayerInfo.manualColor[1] * musicPlayerInfo.colorIntensity);
            color[2] = (int) (musicPlayerInfo.manualColor[2] * musicPlayerInfo.colorIntensity);
            bluetoothComunication.write( color );
        }
    }


    public void flash(){
        if ( musicPlayerInfo.manualControlEnable) {
            new Thread( new Runnable() {
                public void run() {
                    while (musicPlayerInfo.isFlash && musicPlayerInfo.manualControlEnable) {
                        int[] color = new int[3];
                        color[0] = (int) (musicPlayerInfo.manualColor[0] * musicPlayerInfo.colorIntensity);
                        color[1] = (int) (musicPlayerInfo.manualColor[1] * musicPlayerInfo.colorIntensity);
                        color[2] = (int) (musicPlayerInfo.manualColor[2] * musicPlayerInfo.colorIntensity);
                        bluetoothComunication.write( color );
                        try {
                            Thread.sleep( musicPlayerInfo.speedFlash );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        bluetoothComunication.write( off );
                        try {
                            Thread.sleep( musicPlayerInfo.speedFlash );
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } ).start();
        }
    }

    public void fade(){
        if (musicPlayerInfo.manualControlEnable) {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    int[] color = new int[3];
                    while (musicPlayerInfo.isFade && musicPlayerInfo.manualControlEnable) {
                        for (int k = 0; k < 6; k++) {
                            switch (k) {
                                case 0:
                                    for (int j = 0; j < 18; j++) {
                                        color[0] = (int) (255*musicPlayerInfo.colorIntensity);
                                        color[1] = (int) (15 * j*musicPlayerInfo.colorIntensity);
                                        color[2] = 0;
                                        if (musicPlayerInfo.manualControlEnable && musicPlayerInfo.isFade) {
                                            bluetoothComunication.write( color );
                                        }
                                        else{
                                            break;
                                        }
                                        try {
                                            Thread.sleep( musicPlayerInfo.speedFade );
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                case 1:
                                    for (int j = 0; j < 18; j++) {
                                        color[0] = (int) ((255 - j * 15)*musicPlayerInfo.colorIntensity);
                                        color[1] = (int) (255*musicPlayerInfo.colorIntensity);
                                        color[2] = 0;
                                        if (musicPlayerInfo.manualControlEnable && musicPlayerInfo.isFade) {
                                            bluetoothComunication.write( color );
                                        }
                                        else{
                                            break;
                                        }
                                        try {
                                            Thread.sleep( musicPlayerInfo.speedFade );
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                case 2:
                                    for (int j = 0; j < 18; j++) {
                                        color[0] = 0;
                                        color[1] = (int) (255*musicPlayerInfo.colorIntensity);
                                        color[2] = (int) (15 * j*musicPlayerInfo.colorIntensity);
                                        if (musicPlayerInfo.manualControlEnable && musicPlayerInfo.isFade) {
                                            bluetoothComunication.write( color );
                                        }
                                        else{
                                            break;
                                        }
                                        try {
                                            Thread.sleep( musicPlayerInfo.speedFade );
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                case 3:
                                    for (int j = 0; j < 18; j++) {
                                        color[0] = 0;
                                        color[1] = (int) ((255 - 15 * j)*musicPlayerInfo.colorIntensity);
                                        color[2] = (int) (255*musicPlayerInfo.colorIntensity);
                                        if (musicPlayerInfo.manualControlEnable && musicPlayerInfo.isFade) {
                                            bluetoothComunication.write( color );
                                        }
                                        else{
                                            break;
                                        }
                                        try {
                                            Thread.sleep( musicPlayerInfo.speedFade );
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                case 4:
                                    for (int j = 0; j < 18; j++) {
                                        color[0] = (int)(j * 15*musicPlayerInfo.colorIntensity);
                                        color[1] = 0;
                                        color[2] = (int) (255*musicPlayerInfo.colorIntensity);
                                        if (musicPlayerInfo.manualControlEnable && musicPlayerInfo.isFade) {
                                            bluetoothComunication.write( color );
                                        }
                                        else{
                                            break;
                                        }
                                        try {
                                            Thread.sleep( musicPlayerInfo.speedFade );
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                                case 5:
                                    for (int j = 0; j < 18; j++) {
                                        color[0] = (int) (255*musicPlayerInfo.colorIntensity);
                                        color[1] = 0;
                                        color[2] = (int) ((255 - j * 15)*musicPlayerInfo.colorIntensity);
                                        if (musicPlayerInfo.manualControlEnable && musicPlayerInfo.isFade) {
                                            bluetoothComunication.write( color );
                                        }
                                        else{
                                            break;
                                        }
                                        try {
                                            Thread.sleep( musicPlayerInfo.speedFade );
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }
            } ).start();
        }
    }

}
