package com.arcgis.apps.ktpopup

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.view.PagerAdapter
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.esri.arcgisruntime.mapping.popup.PopupMedia
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

/**
 * Created by alex7370 on 6/29/17.
 */
class MediaPager(val context: Context, var pageDatas: ArrayList<PageData>) : PagerAdapter() {


    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val pageData = pageDatas[position]
        val linearLayout = LinearLayout(context)
        linearLayout.orientation = LinearLayout.VERTICAL

        var data = pageData.data
        // Equivalent to Java Switch Statement
        when (pageData.type) {

            PopupMedia.Type.IMAGE -> {
                val textView = TextView(context)
                textView.text = pageData.title
                linearLayout.addView(textView)

                val imageView = ImageView(context)
                imageView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                imageView.adjustViewBounds = true

                if(data is ImageData) {
                    Log.e("Source", data.sourceUrl)
                    Log.e("Link", data.linkUrl)
                    Glide.with(context)
                            .load(data.sourceUrl)
                            .apply(RequestOptions().fitCenter())
                            .into(imageView)
                    if (!data.linkUrl.isNullOrEmpty()) {
                        imageView.setOnClickListener { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.linkUrl))) }
                    }
                }

                linearLayout.addView(imageView)
            }

            PopupMedia.Type.BAR_CHART -> {
                if (data is MediaChartData) {

                    chartStart(pageData, linearLayout, data)

                    var barChart = HorizontalBarChart(context)

                    linearLayout.addView(barChart)

                    val list: ArrayList<BarEntry> = arrayListOf()
                    data.chartData.mapTo(list) { BarEntry(it.x, it.y) }
                    val barDataSet = BarDataSet(list, "DataSet")
                    barDataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
                    val barData = BarData(barDataSet)
                    barData.barWidth = .9f


                    barChart.description.isEnabled = false
                    barChart.legend.isEnabled = false
                    barChart.data = barData
                    barChart.setFitBars(true)
                    barChart.setPinchZoom(false)
                    barChart.isDoubleTapToZoomEnabled = false
                    barChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ -> return@IAxisValueFormatter data.fieldNames[value.toInt()] }
                    barChart.xAxis.granularity = 1f
                    barChart.invalidate()

                    barChart.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    barChart.layoutParams.height = 500
                    barChart.invalidate()

                }

            }

            PopupMedia.Type.COLUMN_CHART -> {
                if (data is MediaChartData) {

                    chartStart(pageData, linearLayout, data)

                    var barChart = BarChart(context)

                    linearLayout.addView(barChart)

                    val list: ArrayList<BarEntry> = arrayListOf()
                    data.chartData.mapTo(list) { BarEntry(it.x, it.y) }
                    val barDataSet = BarDataSet(list, "DataSet")
                    barDataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
                    val barData = BarData(barDataSet)
                    barData.barWidth = .9f

                    barChart.description.isEnabled = false
                    barChart.legend.isEnabled = false
                    barChart.data = barData
                    barChart.setFitBars(true)
                    barChart.setPinchZoom(false)
                    barChart.isDoubleTapToZoomEnabled = false
                    barChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ -> return@IAxisValueFormatter data.fieldNames[value.toInt()] }
                    barChart.xAxis.granularity = 1f
                    barChart.invalidate()

                    barChart.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    barChart.layoutParams.height = 500
                    barChart.invalidate()

                }

            }

            PopupMedia.Type.LINE_CHART -> {
                if (data is MediaChartData) {

                    chartStart(pageData, linearLayout, data)

                    var lineChart = LineChart(context)

                    linearLayout.addView(lineChart)


                    val barDataSet = LineDataSet(data.chartData, "DataSet")
                    barDataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
                    val barData = LineData(barDataSet)

                    lineChart.description.isEnabled = false
                    lineChart.legend.isEnabled = false
                    lineChart.data = barData
                    lineChart.setPinchZoom(false)
                    lineChart.isDoubleTapToZoomEnabled = false
                    lineChart.xAxis.valueFormatter = IAxisValueFormatter { value, _ -> return@IAxisValueFormatter data.fieldNames[value.toInt()] }
                    lineChart.xAxis.granularity = 1f
                    lineChart.invalidate()

                    lineChart.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    lineChart.layoutParams.height = 500
                    lineChart.invalidate()

                }
            }

            PopupMedia.Type.PIE_CHART -> {
                if (data is MediaChartData) {

                    chartStart(pageData, linearLayout, data)

                    var pieChart = PieChart(context)

                    linearLayout.addView(pieChart)

                    val list: ArrayList<PieEntry> = arrayListOf()
                    data.chartData.mapTo(list) { PieEntry(it.y) }

                    val barDataSet = PieDataSet(list, "DataSet")
                    barDataSet.colors = ColorTemplate.MATERIAL_COLORS.asList()
                    val barData = PieData(barDataSet)

                    pieChart.description.isEnabled = false
                    pieChart.legend.isEnabled = false
                    pieChart.data = barData

                    pieChart.invalidate()

                    pieChart.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                    pieChart.layoutParams.height = 500
                    pieChart.invalidate()

                }
            }

            else -> {
                if (data is String) {
                    val webViewContent = data
                    val webView = WebView(context)
                    webView.loadData(webViewContent, "text/html; charset=UTF-8", null)
                    linearLayout.addView(webView)
                }
                else {
                    val textView = TextView(context)
                    textView.text = context.getString(R.string.mediaTypeNotImplemented)
                    linearLayout.addView(textView)
                }
            }
        }


        container?.addView(linearLayout)
        return linearLayout
    }

    fun chartStart(pageData: PageData, viewGroup: ViewGroup, mediaChartData: MediaChartData) {
        val title = TextView(context)
        title.text = pageData.title
        viewGroup.addView(title)
        val caption = TextView(context)
        caption.text = mediaChartData.caption
        viewGroup.addView(caption)
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun getCount(): Int {
        return pageDatas.size
    }

    override fun destroyItem(container: ViewGroup?, position: Int, view: Any?) {
        container?.removeView(view as View)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return pageDatas[position].title
    }

    override fun getItemPosition(`object`: Any?): Int {
        return POSITION_NONE
    }
}