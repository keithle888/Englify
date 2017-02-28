package teamenglify.englify.ReadingModule;


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

public class ReadSwipeImage extends Fragment {
    String imageUrl;
    NetworkImageView networkImageView;

    public ReadSwipeImage() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_read_swipe_image, container, false);
        Bundle bundle = getArguments();
        imageUrl = bundle.getString("imageUrl");
        networkImageView = (NetworkImageView) v.findViewById(R.id.readNetworkImageView);
        networkImageView.setDefaultImageResId(R.drawable.loadinglogo);
        ImageLoader imageLoader = ImageLoadService.getInstance(MainActivity.getMainActivity().getApplicationContext()).getImageLoader();
        networkImageView.setImageUrl(imageUrl, imageLoader);

        return v;
    }
}
