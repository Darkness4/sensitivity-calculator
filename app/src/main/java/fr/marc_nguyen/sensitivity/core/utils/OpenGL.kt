package fr.marc_nguyen.sensitivity.core.utils

import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES30

fun createEglContext(): EGLContext {
    val eglOpenGlEs3Bit = 0x40
    val display: EGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    EGL14.eglInitialize(display, null, 0, null, 0)
    val configs: Array<EGLConfig?> = arrayOfNulls(1)

    EGL14.eglChooseConfig(
        display,
        intArrayOf(EGL14.EGL_RENDERABLE_TYPE, eglOpenGlEs3Bit, EGL14.EGL_NONE),
        0,
        configs,
        0,
        1,
        intArrayOf(0),
        0,
    )

    val context: EGLContext =
        EGL14.eglCreateContext(
            display,
            configs[0],
            EGL14.EGL_NO_CONTEXT,
            intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE),
            0,
        )

    val surface: EGLSurface =
        EGL14.eglCreatePbufferSurface(
            display,
            configs[0],
            intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE),
            0,
        )

    return if (EGL14.eglMakeCurrent(display, surface, surface, context)) {
        context
    } else {
        throw  Exception("Error creating EGL Context")
    }
}

fun createExternalTextureId(): Int = IntArray(1)
    .apply { GLES30.glGenTextures(1, this, 0) }
    .first()
    .apply { GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, this) }

fun destroyEglContext(context: EGLContext) {
    if (!EGL14.eglDestroyContext(EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY), context)) {
        throw Exception("Error destroying EGL context")
    }
}
