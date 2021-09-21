package xyz.siggsy.cvek.utils

import androidx.lifecycle.AndroidViewModel
import xyz.siggsy.cvek.CvekApplication

val AndroidViewModel.http get() = getApplication<CvekApplication>().http
