package com.kanagawa.yamada.anyathermal

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CheckRoot {
    suspend fun isRooted(): Boolean = withContext(Dispatchers.IO) {
        // Initializes the global shell and checks for root.
        // This is universally compatible with Magisk, KernelSU, APatch, etc.
        Shell.getShell().isRoot
    }

    suspend fun isThermalDisabled(): Boolean = withContext(Dispatchers.IO) {
        val result = Shell.cmd("mount | grep -E 'fake_temp|fake_trip'").exec()
        val out = result.out.joinToString("\n")
        out.isNotEmpty()
    }
}
