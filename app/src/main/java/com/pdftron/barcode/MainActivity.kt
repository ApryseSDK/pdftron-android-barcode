package com.pdftron.barcode

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.pdftron.barcode.barcode.RegionSelect
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.config.ToolManagerBuilder
import com.pdftron.pdf.config.ViewerBuilder2
import com.pdftron.pdf.config.ViewerConfig
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment2
import com.pdftron.pdf.model.FileInfo
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.CommonToast
import com.pdftron.pdf.utils.Utils
import com.pdftron.pdf.widget.toolbar.builder.AnnotationToolbarBuilder
import com.pdftron.pdf.widget.toolbar.component.DefaultToolbars

class MainActivity : AppCompatActivity(), PdfViewCtrlTabHostFragment2.TabHostListener {

    private val PERMISSIONS_CAMERA = arrayOf(Manifest.permission.CAMERA)

    private val QR_CODE_ID = 1000
    private val BARCODE_ID = 1001
    private val BARCODE_SCANNER_ID = 1002
    private val SELECT_REGION_ID = 1003

    private val CAMERA_PERMISSION_REQUEST = 1000
    private val SCANNER_REQUEST = 1001

    private var mHandleBarcode = false
    private var mBarcodeLink: String? = null

    private var mPdfViewCtrlTabHostFragment: PdfViewCtrlTabHostFragment2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val f = Utils.copyResourceToLocal(this, R.raw.sample, "sample", ".pdf")
        val uri = Uri.fromFile(f)

        val toolManagerBuilder = ToolManagerBuilder.from()
            .addCustomizedTool(BarcodeCreate.MODE, BarcodeCreate::class.java)
            .addCustomizedTool(ToolManager.ToolMode.ANNOT_EDIT, BarcodeEdit::class.java)
            .addCustomizedTool(RegionSelect.MODE, RegionSelect::class.java)

        val barcodeTag = "Barcode"

        val barcodeToolbarBuilder = AnnotationToolbarBuilder.withTag(barcodeTag)
            .addCustomSelectableButton(
                R.string.tool_qr_code_stamp,
                R.drawable.ic_qr_code_black_24dp, QR_CODE_ID
            )
            .addCustomSelectableButton(
                R.string.tool_barcode_stamp,
                R.drawable.ic_price, BARCODE_ID
            )
            .addCustomButton(
                R.string.tool_barcode_scanner_stamp,
                R.drawable.ic_qr_code_scanner_black_24dp, BARCODE_SCANNER_ID
            )
            .addCustomSelectableButton(
                R.string.tool_select_region,
                R.drawable.ic_select_rectangular_black_24dp, SELECT_REGION_ID
            )
            .addCustomStickyButton(
                com.pdftron.pdf.tools.R.string.undo,
                com.pdftron.pdf.tools.R.drawable.ic_undo_black_24dp,
                DefaultToolbars.ButtonId.UNDO.value()
            )
            .addCustomStickyButton(
                com.pdftron.pdf.tools.R.string.redo,
                com.pdftron.pdf.tools.R.drawable.ic_redo_black_24dp,
                DefaultToolbars.ButtonId.REDO.value()
            )

        val viewToolbarBuilder = AnnotationToolbarBuilder
            .withTag(DefaultToolbars.TAG_VIEW_TOOLBAR)
            .setIcon(R.drawable.ic_view)
            .setToolbarName(getString(R.string.toolbar_title_view))

        val viewerConfig = ViewerConfig.Builder()
            .multiTabEnabled(false)
            .showCloseTabOption(false)
            .saveCopyExportPath(this.filesDir.absolutePath)
            .openUrlCachePath(this.filesDir.absolutePath)
            .setInitialToolbarTag(barcodeTag)
            .rememberLastUsedToolbar(false)
            .toolManagerBuilder(toolManagerBuilder)
            .addToolbarBuilder(viewToolbarBuilder)
            .addToolbarBuilder(barcodeToolbarBuilder)
            .addToolbarBuilder(DefaultToolbars.defaultAnnotateToolbar.setToolbarName(getString(R.string.toolbar_title_annotate)))
            .build()

        mPdfViewCtrlTabHostFragment = ViewerBuilder2.withUri(uri)
            .usingConfig(viewerConfig)
            .usingTheme(R.style.PDFTronAppTheme)
            .build(this)
        mPdfViewCtrlTabHostFragment!!.addHostListener(this)

        // Add the fragment to our activity
        val ft =
            supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, mPdfViewCtrlTabHostFragment!!)
        ft.commit()
    }

    private fun startScannerActivity() {
        val intent = Intent(this, SimpleScannerActivity::class.java)
        startActivityForResult(intent, SCANNER_REQUEST)
    }

    private fun hasCameraPermission(context: Context): Boolean {
        if (Utils.isMarshmallow()) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Camera permissions have not been granted.
                return false
            }
        }
        return true
    }

    private fun requestCameraPermissions(activity: Activity, requestCode: Int) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )
        ) {
            Toast.makeText(activity, R.string.permission_camera_rationale, Toast.LENGTH_SHORT)
                .show()
        } else {
            // Camera permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(activity, PERMISSIONS_CAMERA, requestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SCANNER_REQUEST) {
            mHandleBarcode = true
            mBarcodeLink = data?.getStringExtra("scan_result")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (verifyPermissions(grantResults)) {
                startScannerActivity()
            }
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (mHandleBarcode && mBarcodeLink != null) {
            mHandleBarcode = false

            CommonToast.showText(this, R.string.tool_barcode_tap_toast)

            val toolManager = getToolManager()
            val tool = BarcodeCreate(getPDFViewCtrl()!!)
            tool.setLink(mBarcodeLink)
            tool.setBarcodeType(BarcodeCreate.QR_CODE_TYPE)
            toolManager!!.tool = tool
        }
    }

    private fun getPDFViewCtrl(): PDFViewCtrl? {
        return mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment.pdfViewCtrl
    }

    private fun getToolManager(): ToolManager? {
        return mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment.toolManager
    }

    private fun verifyPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.isEmpty()) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onNavButtonPressed() {
        finish()
    }

    override fun onExitSearchMode() {

    }

    override fun onToolbarOptionsItemSelected(p0: MenuItem?): Boolean {
        if (p0?.itemId == BARCODE_ID) {
            val toolManager = getToolManager()
            val tool = toolManager!!.createTool(BarcodeCreate.MODE, null)
            if (tool is BarcodeCreate) {
                (tool as BarcodeCreate).setBarcodeType(BarcodeCreate.BARCODE_TYPE)
            }
            toolManager.tool = tool
            return true
        } else if (p0?.itemId == BARCODE_SCANNER_ID) {
            if (!hasCameraPermission(this)) {
                requestCameraPermissions(this, CAMERA_PERMISSION_REQUEST)
            } else {
                startScannerActivity()
            }
            return true
        } else if (p0?.itemId == SELECT_REGION_ID) {
            val toolManager = getToolManager()
            val tool = toolManager!!.createTool(RegionSelect.MODE, null)
            toolManager.tool = tool
            return true
        } else if (p0?.itemId == QR_CODE_ID) {
            val toolManager = getToolManager()
            val tool = toolManager!!.createTool(BarcodeCreate.MODE, null)
            if (tool is BarcodeCreate) {
                (tool as BarcodeCreate).setBarcodeType(BarcodeCreate.QR_CODE_TYPE)
            }
            toolManager.tool = tool
            return true
        }
        return false
    }

    override fun onTabHostShown() {

    }

    override fun canShowFileInFolder(): Boolean {
        return false
    }

    override fun onStartSearchMode() {

    }

    override fun canShowFileCloseSnackbar(): Boolean {
        return false
    }

    override fun onToolbarPrepareOptionsMenu(p0: Menu?): Boolean {
        return false
    }

    override fun onTabChanged(p0: String?) {

    }

    override fun onOpenDocError(): Boolean {
        return false
    }

    override fun onTabHostHidden() {

    }

    override fun onTabPaused(p0: FileInfo?, p1: Boolean) {

    }

    override fun canRecreateActivity(): Boolean {
        return true
    }

    override fun onJumpToSdCardFolder() {

    }

    override fun onToolbarCreateOptionsMenu(p0: Menu?, p1: MenuInflater?): Boolean {
        return false
    }

    override fun onTabDocumentLoaded(p0: String?) {

    }

    override fun onLastTabClosed() {

    }

    override fun onShowFileInFolder(p0: String?, p1: String?, p2: Int) {

    }
}