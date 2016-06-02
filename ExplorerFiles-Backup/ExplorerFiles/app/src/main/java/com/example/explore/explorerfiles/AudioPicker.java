package com.example.explore.explorerfiles;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class AudioPicker extends AppCompatActivity {

    private static final int REQUEST_RECORD = 0;

    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;

    private static String audioFilePath;
    private static Button stopButton;
    private static Button playButton;
    private static Button recordButton;
    private boolean isRecording = false;
    private long counter = 1l;
    private static int MY_PERMISSIONS_INTERNAL_STORAGE;


    private File recordsFolder;
    private File audioFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_picker);

        recordButton = (Button) findViewById(R.id.recordButton);
        playButton = (Button)findViewById(R.id.playButton);
        stopButton = (Button)findViewById(R.id.stopButton);

        recordsFolder = new File(Environment.getExternalStorageDirectory()+
                File.separator+"ExplorerFiles"+File.separator+"Recordings");

        if(!hasMicrophone()){
            recordButton.setEnabled(false);
            playButton.setEnabled(false);
            stopButton.setEnabled(false);
        }else{
            playButton.setEnabled(false);
            stopButton.setEnabled(false);
        }
        if(!recordsFolder.exists()){
            recordsFolder.mkdirs();
        }

    }

    public String setName(long num){
        return "/myAudio_"+num+".3gp";
    }

    public boolean hasMicrophone(){
        PackageManager pManager = this.getPackageManager();
        return pManager.hasSystemFeature(
                PackageManager.FEATURE_MICROPHONE
        );
    }

    public void recordAudio(View v) throws IOException{
        if(!recordsFolder.exists()){
            System.out.println("The folder does not exist");
            if(!recordsFolder.mkdirs()){
                System.out.println("Cannot create folder");
            }
        }
        do {
            audioFilePath =
                    recordsFolder.getAbsolutePath()
                            + setName(counter++);
            audioFile = new File(audioFilePath);
        } while (audioFile.exists());
        System.out.println(audioFilePath);
        isRecording = true;
        stopButton.setEnabled(true);
        playButton.setEnabled(false);
        recordButton.setEnabled(false);

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)
                ==PackageManager.PERMISSION_GRANTED){
        try{
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        try {
            mediaRecorder.start();
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
        }else{
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD);
        }

    }

    public void stopRecord(View v){
        stopButton.setEnabled(false);
        playButton.setEnabled(true);

        try {
            if (isRecording) {
                recordButton.setEnabled(false);
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;
            } else {
                mediaPlayer.release();
                mediaPlayer = null;
                recordButton.setEnabled(true);
            }
        }catch(IllegalStateException e){
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Saved as "+audioFilePath, Toast.LENGTH_LONG).show();
    }

    public void playAudio(View v) throws IOException{
        playButton.setEnabled(false);
        recordButton.setEnabled(false);
        stopButton.setEnabled(true);

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }catch(IllegalStateException e){
            e.printStackTrace();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
    }

    public void callExplorer (View v){
        if(ActivityCompat.checkSelfPermission(AudioPicker.this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
            ==PackageManager.PERMISSION_GRANTED){

            Intent intent = new Intent(this,ListFilesActivity.class);
            startActivity(intent);
        }
        else{
            ActivityCompat.requestPermissions(AudioPicker.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_INTERNAL_STORAGE);
        }
    }
}
