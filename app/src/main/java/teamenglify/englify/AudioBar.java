package teamenglify.englify;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.Vocab;

import static teamenglify.englify.MainActivity.mainActivity;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioBar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioBar extends Fragment {
    private ImageButton audioReplayButton;
    private ImageButton audioPlayPauseButton;
    private SeekBar audioSeekBar;
    private TextView audioTextView;
    private int position = 99999; /* Used a random value to trigger MediaPlayer to load for the very first time.*/
    private Handler mHandler = new Handler();
    private String audioBarMessageDisplay;
    private MediaPlayer mediaPlayer;
    private ArrayList<String> audioURLList;
    private boolean readyToPlay = false;
    private String audioURL;
    private Object object;

    public AudioBar() {
        // Required empty public constructor
    }

    public static AudioBar newInstance(Object object) {
        AudioBar fragment = new AudioBar();
        Bundle args = new Bundle();
        fragment.object = object;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio_bar, container, false);
        audioReplayButton = (ImageButton) view.findViewById(R.id.audioReplayButton);
        audioPlayPauseButton = (ImageButton) view.findViewById(R.id.audioPlayPauseButton);
        audioSeekBar = (SeekBar) view.findViewById(R.id.audioSeekBar);
        audioTextView = (TextView) view.findViewById(R.id.audioTextView);
        audioBarMessageDisplay = "";
        audioTextView.setText("Audio files loading...");
        mHandler.postDelayed(mBackgroundThread, 100);
        return view;
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mBackgroundThread = new Runnable() {
        public void run() {
            //update seek bar if mediaPlayer is ready.
            if (mediaPlayer != null && readyToPlay) {
                audioSeekBar.setProgress(mediaPlayer.getCurrentPosition() / 100);
            }
            //check whether current page is has changed.
            if (position != mainActivity.currentPage) {
                mHandler.removeCallbacks(mBackgroundThread);
                readyToPlay = false;
                updateAudioBar();
                Log.d("Englify", "Class AudioBar: Method mBackgroundThread(): Change of image detected.");
            } else {
                // Running this thread after 300 milliseconds
                mHandler.postDelayed(this, 300);
            }
        }
    };

    public void updateAudioBar() {
        //reset mediaPlayer if an old one existed, else, create a new one.
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            setListeners();
        } else {
            readyToPlay = false;
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
            mediaPlayer.reset();
        }
        //Set the buttons to be unselectable for the time being, until onPrepared() on mediaPlayer is ready.
        audioPlayPauseButton.setClickable(false);
        audioReplayButton.setClickable(false);
        //draw on the "new"/refresh set of variables from MainActivity
        position = mainActivity.position;
        //set dataSourceURL depending on who called the Audio Bar , "Conversation" or "Vocab"
        if (object instanceof Read) {
            audioURL = ((Read)object).readParts.get(position).audioURL;
        } else if (object instanceof Vocab) {
            audioURL = ((Vocab)object).vocabParts.get(position).audioURL;
        }
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                audioTextView.setText("MediaPlayer Error: " + Integer.toString(i));
                return false;
            }
        });
        audioTextView.setText(R.string.Loading);
        if (audioURL == null) {
            audioTextView.setText(R.string.Audio_File_Not_Found);
        } else {
            try {
                mediaPlayer.setDataSource(audioURL);
                Log.d("Englify", "Class AudioBar: Method updateAudioBar(): Set data source to: " + audioURL+ " and started preparing MediaPlayer");
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.d("Englify", "Class AudioBar: Method updateAudioBar(): Exception caught trying to set " + audioURL + " as source. -> " + e.toString());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mHandler.removeCallbacks(mBackgroundThread);
            mediaPlayer.release();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mBackgroundThread);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mBackgroundThread);
        mHandler.post(mBackgroundThread);
    }

    public void setListeners() {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                audioTextView.setText(audioBarMessageDisplay);
                audioSeekBar.setMax(mediaPlayer.getDuration()/100);
                readyToPlay = true;
                //Set buttons to be clickable once the audiofile is loaded. To avoid MediaPlayer error -38.
                audioPlayPauseButton.setClickable(true);
                audioReplayButton.setClickable(true);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
        });

        audioPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((!mediaPlayer.isPlaying()) && readyToPlay) {
                    mediaPlayer.start();
                    audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    mediaPlayer.pause();
                    audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });

        audioReplayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying() && readyToPlay) {
                    mediaPlayer.pause();
                    audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
                    mediaPlayer.seekTo(0);
                } else if (readyToPlay) {
                    mediaPlayer.seekTo(0);
                    audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
                }
            }
        });

        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) { }
            /**
             * When user starts moving the progress handler
             * */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // remove message Handler from updating progress bar
                mHandler.removeCallbacks(mBackgroundThread);
            }
            /**
             * When user stops moving the progress hanlder
             * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // forward or backward to certain seconds
                mediaPlayer.seekTo(seekBar.getProgress()*100);
                // update timer progress again
                mHandler.post(mBackgroundThread);
            }
        });
    }
}
