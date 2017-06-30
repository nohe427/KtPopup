package com.arcgis.apps.ktpopup.Models

/**
 * Created by alex7370 on 6/29/17.
 */

data class ImageData(var sourceUrl: String?, var linkUrl: String? = null, override val caption: String) : BasePageData(caption)
