package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import teamenglify.englify.MainActivity;


public class VocabFragmentStateAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> imageUrlList = new ArrayList<>();

    public VocabFragmentStateAdapter(FragmentManager fm, ArrayList<String> imageUrlList) {
        super(fm);
        this.imageUrlList = imageUrlList;
    }


    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new VocabSwipeImage();
        Bundle bundle = new Bundle();
        String prefix = "https://s3-ap-southeast-1.amazonaws.com/englify/res/";
        String vocab  = imageUrlList.get(i);
        String [] vocabArr = vocab.split("-");
        String choices = MainActivity.gradeSelected+"/"+MainActivity.lesson+"/Vocabulary/"+vocabArr[0].trim() + ".png";
        bundle.putString("imageUrl", prefix+choices);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return imageUrlList.size();
    }
}
