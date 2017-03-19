package teamenglify.englify.Tutorial;


import android.content.Context;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

public class SwipeTutorial extends PagerAdapter{
    private int [] imageResource = {R.drawable.tutorial1, R.drawable.tutorial2};

    //private Context context;
    private LayoutInflater layoutInflater;

    /*public SwipeTutorial(Context context){
        context = MainActivity.getMainActivity().getApplicationContext();
    }*/


    @Override
    public int getCount() {
        return imageResource.length;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater =  (LayoutInflater) MainActivity.getMainActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.tutorial_swipe,container,false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.tutorialImage);
        imageView.setImageResource(imageResource[position]);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }
}