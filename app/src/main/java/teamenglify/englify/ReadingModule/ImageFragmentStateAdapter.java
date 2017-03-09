package teamenglify.englify.ReadingModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.ReadPart;

public class ImageFragmentStateAdapter extends FragmentStatePagerAdapter {
    private Read read;
    private ArrayList<String> imageUrlList = new ArrayList<>();

    public ImageFragmentStateAdapter(FragmentManager fm, Read read) {
        super(fm);
        this.read = read;
        for (ReadPart readPart : read.readParts) {
            imageUrlList.add(readPart.imgURL);
        }
    }


    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ReadSwipeImage();
        Bundle bundle = new Bundle();
        bundle.putString("imageUrl", imageUrlList.get(i));
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return imageUrlList.size();
    }
}
