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
    private String listingType;

    public ListingAdapter(ArrayList<String> listOfChoices, String listingType) {
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
                if(listingType.equalsIgnoreCase("Grade")) {
                    mainActivity.currentListingType = "Lesson";
                    //check if the grade selected is the same as before, if not, wipe cached data for lesson, unit and vocab.
                    if(mainActivity.grade != null && !mainActivity.grade.equalsIgnoreCase(selected)) {
                        mainActivity.lessonListing = null;
                        mainActivity.readListing = null;
                        mainActivity.vocabListing = null;
                        mainActivity.readyForAudioBarToLoad = false;
                        Log.d("Englify", "Class ListingAdapter: Method onBindViewHolder(): Deleting Cache.");
                    }
                    mainActivity.grade = selected;
                    mainActivity.loadNextListing();
                } else if (listingType.equalsIgnoreCase("Lesson")) {
                    mainActivity.currentListingType = "Module";
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
                    mainActivity.loadModuleSelection();
                } else if (listingType.equalsIgnoreCase("Read")) {
                    //update Action Bar Title
                    mainActivity.getSupportActionBar().setTitle("Study Read");
                    mainActivity.currentListingType = "Read Module";
                    MainActivity.read = selected;
                    mainActivity.currentPage = 0;
                    mainActivity.loadReadingModule();

                } else if (listingType.equalsIgnoreCase("Vocab")) {
                    mainActivity.getSupportActionBar().setTitle("Vocab Selection");
                    MainActivity.position = position;
                    mainActivity.currentPage = position;
                    MainActivity.vocab = selected;
                    mainActivity.loadVocabModule();
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


