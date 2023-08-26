package io.github.duzhaokun123.fuckcainiao

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getIdByName
import com.github.kyuubiran.ezxhelper.utils.getObjectAs
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
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
        EzXHelperInit.setLogTag(TAG)

        loadClass("com.cainiao.wireless.homepage.view.activity.HomePageActivity")
            .findMethod { name == "onCreate" && parameterTypes.size == 1 }
            .hookAfter {
                val activity = it.thisObject as Activity
                activity.findViewById<LinearLayout>(getIdByName("ll_navigation_tab_layout", activity))
                    .apply {
                        getChildAt(1).visibility = View.GONE
                        getChildAt(2).visibility = View.GONE
                    }
            }

        loadClass("com.cainiao.wireless.cubex.mvvm.view.CubeXLinearLayoutFragment")
            .findMethod { name == "setEmpty" }
            .hookBefore {
                val jsonArray = it.args[1] as MutableList<*>
//            File(AndroidAppHelper.currentApplication().externalCacheDir, "cubex.json").writeText(jsonArray.toString())
                for (i in jsonArray.size - 1 downTo 1) {
                    jsonArray.removeAt(i)
                }
            }

        val class_LdAdsCommonEntity = loadClass("com.taobao.cainiao.logistic.response.model.LdAdsCommonEntity")
        loadClass("com.taobao.cainiao.logistic.ui.view.component.LogisticDetailBannerView")
            .findAllMethods {
                parameterTypes.size == 1 && parameterTypes[0] == class_LdAdsCommonEntity
            }.hookBefore {
                it.args[0] = null
            }

        loadClass("com.cainiao.wireless.homepage.view.activity.WelcomeActivity")
            .findAllMethods { name == "onCreate" }
            .hookBefore {
                val activity = it.thisObject as Activity
                activity.finish()
            }

        loadClass("com.cainiao.wireless.recommend.CNRecommendView")
            .findMethod { name == "initView" }
            .hookAfter {
                val viewGroup = it.thisObject as ViewGroup
                viewGroup.findViewById<View>(getIdByName("recommend_view_root", viewGroup.context)).visibility = View.GONE
            }

        loadClass("com.taobao.cainiao.logistic.ui.newview.LogisticDetailTemplateFragment")
            .findMethod { name == "updateAdsInfo" }
            .hookAfter {
                it.thisObject.getObjectAs<View>("mLogisticRedPacketViewStub").visibility = View.GONE
            }
    }
}