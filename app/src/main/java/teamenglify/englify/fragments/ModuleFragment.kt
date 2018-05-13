package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.fragment_module.*
import teamenglify.englify.MainActivity
import teamenglify.englify.Model.realm.Lesson

import teamenglify.englify.R

class ModuleFragment : Fragment() {
    lateinit var lesson: Lesson

    companion object {
        fun newInstance(lesson: Lesson): Fragment {
            val fragment = ModuleFragment()
            fragment.lesson = lesson
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_module, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Update title
        (activity as MainActivity).supportActionBar?.title = "Module"

        //Assign button behavior
        fragment_module_exercise_button.setOnClickListener {
            if (lesson.exercise == null) {
                showNoModuleDialog()
            } else {
                fragmentManager.beginTransaction().replace(R.id.frame_main, ExerciseFragment(), "EXERCISE_FRAGMENT").commit()
            }
        }

        fragment_module_conversation_button.setOnClickListener {
            if (lesson.conversation == null) {
                showNoModuleDialog()
            } else {
                fragmentManager.beginTransaction().replace(R.id.frame_main, ConversationFragment(), "CONVERSATION_FRAGMENT").commit()
            }
        }

        fragment_module_vocab_button.setOnClickListener {
            if (lesson.vocabulary == null) {
                showNoModuleDialog()
            } else {
                fragmentManager.beginTransaction().replace(R.id.frame_main, VocabFragment.newInstance(lesson.vocabulary!!), "VOCAB_FRAGMENT").commit()
            }
        }
    }

    private fun showNoModuleDialog() {
        val dialog = MaterialDialog.Builder(activity)
                .content("Module not available.")
                .positiveText("Cancel")
                .build()
        dialog.show()
    }
}
