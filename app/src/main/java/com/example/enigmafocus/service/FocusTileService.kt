package com.example.enigmafocus.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.manager.FocusSessionManager

@RequiresApi(Build.VERSION_CODES.N)
class FocusTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        FocusSessionManager.toggleSession(this, 25)
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isActive = AppPreferences.isFocusActive()
        tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.subtitle = if (isActive) "Enfoque Activo" else "Iniciar 25 min"
        tile.updateTile()
    }
}
