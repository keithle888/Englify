package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject

open class Exercise: RealmObject() {
    lateinit var imageUrl: String
    var chapters = RealmList<ExerciseChapter>()
}