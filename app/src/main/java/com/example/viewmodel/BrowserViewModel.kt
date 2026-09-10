package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.example.model.Workspace
import com.example.web.TradingViewScriptHelper
import com.example.web.WebEngineManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

class BrowserViewModel(application: Application) : AndroidViewModel(application) {

  val webEngine = WebEngineManager(application.applicationContext)

  private val prefs = application.getSharedPreferences("triple_chart_prefs", Context.MODE_PRIVATE)

  // Set of window IDs currently hidden (0, 1, or 2)
  private val _hiddenWindows = MutableStateFlow<Set<Int>>(emptySet())
  val hiddenWindows: StateFlow<Set<Int>> = _hiddenWindows.asStateFlow()

  // Currently maximized window ID, or null if normal multi-window mode
  private val _maximizedWindowId = MutableStateFlow<Int?>(null)
  val maximizedWindowId: StateFlow<Int?> = _maximizedWindowId.asStateFlow()

  // Workspaces list
  private val _workspaces = MutableStateFlow<List<Workspace>>(emptyList())
  val workspaces: StateFlow<List<Workspace>> = _workspaces.asStateFlow()

  private val _activeWorkspaceId = MutableStateFlow<String>("crypto")
  val activeWorkspaceId: StateFlow<String> = _activeWorkspaceId.asStateFlow()

  // Dialog states
  private val _editingWindowId = MutableStateFlow<Int?>(null)
  val editingWindowId: StateFlow<Int?> = _editingWindowId.asStateFlow()

  private val _isSaveWorkspaceDialogOpen = MutableStateFlow(false)
  val isSaveWorkspaceDialogOpen: StateFlow<Boolean> = _isSaveWorkspaceDialogOpen.asStateFlow()

  // Loupe magnifier zoom factor
  private val _magnifierZoom = MutableStateFlow(2.5f)
  val magnifierZoom: StateFlow<Float> = _magnifierZoom.asStateFlow()

  init {
    loadWorkspaces()
  }

  private fun loadWorkspaces() {
    val list = mutableListOf<Workspace>()
    list.addAll(Workspace.PRESETS)

    // Load custom workspaces from SharedPreferences
    val savedJson = prefs.getString("custom_workspaces", null)
    if (!savedJson.isNullOrEmpty()) {
      try {
        val array = JSONArray(savedJson)
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          val id = obj.getString("id")
          val name = obj.getString("name")
          val emoji = obj.optString("iconEmoji", "📌")
          val urlsJson = obj.getJSONArray("urls")
          val urls = mutableListOf<String>()
          for (j in 0 until urlsJson.length()) {
            urls.add(urlsJson.getString(j))
          }
          list.add(Workspace(id = id, name = name, iconEmoji = emoji, urls = urls, isCustom = true))
        }
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
    _workspaces.value = list
  }

  fun toggleHideWindow(windowId: Int) {
    // If currently maximized, reset maximize first
    if (_maximizedWindowId.value == windowId) {
      _maximizedWindowId.value = null
    }

    _hiddenWindows.update { current ->
      if (current.contains(windowId)) {
        current - windowId
      } else {
        // Prevent hiding all 3 windows (must keep at least 1 visible)
        if (current.size >= 2) current else current + windowId
      }
    }
  }

  fun toggleMaximizeWindow(windowId: Int) {
    if (_maximizedWindowId.value == windowId) {
      // Restore
      _maximizedWindowId.value = null
    } else {
      // If the window was hidden, unhide it first
      _hiddenWindows.update { it - windowId }
      _maximizedWindowId.value = windowId
    }
  }

  fun restoreAllWindows() {
    _maximizedWindowId.value = null
    _hiddenWindows.value = emptySet()
  }

  fun selectWorkspace(workspace: Workspace) {
    _activeWorkspaceId.value = workspace.id
    webEngine.switchWorkspaceUrls(workspace.urls)
  }

  fun saveCurrentWorkspace(name: String) {
    if (name.isBlank()) return
    val currentUrls = webEngine.windowStates.value.map { it.currentUrl }
    val newWorkspace = Workspace(
      id = "custom_" + System.currentTimeMillis(),
      name = name.trim(),
      iconEmoji = "⭐",
      urls = currentUrls,
      isCustom = true
    )

    val updatedList = _workspaces.value + newWorkspace
    _workspaces.value = updatedList
    _activeWorkspaceId.value = newWorkspace.id

    // Persist to SharedPreferences
    val customOnly = updatedList.filter { it.isCustom }
    val array = JSONArray()
    for (ws in customOnly) {
      val obj = JSONObject()
      obj.put("id", ws.id)
      obj.put("name", ws.name)
      obj.put("iconEmoji", ws.iconEmoji)
      val urlsArr = JSONArray()
      ws.urls.forEach { urlsArr.put(it) }
      obj.put("urls", urlsArr)
      array.put(obj)
    }
    prefs.edit().putString("custom_workspaces", array.toString()).apply()
    _isSaveWorkspaceDialogOpen.value = false
  }

  fun deleteCustomWorkspace(workspaceId: String) {
    val updatedList = _workspaces.value.filterNot { it.id == workspaceId && it.isCustom }
    _workspaces.value = updatedList

    val customOnly = updatedList.filter { it.isCustom }
    val array = JSONArray()
    for (ws in customOnly) {
      val obj = JSONObject()
      obj.put("id", ws.id)
      obj.put("name", ws.name)
      obj.put("iconEmoji", ws.iconEmoji)
      val urlsArr = JSONArray()
      ws.urls.forEach { urlsArr.put(it) }
      obj.put("urls", urlsArr)
      array.put(obj)
    }
    prefs.edit().putString("custom_workspaces", array.toString()).apply()

    if (_activeWorkspaceId.value == workspaceId) {
      _activeWorkspaceId.value = "crypto"
    }
  }

  fun openUrlEditDialog(windowId: Int) {
    _editingWindowId.value = windowId
  }

  fun closeUrlEditDialog() {
    _editingWindowId.value = null
  }

  fun openSaveWorkspaceDialog() {
    _isSaveWorkspaceDialogOpen.value = true
  }

  fun closeSaveWorkspaceDialog() {
    _isSaveWorkspaceDialogOpen.value = false
  }

  fun setMagnifierZoom(zoom: Float) {
    _magnifierZoom.value = zoom.coerceIn(1.5f, 4.0f)
  }

  override fun onCleared() {
    super.onCleared()
    webEngine.destroyAll()
  }
}
