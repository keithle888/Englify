package teamenglify.englify

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Binder
import android.os.IBinder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.kotlin.where
import teamenglify.englify.Model.realm.*
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class DatabaseService : Service() {
    val binder = DatabaseServiceBinder(this)

    companion object {
        private val KEY_VOCAB = "Vocabulary"
        private val KEY_CONVERSATION = "Conversation"
        private val KEY_READ = "Read"
        private val KEY_EXERCISE = "Exercise"
        private val KEY_TEXT = "text"
        private val KEY_TRANSLATION = "translation"
        private val KEY_AUDIO = "audio"
        private val KEY_IMAGE = "graphic"
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    class DatabaseServiceBinder(val service: DatabaseService): Binder()

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
    }

    fun clearDatabase(): Completable {
        return Completable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            try {
                realm.executeTransaction {
                    it.where<Grade>().findAll().deleteAllFromRealm()
                }
                if (!emitter.isDisposed) emitter.onComplete()
            } catch (e: Exception) {
                if (!emitter.isDisposed) emitter.onError(e)
            } finally {
                realm.close()
            }
        }
    }

    /**
     * Returns integer up to 100 on the progress of updating.
     */
    fun updateDatabaseBaseStructure(): Observable<Int> {
        return Observable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            try {
                //For demo
                /*
                initDemoData().subscribe({
                    if (!emitter.isDisposed) emitter.onNext(100)
                    if (!emitter.isDisposed) emitter.onComplete()
                }, {
                    throw it
                })
                */
                FirebaseDatabase.getInstance().getReference()
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(p0: DatabaseError?) {
                                Timber.e("Database read error: ${p0?.message}")
                            }

                            override fun onDataChange(data: DataSnapshot?) {
                                if (data != null) {
                                    processFirebaseRealtimeDbToRealm(data)
                                            .subscribeOn(Schedulers.io())
                                            .subscribe({grades ->
                                                //Save grades
                                                realm.executeTransaction {
                                                    for (grade in grades) {
                                                        it.insertOrUpdate(grade)
                                                    }
                                                }
                                                if (!emitter.isDisposed) emitter.onComplete()
                                            }, {
                                                if (!emitter.isDisposed) emitter.onError(it)
                                            })
                                }
                            }
                        })
            } catch (e: Exception) {
                if (!emitter.isDisposed) emitter.onError(e)
            } finally {
                realm.close()
            }
        }
    }

    fun updateGradeMedia(): Observable<Int> {
        return Observable.create { emitter -> }
    }

    private fun processFirebaseRealtimeDbToRealm(snapshot: DataSnapshot): Single<List<Grade>> {
        return Single.create {emitter ->
            try {
                val grades = ArrayList<Grade>()

                for (gradeSnapshot in snapshot.children) {  //Processing grade
                    val grade = Grade()
                    grade.name = gradeSnapshot.key
                    Timber.d("Created grade: ${grade.name}")

                    for (lessonSnapshot in gradeSnapshot.children) { //Processing lesson
                        val lesson = Lesson()
                        lesson.name = lessonSnapshot.key
                        lesson.description = lessonSnapshot.child("description").value as? String
                        Timber.d("Created lesson: ${lesson.name}")

                        //Process modules
                        //Process Vocabulary.
                        if (lessonSnapshot.hasChild(KEY_VOCAB)) {
                            val vocab = Vocabulary()
                            Timber.d("Created Vocabulary.")
                            //Get text and translations
                            val vocabTextSnapshot = lessonSnapshot.child(KEY_VOCAB).child(KEY_TEXT)
                            for (vocabText in vocabTextSnapshot.children) {
                                val vocabPart = VocabPart()
                                vocabPart.id = vocabText.key
                                vocabPart.text = vocabText.value as String
                                vocabPart.translation = lessonSnapshot.child(KEY_VOCAB).child(KEY_TRANSLATION).child(vocabPart.id).value as? String
                                vocabPart.audioUrl = lessonSnapshot.child(KEY_VOCAB).child(KEY_AUDIO).child(vocabPart.id).value as? String
                                vocabPart.imageUrl = lessonSnapshot.child(KEY_VOCAB).child(KEY_IMAGE).child(vocabPart.id).value as? String
                                Timber.d("Created VocabPart() -> id: ${vocabPart.id}, text:${vocabPart.text}, translation: ${vocabPart.translation}, imageUrl: ${vocabPart.imageUrl}, audioUrl: ${vocabPart.audioUrl}")
                                if (vocabPart.imageUrl != null) {
                                    try {
                                        val imageData = downloadImage(vocabPart.imageUrl!!)
                                        Timber.d("Downloaded image successfully. Data size: ${imageData.size}")
                                        if (imageData.size > 16000000) {
                                            throw Exception("Image size too big. Size: ${imageData.size}")
                                        } else {
                                            vocabPart.imageData = imageData
                                        }
                                    } catch (e: Exception) {
                                        Timber.e(e, "Failed to download image.")
                                    }
                                }
                                vocab.vocabParts.add(vocabPart)
                            }
                            lesson.vocabulary = vocab
                        }
                        //Process Exercise
                        if (lessonSnapshot.hasChild(KEY_EXERCISE)) {
                            //TODO::
                            /*
                            val exercise = Exercise()
                            Timber.d("Created Exercise.")
                            for (chapterSnapshot in lessonSnapshot.child("Exercise").children) {
                                val exerciseChapter = ExerciseChapter()
                                exerciseChapter.name = chapterSnapshot.key
                                Timber.d("Created exercise chapter: ${exerciseChapter.name}")

                                for (chapterPartTextSnapshot in chapterSnapshot.child("text").children) {

                                }
                            }
                            */
                        }
                        //Process Conversation
                        if (lessonSnapshot.hasChild(KEY_READ)) {
                            val conversation = Conversation()
                            Timber.d("Created Conversation.")
                            for (readSnapshot in lessonSnapshot.child("Read").children) {
                                val read = Read()
                                read.name = readSnapshot.key
                                Timber.d("Created read: ${read.name}")

                                for (readPartSnapshot in readSnapshot.child("text").children) {
                                    val readPart = ReadPart()
                                    readPart.id = readPartSnapshot.key
                                    readPart.reading = readPartSnapshot.value as String
                                    readPart.translation = readSnapshot.child("translation").child(readPart.id)?.value as? String
                                    read.readParts.add(readPart)
                                    Timber.d("Created ReadPart: ${readPart.id}, ${readPart.reading}, ${readPart.translation}")
                                }

                                conversation.reads.add(read)
                            }

                            lesson.conversation = conversation
                        }

                        //Add lesson to grade
                        grade.lessons.add(lesson)
                    }

                    //Add grade to arraylist to be saved at the end
                    grades.add(grade)
                }

                Timber.d("Finished processing firebase data.")
                if (!emitter.isDisposed) emitter.onSuccess(grades.toList())
            } catch (e: Exception) {
                if (!emitter.isDisposed) emitter.onError(e)
            }
        }
    }

    private fun downloadImage(url: String): ByteArray {
        val image = Picasso.get()
                .load(url)
                //TODO::Remember to scale the image!
                .get()
        Timber.d("Picasso has finished downloading image.")
        val stream = ByteArrayOutputStream()
        Timber.d("Compressing image.")
        image.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val imageArray = stream.toByteArray()
        Timber.d("Recycling image resources.")
        image.recycle()
        Timber.d("Closing compression stream.")
        stream.flush()
        stream.close()
        Timber.d("Successfully cleared resources.")
        return imageArray
    }

    @Deprecated("Used for old testing purposes")
    private fun initDemoData(): Completable {
        return Completable.create { emitter ->
            val realm = Realm.getDefaultInstance()
            try {
                realm.executeTransaction {
                    //Create all the objects
                    //Grade
                    val grade = it.createObject(Grade::class.java, "Grade 1")
                    //Lesson
                    val lesson = it.createObject(Lesson::class.java, "Lesson 1")
                    grade.lessons.add(lesson)
                    //Modules
                    val exercise = it.createObject(Exercise::class.java)
                    lesson.exercise = exercise
                    val conversation = it.createObject(Conversation::class.java)
                    lesson.conversation = conversation
                    val vocabulary = it.createObject(Vocabulary::class.java)
                    lesson.vocabulary = vocabulary
                    //Exercise
                    val exerciseChapter = it.createObject(ExerciseChapter::class.java, "Chapter 1")
                    exercise.chapters.add(exerciseChapter)
                    val exerciseChapterPart = it.createObject(ExerciseChapterPart::class.java)
                    exerciseChapterPart.question = "This is question _?"
                    exerciseChapterPart.answer.add("1")
                    val choice1 = it.createObject(Choice::class.java)
                    choice1.parts.add("2")
                    val choice2 = it.createObject(Choice::class.java)
                    choice2.parts.add("3")
                    val choice3 = it.createObject(Choice::class.java)
                    choice3.parts.add("4")
                    exerciseChapterPart.choices.add(choice1)
                    exerciseChapterPart.choices.add(choice2)
                    exerciseChapterPart.choices.add(choice3)
                    //Conversation
                    val read = it.createObject(Read::class.java)
                    conversation.reads.add(read)
                    val readPart = it.createObject(ReadPart::class.java)
                    readPart.reading = "This is a reading"
                    readPart.translation = "This is a translation"
                    read.readParts.add(readPart)
                    //Vocabulary
                    val vocabPart = it.createObject(VocabPart::class.java)
                    vocabPart.text = "This is vocab text."
                    vocabulary.vocabParts.add(vocabPart)
                }
                if (!emitter.isDisposed) emitter.onComplete()
            } catch (e: Exception) {
                if (!emitter.isDisposed) emitter.onError(e)
            } finally {
                realm.close()
            }
        }
    }
}
