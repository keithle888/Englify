package teamenglify.englify.ExerciseModule;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.ExerciseChapterPart;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.ReadPart;
import teamenglify.englify.ReadingModule.ReadSwipeImage;

public class ExerciseFragmentStateAdapter extends FragmentStatePagerAdapter {
    private ArrayList<String> imageUrlList = new ArrayList<>();

    public ExerciseFragmentStateAdapter(FragmentManager fm, ExerciseChapter exerciseChapter) {
        super(fm);
        for (ExerciseChapterPart exerciseChapterPart : exerciseChapter.chapterParts) {
            imageUrlList.add(exerciseChapterPart.imageURL);
        }
    }


    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new ExerciseSwipeImage();
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
