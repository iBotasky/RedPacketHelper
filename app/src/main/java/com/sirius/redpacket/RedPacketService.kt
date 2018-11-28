package com.sirius.redpacket

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.PendingIntent
import android.os.Handler
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 *
 *Create by Botasky 2018/11/28
 */
class RedPacketService : AccessibilityService() {
    private val TAG = "RedPacketService"
    private val handler = Handler()
    override fun onInterrupt() {

    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        val classNameChr = event.className
        val className = classNameChr.toString()
        Log.e(TAG, event.toString() + " class:$className")
        when (eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                when (className) {
                    "com.tencent.mm.ui.LauncherUI" -> openRedPacket()
                    "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI" -> clickRedPacket()
                    "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI" -> performBackClick()
                }
            }

            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                if (event.parcelableData != null && event.parcelableData is Notification) {
                    val notification = event.parcelableData as Notification
                    val content = notification.tickerText.toString()
                    if (content.contains("[微信红包]")) {
                        val pendingIntent = notification.contentIntent
                        try {
                            pendingIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    //遍历获得未打开红包
    private fun openRedPacket() {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            val listNode = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cpj")
            if (listNode != null && listNode.size > 0) {
                val redPackets = rootNode.findAccessibilityNodeInfosByText("领取红包")
                if (redPackets.isNotEmpty()){
                    redPackets[0].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            }
        }
    }

    //打开红包
    private fun clickRedPacket() {
        Thread.sleep(1000)
        val nodeInfo = rootInActiveWindow ?: return
        val clickNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cnu")
        if (clickNode != null && clickNode.size > 0) {
            clickNode[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            performBackClick()
        }
    }

    private fun performBackClick() {
        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 1300L)
        Log.e(TAG, "点击返回")
    }
}