package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_home.*
import teamenglify.englify.Model.Grade

import teamenglify.englify.R

class HomeFragment : Fragment() {
    lateinit var realm: Realm

    companion object {
        val TAG = "HOME_FRAGMENT"

        fun newInstance(): Fragment {
            return HomeFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Check if grades have already been downloaded.
        realm = Realm.getDefaultInstance()

        //Init recycler
        fragment_home_recycler.adapter = GradeAdapter()
        val layoutMgr = LinearLayoutManager(activity.applicationContext, LinearLayoutManager.VERTICAL, false)
        fragment_home_recycler.layoutManager = layoutMgr
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private class GradeAdapter: RecyclerView.Adapter<GradeViewHolder>() {
        val grades = ArrayList<Grade>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
            return GradeViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.viewholder_grade, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return grades.size
        }

        override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
            holder.nameTextView.text = grades[position].name
        }

        fun addGrade(grade: Grade) {
            grades.add(grade)
            notifyDataSetChanged()
        }
    }

    private class GradeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameTextView = view.findViewById<TextView>(R.id.viewholder_grade_name)
    }
}
