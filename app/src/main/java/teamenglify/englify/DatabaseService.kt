package teamenglify.englify

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import io.reactivex.Observable
import io.reactivex.Single
import io.realm.Realm
import io.realm.kotlin.where
import teamenglify.englify.Model.Grade
import timber.log.Timber

class DatabaseService : Service() {
    val binder = DatabaseServiceBinder(this)
    lateinit var realm: Realm

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        realm = Realm.getDefaultInstance()
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
        realm.close()
    }

    fun areUpdatesAvailable(): Single<Boolean> {
        return Single.create<Boolean> { emitter ->
            if (realm.where<Grade>().findAll().isEmpty()) {
                if (!emitter.isDisposed) emitter.onSuccess(true)
            } else {
                if (!emitter.isDisposed) emitter.onSuccess(false)
            }
        }
    }

    /**
     * Returns integer up to 100 on the progress of updating.
     */
    fun updateDatabase(): Observable<Int> {
        return Observable.create { emitter ->
            try {

            } catch (e: Exception) {
                if (!emitter.isDisposed) emitter.onError(e)
            }
        }
    }

    class DatabaseServiceBinder(val service: DatabaseService): Binder()
}
