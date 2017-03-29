package teamenglify.englify.Tutorial;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import teamenglify.englify.LoginFragment.LoginFragment;
import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

public class Tutorial extends Fragment {
    ViewPager viewPager;
    SwipeTutorial swipeTutorial;
    //public Drawable [] drawablesList;


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

        final View v = inflater.inflate(R.layout.fragment_tutorial, container, false);
        viewPager = (ViewPager) v.findViewById(R.id.tutorialViewPager);
        swipeTutorial = new SwipeTutorial();
        viewPager.setAdapter(swipeTutorial);

        final ImageButton previousPageBtn = (ImageButton) v.findViewById(R.id.btnLeftPage);
        previousPageBtn.setImageResource(R.drawable.stop);
        previousPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(getItem(-1));
            }
        });


        final ImageButton nextPageBtn = (ImageButton) v.findViewById(R.id.btnRightPage);
        nextPageBtn.setImageResource(R.drawable.right_arrow);
        nextPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(getItem(+1));
            }
        });

        final SwipeTutorial swipeTutorial = new SwipeTutorial();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(viewPager.getCurrentItem()+1==swipeTutorial.getCount()){
                    FrameLayout tutorialLayout = (FrameLayout) v.findViewById(R.id.tutorialLayout);
                    Button gotoMainPage = new Button(getContext());
                    gotoMainPage.setText("Congratulation, you have finished the tutorial. Click me to go to Main Page");
                    gotoMainPage.setTextSize(30);
                    //gotoMainPage.setPadding(0,0,0,1000);
                    tutorialLayout.addView(gotoMainPage);
                    nextPageBtn.setImageResource(R.drawable.stop);
                    gotoMainPage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mainActivity.loadLoginFragment();
                        }
                    });
                }

                if(viewPager.getCurrentItem()>0){
                    previousPageBtn.setImageResource(R.drawable.left_arrow);
                } else {
                    previousPageBtn.setImageResource(R.drawable.stop);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        return v;
    }

    private int getItem(int i){
        return viewPager.getCurrentItem() + i;
    }

}
