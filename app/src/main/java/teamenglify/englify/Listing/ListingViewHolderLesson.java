package teamenglify.englify.Listing;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import teamenglify.englify.R;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Soyunana on 3/29/17.
 */

public class ListingViewHolderLesson extends RecyclerView.ViewHolder  {

    public TextView textUpdate;
    public TextView descUpdate;
    //public ImageView overflow;

    public ListingViewHolderLesson (View itemView) {
        super(itemView);
        this.textUpdate = (TextView) itemView.findViewById(R.id.textUpdate);
        this.descUpdate = (TextView) itemView.findViewById(R.id.descUpdate);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ListingViewHolder", "card view clicked");
            }
        });
    }

    public void updateUI (String text, String desc){
        textUpdate.setText(text);
        descUpdate.setText(desc);
    }
}
