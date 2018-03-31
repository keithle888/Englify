package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject

open class Lesson: RealmObject() {
    lateinit var name: String
    lateinit var description: String
    var vocabulary: Vocabulary? = null
    var conversation: Conversation? = null
    var exercise: Exercise? = null
}