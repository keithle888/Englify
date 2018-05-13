package teamenglify.englify.fragments.support

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.viewholder_single_image.view.*
import teamenglify.englify.R

class SingleImageViewAdapter: RecyclerView.Adapter<SingleImageViewAdapter.SingleImageViewHolder>() {
    val drawables = ArrayList<Int>()

    fun addSourceByResourceId(resId: Int) {
        drawables.add(resId)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleImageViewHolder {
        return SingleImageViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.viewholder_single_image, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return drawables.size
    }

    override fun onBindViewHolder(holder: SingleImageViewHolder, position: Int) {
        holder.imageView.setImageResource(drawables[position])
    }

    class SingleImageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.viewholder_single_image)
    }
}