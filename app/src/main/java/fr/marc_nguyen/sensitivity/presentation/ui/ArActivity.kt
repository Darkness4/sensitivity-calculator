package fr.marc_nguyen.sensitivity.presentation.ui

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import dagger.hilt.android.AndroidEntryPoint
import fr.marc_nguyen.sensitivity.core.utils.PermissionResultEvent
import fr.marc_nguyen.sensitivity.core.utils.hasPermission
import fr.marc_nguyen.sensitivity.core.utils.requestPermission
import fr.marc_nguyen.sensitivity.databinding.ActivityArBinding
import fr.marc_nguyen.sensitivity.presentation.helpers.arcore.ArCore
import fr.marc_nguyen.sensitivity.presentation.helpers.filament.Filament
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.FrameCallback
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.LightRenderer
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.ModelRenderer
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.ModelResource
import fr.marc_nguyen.sensitivity.presentation.viewmodels.ArViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class ArActivity : AppCompatActivity() {

    private val resumeBehavior: MutableStateFlow<Unit?> = MutableStateFlow(null)
    private val arCoreBehavior: MutableStateFlow<Pair<ArCore, FrameCallback>?> =
        MutableStateFlow(null)
    private val requestPermissionResultEvents: MutableSharedFlow<PermissionResultEvent> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private lateinit var filament: Filament
    private lateinit var arCore: ArCore
    private lateinit var lightRenderer: LightRenderer
    private var renderers = mutableListOf<ModelRenderer>()
    private var chosenModel = ModelResource.values().first()

    private val viewModel: ArViewModel by viewModels()

    private var _binding: ActivityArBinding? = null
    private val binding: ActivityArBinding
        get() = _binding!!

    private lateinit var startScope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityArBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        lifecycleScope.launch {
            try {
                createAr()
            } catch (e: Throwable) {
                Timber.e(e)
                finish()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        startScope = CoroutineScope(Dispatchers.Main).also {
            it.launch {
                try {
                    startAr()
                } catch (e: Throwable) {
                    Timber.e(e)
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeBehavior.tryEmit(Unit)
    }

    override fun onPause() {
        super.onPause()
        resumeBehavior.tryEmit(null)
    }

    override fun onStop() {
        startScope.cancel()
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, results)
        requestPermissionResultEvents.tryEmit(PermissionResultEvent(requestCode, results))
    }

    private suspend fun createAr() {
        // wait for activity to resume
        resumeBehavior.filterNotNull().first()

        if (hasPermission()) {
            requestPermission()
            val gotPermission = requestPermissionResultEvents
                .first()
                .grantResults.any { it != PackageManager.PERMISSION_GRANTED }
            if (gotPermission)
                return
        }

        resumeBehavior.filterNotNull().first()

        try {
            filament = Filament(this@ArActivity, binding.surfaceView)
            arCore = ArCore(this@ArActivity, filament,  binding.surfaceView)
            lightRenderer = LightRenderer(this@ArActivity, arCore.filament)

            val doFrame = fun(frame: Frame) {
                val hasTrackedState = frame.getUpdatedTrackables(Plane::class.java)
                    .any { it.trackingState == TrackingState.TRACKING }

                renderers.forEach {
                    it.doFrameEvents.tryEmit(frame)
                }

                lightRenderer.doFrame(frame)
            }

            val frameCallback = FrameCallback(arCore, doFrame)

            arCoreBehavior.emit(Pair(arCore, frameCallback))

            awaitCancellation()
        } finally {
            destroyAR()
        }
    }

    private suspend fun startAr() {
        val (arCore, frameCallback) = arCoreBehavior.filterNotNull().first()
        try {
            arCore.session.resume()
            frameCallback.start()
            awaitCancellation()
        } finally {
            frameCallback.stop()
            arCore.session.pause()
        }
    }

    private fun destroyAR() {
        if (this::filament.isInitialized)
            filament.destroy()
        if (this::arCore.isInitialized)
            arCore.destroy()
        renderers.forEach {
            it.destroy()
        }
    }
}
