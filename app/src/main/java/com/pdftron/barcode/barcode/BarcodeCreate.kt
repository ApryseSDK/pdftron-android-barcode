package com.pdftron.barcode

import android.graphics.Bitmap
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.pdftron.pdf.Annot
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.dialog.simpleinput.TextInputDialog
import com.pdftron.pdf.dialog.simpleinput.TextInputResult
import com.pdftron.pdf.dialog.simpleinput.TextInputViewModel
import com.pdftron.pdf.tools.Stamper
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.Event
import java.io.File
import java.io.FileOutputStream

class BarcodeCreate(ctrl: PDFViewCtrl) : Stamper(ctrl) {

    private val INPUT_REQUEST_CODE = 1001

    private val size = 600
    private val size_width = 660
    private val size_height = 264

    private val BUNDLE_X = "bundle_x"
    private val BUNDLE_Y = "bundle_y"

    private var mLink: String? = null

    override fun getToolMode(): ToolManager.ToolModeBase {
        return MODE
    }

    override fun addStamp() {
        if (mLink == null) {
            showTextInput()
        } else {
            createBarcodeAnnot(mLink!!)
        }
    }

    private fun createBarcodeAnnot(link: String) {
        val bitmap = createImage(link, "Barcode")
        val file = saveBitmap(bitmap)
        if (file != null) {
            createImageStamp(Uri.fromFile(file), 0, null)
            if (mAnnot != null) {
                mAnnot.setCustomData(BARCODE_KEY, link)
            }
        }
        if (mLink != null) {
            val toolManager = mPdfViewCtrl.toolManager as ToolManager
            toolManager.tool = toolManager.createDefaultTool()
        } else {
            mNextToolMode = toolMode
        }
    }

    private fun showTextInput() {
        val toolManager = mPdfViewCtrl.toolManager as ToolManager
        val activity = toolManager.currentActivity

        val textInputViewModel = ViewModelProviders.of(activity!!).get(TextInputViewModel::class.java)
        textInputViewModel.observeOnComplete(activity, Observer<Event<TextInputResult>> {
            if (it != null && !it.hasBeenHandled()) {
                val result = it.contentIfNotHandled!!
                if (result.requestCode == INPUT_REQUEST_CODE) {
                    if (result.extra != null) {
                        val bundle = result.extra
                        setTargetPoint(PointF(bundle!!.getFloat(BUNDLE_X), bundle.getFloat(BUNDLE_Y)), false)
                        createBarcodeAnnot(result.result)
                    }
                }
            }
        })

        val extra = Bundle()
        extra.putFloat(BUNDLE_X, mTargetPoint.x)
        extra.putFloat(BUNDLE_Y, mTargetPoint.y)

        val dialog = TextInputDialog.newInstance(
                INPUT_REQUEST_CODE,
                R.string.tool_barcode_stamp,
                R.string.barcode_dialog_hint,
                R.string.ok,
                R.string.cancel,
                extra)
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PDFTronAppTheme)
        dialog.show(activity.supportFragmentManager, TextInputDialog.TAG)
        mNextToolMode = toolMode
    }

    fun setLink(link: String?) {
        mLink = link
    }

    @Throws(WriterException::class)
    fun createImage(message: String?, type: String?): Bitmap? {
        var bitMatrix: BitMatrix? = null
        bitMatrix = when (type) {
            "QR Code" -> MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, size, size)
            "Barcode" -> MultiFormatWriter().encode(message, BarcodeFormat.CODE_128, size_width, size_height)
            "Data Matrix" -> MultiFormatWriter().encode(message, BarcodeFormat.DATA_MATRIX, size, size)
            "PDF 417" -> MultiFormatWriter().encode(message, BarcodeFormat.PDF_417, size_width, size_height)
            "Barcode-39" -> MultiFormatWriter().encode(message, BarcodeFormat.CODE_39, size_width, size_height)
            "Barcode-93" -> MultiFormatWriter().encode(message, BarcodeFormat.CODE_93, size_width, size_height)
            "AZTEC" -> MultiFormatWriter().encode(message, BarcodeFormat.AZTEC, size, size)
            else -> MultiFormatWriter().encode(message, BarcodeFormat.QR_CODE, size, size)
        }
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (bitMatrix[j, i]) {
                    pixels[i * width + j] = -0x1000000
                } else {
                    pixels[i * width + j] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    fun saveBitmap(bitmap: Bitmap?): File? {
        val activity = (mPdfViewCtrl.toolManager as ToolManager).currentActivity
        if (bitmap != null && activity != null) {
            val tempFile = File(activity.cacheDir, "barcode.png")
            try {
                val out = FileOutputStream(tempFile)
                if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                    out.flush()
                    out.close()
                    return tempFile
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return null
    }

    companion object {
        val MODE: ToolManager.ToolModeBase = ToolManager.ToolMode.addNewMode(Annot.e_Stamp)
        val BARCODE_KEY = "barcode_key"
    }

}