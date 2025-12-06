/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fenix.bookmarks.importBookmarks

import android.content.ContentResolver
import android.net.Uri
import mozilla.appservices.places.BookmarkRoot
import mozilla.components.concept.storage.BookmarksStorage

internal class BookmarkHtmlImporter(
    private val bookmarksStorage: BookmarksStorage,
    private val contentResolver: ContentResolver
) {
    suspend fun importHtml(
        html: String,
        parentGuid: String = BookmarkRoot.Mobile.id,
        folderTitle: String = DEFAULT_FOLDER_TITLE,
        position: UInt? = 0u,
    ): Result<String> {
        val folderGuid = bookmarksStorage.addFolder(
            parentGuid = parentGuid,
            title = folderTitle,
            position = position,
        ).getOrElse { return Result.failure(it) }

        bookmarkRegex.findAll(html).forEach { match ->
            val url = match.groupValues[1].trim()
            val rawTitle = match.groupValues[2].trim()
            if (url.isNotEmpty()) {
                bookmarksStorage.addItem(
                    parentGuid = folderGuid,
                    url = url,
                    title = decodeTitle(rawTitle),
                    position = null,
                )
            }
        }

        return Result.success(folderGuid)
    }

    suspend fun importFromUri(uri: Uri): String? {
        val html = contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return null
        return importHtml(html).getOrNull()
    }

    private fun decodeTitle(rawTitle: String): String {
        return rawTitle
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
    }

    private companion object {
        const val DEFAULT_FOLDER_TITLE = "Imported Bookmarks"
        val bookmarkRegex = Regex(
            """<A\b[^>]*\bHREF\s*=\s*"([^"]+)"[^>]*>(.*?)</A>""",
            RegexOption.IGNORE_CASE,
        )
    }
}
