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

import static teamenglify.englify.MainActivity.mainActivity;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioBar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioBar extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageButton audioReplayButton;
    private ImageButton audioPlayPauseButton;
    private SeekBar audioSeekBar;
    private TextView audioTextView;
    private int currentPage = 99999; /* Used a random value to trigger MediaPlayer to load for the very first time.*/
    private Handler mHandler = new Handler();
    private String audioBarMessageDisplay;
    private MediaPlayer mediaPlayer;
    private ArrayList<String> audioURLList;
    private boolean readyToPlay = false;

    public AudioBar() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AudioBar.
     */
    // TODO: Rename and change types and number of parameters
    public static AudioBar newInstance(String param1, String param2) {
        AudioBar fragment = new AudioBar();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
        mHandler.postDelayed(mUpdateTimeTask, 100);
        return view;
    }

    /**
     * Background Runnable thread
     * */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            //update seek bar if mediaPlayer is ready.
            if (mediaPlayer != null && readyToPlay) {
                audioSeekBar.setProgress(mediaPlayer.getCurrentPosition() / 100);
            }
            //check whether current page is has changed.
            if (MainActivity.getMainActivity().readyForAudioBarToLoad && currentPage != mainActivity.currentPage) {
                mHandler.removeCallbacks(mUpdateTimeTask);
                readyToPlay = false;
                updateAudioBar();
                Log.d("Audio Bar", "Change of image detected.");
            } else {
                // Running this thread after 100 milliseconds
                mHandler.postDelayed(this, 100);
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
        currentPage = MainActivity.getMainActivity().currentPage;
        //set dataSourceURL depending on who called the Audio Bar , "Conversation" or "Vocab"
        String dataSourceURL = "null";
        if (mParam1.equalsIgnoreCase("Conversation")) {
            audioURLList = MainActivity.getMainActivity().audioConversationURLListing;
            dataSourceURL = audioURLList.get(currentPage);
            audioBarMessageDisplay = "Audio: " + (currentPage + 1);
        } else if (mParam1.equalsIgnoreCase("Vocab")) {
            audioURLList = MainActivity.getMainActivity().audioVocabURLListing;
            dataSourceURL = audioURLList.get(currentPage);
            audioBarMessageDisplay = "Audio: " + MainActivity.getMainActivity().vocabListing.get(currentPage);
        }
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                audioTextView.setText("MediaPlayer Error: " + Integer.toString(i));
                return false;
            }
        });
        audioTextView.setText("Audio files loading...");
        if (dataSourceURL.contains("null")) {
            audioTextView.setText("Audio file not found.");
        } else {
            try {
                mediaPlayer.setDataSource(dataSourceURL);
                Log.d("AudioBar", "Set data source to: " + dataSourceURL + " and started preparing MediaPlayer");
                mediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.d("AudioBar", e.toString() + ", tried to set dataSource as :" + dataSourceURL);
            }
        }
        updateProgressBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mHandler.removeCallbacks(mUpdateTimeTask);
            mediaPlayer.release();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler.removeCallbacks(mUpdateTimeTask);
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Update timer on seekbar
     * */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
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
                mHandler.removeCallbacks(mUpdateTimeTask);
            }
            /**
             * When user stops moving the progress hanlder
             * */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);

                // forward or backward to certain seconds
                mediaPlayer.seekTo(seekBar.getProgress()*100);

                // update timer progress again
                updateProgressBar();
            }
        });
    }
}
