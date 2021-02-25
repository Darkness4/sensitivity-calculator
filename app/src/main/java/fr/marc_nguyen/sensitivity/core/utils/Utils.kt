package fr.marc_nguyen.sensitivity.core.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

data class PermissionResultEvent(val requestCode: Int, val grantResults: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PermissionResultEvent

        if (requestCode != other.requestCode) return false
        if (!grantResults.contentEquals(other.grantResults)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestCode
        result = 31 * result + grantResults.contentHashCode()
        return result
    }
}

fun Activity.hasPermission(): Boolean = ContextCompat.checkSelfPermission(
    this,
    Manifest.permission.CAMERA,
) != PackageManager.PERMISSION_GRANTED

fun Activity.requestPermission() =
    run { requestPermissions(arrayOf(Manifest.permission.CAMERA), 1) }
