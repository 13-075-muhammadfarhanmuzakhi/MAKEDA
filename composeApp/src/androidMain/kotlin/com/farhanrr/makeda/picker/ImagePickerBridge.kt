package com.farhanrr.makeda.picker

object ImagePickerBridge {
    var launcher: (() -> Unit)? = null
    private var callback: ((String?) -> Unit)? = null

    fun pick(onResult: (String?) -> Unit) {
        callback = onResult
        launcher?.invoke()
    }

    fun onResult(uriString: String?) {
        callback?.invoke(uriString)
        callback = null
    }
}