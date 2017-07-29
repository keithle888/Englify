package teamenglify.englify.Listing;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import teamenglify.englify.R;
import android.support.v7.widget.RecyclerView;

import org.w3c.dom.Text;

/**
 * Created by Soyunana on 3/29/17.
 */

public class ListingViewHolderLesson extends RecyclerView.ViewHolder  {

    public TextView textUpdate;
    public TextView descUpdate;
    public TextView download_status;
    public String prefix = "Lesson ";
    //public ImageView overflow;

    public ListingViewHolderLesson (View itemView) {
        super(itemView);
        this.textUpdate = (TextView) itemView.findViewById(R.id.textUpdate);
        this.descUpdate = (TextView) itemView.findViewById(R.id.descUpdate);
        this.download_status = (TextView) itemView.findViewById(R.id.lesson_download_status);
    }

    public void updateUI (String text, String desc, String lesson_download_status){
        textUpdate.setText(text);
        descUpdate.setText(desc);
        download_status.setText(lesson_download_status);
    }
}
