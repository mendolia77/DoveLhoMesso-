package com.dovelhomesso.app.data

enum class SearchResultType {
    SPOT, ITEM, DOCUMENT
}

data class SearchResult(
    val id: Long,
    val title: String,
    val subtitle: String?,
    val type: SearchResultType,
    val matchDetails: String? = null
)
