package teamenglify.englify.fragments


import android.os.Bundle
import android.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import teamenglify.englify.Model.realm.Vocabulary

import teamenglify.englify.R

class VocabFragment : Fragment() {
    private lateinit var vocab: Vocabulary

    companion object {
        fun newInstance(vocab: Vocabulary): Fragment {
            val fragment = VocabFragment()
            fragment.vocab = vocab
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vocab, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }
}
