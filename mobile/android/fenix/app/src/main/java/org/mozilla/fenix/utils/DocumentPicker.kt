/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.utils

import android.net.Uri
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

internal interface DocumentPicker {
    fun open(mimeTypes: Array<String>)
}

internal class ActivityResultDocumentPicker(
    caller: ActivityResultCaller,
    private val onPicked: (Uri?) -> Unit,
) : DocumentPicker {
    private val launcher: ActivityResultLauncher<Array<String>> =
        caller.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            onPicked(uri)
        }

    override fun open(mimeTypes: Array<String>) {
        launcher.launch(mimeTypes)
    }
}
