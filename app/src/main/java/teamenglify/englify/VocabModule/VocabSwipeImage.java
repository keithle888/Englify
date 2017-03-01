package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import teamenglify.englify.DataService.ImageLoadService;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link VocabSwipeImage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VocabSwipeImage extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    String imageUrl;
    NetworkImageView networkImageView;

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
        imageUrl = bundle.getString("imageUrl").trim();
        networkImageView = (NetworkImageView) v.findViewById(R.id.vocabNetworkImageView);
        networkImageView.setDefaultImageResId(R.drawable.loadinglogo);
        ImageLoader imageLoader = ImageLoadService.getInstance(MainActivity.getMainActivity().getApplicationContext()).getImageLoader();
        networkImageView.setImageUrl(imageUrl, imageLoader);

        return v;
    }

}
