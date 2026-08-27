package com.kanagawa.yamada.anyathermal

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CheckRoot {
    val thermalStateFlow = kotlinx.coroutines.flow.MutableSharedFlow<Boolean>(extraBufferCapacity = 1, onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST)

    suspend fun isRooted(): Boolean = withContext(Dispatchers.IO) {
        // Initializes the global shell and checks for root.
        // This is universally compatible with Magisk, KernelSU, APatch, etc.
        Shell.getShell().isRoot
    }

    suspend fun isThermalDisabled(): Boolean = withContext(Dispatchers.IO) {
        val result = Shell.cmd("cat /data/adb/modules/AnyaMelfissa/AnyaConfig.txt").exec()
        val out = result.out.joinToString("\n")
        out.contains("ANYA 1")
    }
}
