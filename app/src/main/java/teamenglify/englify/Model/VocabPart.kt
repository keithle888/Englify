package teamenglify.englify.Model

import io.realm.RealmObject

open class VocabPart: RealmObject() {
    lateinit var text: String
}