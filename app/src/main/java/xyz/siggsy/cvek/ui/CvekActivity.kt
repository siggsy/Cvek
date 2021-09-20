package xyz.siggsy.cvek.ui

import androidx.appcompat.app.AppCompatActivity
import xyz.siggsy.cvek.CvekApplication

open class CvekActivity : AppCompatActivity() {

    val http get() = (application as CvekApplication).http

}