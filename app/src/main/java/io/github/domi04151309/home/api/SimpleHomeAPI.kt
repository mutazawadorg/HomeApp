package io.github.domi04151309.home.api

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import io.github.domi04151309.home.helpers.Global.volleyError
import io.github.domi04151309.home.R
import io.github.domi04151309.home.data.UnifiedRequestCallback
import io.github.domi04151309.home.helpers.Global
import io.github.domi04151309.home.interfaces.HomeRecyclerViewHelperInterface

class SimpleHomeAPI(
    c: Context,
    deviceId: String,
    recyclerViewInterface: HomeRecyclerViewHelperInterface?
) : UnifiedAPI(c, deviceId, recyclerViewInterface) {

    init {
        dynamicSummaries = false
    }

    override fun loadList(callback: CallbackInterface) {
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url + "commands", null,
            { response ->
                callback.onItemsLoaded(
                    UnifiedRequestCallback(
                        SimpleHomeAPIParser(c.resources).parseResponse(response),
                        deviceId
                    ),
                    recyclerViewInterface
                )
            },
            { error ->
                callback.onItemsLoaded(UnifiedRequestCallback(null, deviceId, volleyError(c, error)), null)
            }
        )
        queue.add(jsonObjectRequest)
    }

    override fun execute(path: String, callback: CallbackInterface) {
        val splitCharPos = path.lastIndexOf('@')
        val realPath = path.substring(splitCharPos + 1)
        when (path.substring(0, splitCharPos)) {
            "none", "switch" -> { }
            "input" -> {
                val nullParent: ViewGroup? = null
                val view = LayoutInflater.from(c).inflate(R.layout.dialog_input, nullParent, false)
                val input = view.findViewById<EditText>(R.id.input)
                AlertDialog.Builder(c)
                    .setTitle(R.string.input_title)
                    .setView(view)
                    .setPositiveButton(R.string.str_send) { _, _ ->
                        val jsonObjectRequest = JsonObjectRequest(
                            Request.Method.GET,
                            url + realPath + "?input=" + input.text.toString(),
                            null,
                            { },
                            { e -> Log.e(Global.LOG_TAG, e.toString()) }
                        )
                        queue.add(jsonObjectRequest)
                    }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> }
                    .show()
            }
            else -> {
                val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url + realPath, null,
                    { response ->
                        callback.onExecuted(
                            response.optString("toast", c.resources.getString(R.string.main_execution_completed)),
                            response.optBoolean("refresh", false)
                        )
                    },
                    { error ->
                        callback.onExecuted(volleyError(c, error))
                    }
                )
                queue.add(jsonObjectRequest)
            }
        }
    }

    override fun changeSwitchState(id: String, state: Boolean) {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            url + id.substring(id.lastIndexOf('@') + 1) + "?input=" + (if (state) 1 else 0),
            null,
            { },
            { e -> Log.e(Global.LOG_TAG, e.toString()) }
        )
        queue.add(jsonObjectRequest)
    }
}