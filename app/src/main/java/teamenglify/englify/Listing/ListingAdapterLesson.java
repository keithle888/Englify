package teamenglify.englify.Listing;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import android.content.Context;

import teamenglify.englify.DataService.DataManager;
import teamenglify.englify.MainActivity;
import teamenglify.englify.Model.Grade;
import teamenglify.englify.Model.Lesson;
import teamenglify.englify.ModuleSelection.ModuleSelection;
import teamenglify.englify.R;
import android.util.Log;

import static teamenglify.englify.MainActivity.mainActivity;
/**
 * Created by Soyunana on 3/29/17.
 */

public class ListingAdapterLesson extends RecyclerView.Adapter<ListingViewHolderLesson> {

    private Context mContext;
    private Grade grade;
    public static final String lessonPrefix = "Lesson ";

    public ListingAdapterLesson(Grade grade) {
        this.grade = grade;
        this.mContext = mainActivity.getApplicationContext();
    }

    @Override
    public ListingViewHolderLesson onCreateViewHolder(ViewGroup parent, int viewType) {

        View choice = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.selection_box_lesson, parent, false);

        return new ListingViewHolderLesson(choice);
    }

    @Override
    public void onBindViewHolder(final ListingViewHolderLesson holder, final int position) {
        final String selected = grade.lessons.get(position).name;
        final String selectedDesc = grade.lessons.get(position).description;
        //set download status
        String download_status_text = "";
        if (grade.lessons.get(position).modules != null && grade.lessons.get(position).modules.size() != 0) {
            download_status_text = mainActivity.getString(R.string.lesson_has_been_downloaded_cardview_text);
        } else {
            download_status_text = mainActivity.getString(R.string.lesson_not_downloaded_cardview_text);
        }
        holder.updateUI(lessonPrefix + selected, selectedDesc, download_status_text);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setBackgroundColor(Color.parseColor("#ffffbb33"));
                MainActivity.lesson = selected;
                Lesson lesson = grade.findLesson(selected);
                if (lesson.modules.size() == 0) { //Lesson has not been downloaded.
                    new DataManager().download_lesson(grade, lesson);
                } else {
                    mainActivity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.activity_main_container,
                                    ModuleSelection.newInstance(lesson,
                                            mainActivity.getSupportActionBar().getTitle().toString()),
                                    "Module Selection")
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return grade.lessons.size();
    }
}
