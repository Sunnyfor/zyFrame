package com.sunny.zy.base

import android.view.View

/**
 * Desc
 * Author ZhangYe
 * Mail zhangye98@foxmail.com
 * Date 2020/11/6 17:35
 */
interface OnTitleListener {

    fun setTitleSimple(title: String, vararg menuItem: BaseMenuBean)

    fun setTitleCenterSimple(title: String, vararg menuItem: BaseMenuBean)

    fun setTitleDefault(title: String, vararg menuItem: BaseMenuBean)

    fun setTitleCenterDefault(title: String, vararg menuItem: BaseMenuBean)

    fun setTitleCustom(layoutRes: Int)

    fun setStatusBarColor(color: Int)

    fun setStatusBarDrawable(drawable: Int, relevantView: View? = null)

    fun setStatusBarTextModel(isDark: Boolean)
}