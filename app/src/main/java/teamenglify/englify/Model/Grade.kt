package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject
import java.util.*

open class Grade: RealmObject() {
    lateinit var name: String
    var lessons = RealmList<Lesson>()
}