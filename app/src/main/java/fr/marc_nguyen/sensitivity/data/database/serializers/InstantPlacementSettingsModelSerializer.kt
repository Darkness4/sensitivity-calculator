package fr.marc_nguyen.sensitivity.data.database.serializers

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import fr.marc_nguyen.sensitivity.data.models.InstantPlacementSettingsModel
import java.io.InputStream
import java.io.OutputStream

object InstantPlacementSettingsModelSerializer : Serializer<InstantPlacementSettingsModel> {
    override val defaultValue: InstantPlacementSettingsModel =
        InstantPlacementSettingsModel.getDefaultInstance()

    override fun readFrom(input: InputStream): InstantPlacementSettingsModel {
        try {
            return InstantPlacementSettingsModel.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: InstantPlacementSettingsModel, output: OutputStream) = t.writeTo(output)
}
