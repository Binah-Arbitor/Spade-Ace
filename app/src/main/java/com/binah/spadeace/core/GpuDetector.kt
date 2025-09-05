package com.binah.spadeace.core

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.os.Build
import com.binah.spadeace.data.GpuInfo
import javax.microedition.khronos.egl.*

class GpuDetector(private val context: Context) {
    
    fun detectGpuInfo(): GpuInfo {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val configInfo = activityManager.deviceConfigurationInfo
            
            // Get GPU information using EGL
            val gpuInfo = getGpuInfoViaEGL()
            
            // Detect chipset based on GPU renderer
            val chipset = detectChipset(gpuInfo.renderer)
            
            GpuInfo(
                renderer = gpuInfo.renderer,
                vendor = gpuInfo.vendor,
                version = gpuInfo.version,
                isVulkanSupported = isVulkanSupported(),
                isOpenClSupported = false, // OpenCL detection is complex on Android
                chipset = chipset,
                supportedComputeUnits = estimateComputeUnits(chipset)
            )
        } catch (e: Exception) {
            GpuInfo() // Return default if detection fails
        }
    }
    
    private fun getGpuInfoViaEGL(): GpuInfo {
        return try {
            val egl = EGLContext.getEGL() as EGL10
            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            egl.eglInitialize(display, null)
            
            val configs = arrayOfNulls<EGLConfig>(1)
            val configAttribs = intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                EGL10.EGL_NONE
            )
            val numConfigs = IntArray(1)
            egl.eglChooseConfig(display, configAttribs, configs, 1, numConfigs)
            
            val context = egl.eglCreateContext(
                display, configs[0], EGL10.EGL_NO_CONTEXT,
                intArrayOf(12440, 2, EGL10.EGL_NONE) // EGL_CONTEXT_CLIENT_VERSION, 2
            )
            
            val surface = egl.eglCreatePbufferSurface(
                display, configs[0],
                intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE)
            )
            
            egl.eglMakeCurrent(display, surface, surface, context)
            
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
            val version = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"
            
            egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
            egl.eglDestroySurface(display, surface)
            egl.eglDestroyContext(display, context)
            egl.eglTerminate(display)
            
            GpuInfo(renderer = renderer, vendor = vendor, version = version)
        } catch (e: Exception) {
            GpuInfo()
        }
    }
    
    private fun detectChipset(renderer: String): String {
        return when {
            renderer.contains("Adreno", ignoreCase = true) -> {
                when {
                    renderer.contains("740", ignoreCase = true) -> "Adreno 740 (Snapdragon 8 Gen 2)"
                    renderer.contains("730", ignoreCase = true) -> "Adreno 730 (Snapdragon 8 Gen 1)"
                    renderer.contains("660", ignoreCase = true) -> "Adreno 660 (Snapdragon 888)"
                    renderer.contains("650", ignoreCase = true) -> "Adreno 650 (Snapdragon 865)"
                    renderer.contains("640", ignoreCase = true) -> "Adreno 640 (Snapdragon 855)"
                    renderer.contains("630", ignoreCase = true) -> "Adreno 630 (Snapdragon 845)"
                    renderer.contains("540", ignoreCase = true) -> "Adreno 540 (Snapdragon 835)"
                    renderer.contains("530", ignoreCase = true) -> "Adreno 530 (Snapdragon 820/821)"
                    else -> "Adreno (Qualcomm Snapdragon)"
                }
            }
            renderer.contains("Mali", ignoreCase = true) -> {
                when {
                    renderer.contains("G715", ignoreCase = true) -> "Mali-G715 (Dimensity 9000)"
                    renderer.contains("G710", ignoreCase = true) -> "Mali-G710 (Exynos 2200)"
                    renderer.contains("G78", ignoreCase = true) -> "Mali-G78 (Exynos 2100/Dimensity 1200)"
                    renderer.contains("G77", ignoreCase = true) -> "Mali-G77 (Exynos 990/Dimensity 1000)"
                    renderer.contains("G76", ignoreCase = true) -> "Mali-G76 (Exynos 9820/Kirin 980)"
                    renderer.contains("G72", ignoreCase = true) -> "Mali-G72 (Exynos 8895)"
                    renderer.contains("G71", ignoreCase = true) -> "Mali-G71 (Exynos 8890)"
                    else -> "Mali (ARM/Samsung/MediaTek)"
                }
            }
            renderer.contains("PowerVR", ignoreCase = true) -> {
                "PowerVR (Imagination Technologies)"
            }
            renderer.contains("Intel", ignoreCase = true) -> {
                "Intel HD Graphics"
            }
            renderer.contains("NVIDIA", ignoreCase = true) -> {
                "NVIDIA Tegra"
            }
            else -> "Unknown GPU Chipset"
        }
    }
    
    private fun estimateComputeUnits(chipset: String): Int {
        return when {
            chipset.contains("Adreno 740") -> 8
            chipset.contains("Adreno 730") -> 8
            chipset.contains("Adreno 660") -> 8
            chipset.contains("Adreno 650") -> 6
            chipset.contains("Adreno 640") -> 6
            chipset.contains("Mali-G715") -> 16
            chipset.contains("Mali-G710") -> 14
            chipset.contains("Mali-G78") -> 24
            chipset.contains("Mali-G77") -> 11
            chipset.contains("Mali-G76") -> 12
            else -> 4 // Conservative estimate
        }
    }
    
    private fun isVulkanSupported(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val configInfo = activityManager.deviceConfigurationInfo
                configInfo.reqGlEsVersion >= 0x00030001 // OpenGL ES 3.1+ usually indicates Vulkan support
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
    
    fun getSupportedChipsetsList(): List<String> {
        return listOf(
            "Adreno 740 (Snapdragon 8 Gen 2)",
            "Adreno 730 (Snapdragon 8 Gen 1)", 
            "Adreno 660 (Snapdragon 888)",
            "Adreno 650 (Snapdragon 865)",
            "Adreno 640 (Snapdragon 855)",
            "Adreno 630 (Snapdragon 845)",
            "Mali-G715 (Dimensity 9000)",
            "Mali-G710 (Exynos 2200)",
            "Mali-G78 (Exynos 2100/Dimensity 1200)",
            "Mali-G77 (Exynos 990/Dimensity 1000)",
            "Mali-G76 (Exynos 9820/Kirin 980)",
            "PowerVR (Apple/Intel devices)",
            "NVIDIA Tegra (Shield devices)"
        )
    }
    
    fun isHardwareAccelerationSupported(): Boolean {
        val gpuInfo = detectGpuInfo()
        return gpuInfo.renderer != "Unknown" && 
               (gpuInfo.renderer.contains("Adreno", ignoreCase = true) ||
                gpuInfo.renderer.contains("Mali", ignoreCase = true) ||
                gpuInfo.renderer.contains("PowerVR", ignoreCase = true) ||
                gpuInfo.renderer.contains("NVIDIA", ignoreCase = true))
    }
}