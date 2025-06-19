package com.chaeny.busoda.stoplist

sealed class SearchEvent {
    data object NoResult : SearchEvent()
    data object NoInternet : SearchEvent()
    data object NetworkError : SearchEvent()
    data object ShortKeyword : SearchEvent()
}
