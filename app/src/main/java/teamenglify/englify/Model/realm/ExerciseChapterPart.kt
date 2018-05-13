package teamenglify.englify.Model.realm

import io.realm.RealmList
import io.realm.RealmObject

open class ExerciseChapterPart: RealmObject() {
    lateinit var question: String
    var answer = RealmList<String>()
    var choices = RealmList<Choice>()
    lateinit var translation: String
    lateinit var imageUrl: String
    lateinit var audioUrl: String
}