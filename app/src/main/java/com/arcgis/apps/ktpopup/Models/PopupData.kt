package com.arcgis.apps.ktpopup.Models

/**
 * Created by alex7370 on 6/30/17.
 */
data class PopupData(val definition: String, override val caption: String) : BasePageData(caption) {
    constructor(definition: String) : this(definition, "")
}