package teamenglify.englify.Listing;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import teamenglify.englify.MainActivity;
import teamenglify.englify.R;

public class ListingAdapter extends RecyclerView.Adapter<ListingViewHolder> {

    private ArrayList<String> listOfChoices;
    private MainActivity mainActivity = MainActivity.getMainActivity();
    private int listingType;

    public ListingAdapter(ArrayList<String> listOfChoices, int listingType) {
        this.listOfChoices = listOfChoices;
        this.listingType = listingType;
    }

    @Override
    public void onBindViewHolder(ListingViewHolder holder, final int position) {
        final String selected = listOfChoices.get(position);
        holder.updateUI(selected);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(Color.parseColor("#ffffbb33"));
                //load lesson list if the current listing is grade
                if(listingType == ListingFragment.GRADE_LISTING) {
                    //check if the grade selected is the same as before, if not, wipe cached data for lesson, unit and vocab.
                    if(mainActivity.gradeSelected != null && !mainActivity.gradeSelected.equalsIgnoreCase(selected)) {
                        mainActivity.lessonListing = null;
                        mainActivity.readListing = null;
                        mainActivity.vocabListing = null;
                        mainActivity.readyForAudioBarToLoad = false;
                        Log.d("Englify", "Class ListingAdapter: Method onBindViewHolder(): Deleting Cache.");
                    }
                    mainActivity.gradeSelected = selected;
                    mainActivity.loadNextListing(ListingFragment.LESSON_LISTING, selected, null, null, null);
                } else if (listingType == ListingFragment.LESSON_LISTING) {
                    //check if the lesson selected is the same as before, if not, wipe cached data for unit and vocab.
                    if(mainActivity.lesson != null && !mainActivity.lesson.equalsIgnoreCase(selected)) {
                        mainActivity.readListing = null;
                        mainActivity.vocabListing = null;
                        mainActivity.readyForAudioBarToLoad = false;
                        mainActivity.audioConversationURLListing = null;
                        mainActivity.audioVocabURLListing = null;
                        Log.d("Englify", "Class ListingAdapter: Method onBindViewHolder: Deleting Cache.");
                    }
                    MainActivity.lesson = selected;
                    mainActivity.loadModuleListing(ListingFragment.MODULE_LISTING);
                } else if (listingType == ListingFragment.READ_LISTING) {
                    //update Action Bar Title
                    mainActivity.getSupportActionBar().setTitle("Study Read");
                    MainActivity.read = selected;
                    mainActivity.currentPage = 0;
                    //mainActivity.loadNextListing();
                } else if (listingType == ListingFragment.VOCAB_LISTING) {
                    mainActivity.getSupportActionBar().setTitle("Vocab Selection");
                    MainActivity.position = position;
                    mainActivity.currentPage = position;
                    MainActivity.vocab = selected;
                    //mainActivity.loadVocabModule();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listOfChoices.size();
    }

    @Override
    public ListingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View choice = LayoutInflater.from(parent.getContext()).inflate(R.layout.selection_box, parent, false);

        return new ListingViewHolder(choice);
    }
}


