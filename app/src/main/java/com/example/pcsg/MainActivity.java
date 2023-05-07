package com.example.pcsg;

import org.tensorflow.lite.Interpreter;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;



public class MainActivity extends AppCompatActivity {
    private MediaRecorder mediaRecorder;
    private String fileName = null;
    private static final String LOG_TAG = "AudioRecording";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};
    private Button startRecordingButton, stopRecordingButton;
    private TextView resultTextView;


    SoundClassifier soundClassifier;

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            soundClassifier = new SoundClassifier(this);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load model", e);
        }



        startRecordingButton = findViewById(R.id.recordButton);
        stopRecordingButton = findViewById(R.id.stopButton);
        resultTextView = findViewById(R.id.resultText);

        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.wav";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        startRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (permissionToRecordAccepted) {
                    startRecording();
                    Toast.makeText(getApplicationContext(), "Recording started...", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please grant permission to record audio.", Toast.LENGTH_LONG).show();
                }
            }
        });

        stopRecordingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
                Toast.makeText(getApplicationContext(), "Recording stopped...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    private void stopRecording() {
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();
        // Notify user that recording functionality is disabled as the permissions were not granted.
    }

    private void classifySound(float[][][] soundData) {
        String result = soundClassifier.classify(soundData);
        Log.i(TAG, "Classification result: " + result);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultTextView.setText(result);
                resultTextView.setVisibility(View.VISIBLE);
            }
        });
    }




}