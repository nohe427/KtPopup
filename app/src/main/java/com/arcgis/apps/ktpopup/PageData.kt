package com.arcgis.apps.ktpopup

import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.popup.PopupMedia

/**
 * Created by alex7370 on 6/29/17.
 */
data class PageData(val title: String, var type: PopupMedia.Type? = null, var data: Any? = null)