package teamenglify.englify;

import android.content.ContentProvider;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.Vocab;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioBar#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioBar extends Fragment {
    private ImageButton audioPlayPauseButton;
    private int position = 99999; /* Used a random value to trigger MediaPlayer to load for the very first time.*/
    private MediaPlayer mediaPlayer;
    private boolean readyToPlay = false;
    private String audioURL;
    private Object object;
    private FileInputStream fis;

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
        audioPlayPauseButton = (ImageButton) view.findViewById(R.id.audioPlayPauseButton);
        setupAudioBar();
        return view;
    }

    public void setupAudioBar() {
        //reset mediaPlayer if an old one existed, else, create a new one.
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        } else {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
        }
        mediaPlayer.reset();
        setListeners();
        readyToPlay = false;
        setAudioTrack(0);
    }

    public void setAudioTrack(int pageNumber) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        }
        mediaPlayer.reset();
        setListeners();
        try {
            fis = LocalSave.loadAudio(getDataSourceFromObject(pageNumber));
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    public void setListeners() {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
            readyToPlay = true;
            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception e) {
                    Log.d(mainActivity.getString(R.string.app_name), "Class AudioBar: Method setListeners(): Caught Exception -> " + e.toString());
                }
            }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(0);
                audioPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                audioPlayPauseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(mainActivity, R.string.Audio_Player_Loading_Error, Toast.LENGTH_LONG).show();
                    }
                });
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (Exception e) {
                        Log.d(mainActivity.getString(R.string.app_name), "Class AudioBar: Method setListeners(): Caught Exception -> " + e.toString());
                    }
                }
                return false;
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
    }

    public String getDataSourceFromObject(int position) {
        String toReturn = null;
        if (object instanceof Read) {
            Read read =  (Read)object;
            if (read.readParts != null && read.readParts.size() != 0) {
                toReturn = read.readParts.get(position).audioURL;
            }
        } else if (object instanceof Vocab) {
            Vocab vocab =  (Vocab)object;
            if (vocab.vocabParts != null && vocab.vocabParts.size() != 0) {
                toReturn = vocab.vocabParts.get(position).audioURL;
            }
        } else if (object instanceof ExerciseChapter) {
            ExerciseChapter exerciseChapter = (ExerciseChapter)object;
            if (exerciseChapter.chapterParts != null && exerciseChapter.chapterParts.size() != 0) {
                toReturn = exerciseChapter.chapterParts.get(position).audioURL;
            }
        }
        return toReturn;
    }
}
