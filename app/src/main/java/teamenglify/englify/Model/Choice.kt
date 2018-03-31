package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject

open class Choice: RealmObject() {
    var parts = RealmList<String>()
}