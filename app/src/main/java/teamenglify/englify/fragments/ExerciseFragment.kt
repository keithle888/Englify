package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import teamenglify.englify.Model.ExerciseChapter
import teamenglify.englify.Model.Read

import teamenglify.englify.R

class ExerciseFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    private class ExerciseChapterAdapter: RecyclerView.Adapter<ExerciseChapterViewHolder>() {
        val chapters = ArrayList<ExerciseChapter>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseChapterViewHolder {
            return ExerciseChapterViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.viewholder_exercise_chapter, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return chapters.size
        }

        override fun onBindViewHolder(holder: ExerciseChapterViewHolder, position: Int) {
            //
        }

        fun addRead(exerciseChapter: ExerciseChapter) {
            chapters.add(exerciseChapter)
            notifyDataSetChanged()
        }
    }

    private class ExerciseChapterViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameTextView = view.findViewById<TextView>(R.id.viewholder_exercise_chapter_name)
    }
}
