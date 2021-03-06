package com.example.skhad.recordattacks;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.net.rtp.AudioStream;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.View;
import android.content.Intent;
import android.speech.RecognizerIntent;

import java.io.File;
import java.lang.Object;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Locale;
import java.util.ArrayList;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;
import android.net.Uri;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_SPEECH="Audio2Speech";
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private Button at_btn;
    private Button not_at_btn;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    /*fOR AUDIO*/
    private static final String LOG_TAG_AUDIO = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String mFileName = null;
    private static String command = null;
    private static Uri audioUri = null;
    //private RecordButton mRecordButton = null;
    private MediaRecorder mRecorder = null;
    private ImageButton   mPlayButton = null;
    private MediaPlayer   mPlayer = null;
    private static boolean mStartPlaying = true;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public void saveAttack(boolean attack) {
        String atk;
        if (attack) atk="attack_";
        else atk = "not_attack_";
        String timeStamp = new SimpleDateFormat("_YYYY-MM-dd_HH:mm:ss_").format(new Date());
        System.out.println("getFilesDir(): " + getFilesDir());
        if (audioUri!= null) {
                    /*Save sample in attack folder*/
            System.out.println(audioUri.getPath());
            try {
                InputStream input = getContentResolver().openInputStream(audioUri);
                try {
                    File file = new File(mFileName, atk+command+"_"+timeStamp+".amr");
                    OutputStream output = new FileOutputStream(file);
                    try {
                        byte[] buffer = new byte[4 * 1024]; // or other buffer size
                        int read;

                        try {
                            while ((read = input.read(buffer)) != -1) {
                                output.write(buffer, 0, read);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            output.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } finally {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } finally {
                    input.close();
                }
                System.out.println("Saved audio");
            } catch ( IOException a) {
                Log.e(LOG_TAG_AUDIO, "audioURI not found");
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        //TextView tv = (TextView) findViewById(R.id.sample_text);
        //tv.setText(stringFromJNI());

        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.command);
        btnSpeak = (ImageButton) findViewById(R.id.mic);
        at_btn = (Button) findViewById(R.id.attack_btn);
        not_at_btn = (Button) findViewById(R.id.not_attack_btn);

        /*Start speech recognizer/audio recorder*/
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
                Log.i(LOG_SPEECH, "PromptSpeech Done..");
            }
        });

        /*Save audio sample into respective folder*/
        at_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Save audio as attack sample*/
                saveAttack(true);
            }
        });
        not_at_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Save audio as not_attack sample*/
                saveAttack(false);
            }
        });


        // hide the action bar
        //getActionBar().hide();

        /*Initialize audio recording stuff*/
        mFileName = getExternalCacheDir().getAbsolutePath();
        //mFileName += "/audiorecordtest.3gp";
        mPlayButton = (ImageButton) findViewById(R.id.play_btn);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("OnPlay starting..");
                onPlay(mStartPlaying);
                //System.out.println("OnPlay stopped");
                mStartPlaying = !mStartPlaying;
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /*Display google speech input*/
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        //intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
        //        getString(R.string.speech_prompt));
        intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        intent.putExtra("android.speech.extra.GET_AUDIO", true);
        //Start the speech--> Text activity

        try {
            //onRecord(true);
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            //onRecord(false);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /* Receiving speech input */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    /*Get string spoken*/
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));

                    /*Set filename to save audio*/
                    command = result.get(0);

                    /*What's in the bundle?*/
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        System.out.println("Printing all the stuff in te bundle..");
                        for (String key : bundle.keySet()) {
                            Object value = bundle.get(key);
                            System.out.println(String.format("%s %s (%s)", key,
                                    value.toString(), value.getClass().getName()));
                        }
                    }

                    /*Get audio spoken*/
                    audioUri = data.getData();
                    System.out.println("Audio: "+audioUri);
                    /*mPlayer = new MediaPlayer();
                    try {
                        System.out.println("Setting audio source");
                        mPlayer.setDataSource(getApplicationContext(), audioUri);
                        System.out.println("Preparing audio source");
                        mPlayer.prepare();
                        System.out.println("Starting audio source");
                        mPlayer.start();
                        System.out.println("Audio source DONE STARTING");
                    } catch (IOException e) {
                        Log.e(LOG_TAG_AUDIO, "prepare() failed");
                    }
                    System.out.println("Releasing audio source");
                    mPlayer.release();
                    System.out.println("Audio source RELEASED!!");
                    mPlayer = null;*/
                }
                break;
            }

        }
    }

    /*
        Audio Recording stuff....
    * */


    /*pLAY*/
    private void onPlay(boolean start) {
        //startPlaying();
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        /*Start playing*/
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(getApplicationContext(), audioUri);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG_AUDIO, "prepare() failed");
        }
        /*Release the player*/
        //mPlayer.release();
        //mPlayer = null;
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }



}
