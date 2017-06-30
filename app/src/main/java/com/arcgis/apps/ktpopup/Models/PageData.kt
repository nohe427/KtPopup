package com.arcgis.apps.ktpopup.Models

import com.esri.arcgisruntime.mapping.popup.PopupMedia

/**
 * Created by alex7370 on 6/29/17.
 */
data class PageData(val title: String, var type: PopupMedia.Type? = null, var data: BasePageData? = null)