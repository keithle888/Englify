package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Single
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_home.*
import teamenglify.englify.Model.Grade

import teamenglify.englify.R
import timber.log.Timber

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

        //Init random stuff
        activity.actionBar.title = "Grades"

        //Check if grades tree is downloaded.



    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun getGrades(): Single<List<Grade>> {
        val loadingDialog = MaterialDialog.Builder(activity.applicationContext)
                .title("Loading")
                .content("Loading textbooks...")
                .build()

        return Single.create<List<Grade>> { emitter ->
            //Check local database
            val grades = realm.where<Grade>().findAll()
            if (grades.isNotEmpty()) {

            } else {
                //If not there, go to firebase and start downloading
                FirebaseDatabase.getInstance().reference.addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onCancelled(p0: DatabaseError?) {
                        if (!emitter.isDisposed) emitter.onError(p0!!.toException())
                    }

                    override fun onDataChange(p0: DataSnapshot?) {

                    }
                })
            }
        }.doOnSubscribe{ loadingDialog.show() }
                .doFinally { if(loadingDialog.isShowing) loadingDialog.dismiss() }
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
