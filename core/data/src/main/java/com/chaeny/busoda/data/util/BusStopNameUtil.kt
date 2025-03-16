package com.chaeny.busoda.data.util

fun String.replaceStopNameEntities(): String {
    return this.replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
}
