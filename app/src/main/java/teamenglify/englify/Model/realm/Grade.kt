package teamenglify.englify.Model.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Grade: RealmObject() {
    @PrimaryKey lateinit var name: String
    var lessons = RealmList<Lesson>()
}