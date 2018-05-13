package teamenglify.englify.Model.realm

import io.realm.RealmObject

open class VocabPart: RealmObject() {
    //From firebase real-time store
    lateinit var id: String
    lateinit var text: String
    var translation: String? = null
    var imageUrl: String? = null
    var audioUrl: String? = null

    //From firebase storage
    var imageData: ByteArray? = null
    var audioData: ByteArray? = null
}