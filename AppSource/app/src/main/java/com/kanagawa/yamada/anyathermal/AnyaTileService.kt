package com.kanagawa.yamada.anyathermal

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import android.util.Log

class AnyaTileService : TileService() {
    private val serviceJob = Job()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartListening() {
        super.onStartListening()
        Log.d("AnyaTile", "onStartListening called")
        updateTileState(null)
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        Log.d("AnyaTile", "onClick called")

        coroutineScope.launch {
            val isCurrentlyDisabled = CheckRoot.isThermalDisabled()
            Log.d("AnyaTile", "Currently disabled: $isCurrentlyDisabled")
            
            val arg = if (isCurrentlyDisabled) "0" else "1"
            val newState = arg == "1"
            
            // Execute the binary to toggle thermal state
            Log.d("AnyaTile", "Executing AnyaMelfissa $arg")
            val scriptCommand = if (arg == "1") "su -c '/data/adb/modules/AnyaMelfissa/AnyaMelfissa 1'" else "su -c '/data/adb/modules/AnyaMelfissa/AnyaMelfissa 0'"
            Shell.cmd(scriptCommand).exec()
            
            // Optimistically update the tile state without querying mount again immediately
            updateTileState(newState)
            CheckRoot.thermalStateFlow.tryEmit(newState)
        }
    }

    private fun updateTileState(forcedState: Boolean?) {
        coroutineScope.launch {
            val isDisabled = forcedState ?: CheckRoot.isThermalDisabled()
            Log.d("AnyaTile", "updateTileState applying isDisabled=$isDisabled")
            
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                val tile = qsTile ?: return@withContext
                if (isDisabled) {
                    tile.state = Tile.STATE_ACTIVE
                    tile.label = "Anya Thermal"
                } else {
                    tile.state = Tile.STATE_INACTIVE
                    tile.label = "Anya Normal"
                }
                tile.updateTile()
                Log.d("AnyaTile", "tile.updateTile() called on Main thread with state=${tile.state}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
