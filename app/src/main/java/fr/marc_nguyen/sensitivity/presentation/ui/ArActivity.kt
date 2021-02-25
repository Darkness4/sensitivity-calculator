package fr.marc_nguyen.sensitivity.presentation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import dagger.hilt.android.AndroidEntryPoint
import fr.marc_nguyen.sensitivity.R
import fr.marc_nguyen.sensitivity.core.utils.x
import fr.marc_nguyen.sensitivity.core.utils.y
import fr.marc_nguyen.sensitivity.databinding.ActivityArBinding
import fr.marc_nguyen.sensitivity.presentation.helpers.CameraPermissionHelper
import fr.marc_nguyen.sensitivity.presentation.helpers.arcore.ArCore
import fr.marc_nguyen.sensitivity.presentation.helpers.arcore.ScreenPosition
import fr.marc_nguyen.sensitivity.presentation.helpers.arcore.TouchEvent
import fr.marc_nguyen.sensitivity.presentation.helpers.arcore.ViewRect
import fr.marc_nguyen.sensitivity.presentation.helpers.arcore.toViewRect
import fr.marc_nguyen.sensitivity.presentation.helpers.filament.Filament
import fr.marc_nguyen.sensitivity.presentation.helpers.gesture.DragGesture
import fr.marc_nguyen.sensitivity.presentation.helpers.gesture.DragGestureRecognizer
import fr.marc_nguyen.sensitivity.presentation.helpers.gesture.GesturePointersUtility
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.FrameCallback
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.LightRenderer
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.ModelEvent
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.ModelRenderer
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.ModelResource
import fr.marc_nguyen.sensitivity.presentation.helpers.renderer.PlaneRenderer
import fr.marc_nguyen.sensitivity.presentation.viewmodels.ArViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.coroutineContext

@AndroidEntryPoint
class ArActivity : AppCompatActivity() {

    private val arCoreBehavior: MutableStateFlow<Pair<ArCore, FrameCallback>?> =
        MutableStateFlow(null)
    private val dragEvents: MutableSharedFlow<Pair<ViewRect, TouchEvent>> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private lateinit var filament: Filament
    private lateinit var arCore: ArCore
    private lateinit var lightRenderer: LightRenderer
    private lateinit var planeRenderer: PlaneRenderer
    private lateinit var modelRenderer: ModelRenderer
    private var chosenModel = ModelResource.values().first()

    private var userRequestedInstall = false

    private val viewModel: ArViewModel by viewModels()

    private var _binding: ActivityArBinding? = null
    private val binding: ActivityArBinding
        get() = _binding!!

    private lateinit var startScope: CoroutineScope

    /* Gestures */
    private val gesturePointersUtility by lazy { GesturePointersUtility(resources.displayMetrics) }
    private lateinit var dragRecognizer: DragGestureRecognizer

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityArBinding.inflate(layoutInflater)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)

        dragRecognizer = DragGestureRecognizer(gesturePointersUtility)

        dragRecognizer.addOnGestureStartedListener(
            object : DragGestureRecognizer.OnGestureStartedListener {
                override fun onGestureStarted(gesture: DragGesture) {
                    Pair(
                        binding.surfaceView.toViewRect(),
                        TouchEvent.Move(gesture.position.x, gesture.position.y),
                    )
                        .let { dragEvents.tryEmit(it) }

                    gesture.setGestureEventListener(
                        object : DragGesture.OnGestureEventListener {
                            override fun onFinished(gesture: DragGesture) {
                                Pair(
                                    binding.surfaceView.toViewRect(),
                                    TouchEvent.Stop(gesture.position.x, gesture.position.y),
                                )
                                    .let { dragEvents.tryEmit(it) }
                            }

                            override fun onUpdated(gesture: DragGesture) {
                                Pair(
                                    binding.surfaceView.toViewRect(),
                                    TouchEvent.Move(gesture.position.x, gesture.position.y),
                                )
                                    .let { dragEvents.tryEmit(it) }
                            }
                        },
                    )
                }
            }
        )

        // tap and gesture events
        binding.surfaceView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP &&
                (motionEvent.eventTime - motionEvent.downTime) <
                resources.getInteger(R.integer.tap_event_milliseconds)
            ) {
                Pair(
                    binding.surfaceView.toViewRect(),
                    TouchEvent.Stop(motionEvent.x, motionEvent.y),
                )
                    .let { dragEvents.tryEmit(it) }
            }

            dragRecognizer.onTouch(motionEvent)
            true
        }

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
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this)
            return
        }

        when (ArCoreApk.getInstance().requestInstall(this, !userRequestedInstall)!!) {
            ArCoreApk.InstallStatus.INSTALLED -> {
                // Success: Safe to create the AR session.
            }
            ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                // When this method returns `INSTALL_REQUESTED`:
                // 1. ARCore pauses this activity.
                // 2. ARCore prompts the user to install or update Google Play
                //    Services for AR (market://details?id=com.google.ar.core).
                // 3. ARCore downloads the latest device profile data.
                // 4. ARCore resumes this activity. The next invocation of
                //    requestInstall() will either return `INSTALLED` or throw an
                //    exception if the installation or update did not succeed.
                userRequestedInstall = false
                return
            }
        }
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
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(
                this,
                "Camera permission is needed to run this application",
                Toast.LENGTH_LONG
            )
                .show()
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(this)
            }
            finish()
        }
    }

    private suspend fun createAr() {
        try {
            filament = Filament(this@ArActivity, binding.surfaceView)
            arCore = ArCore(this@ArActivity, filament, binding.surfaceView)
            lightRenderer = LightRenderer(this@ArActivity, arCore.filament)
            planeRenderer = PlaneRenderer(this@ArActivity, arCore.filament)
            modelRenderer = ModelRenderer(this@ArActivity, arCore, arCore.filament, chosenModel)

            val doFrame = fun(frame: Frame) {
                val hasTrackedState = frame.getUpdatedTrackables(Plane::class.java)
                    .any { it.trackingState == TrackingState.TRACKING }

                lightRenderer.doFrame(frame)
                planeRenderer.doFrame(frame)
                modelRenderer.doFrame(frame)
            }

            val frameCallback = FrameCallback(arCore, doFrame)

            arCoreBehavior.emit(Pair(arCore, frameCallback))

            with(CoroutineScope(coroutineContext)) {
                launch {
                    dragEvents
                        .map { (viewRect, touchEvent) ->
                            ScreenPosition(
                                x = touchEvent.x / viewRect.width,
                                y = touchEvent.y / viewRect.height,
                            )
                                .let { ModelEvent.Move(it.x, it.y) }
                        }
                        .collect { modelRenderer.modelEvents.tryEmit(it) }
                }
            }

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
        modelRenderer.destroy()
    }
}
