package teamenglify.englify.Listing;

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

import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Conversation;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.Model.Read;
import teamenglify.englify.Model.RootListing;
import teamenglify.englify.Model.Vocab;
import teamenglify.englify.Model.VocabPart;
import teamenglify.englify.R;

import static teamenglify.englify.MainActivity.mainActivity;

public class ListingAdapter extends RecyclerView.Adapter<ListingViewHolder> {

    private int listingType;
    private Object object;
    private ArrayList<String> listings;

    public ListingAdapter(Object object, int listingType) {
        this.object = object;
        this.listingType = listingType;
        //Generate listings on constructor
        listings = new ArrayList<>();
        if (listingType == ListingFragment.GRADE_LISTING) {
            RootListing root = (RootListing) object;
            for (Grade grade : root.grades) {
                listings.add(grade.name);
            }
        } else if (listingType == ListingFragment.LESSON_LISTING) {
            Grade grade = (Grade) object;
            for (Lesson lesson : grade.lessons) {
                listings.add(lesson.name);
            }
        } else if (listingType == ListingFragment.READ_LISTING) {
            Conversation conversation = (Conversation) object;
            for (Read read : conversation.reads) {
                listings.add(read.name);
            }
        } else if (listingType == ListingFragment.VOCAB_LISTING) {
            Vocab vocab = (Vocab) object;
            for (VocabPart vocabPart : vocab.vocabParts) {
                listings.add(vocabPart.text);
            }
        }
    }

    @Override
    public void onBindViewHolder(ListingViewHolder holder, final int position) {
        final String selected = listings.get(position);
        holder.updateUI(selected);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(Color.parseColor("#ffffbb33"));
                //load lesson list if the current listing is grade
                if(listingType == ListingFragment.GRADE_LISTING) {
                    //check if the grade selected is the same as before, if not, wipe cached data for lesson, unit and vocab.
                    if(mainActivity.grade != null) {
                        mainActivity.lessonListing = null;
                        mainActivity.readListing = null;
                        mainActivity.vocabListing = null;
                        //mainActivity.readyForAudioBarToLoad = false;
                        Log.d("Englify", "Class ListingAdapter: Method onBindViewHolder(): Deleting Cache.");
                    }
                    mainActivity.loadNextListing(ListingFragment.LESSON_LISTING, ((RootListing)object).grades.get(position));
                    Log.d("Englify", "Class ListingAdapter: Method onBindViewHolder(): Asked mainActivity to loadNextListing()");
                } else if (listingType == ListingFragment.LESSON_LISTING) {
                    if(mainActivity.lesson != null && !mainActivity.lesson.equalsIgnoreCase(selected)) {//check if the lesson selected is the same as before, if not, wipe cached data for unit and vocab.
                        mainActivity.readListing = null;
                        mainActivity.vocabListing = null;
                        //mainActivity.readyForAudioBarToLoad = false;
                        mainActivity.audioConversationURLListing = null;
                        mainActivity.audioVocabURLListing = null;
                        Log.d("Englify", "Class ListingAdapter: Method onBindViewHolder: Deleting Cache.");
                    }
                    MainActivity.lesson = selected;
                    mainActivity.loadModuleListing(ListingFragment.MODULE_LISTING, ((Grade)object).lessons.get(position));
                } else if (listingType == ListingFragment.READ_LISTING) {
                    MainActivity.read = selected;
                    mainActivity.position = 0;
                    mainActivity.loadReadingModule(((Conversation)object).reads.get(position));
                } else if (listingType == ListingFragment.VOCAB_LISTING) {
                    mainActivity.getSupportActionBar().setTitle("Vocab Selection");
                    MainActivity.position = position;
                    MainActivity.vocab = selected;
                    mainActivity.loadVocabModule((Vocab)object);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    @Override
    public ListingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View choice = LayoutInflater.from(parent.getContext()).inflate(R.layout.selection_box, parent, false);

        return new ListingViewHolder(choice);
    }
}


