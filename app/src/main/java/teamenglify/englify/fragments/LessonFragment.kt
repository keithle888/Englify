package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_lesson.*
import teamenglify.englify.Model.Grade
import teamenglify.englify.Model.Lesson

import teamenglify.englify.R

class LessonFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lesson, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragment_lesson_recycler.adapter = LessonAdapter()
    }

    private class LessonAdapter: RecyclerView.Adapter<LessonViewHolder>() {
        val lessons = ArrayList<Lesson>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LessonViewHolder {
            return LessonViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.viewholder_lesson, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return lessons.size
        }

        override fun onBindViewHolder(holder: LessonViewHolder, position: Int) {
            holder.nameTextView.text = lessons[position].name
        }

        fun addGrade(lesson: Lesson) {
            lessons.add(lesson)
            notifyDataSetChanged()
        }
    }

    private class LessonViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameTextView = view.findViewById<TextView>(R.id.viewholder_lesson_name)
    }
}
