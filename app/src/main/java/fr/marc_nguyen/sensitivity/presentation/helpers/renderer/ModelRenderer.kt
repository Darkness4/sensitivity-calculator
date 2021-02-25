package fr.marc_nguyen.sensitivity.presentation.helpers.renderer

import android.content.Context
import com.google.android.filament.gltfio.FilamentAsset
import com.google.ar.core.Frame
import com.google.ar.core.Point
import fr.marc_nguyen.sensitivity.core.utils.V3
import fr.marc_nguyen.sensitivity.core.utils.clampToTau
import fr.marc_nguyen.sensitivity.core.utils.m4Identity
import fr.marc_nguyen.sensitivity.core.utils.rotate
import fr.marc_nguyen.sensitivity.core.utils.scale
import fr.marc_nguyen.sensitivity.core.utils.toDegrees
import fr.marc_nguyen.sensitivity.core.utils.translate
import fr.marc_nguyen.sensitivity.core.utils.v3Origin
import fr.marc_nguyen.sensitivity.core.utils.x
import fr.marc_nguyen.sensitivity.core.utils.y
import fr.marc_nguyen.sensitivity.core.utils.z
import fr.marc_nguyen.sensitivity.presentation.helpers.arcore.ArCore
import fr.marc_nguyen.sensitivity.presentation.helpers.filament.Filament
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer

class ModelRenderer(
    private val context: Context,
    private val arCore: ArCore,
    private val filament: Filament,
    private val initialModel: ModelResource
) {

    val modelEvents: MutableSharedFlow<ModelEvent> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val doFrameEvents: MutableSharedFlow<Frame> =
        MutableSharedFlow(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    private val canDrawBehavior: MutableStateFlow<Unit?> =
        MutableStateFlow(null)

    private var translation: V3 = v3Origin
    private var rotate: Float = 0f
    private var scale: Float = 1f

    private val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.Main)

    private var filamentAsset: FilamentAsset? = null

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun load3DModel(resource: ModelResource) = withContext(Dispatchers.IO) {
        context.assets
            .open(resource.res)
            .use { input ->
                val bytes = ByteArray(input.available())
                input.read(bytes)
                val byteBuffer = ByteBuffer.wrap(bytes)
                filament.assetLoader.createAssetFromBinary(byteBuffer)!!
            }
    }.also {
        filament.resourceLoader.loadResources(it)
    }

    init {
        coroutineScope.launch {
            filamentAsset = load3DModel(initialModel)

            // Translation
            launch {
                modelEvents.mapNotNull { modelEvent ->
                    (modelEvent as? ModelEvent.Move)
                        ?.let {
                            arCore.frame
                                .hitTest(
                                    filament.surfaceView.width.toFloat() * modelEvent.x,
                                    filament.surfaceView.height.toFloat() * modelEvent.y,
                                )
                                .maxByOrNull { it.trackable is Point }
                        }?.let {
                            V3(it.hitPose.translation)
                        }
                }.collect {
                    canDrawBehavior.tryEmit(Unit)
                    translation = it
                }
            }

            // Rotate and scale
            launch {
                modelEvents.collect { modelEvent ->
                    when (modelEvent) {
                        is ModelEvent.Update ->
                            Pair((rotate + modelEvent.rotate).clampToTau, scale * modelEvent.scale)
                        else ->
                            Pair(rotate, scale)
                    }.let { (r, s) ->
                        rotate = r
                        scale = s
                    }
                }
            }

            launch {
                canDrawBehavior.filterNotNull().first()

                doFrameEvents.collect {
                    val entity = filament.engine.transformManager.getInstance(filamentAsset!!.root)
                    val localTransform = m4Identity()
                        .translate(translation.x, translation.y, translation.z)
                        .rotate(rotate.toDegrees, 0f, 1f, 0f)
                        .scale(scale, scale, scale)
                        .floatArray
                    filament.scene.addEntities(filamentAsset!!.entities)
                    filament.engine.transformManager.setTransform(entity, localTransform)
                }
            }
        }
    }

    fun destroy() {
        destroyAssets()
        coroutineScope.cancel()
    }

    private fun destroyAssets() {
        if (filamentAsset != null) {
            filament.scene.removeEntities(filamentAsset!!.entities)
            filament.assetLoader.destroyAsset(filamentAsset!!)
            filamentAsset = null
        }
    }

    fun doFrame(frame: Frame) {
        doFrameEvents.tryEmit(frame)
    }
}
