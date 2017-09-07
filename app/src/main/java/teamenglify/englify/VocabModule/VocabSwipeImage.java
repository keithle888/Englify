package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import teamenglify.englify.DataService.LocalSave;
import teamenglify.englify.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VocabSwipeImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VocabSwipeImage extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = VocabSwipeImage.class.getSimpleName();
    private String mParam1;
    private String mParam2;
    String imageUrl;
    ImageView imageView;

    public VocabSwipeImage() {
        // Required empty public constructor
    }

    public static VocabSwipeImage newInstance(String param1, String param2) {
        VocabSwipeImage fragment = new VocabSwipeImage();
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
        View v = inflater.inflate(R.layout.fragment_vocab_swipe_image, container, false);
        Bundle bundle = getArguments();
        imageUrl = bundle.getString("imageUrl");
        imageView = (ImageView) v.findViewById(R.id.vocabImageView);
        Log.d(TAG,"Loading image from URL: " + imageUrl);
        Glide.with(this)
                .load(LocalSave.loadImage(imageUrl))
                .placeholder(R.drawable.logonocontent)
                .fitCenter()
                .into(imageView);
        return v;
    }

}
