package com.example.imagebrowser

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.exifinterface.media.ExifInterface
import com.example.imagebrowser.databinding.BottomSheetImageInfoBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ImageInfoBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_URI = "arg_uri"
        private const val ARG_NAME = "arg_name"
        private const val ARG_SIZE = "arg_size"
        private const val ARG_WIDTH = "arg_width"
        private const val ARG_HEIGHT = "arg_height"
        private const val ARG_DATE = "arg_date"
        private const val ARG_MIME = "arg_mime"
        private const val ARG_PATH = "arg_path"

        fun newInstance(
            uri: String, name: String, size: Long,
            width: Int, height: Int, dateModified: Long,
            mimeType: String, relativePath: String
        ) = ImageInfoBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_URI, uri)
                putString(ARG_NAME, name)
                putLong(ARG_SIZE, size)
                putInt(ARG_WIDTH, width)
                putInt(ARG_HEIGHT, height)
                putLong(ARG_DATE, dateModified)
                putString(ARG_MIME, mimeType)
                putString(ARG_PATH, relativePath)
            }
        }
    }

    private var _binding: BottomSheetImageInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetImageInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        val uri = args.getString(ARG_URI) ?: return
        val name = args.getString(ARG_NAME) ?: ""
        val size = args.getLong(ARG_SIZE)
        val width = args.getInt(ARG_WIDTH)
        val height = args.getInt(ARG_HEIGHT)
        val date = args.getLong(ARG_DATE)
        val mime = args.getString(ARG_MIME) ?: ""
        val path = args.getString(ARG_PATH) ?: ""

        binding.tvFilename.text = name
        binding.tvDimensions.text = if (width > 0 && height > 0) "${width} × ${height}" else "—"
        binding.tvSize.text = formatSize(size)
        binding.tvDate.text = formatDate(date)
        binding.tvFormat.text = mime.ifEmpty { "—" }
        binding.tvPath.text = path.ifEmpty { "—" }

        // 读取 EXIF 信息
        readExif(uri)
    }

    private fun readExif(uriStr: String) {
        Thread {
            try {
                val uri = Uri.parse(uriStr)
                val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return@Thread
                val exif = ExifInterface(inputStream)
                inputStream.close()

                val make = exif.getAttribute(ExifInterface.TAG_MAKE) ?: ""
                val model = exif.getAttribute(ExifInterface.TAG_MODEL) ?: ""
                val camera = listOf(make, model).filter { it.isNotBlank() }.joinToString(" ")

                val aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER)
                    ?.let { "f/$it" }
                val shutter = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)
                    ?.let { formatShutter(it.toDoubleOrNull() ?: 0.0) }
                val iso = exif.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY)
                    ?.let { "ISO $it" }
                val focal = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)
                    ?.let { formatFocal(it) }

                val latLong = FloatArray(2)
                val hasGps = exif.getLatLong(latLong)
                val gps = if (hasGps) "%.6f, %.6f".format(latLong[0], latLong[1]) else null

                // 拍摄时间（EXIF 优先）
                val exifDate = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

                activity?.runOnUiThread {
                    if (_binding == null) return@runOnUiThread
                    if (camera.isNotBlank()) {
                        binding.tvCamera.text = camera
                        binding.rowCamera.visibility = View.VISIBLE
                    } else binding.rowCamera.visibility = View.GONE

                    if (aperture != null) {
                        binding.tvAperture.text = aperture
                        binding.rowAperture.visibility = View.VISIBLE
                    } else binding.rowAperture.visibility = View.GONE

                    if (shutter != null) {
                        binding.tvShutter.text = shutter
                        binding.rowShutter.visibility = View.VISIBLE
                    } else binding.rowShutter.visibility = View.GONE

                    if (iso != null) {
                        binding.tvIso.text = iso
                        binding.rowIso.visibility = View.VISIBLE
                    } else binding.rowIso.visibility = View.GONE

                    if (focal != null) {
                        binding.tvFocal.text = focal
                        binding.rowFocal.visibility = View.VISIBLE
                    } else binding.rowFocal.visibility = View.GONE

                    if (gps != null) {
                        binding.tvGps.text = gps
                        binding.rowGps.visibility = View.VISIBLE
                    } else binding.rowGps.visibility = View.GONE

                    // 用 EXIF 时间覆盖
                    if (!exifDate.isNullOrBlank()) {
                        binding.tvDate.text = exifDate.replace(":", "-", false).let {
                            // 格式 2024:01:01 12:00:00 → 2024-01-01 12:00:00
                            if (it.length >= 10) it.substring(0, 10).replace(":", "-") + it.substring(10) else it
                        }
                    }
                }
            } catch (e: IOException) {
                // EXIF 读取失败时保持原有数据
            }
        }.start()
    }

    private fun formatSize(bytes: Long): String {
        if (bytes <= 0) return "—"
        return when {
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }

    private fun formatDate(timestamp: Long): String {
        if (timestamp <= 0) return "—"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun formatShutter(seconds: Double): String {
        if (seconds <= 0) return "—"
        return if (seconds >= 1) "%.1fs".format(seconds)
        else "1/${(1.0 / seconds).toInt()}s"
    }

    private fun formatFocal(raw: String): String {
        // 格式如 "50/1" 或 "35.0"
        return if (raw.contains("/")) {
            val parts = raw.split("/")
            val n = parts[0].toDoubleOrNull() ?: return raw
            val d = parts[1].toDoubleOrNull() ?: return raw
            if (d == 0.0) return raw
            "%.0fmm".format(n / d)
        } else "${raw}mm"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
