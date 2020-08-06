package com.pdftron.barcode.barcode

import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.pdftron.pdf.*
import com.pdftron.pdf.Rect
import com.pdftron.pdf.tools.AnnotEditRectGroup
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.Utils
import java.util.*
import kotlin.math.max
import kotlin.math.min


class RegionSelect(ctrl: PDFViewCtrl) : AnnotEditRectGroup(ctrl) {

    private var mMinX = 0f
    private var mMinY = 0f
    private var mMaxX = 0f
    private var mMaxY = 0f

    private var mPageNum = 0

    override fun getToolMode(): ToolManager.ToolModeBase {
        return super.getToolMode()
    }

    override fun onDown(e: MotionEvent?): Boolean {
        mMinX = e!!.x
        mMinY = e.y
        mMaxX = e.x
        mMaxY = e.y

        mPageNum = mPdfViewCtrl.getPageNumberFromScreenPt(e.x.toDouble(), e.y.toDouble())

        return super.onDown(e)
    }

    override fun onMove(e1: MotionEvent?, e2: MotionEvent?, x_dist: Float, y_dist: Float): Boolean {
        val x = e2!!.x
        val y = e2.y

        mMinX = min(mMinX, x)
        mMinY = min(mMinY, y)
        mMaxX = max(mMaxX, x)
        mMaxY = max(mMaxY, y)

        return super.onMove(e1, e2, x_dist, y_dist)
    }

    override fun onUp(e: MotionEvent?, priorEventMode: PDFViewCtrl.PriorEventMode?): Boolean {

        val x = e!!.x
        val y = e.y

        mMinX = min(mMinX, x)
        mMinY = min(mMinY, y)
        mMaxX = max(mMaxX, x)
        mMaxY = max(mMaxY, y)

        val pts1 = mPdfViewCtrl.convScreenPtToPagePt(mMinX.toDouble(), mMaxY.toDouble(), mPageNum)
        val pts2 = mPdfViewCtrl.convScreenPtToPagePt(mMaxX.toDouble(), mMinY.toDouble(), mPageNum)

        val cropRect = Rect(pts1[0], pts1[1], pts2[0], pts2[1])
        val page: Page = mPdfViewCtrl.doc.getPage(mPageNum)
        val draw = PDFDraw()
        draw.setClipRect(cropRect)
        val bMap = draw.getBitmap(page)

        val intArray = IntArray(bMap.width * bMap.height)
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.width, 0, 0, bMap.width, bMap.height)
        val source: LuminanceSource =
            RGBLuminanceSource(bMap.width, bMap.height, intArray)
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        val reader: Reader = MultiFormatReader()
        val tmpHintsMap: MutableMap<DecodeHintType, Any> = EnumMap(
            DecodeHintType::class.java
        )
        tmpHintsMap[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE
        tmpHintsMap[DecodeHintType.POSSIBLE_FORMATS] = EnumSet.allOf(BarcodeFormat::class.java)

        try {
            val result: Result = reader.decode(bitmap, tmpHintsMap)
            val contents = result.text

            Log.d("barcode", "link:$contents")

            Utils.safeShowAlertDialog(mPdfViewCtrl.context, contents, "Content")

        } catch (e: Exception) {
            Log.e("QrTest", "Error decoding barcode", e)
        }

        return super.onUp(e, priorEventMode)
    }

    companion object {
        val MODE: ToolManager.ToolModeBase = ToolManager.ToolMode.addNewMode(Annot.e_Unknown)
    }

}