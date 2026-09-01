package com.example.enigmafocus.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.example.enigmafocus.data.AppPreferences
import com.example.enigmafocus.manager.GrayscaleManager

@RequiresApi(Build.VERSION_CODES.N)
class GrayscaleTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        if (GrayscaleManager.hasSecureSettingsPermission(this)) {
            GrayscaleManager.toggleGrayscale(this)
            updateTileState()
        } else {
            // Permission needed
            qsTile?.state = Tile.STATE_UNAVAILABLE
            qsTile?.updateTile()
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val isEng = AppPreferences.isEnglish()
        if (!GrayscaleManager.hasSecureSettingsPermission(this)) {
            tile.state = Tile.STATE_INACTIVE
            tile.subtitle = if (isEng) "Requires ADB grant" else "Requiere permiso ADB"
        } else {
            val isActive = GrayscaleManager.isGrayscaleActive(this)
            tile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.subtitle = if (isActive) {
                if (isEng) "Monochrome ON" else "Activado"
            } else {
                if (isEng) "Full Color" else "Desactivado"
            }
        }
        tile.updateTile()
    }
}
