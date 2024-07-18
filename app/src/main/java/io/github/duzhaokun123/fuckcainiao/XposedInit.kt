package io.github.duzhaokun123.fuckcainiao

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findField
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getIdByName
import com.github.kyuubiran.ezxhelper.utils.getObject
import com.github.kyuubiran.ezxhelper.utils.getObjectAs
import com.github.kyuubiran.ezxhelper.utils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAutoAs
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
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

        loadClassOrNull("com.cainiao.wireless.homepage.v9.HomeHeaderSection")
            ?.findMethod { name == "processHeaderData" }
            ?.hookAfter {
                val dataArray = it.args[0] // com.alibaba.fastjson.JSONArray
                val size = dataArray.invokeMethodAutoAs<Int>("size")!!
                for (i in (size - 1) downTo 1) {
                    dataArray.invokeMethodAutoAs<Any>("remove", i)
                }
            }

        loadClassOrNull("com.cainiao.wireless.homepage.view.widget.PackageTimeLineDecorateView")
            ?.findMethod { name == "setData" }
            ?.hookBefore {
                val data = it.args[0] ?: return@hookBefore
                if (data.getObjectOrNull("tagIconList") != null) {
                    data.javaClass.findField { name == "tagIconList" }.set(data, arrayListOf<Any>())
                }
            }
    }
}