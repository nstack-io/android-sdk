package dk.nodes.nstack.managers

import dk.nodes.nstack.models.AppOpenSettings

interface AppOpenSettingsManager {
    fun getAppOpenSettings(): AppOpenSettings
    fun setUpdateDate()
}