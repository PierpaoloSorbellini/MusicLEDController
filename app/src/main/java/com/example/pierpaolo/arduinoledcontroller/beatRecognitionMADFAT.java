package com.example.pierpaolo.arduinoledcontroller;

import java.io.IOException;
import java.io.OutputStream;

class beatRecognitionMADFAT {

    private static int[][] beat;
    private static double[][] band;
    private static double[][] avg;
    private static double[][] derivate;
    private static double[] thr;
    private static double[] filterBandValues;
    private static double[] filterDerivateValues;
    private static double[] thrMemory;
    private static double[] thrWeight;
    private static double[] flashFilterNPoint;
    private static double[] flashFilterDistance;
    private static int timeForBackgroundSound;
    private static int fs;
    private static int Nbands;
    private static int beatLength;
    private static int bandLength;
    private MusicPlayerInfo musicPlayerInfo = MusicPlayerInfo.getMusicPlayerInfo();
    private BluetoothComunication bluetoothComunication = BluetoothComunication.getBluetoothComunication();

    beatRecognitionMADFAT() {
        fs = 44100;
        Nbands = 8;
        bandLength = 30;
        beatLength = 50;
        beat = new int[Nbands][beatLength];
        band = new double[Nbands][bandLength];
        avg = new double[Nbands][bandLength];
        derivate = new double[Nbands][bandLength];
        thr = new double[]{10, 40, 5, 2, 1, 0.5, 2, 0.5};
        filterBandValues = new double[]{110, 40, 30, 3, 1, 0.5, 8, 0.3};
        filterDerivateValues = new double[]{20, 5, 5, 1, 0.03, 0.05, 0.3, 0.04};
        thrWeight = new double[]{0.2, 0.6, 0.5, 0.5, 0.5, 0.5, 0.6, 0.6};
        thrMemory = new double[]{5, 5, 5, 5, 5, 5, 5, 5};
        flashFilterNPoint = new double[]{7, 6, 6, 6, 3, 4, 5, 7};
        flashFilterDistance = new double[]{3, 3, 3, 3, 6, 5, 4, 3};
        timeForBackgroundSound = 40;
        timeForBackgroundSound = 30;
    }

    public void displayColor(double[][] PCMData) {

        // COMPUTE FOURIER TRANSFORM
        double[] spectrum1 = FastFourierTransform( vectorMean( PCMData[0], PCMData[1] ) );
        double[] spectrum2 = FastFourierTransform( vectorDifference( PCMData[0], PCMData[1] ) );
        double fbin = fs / PCMData[0].length;
        double[] frequency = new double[spectrum1.length];
        for (int k = 0; k < spectrum1.length; k++) {
            frequency[k] = k * fbin;
        }

        // ANALYZE
        analyze( vectorDifference(spectrum1,spectrum2), vectorAbs(spectrum2), frequency );
        int[] color = colorAssignment( antiFlashFilter() );

        if (musicPlayerInfo.bluetoothSocket != null) {
            // if device is connected send the information to the controller
            if (musicPlayerInfo.bluetoothSocket.isConnected() && musicPlayerInfo.musicControlEnable) {
                bluetoothComunication.write( color );
            }
        }
    }




    private void analyze(double[] spectrum1, double[] spectrum2, double[] frequency) {


        // TIME SHIFTING
        for (int k = 0 ; k < Nbands ; k ++) {
            System.arraycopy( band[k], 0, band[k], 1, bandLength - 1 );
            System.arraycopy( derivate[k], 0, derivate[k], 1, bandLength - 1 );
            System.arraycopy( avg[k], 0, avg[k], 1, bandLength - 1 );
            System.arraycopy( beat[k], 0, beat[k], 1, beatLength - 1 );
            beat[k][0] = 0;
        }


        // COMPUTING AVG
        avg[0][0] = bandMean( spectrum1, frequency, 0, 160 );
        avg[1][0] = bandMean( spectrum1, frequency, 160, 400 );
        avg[2][0] = bandMean( spectrum1, frequency, 400, 1000 );
        avg[3][0] = bandMean( spectrum1, frequency, 1000, 2500 );
        avg[4][0] = bandMean( spectrum1, frequency, 2500, 6500 );
        avg[5][0] = bandMean( spectrum1, frequency, 6500, fs );
        avg[6][0] = bandMean( spectrum2, frequency, 0, 500 );
        avg[7][0] = bandMean( spectrum2, frequency, 500, fs );


        // COMPUTING BAND
        band[0][0] = avg[0][0] - mean( getRow( avg, 0 ) );
        band[1][0] = avg[1][0] - mean( getRow( avg, 1 ) );
        band[2][0] = avg[2][0] - mean( getRow( avg, 2 ) );
        band[3][0] = avg[3][0] - mean( getRow( avg, 3 ) );
        band[4][0] = avg[4][0] - mean( getRow( avg, 4 ) );
        band[5][0] = avg[5][0] - mean( getRow( avg, 5 ) );
        band[6][0] = avg[6][0] - mean( getRow( avg, 6 ) );
        band[7][0] = avg[7][0] - mean( getRow( avg, 7 ) );


        // MAIN CYCLE
        for (int k = 0; k < Nbands; k++) {

            // FILTERING BAND
            if (band[k][0] > filterBandValues[k]) {
                band[k][0] = band[k][0] - filterBandValues[k];
            } else {
                band[k][0] = 0;
            }

            // COMPUTE DERIVATE
            derivate[k][0] = band[k][0] - band[k][1];

            // FILTERING DERIVATE
            if (derivate[k][0] > -filterDerivateValues[k] && derivate[k][0] < filterDerivateValues[k]) {
                derivate[k][0] = 0;
            }

            // AUTOMATIC THRESHOLDING
            int cnt = 0;
            double sum = 0;
            for (int j = 0; j < thrMemory[k]; j++) {
                if (derivate[k][j] < 0 && derivate[k][j + 1] > 0) {
                    cnt = cnt + 1;
                    sum = sum + band[k][j + 1];
                }
            }
            if (cnt > 0) {
                thr[k] = (sum / cnt) * thrWeight[k];
            }

        }


        // BEAT 0
        if (band[0][0] > thr[0]) {
            beat[0][0] = 1;
            band[1][0] = 0;
            band[2][0] = 0;
            band[3][0] = 0;
            band[4][0] = 0;
            band[5][0] = 0;
            derivate[1][0] = 0;
            derivate[2][0] = 0;
            derivate[3][0] = 0;
            derivate[4][0] = 0;
            derivate[5][0] = 0;
            band[1][1] = 0;
            derivate[1][1] = 0;
        }


        // BEAT 1
        if (band[1][1] > thr[1]) {
            beat[1][1] = 1;
            band[2][1] = 0;
            band[3][1] = 0;
            band[4][1] = 0;
            band[5][1] = 0;
            derivate[2][1] = 0;
            derivate[3][1] = 0;
            derivate[4][1] = 0;
            derivate[5][1] = 0;
        }


        // BEAT 2
        if (band[2][1] > thr[2]) {
            beat[2][1] = 1;
            band[3][1] = 0;
            band[4][1] = 0;
            band[5][1] = 0;
            derivate[3][1] = 0;
            derivate[4][1] = 0;
            derivate[5][1] = 0;
        }


        // BEAT 3
        if (band[3][1] > thr[3]) {
            beat[3][1] = 1;
            band[4][1] = 0;
            band[5][1] = 0;
            derivate[4][1] = 0;
            derivate[5][1] = 0;
        }


        // BEAT 4
        if (band[4][1] > thr[4]) {
            beat[4][1] = 1;
            band[5][1] = 0;
            derivate[5][1] = 0;
        }


        // BEAT 5
        if (band[5][1] > thr[5]) {
            beat[4][1] = 1;
        }


        // BEAT 6
        if (band[6][0] > thr[6] && lastBeatDetected()) {
            beat[6][0] = 1;
            band[7][0] = 0;
            band[7][1] = 0;
            derivate[7][0] = 0;
            derivate[7][1] = 0;
        }


        // BEAT 6
        if (band[7][0] > thr[7] && lastBeatDetected()) {
            beat[7][0] = 1;
        }


    }


    private double[] vectorDifference(double vect1[], double vect2[]) {
        double[] result = new double[vect1.length];
        for (int k = 0; k < vect1.length; k++) {
            result[k] = vect1[k] - vect2[k];
        }
        return result;
    }


    private double[] vectorMean(double vect1[], double vect2[]) {
        double[] result = new double[vect1.length];
        for (int k = 0; k < vect1.length; k++) {
            result[k] = (vect1[k] + vect2[k])/2;
        }
        return result;
    }

    private double[] vectorSum(double vect1[], double vect2[]) {
        double[] result = new double[vect1.length];
        for (int k = 0; k < vect1.length; k++) {
            result[k] = vect1[k] + vect2[k];
        }
        return result;
    }

    private double[] vectorAbs(double vect1[]) {
        double[] result = new double[vect1.length];
        for (int k = 0; k < vect1.length; k++) {
            result[k] = Math.abs(vect1[k]);
        }
        return result;
    }


    private double bandMean(double spec[], double[] freq, double fstart, double fstop) {
        double result;
        int cnt = 0;
        double sum = 0;
        int numberOfElements = 0;
        while (cnt < freq.length) {
            if (freq[cnt] < fstart) {
                cnt = cnt + 1;
            } else if (freq[cnt] >= fstart && freq[cnt] <= fstop) {
                sum = sum + spec[cnt];
                cnt = cnt + 1;
                numberOfElements = numberOfElements + 1;
            } else if (freq[cnt] > fstop) {
                break;
            }
        }
        result = sum / numberOfElements;
        return result;
    }


    private double mean(double vect[]) {
        double result;
        double sum = 0;
        for (double aVect : vect) {
            sum = sum + aVect;
        }
        result = sum / vect.length;
        return result;
    }


    private double[] getRow(double vect[][], int rowNumber) {
        double[] result = new double[vect[rowNumber].length];
        for (int k = 0; k < vect[rowNumber].length; k++) {
            result[k] = vect[rowNumber][k];
        }
        return result;
    }

    private int[] getRow(int vect[][], int rowNumber) {
        int[] result = new int[vect[rowNumber].length];
        for (int k = 0; k < vect[rowNumber].length; k++) {
            result[k] = vect[rowNumber][k];
        }
        return result;
    }


    private boolean lastBeatDetected() {
        int sum = 0;
        for (int k = 0; k < Nbands - 2; k++) {
            for (int j = 0; j < timeForBackgroundSound; j++) {
                sum = sum + beat[k][j];
            }
        }
        return sum <= 0;
    }


    private int[] antiFlashFilter() {
        int[] beatFiltered = new int[Nbands];
        for (int k = 0; k < Nbands; k++) {
            double[] temp = dist( getRow( beat, k ) );
            if (temp[0] > flashFilterNPoint[k] && temp[1] < flashFilterDistance[k]) {
                beatFiltered[k] = 0;
            } else {
                beatFiltered[k] = beat[k][1];
            }
        }
        return beatFiltered;
    }


    private double[] dist(int[] vect) {
        int point1 = 0;
        int point2 = 0;
        double[] result = new double[2];
        double sum = 0;
        int d;
        int cnt = 0;
        for (int k = 0; k < vect.length; k++) {
            if (vect[k] == 1) {
                if (point1 == 0) {
                    point1 = k;
                } else {
                    point2 = k;
                    d = point2 - point1 - 1;
                    if (d > 0 && d < 10) {
                        sum = sum + d;
                        cnt = cnt + 1;
                    }
                    point1 = point2;
                }
            }
        }
        if (point2 == 0) {
            result[0] = 0;
            result[1] = 0;
        } else {
            result[0] = cnt;
            result[1] = sum / cnt;
        }
        return result;
    }


    // ********************************************************************************************************************
    //                         FAST FOURIER TRANSFORM
    // ********************************************************************************************************************
    private double[] FastFourierTransform(double[] vect) {

        double[] data = null;
        if (vect != null) {

            // data to store
            double[] dataReal = new double[vect.length];
            double[] dataImag = new double[vect.length];
            data = new double[vect.length / 2];

            // translate short into double
            for (int i = 0; i < dataReal.length; i++) {
                dataReal[i] = vect[i];
                dataImag[i] = 0;
            }

            // FFT
            Fft.transformBluestein( dataReal, dataImag );

            // compute the magnitude and allocate the new vector without repetition
            for (int i = 0; i < data.length; i++) {
                data[i] = Math.sqrt( dataImag[i] * dataImag[i] + dataReal[i] * dataReal[i] );
            }
        }
        return data;
    }


    private int cnt = 0;
    private int currentBeatDetected = 0;
    private int[] colorAssignment(int[] b) {
        int[] color = new int[] {0,0,0,0,0,0};
        if (b[0] == 1) {
            currentBeatDetected = 0;
            cnt = 0;
            color = new int[] {0,0,0,255,0,0};
        } else if (b[1] == 1) {
            currentBeatDetected = 1;
            cnt = 0;
            color = new int[] {0,0,0,255,50,0};
        } else if (b[2] == 1) {
            cnt = 0;
            currentBeatDetected = 2;
            color = new int[] {0,0,0,0,0,255};
        } else if (b[3] == 1) {
            cnt = 0;
            currentBeatDetected = 3;
            color = new int[] {0,0,0,255,255,255};
        } else if (b[4] == 1) {
            cnt = 0;
            currentBeatDetected = 4;
            color = new int[] {0,0,0,255,180,0};
        } else if (b[5] == 1) {
            cnt = 0;
            currentBeatDetected = 5;
            color = new int[] {0,0,0,255,100,0};
        } else if (b[6] == 1) {
            cnt = 0;
            currentBeatDetected = 6;
            color = new int[] {0,0,0,0,255,0};
        } else if (b[7] == 1) {
            cnt = 0;
            currentBeatDetected = 7;
            color = new int[] {0,0,0,0,255,255};
        } else {
            switch (currentBeatDetected) {
                case 0:
                    switch (cnt) {
                        case 0:
                            color = new int[] {255,0,0,150,0,20};
                            break;
                        case 1:
                            color = new int[] {180,0,20,80,0,20};
                            break;
                        case 2:
                            color = new int[] {80,0,20,70,0,10};
                            break;
                        case 3:
                            color = new int[] {70,0,10,50,0,10};
                            break;
                        case 4:
                            color = new int[] {50,0,10,30,0,10};
                            break;
                        case 5:
                            color = new int[] {30,0,10,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
                case 1:
                    switch (cnt) {
                        case 0:
                            color = new int[] {250,0,50,100,0,20};
                            break;
                        case 1:
                            color = new int[] {100,0,50,70,0,30};
                            break;
                        case 2:
                            color = new int[] {70,0,30,50,0,10};
                            break;
                        case 3:
                            color = new int[] {50,0,10,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
                case 2:
                    switch (cnt) {
                        case 0:
                            color = new int[] {0,0,255,0,0,100};
                            break;
                        case 1:
                            color = new int[] {0,0,100,0,10,80};
                            break;
                        case 2:
                            color = new int[] {0,10,50,0,10,50};
                            break;
                        case 3:
                            color = new int[] {0,10,50,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
                case 3:
                    switch (cnt) {
                        case 0:
                            color = new int[] {255,50,0,100,20,0};
                            break;
                        case 1:
                            color = new int[] {100,20,0,80,20,0};
                            break;
                        case 2:
                            color = new int[] {80,20,0,50,10,0};
                            break;
                        case 3:
                            color = new int[] {50,10,0,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
                case 4:
                    switch (cnt) {
                        case 0:
                            color = new int[] {255,180,0,100,50,0};
                            break;
                        case 1:
                            color = new int[] {100,50,0,50,10,0};
                            break;
                        case 2:
                            color = new int[] {50,10,0,0,0,0};
                            break;
                        case 3:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                        case 4:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
                case 5:
                    switch (cnt) {
                        case 0:
                            color = new int[] {255,0,100,230,0,150};
                            break;
                        case 1:
                            color = new int[] {230,0,150,150,0,120};
                            break;
                        case 2:
                            color = new int[] {150,0,120,100,0,80};
                            break;
                        case 3:
                            color = new int[] {100,0,80,50,0,50};
                            break;
                        case 4:
                            color = new int[] {50,0,50,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
                case 6:
                    switch (cnt) {
                        case 0:
                            color = new int[] {0,255,0,0,200,0};
                            break;
                        case 1:
                            color = new int[] {0,200,0,0,150,0};
                            break;
                        case 2:
                            color = new int[] {0,150,0,0,100,0};
                            break;
                        case 3:
                            color = new int[] {0,100,0,0,50,0};
                            break;
                        case 4:
                            color = new int[] {0,50,0,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
                case 7:
                    switch (cnt) {
                        case 0:
                            color = new int[] {0,255,255,0,200,200};
                            break;
                        case 1:
                            color = new int[] {0,200,200,50,180,180};
                            break;
                        case 2:
                            color = new int[] {50,180,180,50,100,100};
                            break;
                        case 3:
                            color = new int[] {50,100,100,50,50,50};
                            break;
                        case 4:
                            color = new int[] {50,50,50,0,0,0};
                            break;
                        case 5:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                        default:
                            color = new int[] {0,0,0,0,0,0};
                            break;
                    }
                    cnt = cnt + 1;
                    break;
            }
        }
        return color;
    }

}

