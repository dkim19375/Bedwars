package me.dkim19375.bedwars.plugin.data

import me.dkim19375.dkimcore.extension.toUUID
import java.util.*

data class UserCacheJson(
    val name: String,
    val uuid: String,
    val expiresOn: String
) {
    fun getRealUUID(): UUID? = uuid.toUUID()
}