package teamenglify.englify.ReadingModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class ImageFragmentStateAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> imageUrlList = new ArrayList<>();

    public ImageFragmentStateAdapter(FragmentManager fm, ArrayList<String> imageUrlList) {
        super(fm);
        this.imageUrlList = imageUrlList;
    }


    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ReadSwipeImage();
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", imageUrlList.get(i));
        //Log.d("StateAdapter", imageUrlList.get(i));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return imageUrlList.size();
    }
}
