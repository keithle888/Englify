package teamenglify.englify.Listing;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.content.Context;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.R;
import android.util.Log;

import static teamenglify.englify.MainActivity.mainActivity;
/**
 * Created by Soyunana on 3/29/17.
 */

public class ListingAdapterLesson extends RecyclerView.Adapter<ListingViewHolderLesson> {

    private Context mContext;
    private int listingType;
    private Object object;
    private ArrayList<String> listings;
    private ArrayList<String> listingLessonDesc;

    public ListingAdapterLesson(Object object, int listingType) {
        this.object = object;
        this.listingType = listingType;
        this.mContext = mainActivity.getApplicationContext();
        //Generate listings on constructor
        listings = new ArrayList<>();
        listingLessonDesc = new ArrayList<>();

        Grade grade = (Grade) object;
        for (Lesson lesson : grade.lessons) {
            listings.add(lesson.name);
            listingLessonDesc.add(lesson.description);
        }
    }

    @Override
    public ListingViewHolderLesson onCreateViewHolder(ViewGroup parent, int viewType) {

        View choice = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selection_box_lesson, parent, false);

        return new ListingViewHolderLesson(choice);
    }

    @Override
    public void onBindViewHolder(final ListingViewHolderLesson holder, final int position) {
        final String selected = listings.get(position);
        final String selectedDesc = listingLessonDesc.get(position);
        holder.updateUI(selected, selectedDesc);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(Color.parseColor("#ffffbb33"));
                MainActivity.lesson = selected;
                mainActivity.loadModuleListing(ListingFragment.MODULE_LISTING, ((Grade)object).lessons.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return listings.size();
    }
}
