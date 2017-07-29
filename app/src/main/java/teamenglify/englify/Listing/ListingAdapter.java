package teamenglify.englify.Listing;

import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;


import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Exercise;
import teamenglify.englify.Model.ExerciseChapter;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.Model.VocabPart;
import teamenglify.englify.R;
import teamenglify.englify.ReadingModule.ReadImage;
import teamenglify.englify.ReadingModule.ReadingModule;
import teamenglify.englify.VocabModule.VocabImage;

import static teamenglify.englify.MainActivity.bucketName;
import static teamenglify.englify.MainActivity.mainActivity;

public class ListingAdapter extends RecyclerView.Adapter<ListingViewHolder> {

    private Context mContext;
    private int listingType;
    private Object object;
    private ArrayList<String> listings;
    private ArrayList<String> listingLessonDesc;

    public static final String gradePrefix = "Grade ";
    public static final String exerciseChapterPrefix = "Chapter ";
    public static final String readPrefix = "Read ";

    public ListingAdapter(Object object, int listingType) {
        this.object = object;
        this.listingType = listingType;
        this.mContext = mainActivity.getApplicationContext();
        //Generate listings on constructor
        listings = new ArrayList<>();
        listingLessonDesc = new ArrayList<>();
        if (listingType == ListingFragment.LIST_GRADES) {
            RootListing root = (RootListing) object;
            for (Grade grade : root.grades) {
                listings.add(grade.name);
            }
        } else if (listingType == ListingFragment.LIST_LESSONS) {
            Grade grade = (Grade) object;
            for (Lesson lesson : grade.lessons) {
                listings.add(lesson.name);
                listingLessonDesc.add(lesson.description);
            }
        } else if (listingType == ListingFragment.LIST_READS) {
            Conversation conversation = (Conversation) object;
            for (Read read : conversation.reads) {
                listings.add(read.name);
            }
        } else if (listingType == ListingFragment.LIST_VOCABS) {
            Vocab vocab = (Vocab) object;
            for (VocabPart vocabPart : vocab.vocabParts) {
                listings.add(vocabPart.text);
            }
        } else if (listingType == ListingFragment.LIST_EXERCISES) {
            Exercise exercise = (Exercise) object;
            for (ExerciseChapter exerciseChapter : exercise.chapters) {
                listings.add(exerciseChapter.name);
            }
        }
    }

    @Override
    public ListingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (listingType == ListingFragment.LIST_VOCABS){ //When the listing is VOCAB_LISTING
            View choice = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selection_box_list, parent, false);

            return new ListingViewHolder(choice);
        }

        if (listingType == ListingFragment.LIST_LESSONS){ //When the listing is VOCAB_LISTING
            View choice = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selection_box_lesson, parent, false);

            return new ListingViewHolder(choice);
        }

        if (listingType == ListingFragment.LIST_READS){ //When the listing is VOCAB_LISTING
            View choice = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.selection_box_read, parent, false);

            return new ListingViewHolder(choice);
        }

        View choice = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selection_box, parent, false);

        return new ListingViewHolder(choice);
    }

    @Override
    public void onBindViewHolder(final ListingViewHolder holder, final int position) {
        final String selected = listings.get(position);
        updateHolderUI(holder, selected);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(Color.parseColor("#ffffbb33"));
                //load lesson list if the current listing is grade
                if(listingType == ListingFragment.LIST_GRADES) {
                    MainActivity.strGrade = selected;
                    mainActivity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.activity_main_container,
                                    ListingFragment.newInstance(ListingFragment.LIST_LESSONS,
                                            ((RootListing)object).findGrade(selected),
                                            mainActivity.getSupportActionBar().getTitle().toString()),
                                    "LESSON_LISTING")
                            .addToBackStack(null).commit();
                    Log.d(bucketName, "Class ListingAdapter: Method onBindViewHolder(): " + selected + " was selected.");
                } else if (listingType == ListingFragment.LIST_READS) {
                    MainActivity.read = selected;
                    ReadImage.recordDataRead(position);
                    mainActivity.position = 0;
                    mainActivity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.activity_main_container, ReadingModule.newInstance(((Conversation) object).findRead(selected)))
                            .addToBackStack(null)
                            .commit();
                } else if (listingType == ListingFragment.LIST_VOCABS) {
                    MainActivity.position = position;
                    MainActivity.vocab = selected;
                    VocabImage.recordDataVocab(position);
                    mainActivity.loadVocabModule((Vocab)object);
                } else if (listingType == ListingFragment.LIST_EXERCISES) {
                    mainActivity.position = 0;
                    mainActivity.loadExerciseModule((ExerciseChapter)((Exercise) object).chapters.get(position));
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return listings.size();
    }

    public void updateHolderUI(ListingViewHolder holder, String selected) {
        switch (listingType) {
            case ListingFragment.LIST_GRADES:
                holder.updateUI(gradePrefix + selected);
                break;
            case ListingFragment.LIST_READS:
                holder.updateUI(readPrefix + selected);
                break;
            case ListingFragment.LIST_EXERCISES:
                holder.updateUI(exerciseChapterPrefix + selected);
                break;
            case ListingFragment.LIST_VOCABS:
                holder.updateUI(selected);
                break;
        }
    }
}


