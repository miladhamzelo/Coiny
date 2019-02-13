package com.binarybricks.coiny.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.PluralsRes
import android.support.annotation.StringRes
import android.support.v4.content.ContextCompat

/**
 * Created by Pranay Airan 1/16/18.
 */
class ResourceProviderImpl(context: Context) : ResourceProvider {

    private val appContext: Context = context.applicationContext

    override fun getString(@StringRes resId: Int): String {
        return appContext.getString(resId)
    }

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return appContext.getString(resId, *formatArgs)
    }

    override fun getQuantityString(@PluralsRes resId: Int, quantity: Int): String {
        return appContext.resources.getQuantityString(resId, quantity)
    }

    override fun getQuantityString(@PluralsRes resId: Int, quantity: Int, vararg formatArgs: Any): String {
        return appContext.resources.getQuantityString(resId, quantity, *formatArgs)
    }

    override fun getColor(resId: Int): Int {
        return ContextCompat.getColor(appContext, resId)
    }

    override fun getDrawable(resId: Int): Drawable? {
        return ContextCompat.getDrawable(appContext, resId)
    }
}
