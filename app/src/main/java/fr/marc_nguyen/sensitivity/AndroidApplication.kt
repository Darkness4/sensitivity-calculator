package fr.marc_nguyen.sensitivity

import android.app.Application
import com.google.android.filament.utils.Utils
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AndroidApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } // You may want to plant a Crashlytics Tree in the else body

        Utils.init()
    }
}
