package com.kanagawa.yamada.anyathermal

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CheckRoot {
    suspend fun isRooted(): Boolean = withContext(Dispatchers.IO) {
        Shell.isAppGrantedRoot() == true
    }

    suspend fun isThermalDisabled(): Boolean = withContext(Dispatchers.IO) {
        val result = Shell.cmd("ps -A | grep thermal").exec()
        val out = result.out.joinToString("\n")
        !out.contains("thermald") && out.isNotEmpty()
    }
}
