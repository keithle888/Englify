package teamenglify.englify.ReadingModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import teamenglify.englify.LocalSave;
import teamenglify.englify.R;

public class ReadSwipeImage extends Fragment {
    String imageUrl;
    ImageView imageView;

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
        imageView = (ImageView) v.findViewById(R.id.readImageView);
        Glide.with(this)
                .load(LocalSave.loadImage(imageUrl))
                .into(imageView);

        return v;
    }
}
