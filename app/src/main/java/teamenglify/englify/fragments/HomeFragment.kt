package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Single
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_home.*
import teamenglify.englify.DatabaseService
import teamenglify.englify.MainActivity
import teamenglify.englify.Model.realm.Grade

import teamenglify.englify.R
import timber.log.Timber
import java.util.*

class HomeFragment : Fragment() {
    lateinit var realm: Realm
    private var databaseService: DatabaseService? = null
    private var hasGotDatabaseToUpdate = false
    private val adapter = GradeAdapter(this)

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

        //Check if grades have already been downloaded. Take from mainActivity! Do not close!
        realm = (activity as MainActivity).realm

        //Connect to service
        val databaseServiceIntent = Intent(activity.applicationContext, DatabaseService::class.java)
        activity.bindService(databaseServiceIntent, mDatabaserServiceListener, Context.BIND_AUTO_CREATE)

        //Init recycler
        fragment_home_recycler.adapter = adapter
        val layoutMgr = LinearLayoutManager(activity.applicationContext, LinearLayoutManager.VERTICAL, false)
        fragment_home_recycler.layoutManager = layoutMgr

        //Init random stuff
        (activity as MainActivity).supportActionBar?.title = "Grades"
    }

    override fun onDestroy() {
        super.onDestroy()
        activity.unbindService(mDatabaserServiceListener)
    }

    fun getGrades(): Single<List<Grade>> {
        return Single.create<List<Grade>> { emitter ->
            //Check local database
            val grades = realm.where<Grade>().findAll()
            if (grades.isNotEmpty()) {
                if (!emitter.isDisposed) emitter.onSuccess(grades.toList())
            } else {
                if (!emitter.isDisposed) emitter.onSuccess(grades.toList())
            }
        }
    }

    private class GradeAdapter(val fragment: HomeFragment): RecyclerView.Adapter<GradeViewHolder>() {
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
            holder.itemView.setOnClickListener { fragment.fragmentManager.beginTransaction().replace(R.id.frame_main, LessonFragment.newInstance(grades[position]), "LESSON_FRAGMENT").commit() }
        }

        fun addGrade(grade: Grade) {
            grades.add(grade)
            notifyDataSetChanged()
        }
    }

    private class GradeViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val nameTextView = view.findViewById<TextView>(R.id.viewholder_grade_name)
    }

    private val mDatabaserServiceListener = object: ServiceConnection {
        override fun onBindingDied(name: ComponentName?) {
            Timber.d("onBindingDied() -> name: ${name.toString()}")
            databaseService = null
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("onServiceDisconnected() -> name: ${name.toString()}")
            databaseService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.d("onServiceConnected() -> name: ${name.toString()}")
            databaseService = (service as DatabaseService.DatabaseServiceBinder).service
            /*
            if (!hasGotDatabaseToUpdate) {
                databaseService!!.areUpdatesAvailable()
                        .flatMapObservable {
                            if (it) {
                                return@flatMapObservable databaseService?.updateDatabaseBaseStructure()
                            } else {
                                return@flatMapObservable Observable.empty<Int>()
                            }
                        }.subscribe({
                            Timber.d("Progress update: $it")
                        }, {
                            Timber.e(it, "Error trying to load local content.")
                        }, {
                            Timber.d("Update completed!")
                            getGrades().subscribe({
                                Timber.d("Got grades from realm: ${Arrays.toString(it.toTypedArray())}")
                                for (grade in it) {
                                    adapter.addGrade(grade)
                                }
                            }, {
                                Timber.e(it, "Failed to get grades.")
                            })
                        })
            }
            */
            databaseService!!.clearDatabase()
                    .andThen(databaseService!!.updateDatabaseBaseStructure())
                    .subscribe({
                        Timber.d("Update database progress: $it")
                    }, {
                        Timber.e(it, "Failed to update database.")
                        //Show notification to restart app.
                        MaterialDialog.Builder(activity)
                                .content("Failed to update database.")
                                .positiveText("Close app")
                                .onPositive { _, _ ->
                                    activity.finish()
                                }
                    }, {
                        Timber.d("Update database completed.")
                        getGrades().subscribe({
                            for (grade in it) {
                                adapter.addGrade(grade)
                            }
                        }, {
                            Timber.e(it, "Failed to get grades.")
                        })
                    })
        }
    }
}
