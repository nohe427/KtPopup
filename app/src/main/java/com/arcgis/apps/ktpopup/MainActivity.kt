package com.arcgis.apps.ktpopup

import android.graphics.Color
import android.graphics.Point
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import com.arcgis.apps.ktpopup.Models.*
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.popup.Popup
import com.esri.arcgisruntime.mapping.popup.PopupField
import com.esri.arcgisruntime.mapping.popup.PopupManager
import com.esri.arcgisruntime.mapping.popup.PopupMedia
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.portal.Portal
import com.esri.arcgisruntime.portal.PortalItem
import com.github.mikephil.charting.data.Entry
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.popup_template.*
import kotlinx.android.synthetic.main.popup_template.view.*

class MainActivity : AppCompatActivity() {

    var mediaPager : MediaPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaPager = MediaPager(applicationContext, arrayListOf<PageData>())

        sliding_layout.anchorPoint = .45f

        val webMap = getWebMap()
        displayMapView.map = webMap

        val popup_template = layoutInflater.inflate(R.layout.popup_template, popup_container, true)

        setUpViewPager(popup_media)

        displayMapView.onTouchListener = PopupTouchListener(popup_template as ViewGroup) //Kotlin-ism for casting


    }

    fun setUpViewPager(viewPager: ViewPager) {
        viewPager.adapter = mediaPager
    }

    override fun onBackPressed() {
        if (sliding_layout.panelState != SlidingUpPanelLayout.PanelState.HIDDEN ||
                sliding_layout.panelState != SlidingUpPanelLayout.PanelState.COLLAPSED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
        } else if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.HIDDEN) {
            super.onBackPressed()
        }
        else if (sliding_layout.panelState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            sliding_layout.panelState = SlidingUpPanelLayout.PanelState.HIDDEN
        }

    }

    private fun getWebMap(): ArcGISMap {
        //val webMapId = "3ba6fc4fc10042aa82e2379eb6db7794" //No popups defined
        val webMapId = "41aa8d3b18434df693a96d8307c44a5b" //Popups defined

        val portal = Portal("http://arcgis.com")
        val portalItem = PortalItem(portal, webMapId)
        portalItem.loadAsync()
        return ArcGISMap(portalItem)
    }

    inner class PopupTouchListener(popupLayout: ViewGroup) : DefaultMapViewOnTouchListener(applicationContext, displayMapView) {

        val viewGroup = popupLayout


        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            Log.e("NOHE", e?.action.toString())
            val point = Point(e?.x!!.toInt(), e?.y!!.toInt())
            val identifyLayersAsync = displayMapView.identifyLayersAsync(point, 10.0, true)
            val identifyResults = identifyLayersAsync.get()

            for (identifyResult: IdentifyLayerResult in identifyResults) {
                if (identifyResult.popups.size == 1) {
                    val popup = identifyResult.popups[0]
                    val popupManager = PopupManager(applicationContext, popup)
                    val title = popup.title
                    val symbol = popup.symbol.createSwatchAsync(applicationContext, Color.WHITE).get()
                    val definition = popup.description

                    Log.e("NOHE", "${title}, ${definition}")
                    viewGroup.symbolView.setImageBitmap(symbol)
                    viewGroup.titleText.text = title

                    val definitionText = createDefinitionText(popup)

                    val pageData = arrayListOf<PageData>()
                    pageData.add(PageData("Popup", data = PopupData(definitionText)))
                    if(popupManager.isShowMedia) {

                        //Kotlin-ism
                        popup.popupDefinition.media.mapTo(pageData) {

                            val data = mediaPageGenerator(it, popupManager)

                            PageData(it.title, it.type, data)
                        }
                    }

                    mediaPager?.pageDatas = pageData
                    mediaPager?.notifyDataSetChanged()

                } else {
                    //TODO: Modify this in the event that the user clicks on multiple features at once
                }
            }
            return super.onSingleTapConfirmed(e)
        }

        private fun mediaPageGenerator(it: PopupMedia, popupManager: PopupManager): BasePageData? {
            var data: BasePageData? = null
            if (it.type == PopupMedia.Type.IMAGE) {
                Log.e("NOHE", it.value.sourceUrl)
                data = ImageData(fieldToData(it.value.sourceUrl, popupManager), fieldToData(it.value.linkUrl, popupManager), it.caption)
            }
            if (it.type == PopupMedia.Type.BAR_CHART
                    || it.type == PopupMedia.Type.LINE_CHART
                    || it.type == PopupMedia.Type.PIE_CHART
                    || it.type == PopupMedia.Type.COLUMN_CHART) {
                val entries: MutableList<Entry> = arrayListOf()
                val fieldNames: MutableList<String> = arrayListOf()

                //Kotlin-ism  This creates a for loop and an incrementer while it iterates over it
                for ((count, field: String) in it.value.fieldNames.withIndex()) {
                    Log.e("NOHE", field)
                    var fieldValue = fieldToData(field, popupManager)
                    if (!it.value.normalizeFieldName.isNullOrEmpty()) {
                        val normalizer = fieldToData(it.value.normalizeFieldName, popupManager)
                        fieldValue = (fieldValue.toFloat() / normalizer.toFloat()).toString()
                    }
                    entries.add(Entry(count.toFloat(),fieldValue.toFloat()))
                    fieldNames.add(field)
                }
                data = MediaChartData(entries, fieldNames, it.caption)
            }
            return data
        }

        private fun fieldToData(fieldFromPopup: String, popupManager: PopupManager): String {
            val popup = popupManager.popup

            if (fieldFromPopup.startsWith("{") && fieldFromPopup.endsWith("}"))
            {
                var fieldName = fieldFromPopup.substring(1, fieldFromPopup.length - 1)
                var field = popup.popupDefinition.fields.find { popupField: PopupField? -> popupField?.fieldName.equals(fieldName) }
                var fieldValue = popupManager.getFieldValue(field)
                Log.e("NOHE", fieldValue as String)

                return fieldValue
            } else if (popup.popupDefinition.fields.contains(popup.popupDefinition.fields.find { popupField -> popupField?.fieldName.equals(fieldFromPopup) })) {
                val field = popup.popupDefinition.fields.find { popupField -> popupField?.fieldName.equals(fieldFromPopup) }
                var fieldValue = popupManager.getFieldValue(field)
                return fieldValue.toString()
            }
            return fieldFromPopup
        }

        private fun createDefinitionText(popup: Popup): String {

            val popupManager = PopupManager(applicationContext, popup)
            var sb: StringBuilder = StringBuilder()
            sb.append("<html>")
            if (popup.description != "") {
                sb.append(popupManager.customHtmlDescription)
            } else {
                for (field: PopupField in popupManager.displayedFields) {
                    sb.append("<b>${field.label}</b> : ${popupManager.getFieldValue(field)} <br>")
                }
            }
            sb.append("</html>")
            Log.e("NOHE",sb.toString())
            return sb.toString()
        }
    }
}
