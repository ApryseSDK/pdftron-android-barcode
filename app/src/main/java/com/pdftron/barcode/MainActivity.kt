package com.pdftron.barcode

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.pdftron.pdf.config.ViewerBuilder
import com.pdftron.pdf.config.ViewerConfig
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment
import com.pdftron.pdf.model.FileInfo
import com.pdftron.pdf.utils.Utils

class MainActivity : AppCompatActivity(), PdfViewCtrlTabHostFragment.TabHostListener {

    private var mPdfViewCtrlTabHostFragment: PdfViewCtrlTabHostFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Instantiate a PdfViewCtrlTabHostFragment with a document Uri

        // Instantiate a PdfViewCtrlTabHostFragment with a document Uri
        val f =
            Utils.copyResourceToLocal(this, R.raw.sample, "sample", ".pdf")
        val uri = Uri.fromFile(f)
        val viewerConfig = ViewerConfig.Builder()
            .multiTabEnabled(false)
            .build()
        mPdfViewCtrlTabHostFragment = ViewerBuilder.withUri(uri)
            .usingConfig(viewerConfig)
            .build(this)
        mPdfViewCtrlTabHostFragment!!.addHostListener(this)

        // Add the fragment to our activity
        val ft =
            supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, mPdfViewCtrlTabHostFragment!!)
        ft.commit()
    }

    override fun onNavButtonPressed() {
        finish()
    }

    override fun onExitSearchMode() {

    }

    override fun onToolbarOptionsItemSelected(p0: MenuItem?): Boolean {
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