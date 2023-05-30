package io.github.duzhaokun123.fuckcainiao

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.hookReplace
import com.github.kyuubiran.ezxhelper.utils.loadClass
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedInit : IXposedHookLoadPackage {
    companion object {
        val TAG = "FuckCainiao"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName != "com.cainiao.wireless") return
        EzXHelperInit.initHandleLoadPackage(lpparam)

        loadClass("com.cainiao.wireless.homepage.view.activity.HomePageActivity").findMethod {
            name == "onCreate" && parameterTypes.size == 1
        }.hookAfter {
            val thiz = it.thisObject as Activity
            thiz.findViewById<LinearLayout>(thiz.getResId("ll_navigation_tab_layout", "id")).apply {
                getChildAt(1).visibility = View.GONE
                getChildAt(2).visibility = View.GONE
            }
        }

        loadClass("com.cainiao.wireless.homepage.view.fragment.HomePageRecycleViewFragment").findMethod {
            name == "setCNRecommendViewLayoutParams"
        }.hookReplace { }


        loadClass("com.cainiao.wireless.cubex.mvvm.view.CubeXLinearLayoutFragment").findMethod {
            name == "setEmpty"
        }.hookBefore {
            val jsonArray = it.args[1] as MutableList<*>
            jsonArray.removeAt(1)
        }

        loadClass("com.taobao.cainiao.logistic.ui.view.component.LogisticDetailBannerView").findAllMethods {
            parameterTypes.size == 1 && parameterTypes[0] == loadClass("com.taobao.cainiao.logistic.response.model.LdAdsCommonEntity")
        }.hookBefore {
            it.args[0] = null
        }

        loadClass("com.taobao.cainiao.logistic.business.f").findMethod {
            name == "getRecommendViewCount"
        }.hookAfter {
            it.result = 0
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun Context.getResId(name: String, type: String): Int {
        return resources.getIdentifier(name, type, packageName)
    }
}