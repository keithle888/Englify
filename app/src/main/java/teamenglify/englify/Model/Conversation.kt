package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject

open class Conversation: RealmObject() {
    var reads = RealmList<Read>()
}