package com.blanke.mdwechat.hookers.main

import android.view.View
import android.view.ViewGroup
import com.blanke.mdwechat.Objects
import com.blanke.mdwechat.Version
import com.blanke.mdwechat.WechatGlobal
import com.blanke.mdwechat.config.HookConfig
import com.blanke.mdwechat.util.LogUtil
import com.blanke.mdwechat.util.ViewUtils
import com.blanke.mdwechat.util.waitInvoke
import de.robv.android.xposed.XposedHelpers

object HomeActionBarHook {
    fun fix(viewPagerLinearLayout: ViewGroup) {
        if (WechatGlobal.wxVersion!! < Version("7.0.0")) {
            return
        }
        val is_tab_layout_on_top = !HookConfig.is_key_hide_tab && HookConfig.is_hook_tab && HookConfig.is_tab_layout_on_top
        val cb = { actionHeight: Int ->
            val viewpager = viewPagerLinearLayout.getChildAt(0)
            val layoutParams = viewpager.layoutParams as ViewGroup.MarginLayoutParams
            val offset = HookConfig.value_main_text_offset + HookConfig.value_tab_layout_offset
            LogUtil.log("offset: $offset = value_main_text_offset: ${HookConfig.value_main_text_offset} + value_tab_layout_offset: ${HookConfig.value_tab_layout_offset}")
            if (!HookConfig.is_hook_hide_actionbar && is_tab_layout_on_top) {
                layoutParams.topMargin = actionHeight + offset
            } else if (HookConfig.is_hook_hide_actionbar && !is_tab_layout_on_top) {
                layoutParams.topMargin = -actionHeight + offset
            } else {
                layoutParams.topMargin = offset
            }
            viewpager.layoutParams = layoutParams
            viewpager.requestLayout()
        }
        val mActionBar = Objects.Main.HomeUI_mActionBar!!
        waitInvoke(10, true, {
            XposedHelpers.callMethod(mActionBar, "getHeight") as Int > 0
        }, {
            val actionHeight = XposedHelpers.callMethod(mActionBar, "getHeight") as Int
            LogUtil.log("actionHeight=$actionHeight")
            cb(actionHeight)
            if (HookConfig.is_hook_hide_actionbar) {
                LogUtil.log("隐藏 actionBar $mActionBar")
                XposedHelpers.callMethod(mActionBar, "hide")

                val actionView = XposedHelpers.callMethod(mActionBar, "getCustomView") as View
//                val actionView = on(mActionBar).call("getCustomView").get<View>()
                ViewUtils.getParentView(actionView, 1)?.visibility = View.GONE
                LogUtil.log("隐藏 actionBar 成功")
            }
        })
    }
}