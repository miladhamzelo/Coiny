package com.binarybricks.coiny.adapterdelegates

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.components.DashboardNewsModule
import com.binarybricks.coiny.components.ModuleItem
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by Pranay Airan
 */

class DashboardNewsAdapterDelegate : AdapterDelegate<List<ModuleItem>>() {

    private val dashboardNewsModule by lazy {
        DashboardNewsModule()
    }

    override fun isForViewType(items: List<ModuleItem>, position: Int): Boolean {
        return items[position] is DashboardNewsModule.DashboardNewsModuleData
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val dashboardNewsModuleView = dashboardNewsModule.init(LayoutInflater.from(parent.context), parent)
        return DashboardNewsHolder(dashboardNewsModuleView, dashboardNewsModule)
    }

    override fun onBindViewHolder(items: List<ModuleItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val dashboardNewsHolder = holder as DashboardNewsHolder
        dashboardNewsHolder.showNewsOnDashboard((items[position] as DashboardNewsModule.DashboardNewsModuleData))
    }

    class DashboardNewsHolder(override val containerView: View, private val dashboardNewsModule: DashboardNewsModule)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun showNewsOnDashboard(dashboardNewsModuleData: DashboardNewsModule.DashboardNewsModuleData) {
            dashboardNewsModule.showNewsOnDashboard(itemView, dashboardNewsModuleData)
        }
    }
}