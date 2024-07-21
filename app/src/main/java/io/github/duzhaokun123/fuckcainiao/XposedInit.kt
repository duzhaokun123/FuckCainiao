package io.github.duzhaokun123.fuckcainiao

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findAllMethods
import com.github.kyuubiran.ezxhelper.utils.findField
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.getIdByName
import com.github.kyuubiran.ezxhelper.utils.getObjectAs
import com.github.kyuubiran.ezxhelper.utils.getObjectOrNull
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import com.github.kyuubiran.ezxhelper.utils.invokeMethod
import com.github.kyuubiran.ezxhelper.utils.invokeMethodAutoAs
import com.github.kyuubiran.ezxhelper.utils.loadClass
import com.github.kyuubiran.ezxhelper.utils.loadClassOrNull
import com.github.kyuubiran.ezxhelper.utils.paramCount
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

class XposedInit : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        val TAG = "FuckCainiao"
        val URL_ABOUT_FUCK_CAINIAO = "guoguo://go/about_fuck_cainiao"
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
                activity.window.apply {
                    navigationBarColor = Color.rgb(0x1a, 0x1a, 0x1a)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        navigationBarDividerColor = Color.rgb(0x1a, 0x1a, 0x1a)
                    }
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

//        loadClass("com.cainiao.wireless.homepage.view.widget.bottom.NewBottomFloatBanner")
//            .findAllMethods { true }
//            .hookBefore {
//                Log.d("${it.method.name}")
//            }

        val class_fastJSONArray = loadClass("com.alibaba.fastjson.JSONArray")
        loadClass("com.cainiao.wireless.cubex.mvvm.adapter.DXRecyclerViewAdapter")
            .findMethod { name == "setData" }
            .hookBefore {
                val data = it.args[0] as MutableList<Any>
                val last = data.last() as Map<String, Any>
                val template = last["template"] as? Map<String, Any> ?: return@hookBefore
                val name = template["name"] as? String ?: return@hookBefore
                if (name == "guoguo_new_my_settings_quit") {
                    val guoguo_new_my_settings_item = data.get(data.size - 4)
                    Log.d("$guoguo_new_my_settings_item")
                    val aboutFuckCainiao = guoguo_new_my_settings_item.invokeMethod("clone")!!.apply {
                        this as MutableMap<String, Any>
                        this["adUtArgs"] = "xxx"
                        this["utLdArgs"] = "xxx"
                        this["id"] = "114514"
                        this["materialId"] = "114514"
                        this["pitId"] = "114514"
                        this["materialContentMapper"] = this["materialContentMapper"]!!.invokeMethod("clone")!!
                        (this["materialContentMapper"] as MutableMap<String, Any>).apply {
                            this["hasGroupHeader"] = ""
                            this["groupTitle"] = ""
                            this["title"] = "关于 FuckCainiao"
                            this["title_right"] = "版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
                            this["type"] = "normal"
                            this["jumpUrl"] = URL_ABOUT_FUCK_CAINIAO
                        }
                    }
                    data.add(data.size - 2, aboutFuckCainiao)
                    Log.d("final aboutCainiao\n$aboutFuckCainiao")
                }
            }

        Activity::class.java
            .findMethod { name == "startActivity" && paramCount == 2}
            .hookBefore {
//                Log.d("${it.args[0]}")
                val intent = it.args[0] as Intent
                if (intent.dataString?.startsWith(URL_ABOUT_FUCK_CAINIAO) == true) {
                    val activity = it.thisObject as Activity
                    AlertDialog.Builder(activity)
                        .setTitle("关于 FuckCainiao")
                        .setMessage("""
                            版本 ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
                        """.trimIndent())
                        .setNegativeButton(android.R.string.cancel) { _, _ ->
                            activity.finish()
                        }
                        .setPositiveButton("捐赠") { _, _ ->
                            activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
                                data = Uri.parse("https://duzhaokun123.github.io/donate/")
                            })
                            activity.finish()
                        }.setOnCancelListener {
                            activity.finish()
                        }
                        .show()
                }
            }
    }

    override fun initZygote(startupParam: IXposedHookZygoteInit.StartupParam) {
        EzXHelperInit.initZygote(startupParam)
    }
}