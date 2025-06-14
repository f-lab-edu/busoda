package com.chaeny.busoda.stoplist

sealed class GetSearchEvent {
    data object NoResult : GetSearchEvent()
    data object NoInternet : GetSearchEvent()
    data object NetworkError : GetSearchEvent()
    data object ShortKeyword : GetSearchEvent()
}
