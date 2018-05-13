package teamenglify.englify.Model.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Lesson: RealmObject() {
    lateinit var name: String
    var description: String? = null
    var vocabulary: Vocabulary? = null
    var conversation: Conversation? = null
    var exercise: Exercise? = null
}