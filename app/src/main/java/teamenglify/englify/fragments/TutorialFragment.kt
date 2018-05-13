package teamenglify.englify.fragments

import android.os.Bundle
import android.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.yarolegovich.discretescrollview.DiscreteScrollView
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.fragment_tutorial.*
import teamenglify.englify.MainActivity
import teamenglify.englify.Model.realm.AppSettings

import teamenglify.englify.R
import timber.log.Timber

class TutorialFragment : Fragment() {
    lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tutorial, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //Init
        realm = Realm.getDefaultInstance()

        val adapter = TutorialAdapter()
        fragment_tutorial_carousel.adapter = adapter
        fragment_tutorial_carousel.setSlideOnFling(false)

        fragment_tutorial_carousel.addScrollStateChangeListener(object: DiscreteScrollView.ScrollStateChangeListener<TutorialViewHolder> {
            override fun onScroll(scrollPosition: Float, currentPosition: Int, newPosition: Int, currentHolder: TutorialViewHolder?, newCurrent: TutorialViewHolder?) {
                //Do nothing
            }

            override fun onScrollEnd(currentItemHolder: TutorialViewHolder, adapterPosition: Int) {
                Timber.d("onScrollEnd() -> adapterPosition: $adapterPosition")
                if (adapterPosition == (adapter.tutorials.size-1)) {
                    Timber.d("Done with tutorial.")
                    realm.executeTransaction { it.where<AppSettings>().findFirst()!!.tutorialCompleted = true }
                    (activity as MainActivity).loadHomeFragment()
                }
            }

            override fun onScrollStart(currentItemHolder: TutorialViewHolder, adapterPosition: Int) {
                //Do nothing
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    private class TutorialAdapter: RecyclerView.Adapter<TutorialViewHolder>() {
        val tutorials = listOf(
                R.drawable.tutorial1,
                R.drawable.tutorial2,
                R.drawable.tutorial3,
                R.drawable.tutorial4,
                R.drawable.tutorial5,
                R.drawable.tutorial6,
                R.drawable.tutorial7,
                R.drawable.tutorial8,
                R.drawable.tutorial9,
                R.drawable.tutorial10,
                R.drawable.tutorial11,
                R.drawable.tutorial12,
                R.drawable.tutorial13
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
            return TutorialViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.viewholder_tutorial, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return tutorials.size
        }

        override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
            holder.imageView.setImageResource(tutorials[position])
        }
    }

    private class TutorialViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.viewholder_tutorial_image)
    }
}
