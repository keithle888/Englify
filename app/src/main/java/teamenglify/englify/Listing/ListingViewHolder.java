package teamenglify.englify.Listing;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import teamenglify.englify.R;


public class ListingViewHolder extends RecyclerView.ViewHolder {
    private TextView textUpdate;
    private ImageView imageUpdate;

    public ListingViewHolder(View itemView) {
        super(itemView);
        this.textUpdate = (TextView) itemView.findViewById(R.id.textUpdate);
        this.imageUpdate = (ImageView)itemView.findViewById(R.id.listing_image);
    }

    public void updateUI (String text){
        textUpdate.setText(text);
    }
}