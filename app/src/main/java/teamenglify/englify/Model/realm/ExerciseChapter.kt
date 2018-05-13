package teamenglify.englify.Model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ExerciseChapter: RealmObject() {
    lateinit var name: String
    var chapterParts = RealmList<ExerciseChapterPart>()
}