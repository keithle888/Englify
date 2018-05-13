package teamenglify.englify.Model.realm

import io.realm.RealmList
import io.realm.RealmObject

open class Exercise: RealmObject() {
    var chapters = RealmList<ExerciseChapter>()
}