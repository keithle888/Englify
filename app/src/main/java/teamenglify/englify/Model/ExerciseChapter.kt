package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject

open class ExerciseChapter: RealmObject() {
    lateinit var name: String
    var chapterParts = RealmList<ExerciseChapterPart>()
}