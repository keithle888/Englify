package teamenglify.englify.VocabModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.Model.VocabPart;


public class VocabFragmentStateAdapter extends FragmentStatePagerAdapter {
    private Vocab vocab;
    private ArrayList<String> imageUrlList = new ArrayList<>();

    public VocabFragmentStateAdapter(FragmentManager fm, Vocab vocab) {
        super(fm);
        this.vocab = vocab;
        for (VocabPart vocabPart : vocab.vocabParts) {
            imageUrlList.add(vocabPart.imgURL);
        }
    }


    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new VocabSwipeImage();
        Bundle bundle = new Bundle();
        String vocab  = imageUrlList.get(i);
        bundle.putString("imageUrl", vocab);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getCount() {
        return imageUrlList.size();
    }
}
