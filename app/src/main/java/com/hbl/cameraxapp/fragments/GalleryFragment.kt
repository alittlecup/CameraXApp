package com.hbl.cameraxapp.fragments

import android.content.Intent
import android.media.Image
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.hbl.cameraxapp.BuildConfig
import com.hbl.cameraxapp.R
import com.hbl.cameraxapp.utils.padWithDisplayCutout
import com.hbl.cameraxapp.utils.showImm
import java.io.File

val EXTENSION_WHITELIST = arrayOf("JPG")

class GalleryFragment internal constructor() : Fragment() {
    private val args: GalleryFragmentArgs by navArgs()

    private lateinit var mediaList: MutableList<File>

    inner class MediaPagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_SET_USER_VISIBLE_HINT) {
        override fun getItem(position: Int): Fragment = PhotoFragment.create(mediaList[position])

        override fun getCount(): Int = mediaList.size

        override fun getItemPosition(`object`: Any): Int = POSITION_NONE

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true

        val rootDirectory = File(args.rootDirectory)
        mediaList = rootDirectory.listFiles { file ->
            EXTENSION_WHITELIST.contains(file.extension.toUpperCase())
        }.sorted().reversed().toMutableList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mediaViewPager = view.findViewById<ViewPager>(R.id.photo_view_pager).apply {
            offscreenPageLimit = 2
            adapter = MediaPagerAdapter(childFragmentManager)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.findViewById<ConstraintLayout>(R.id.cutout_safe_area).padWithDisplayCutout()
        }
        view.findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            fragmentManager?.popBackStack()
        }
        view.findViewById<ImageButton>(R.id.share_button).setOnClickListener {
            mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->
                val intent = Intent().apply {
                    val mediaType =
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(mediaFile.extension)

                    val uri = FileProvider.getUriForFile(
                        view.context,
                        BuildConfig.APPLICATION_ID + ".provider",
                        mediaFile
                    )

                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = mediaType
                    action = Intent.ACTION_SEND
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share_hint)))
            }
        }
        // Handle delete button press
        view.findViewById<ImageButton>(R.id.delete_button).setOnClickListener {
            AlertDialog.Builder(view.context, android.R.style.Theme_Material_Dialog)
                .setTitle(getString(R.string.delete_title))
                .setMessage(getString(R.string.delete_dialog))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    mediaList.getOrNull(mediaViewPager.currentItem)?.let { mediaFile ->

                        // Delete current photo
                        mediaFile.delete()

                        // Send relevant broadcast to notify other apps of deletion
                        MediaScannerConnection.scanFile(
                            view.context, arrayOf(mediaFile.absolutePath), null, null
                        )

                        // Notify our view pager
                        mediaList.removeAt(mediaViewPager.currentItem)
                        mediaViewPager.adapter?.notifyDataSetChanged()

                        // If all photos have been deleted, return to camera
                        if (mediaList.isEmpty()) {
                            fragmentManager?.popBackStack()
                        }
                    }
                }

                .setNegativeButton(android.R.string.no, null)
                .create().showImm()
        }

    }
}



























