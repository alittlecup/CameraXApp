package com.hbl.cameraxapp.fragments

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Camera
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Telephony.Mms.Part.FILENAME
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hbl.cameraxapp.CamerabasicActivity
import com.hbl.cameraxapp.KEY_EVENT_ACTION
import com.hbl.cameraxapp.KEY_EVENT_EXTRA
import com.hbl.cameraxapp.R
import com.hbl.cameraxapp.utils.ANIMATION_LOW_MILLIS
import com.hbl.cameraxapp.utils.AutoFitPreviewBuilder
import com.hbl.cameraxapp.utils.simulateClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.ByteBuffer
import java.nio.file.Files.createFile
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

typealias LumaListener = (luma: Double) -> Unit

class CameraFragment : Fragment() {
    private lateinit var container: ConstraintLayout
    private lateinit var viewFinder: TextureView
    private lateinit var outpueDirectory: File
    private lateinit var broadcastManager: LocalBroadcastManager

    private var displayId = -1
    private var lensFacing = CameraX.LensFacing.BACK//???

    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null

    private val volumeDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intet: Intent) {
            val intExtra = intet.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN)
            when (intExtra) {
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val shutter = container.findViewById<ImageButton>(R.id.camera_capture_button)
                    shutter.simulateClick()
                }
            }
        }
    }

    private val analzerThread = HandlerThread("LuminosityAnalysis").apply { start() }
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(p0: Int) = Unit

        override fun onDisplayRemoved(p0: Int) = Unit

        override fun onDisplayChanged(p0: Int) = view?.let { view ->
            if (displayId == this@CameraFragment.displayId) {
                Log.d("TAG", "Rotation Change: ${view.display.rotation}")
                preview?.setTargetRotation(view.display.rotation)
                imageCapture?.setTargetRotation(view.display.rotation)
                imageAnalysis?.setTargetRotation(view.display.rotation)
            }
        } ?: Unit
    }

    private lateinit var displayManager: DisplayManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onResume() {
        super.onResume()

        if (!PermissionFragment.hasPermission(requireContext())) {
            Navigation.findNavController(requireActivity(),R.id.fragment_container).navigate(CameraFragmentDirections.actionCameraToPermissions())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        broadcastManager.unregisterReceiver(volumeDownReceiver)
        displayManager.unregisterDisplayListener(displayListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout

        viewFinder = container.findViewById(R.id.view_finder)
        broadcastManager = LocalBroadcastManager.getInstance(view.context)

        val filter = IntentFilter().apply { addAction(KEY_EVENT_ACTION) }
        broadcastManager.registerReceiver(volumeDownReceiver, filter)

        displayManager =
            viewFinder.context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager

        displayManager.registerDisplayListener(displayListener, null)

        outpueDirectory = CamerabasicActivity.getOutputDirectory(requireContext())

        viewFinder.post {
            displayId = viewFinder.display.displayId

            updateCameraUi()
            bindCameraUseCases()

            lifecycleScope.launch(Dispatchers.IO) {
                outpueDirectory.listFiles { file ->
                    EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
                }.sorted().reversed().firstOrNull()?.let { setGalleryThumbnail(it) }
            }
        }
    }

    private fun setGalleryThumbnail(it: File) {
        val thumbnail = container.findViewById<ImageButton>(R.id.photo_view_button)
        thumbnail.post {
            thumbnail.setPadding(resources.getDimension(R.dimen.stroke_small).toInt())
            Glide.with(thumbnail).load(it).apply(RequestOptions.circleCropTransform())
                .into(thumbnail)
        }
    }

    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        override fun onError(
            error: ImageCapture.UseCaseError, message: String, exc: Throwable?
        ) {
            Log.e(TAG, "Photo capture failed: $message")
            exc?.printStackTrace()
        }

        override fun onImageSaved(photoFile: File) {
            Log.d(TAG, "Photo capture succeeded: ${photoFile.absolutePath}")


            // We can only change the foreground Drawable using API level 23+ API
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                // Update the gallery thumbnail with latest picture taken
                setGalleryThumbnail(photoFile)
            }

            // Implicit broadcasts will be ignored for devices running API
            // level >= 24, so if you only target 24+ you can remove this statement
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                requireActivity().sendBroadcast(
                    Intent(android.hardware.Camera.ACTION_NEW_PICTURE, Uri.fromFile(photoFile))
                )
            }

            // If the folder selected is an external media directory, this is unnecessary
            // but otherwise other apps will not be able to access our images unless we
            // scan them using [MediaScannerConnection]
            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(photoFile.extension)
            MediaScannerConnection.scanFile(
                context, arrayOf(photoFile.absolutePath), arrayOf(mimeType), null
            )
        }
    }

    private fun bindCameraUseCases() {
        val metrics = DisplayMetrics().also {
            viewFinder.display.getRealMetrics(it)
        }
        val screenWidth = Rational(metrics.widthPixels, metrics.heightPixels)
        Log.d("TAG", "Screen Metrics: ${metrics.widthPixels} X ${metrics.heightPixels}")

        val viewFinderConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetAspectRatio(screenWidth)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        preview = AutoFitPreviewBuilder.build(viewFinderConfig, viewFinder)

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            setTargetAspectRatio(screenWidth)
            setTargetRotation(viewFinder.display.rotation)
        }.build()
        imageCapture = ImageCapture(imageCaptureConfig)

        val imageAnalysisConfig = ImageAnalysisConfig.Builder().apply {
            setLensFacing(lensFacing)
            setCallbackHandler(Handler(analzerThread.looper))
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            setTargetRotation(viewFinder.display.rotation)
        }.build()

        imageAnalysis = ImageAnalysis(imageAnalysisConfig).apply {
            analyzer = LuminosityAnalyzer { luma ->
                val fps = (analyzer as LuminosityAnalyzer).framesPerSecond
                Log.d(
                    TAG, "Average luminosity: $luma. " +
                            "Frames per second: ${"%.01f".format(fps)}"
                )
            }
        }
        CameraX.bindToLifecycle(viewLifecycleOwner, preview, imageCapture, imageAnalysis)

    }

    @SuppressLint("RestrictedApi")
    private fun updateCameraUi() {
        container.findViewById<ConstraintLayout>(R.id.camera_ui_container)?.let {
            container.removeView(it)
        }
        val controls = View.inflate(requireContext(), R.layout.camera_ui_container, container)

        controls.findViewById<ImageButton>(R.id.camera_capture_button).setOnClickListener {
            imageCapture?.let { imageCapture ->
                val photoFile = createFile(outpueDirectory, FILENAME, PHOTO_EXTENSION)
                val metadata = ImageCapture.Metadata().apply {
                    isReversedHorizontal = lensFacing == CameraX.LensFacing.FRONT
                }
                imageCapture.takePicture(photoFile, imageSavedListener, metadata)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    container.postDelayed({
                        container.foreground = ColorDrawable(Color.WHITE)
                        container.postDelayed({ container.foreground = null }, ANIMATION_LOW_MILLIS)
                    }, ANIMATION_LOW_MILLIS)
                }
                photoFile
            }
        }
        controls.findViewById<ImageButton>(R.id.camera_switch_button).setOnClickListener {
            lensFacing = if (CameraX.LensFacing.FRONT == lensFacing) {
                CameraX.LensFacing.BACK
            } else {
                CameraX.LensFacing.FRONT
            }
            try {
                CameraX.getCameraWithLensFacing(lensFacing)
                CameraX.unbindAll()
                bindCameraUseCases()
            } catch (exc: Exception) {
                //
            }

        }
        controls.findViewById<ImageButton>(R.id.photo_view_button).setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(CameraFragmentDirections.actionCameraToGallery(outpueDirectory.absolutePath))
        }
    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) =
            File(
                baseFolder, SimpleDateFormat(format, Locale.US)
                    .format(System.currentTimeMillis()) + extension
            )
    }

    private class LuminosityAnalyzer(listener: LumaListener? = null) :
        ImageAnalysis.Analyzer {
        private val frameRateWindow = 8
        private val frameTimestamps = ArrayDeque<Long>(5)
        private val listeners = ArrayList<LumaListener>().apply { listener?.let { add(it) } }
        private var lastAnalyzedTimestamp = 0L
        var framesPerSecond: Double = -1.0
            private set

        /**
         * Used to add listeners that will be called with each luma computed
         */
        fun onFrameAnalyzed(listener: LumaListener) = listeners.add(listener)

        /**
         * Helper extension function used to extract a byte array from an image plane buffer
         */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        /**
         * Analyzes an image to produce a result.
         *
         * <p>The caller is responsible for ensuring this analysis method can be executed quickly
         * enough to prevent stalls in the image acquisition pipeline. Otherwise, newly available
         * images will not be acquired and analyzed.
         *
         * <p>The image passed to this method becomes invalid after this method returns. The caller
         * should not store external references to this image, as these references will become
         * invalid.
         *
         * @param image image being analyzed VERY IMPORTANT: do not close the image, it will be
         * automatically closed after this method returns
         * @return the image analysis result
         */
        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
            // If there are no listeners attached, we don't need to perform analysis
            if (listeners.isEmpty()) return

            // Keep track of frames analyzed
            frameTimestamps.push(System.currentTimeMillis())

            // Compute the FPS using a moving average
            while (frameTimestamps.size >= frameRateWindow) frameTimestamps.removeLast()
            framesPerSecond = 1.0 / ((frameTimestamps.peekFirst() -
                    frameTimestamps.peekLast()) / frameTimestamps.size.toDouble()) * 1000.0

            // Calculate the average luma no more often than every second
            if (frameTimestamps.first - lastAnalyzedTimestamp >= TimeUnit.SECONDS.toMillis(1)) {
                lastAnalyzedTimestamp = frameTimestamps.first

                // Since format in ImageAnalysis is YUV, image.planes[0] contains the luminance
                //  plane
                val buffer = image.planes[0].buffer

                // Extract image data from callback object
                val data = buffer.toByteArray()

                // Convert the data into an array of pixel values ranging 0-255
                val pixels = data.map { it.toInt() and 0xFF }

                // Compute average luminance for the image
                val luma = pixels.average()

                // Call all listeners with new value
                listeners.forEach { it(luma) }
            }
        }

    }


}






























