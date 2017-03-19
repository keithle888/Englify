package teamenglify.englify.Tutorial;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Tutorial#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Tutorial extends Fragment {
    ViewPager viewPager;


    public Tutorial() {
        // Required empty public constructor
    }

    public static Tutorial newInstance() {
        Tutorial fragment = new Tutorial();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_tutorial, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.tutorialViewPager);

        Resources res = MainActivity.getMainActivity().getResources();
        //res.getDrawable(res.getIdentifier(R.mipmap.screenshot1.));

        String [] tutorialRes= getResources().getStringArray(R.array.tutorial);
        Log.d("Tutorial", tutorialRes[0]);
        Log.d("Tutorial", tutorialRes[1]);
        Log.d("Tutorial", tutorialRes[2]);
        ImageView imageView = (ImageView) v.findViewById(R.id.imagetemptutorial);
        for (int j = 1; j < 3; j++) {
            Drawable drawable = getResources().getDrawable(getResources()
                    .getIdentifier("screenshot"+j, "drawable", MainActivity.getMainActivity().getPackageName()));
            Log.d("tutorial", drawable.toString());
        }

        return v;
    }

}
