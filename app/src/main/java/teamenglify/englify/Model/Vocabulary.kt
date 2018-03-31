package teamenglify.englify.Model

import io.realm.RealmList
import io.realm.RealmObject

open class Vocabulary: RealmObject() {
    var vocabParts = RealmList<VocabPart>()
}