package teamenglify.englify

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.multidex.MultiDex
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import teamenglify.englify.Model.AppSettings
import teamenglify.englify.fragments.HomeFragment
import teamenglify.englify.fragments.TutorialFragment
import timber.log.Timber

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var realm: Realm

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        MultiDex.install(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        //Initialization stuff
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .name("Englify")
                .schemaVersion(1L)
                .build()
        Realm.setDefaultConfiguration(realmConfig)
        val mAuth = FirebaseAuth.getInstance()
        //Variable initialization
        realm = Realm.getDefaultInstance()

        loginAnonymousUser()
                .andThen(decideNextUiToLoad())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Timber.d("onCreate tasks finished.")
                }, {
                    Timber.e(it,"Failed to finish onCreate tasks.")
                    finish()
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        realm.close()
    }

    fun decideNextUiToLoad(): Completable {
        return Completable.create { emitter ->
            //Check app settings and load tutorial if app opened first time
            val appSettings = realm.where<AppSettings>().findFirst()
            if (appSettings == null) {
                Timber.d("App settings missing. Creating and installing.")
                realm.executeTransaction {
                    it.copyToRealm(AppSettings())
                }
                Timber.d("Saved new app settings.")
                loadTutorial()
                if (!emitter.isDisposed) emitter.onComplete()
            } else if (!appSettings.tutorialCompleted) {
                loadTutorial()
                if (!emitter.isDisposed) emitter.onComplete()
            } else {
                loadHomeFragment()
                if (!emitter.isDisposed) emitter.onComplete()
            }
        }
    }

    fun loadHomeFragment() {
        fragmentManager.beginTransaction()
                .replace(R.id.frame_main, HomeFragment.newInstance(), HomeFragment.TAG)
                .commit()
    }

    fun loadTutorial() {
        fragmentManager.beginTransaction()
                .replace(R.id.frame_main, TutorialFragment(), HomeFragment.TAG)
                .commit()
    }

    fun loginAnonymousUser(): Completable {
        val dialog = MaterialDialog.Builder(applicationContext)
                .title("Initializing")
                .content("Making our secret sauce...")
                .progress(true, 0)
                .build()

        return Completable.create { emitter ->
            //Check firebase auth done
            val mAuth = FirebaseAuth.getInstance()
            if (mAuth.currentUser == null) {
                mAuth.signInAnonymously()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Timber.d("Successfully logged in new user.")
                                if (!emitter.isDisposed) emitter.onComplete()
                            } else {
                                Timber.e("Error trying to create new user.")
                                if (!emitter.isDisposed) emitter.onError(Exception("Login failure."))
                            }
                        }
            } else {
                if (!emitter.isDisposed) emitter.onComplete()
            }
        }.doOnSubscribe { dialog.show() }
                .doFinally { if (dialog.isShowing) dialog.dismiss() }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
