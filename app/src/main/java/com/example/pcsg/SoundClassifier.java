package com.example.pcsg;

import android.content.Context;
import org.tensorflow.lite.Interpreter;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.os.ParcelFileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class SoundClassifier extends Activity implements SensorEventListener {

    private Interpreter tflite;
    private SensorManager sensorManager;

    public SoundClassifier(Context context) throws IOException {
        MappedByteBuffer model = loadModelFile(context, "model.tflite");
        tflite = new Interpreter(model);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public String classify(float[][][] input) {
        float[][] output = new float[1][3];
        tflite.run(input, output);
        int index = argmax(output);
        switch (index) {
            case 0:
                return "Dull";
            case 1:
                return "Reasonant";
            case 2:
                return "Tympanic";
            default:
                return "Unknown";
        }
    }


    private MappedByteBuffer loadModelFile(Context context, String fileName) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(fileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private int argmax(float[][] array) {
        int maxIndex = 0;
        float maxVal = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < array[0].length; i++) {
            if (array[0][i] > maxVal) {
                maxVal = array[0][i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // implement your sensor event logic here
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // implement your accuracy changed logic here
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}