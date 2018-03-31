package teamenglify.englify.Model

import io.realm.RealmObject

open class ReadPart: RealmObject() {
    lateinit var reading: String
    lateinit var translation: String
}