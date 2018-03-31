package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject

open class Read: RealmObject() {
    lateinit var name: String
    var readParts = RealmList<ReadPart>()
}