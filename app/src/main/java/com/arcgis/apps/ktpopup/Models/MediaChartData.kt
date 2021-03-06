package com.arcgis.apps.ktpopup.Models

import com.github.mikephil.charting.data.Entry

/**
 * Created by alex7370 on 6/30/17.
 */
data class MediaChartData(val chartData: MutableList<Entry>, val fieldNames: MutableList<String>, override val caption: String) : BasePageData(caption)