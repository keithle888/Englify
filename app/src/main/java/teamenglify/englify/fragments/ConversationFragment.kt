package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_conversation.*
import teamenglify.englify.Model.realm.Read

import teamenglify.englify.R

class ConversationFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_conversation, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragment_conversation_recycler.adapter = ReadAdapter()
    }

    private class ReadAdapter: RecyclerView.Adapter<ReadViewHolder>() {
        val reads = ArrayList<Read>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadViewHolder {
            return ReadViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.viewholder_read, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return reads.size
        }

        override fun onBindViewHolder(holder: ReadViewHolder, position: Int) {
            //
        }

        fun addRead(read: Read) {
            reads.add(read)
            notifyDataSetChanged()
        }
    }

    private class ReadViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameTextView = view.findViewById<TextView>(R.id.viewholder_read_name)
    }
}
