package com.dicoding.asclepius.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    var currentImageUri: Uri? = null
    var resizedBitmap: Bitmap? = null
}
