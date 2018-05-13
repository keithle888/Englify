package teamenglify.englify.Model.realm

import io.realm.RealmList
import io.realm.RealmObject

open class Vocabulary: RealmObject() {
    var vocabParts = RealmList<VocabPart>()
}