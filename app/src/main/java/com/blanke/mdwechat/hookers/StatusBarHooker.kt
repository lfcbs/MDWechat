package com.blanke.mdwechat.hookers

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import com.blanke.mdwechat.*
import com.blanke.mdwechat.Classes.PhoneWindow
import com.blanke.mdwechat.config.AppCustomConfig
import com.blanke.mdwechat.config.HookConfig
import com.blanke.mdwechat.hookers.base.Hooker
import com.blanke.mdwechat.hookers.base.HookerProvider
import com.blanke.mdwechat.util.ColorUtils
import com.blanke.mdwechat.util.NightModeUtils
import com.blanke.mdwechat.util.mainThread
import com.blankj.utilcode.util.BarUtils
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod


object StatusBarHooker : HookerProvider {
    override fun provideStaticHookers(): List<Hooker>? {
        return listOf(phoneWindowHook)
    }

    private val phoneWindowHook = Hooker {
        val color = getStatusBarColor()
        if (WechatGlobal.wxVersion!! < Version("7.0.3")) {
            findAndHookMethod(PhoneWindow, "setStatusBarColor", CC.Int, object : XC_MethodHook() {
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam?) {
                    val oldColor = param?.args!![0] as Int
                    if (oldColor == Color.parseColor("#F2F2F2")
                            || oldColor == Color.parseColor("#FFFAFAFA")
                            || oldColor == 0) {
                        return
                    }
                    if (color != oldColor) {
                        WeChatHelper.reloadPrefs()
                        val window = param.thisObject as Window
                        window.statusBarColor = color
                        window.navigationBarColor = color
                        param.result = null
                    }
                }
            })
        } else {
            findAndHookMethod(CC.Activity, "onCreate", CC.Bundle, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as Activity
//                    LogUtil.log("activity onCreate " + activity)
                    val statusView = View(activity)
                    statusView.background = NightModeUtils.getBackgroundDrawable(AppCustomConfig.getStatusBarBitmap())
                    statusView.elevation = 1F
//                        val statusParam = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                        statusParam.topMargin = 0
//                        statusParam.height = BarUtils.getStatusBarHeight()

                    statusView.layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, BarUtils.getStatusBarHeight())
                    val window = activity.window
                    val rootView = window.decorView as ViewGroup
                    val clazz = activity::class.java.name
                    mainThread(100) {
                        if ((!clazz.equals("com.tencent.mm.ui.chatting.gallery.ImageGalleryUI"))
                                && (!clazz.equals("com.tencent.mm.plugin.recordvideo.activity.MMRecordUI"))
                                && (!clazz.equals("com.tencent.mm.plugin.sns.ui.SnsBrowseUI"))
                                && (!clazz.equals("com.tencent.mm.plugin.sns.ui.SnsOnlineVideoActivity"))
                        ) {
                            rootView.addView(statusView)
                        }
                        setColor(window, color)
                    }
                    if (activity::class.java == Classes.LauncherUI) {
                        mainThread(1000) {
                            setColor(window, color)
                        }
//                        val mThread = CustomThread()
//                        mThread.init(activity.window, color)
//                        mThread.start()
                    }
                }
            })
        }
    }

    fun setColor(window: Window, color: Int) {
        window?.statusBarColor = color
        window?.navigationBarColor = color
    }

    fun getStatusBarColor(): Int {
        return if (HookConfig.is_hook_statusbar_transparent) NightModeUtils.colorPrimary else ColorUtils.getDarkerColor(NightModeUtils.colorPrimary, 0.85f)
    }
    //    class CustomThread : Thread() {
//        lateinit var window: Window
//        var color: Int = 0
//        fun init(window: Window, color: Int) {
//            this.window = window
//            this.color = color
//        }
//
//        override fun run() {
//            super.run()
//            sleep(1000)
////            try {
//            setColor(window, color)
////                window?.setStatusBarColor(color)
////            } catch (e: Exception) {
////                logXp(e)
////                log(e)
////            }
//        }
//    }

}