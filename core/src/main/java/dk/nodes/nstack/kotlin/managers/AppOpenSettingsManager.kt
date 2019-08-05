package dk.nodes.nstack.kotlin.managers

import dk.nodes.nstack.kotlin.models.AppOpenSettings

interface AppOpenSettingsManager {
    fun getAppOpenSettings(): AppOpenSettings
    fun setUpdateDate()
}