package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_lesson.*
import teamenglify.englify.MainActivity
import teamenglify.englify.Model.realm.Grade
import teamenglify.englify.Model.realm.Lesson

import teamenglify.englify.R

class LessonFragment: Fragment() {
    lateinit var grade: Grade
    private val adapter = LessonAdapter(this)

    companion object {
        fun newInstance(grade: Grade): Fragment {
            val fragment = LessonFragment()
            fragment.grade = grade
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_lesson, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fragment_lesson_recycler.adapter = adapter
        val layoutMgr = LinearLayoutManager(activity.applicationContext, LinearLayoutManager.VERTICAL, false)
        fragment_lesson_recycler.layoutManager = layoutMgr

        //Update action bar
        (activity as MainActivity).supportActionBar?.title = "Lessons"

        //Update ui
        for (lesson in grade.lessons) {
            adapter.addLesson(lesson)
        }
    }

    private class LessonAdapter(val fragment: Fragment): RecyclerView.Adapter<LessonViewHolder>() {
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
            holder.itemView.setOnClickListener { fragment.fragmentManager.beginTransaction().replace(R.id.frame_main, ModuleFragment.newInstance(lessons[position]), "MODULE_FRAGMENT").commit() }
        }

        fun addLesson(lesson: Lesson) {
            lessons.add(lesson)
            notifyDataSetChanged()
        }
    }

    private class LessonViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameTextView = view.findViewById<TextView>(R.id.viewholder_lesson_name)
    }
}
