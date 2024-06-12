package com.example.riotnews.utils

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.riotnews.R
import com.example.riotnews.models.AccountInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

fun makeVolleyRequest(context: Context, urlString: String, apiKey: String, onResponse: (response: JSONObject?) -> Unit, onError: (error: String) -> Unit) {
    val queue = Volley.newRequestQueue(context)
    val request = object : JsonObjectRequest(
        Request.Method.GET, urlString, null,
        Response.Listener { response ->
            onResponse(response)
        },
        Response.ErrorListener { error ->
            onError(error.message ?: "Unknown error occurred")
        }) {
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["X-Riot-Token"] = apiKey
            return headers
        }
    }
    queue.add(request)
    println(request.headers.toString())
}

fun makeVolleyJsonArrayRequest(
    context: Context,
    urlString: String,
    apiKey: String,
    onResponse: (response: JSONArray?) -> Unit,
    onError: (error: String) -> Unit
) {
    val queue = Volley.newRequestQueue(context)

    val request = object : JsonArrayRequest(
        Request.Method.GET, urlString, null,
        Response.Listener { response ->
            onResponse(response)
        },
        Response.ErrorListener { error ->
            onError(error.message ?: "Unknown error occurred")
        }) {
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()
            headers["X-Riot-Token"] = apiKey
            return headers
        }
    }

    queue.add(request)
}

fun addFriend(context: Context, pseudo: String, tag: String, onResult: (AccountInfo?) -> Unit) {
    val lolApiKey = context.getString(R.string.lolAPIKey)
    val cleanApiKey = lolApiKey.replace("–", "-")  // Replace en dash with hyphen if present

    getLolAccount(context, pseudo, tag, cleanApiKey,
        onResponse = { friend ->
            println("success: $friend")
            onResult(friend)
        },
        onError = { error ->
            println("err: $error")
            Log.e("addFriend", "Error adding friend: $error")
            onResult(null)
        }
    )
}

fun getLolAccount(context: Context, gameName: String, tagLine: String, lolApiKey: String, onResponse: (friend: AccountInfo) -> Unit, onError: (error: String) -> Unit) {
    val accountUrl = "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/$gameName/$tagLine"
    makeVolleyRequest(context, accountUrl, lolApiKey,
        onResponse = { accountResponse ->
            try {
                println(accountResponse)
                val accountPuuid = accountResponse?.getString("puuid") ?: ""
                val summonerUrl = "https://euw1.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/$accountPuuid"
                makeVolleyRequest(context, summonerUrl, lolApiKey,
                    onResponse = { summonerResponse ->
                        try {
                            println(summonerResponse)
                            val accountLvl = summonerResponse?.getInt("summonerLevel") ?: 0
                            val accountId = summonerResponse?.getString("id") ?: ""
                            val accountIcon = "https://ddragon.leagueoflegends.com/cdn/11.16.1/img/profileicon/${summonerResponse?.getInt("profileIconId")}.png"

                            // Fetch ranks for the summoner
                            val ranksUrl = "https://euw1.api.riotgames.com/lol/league/v4/entries/by-summoner/$accountId"
                            makeVolleyJsonArrayRequest(context, ranksUrl, lolApiKey,
                                onResponse = { ranksResponse ->
                                    try {
                                        println(ranksResponse)
                                        if (ranksResponse != null && ranksResponse.length() > 0) {
                                            val rankObject = ranksResponse.getJSONObject(0) // Assuming the first entry is the most relevant one
                                            val rank = (rankObject.optString("tier", "unknown") + " " + rankObject.optString("rank", "unknown")).trim()
                                            val leaguePoints = rankObject.optInt("leaguePoints", 0)
                                            val friend = AccountInfo(gameName, tagLine, accountPuuid, accountLvl, accountIcon, rank, leaguePoints)
                                            onResponse(friend)
                                        } else {
                                            onError("No rank data found")
                                        }
                                    } catch (e: Exception) {
                                        onError("Error parsing ranks data: ${e.message}")
                                    }
                                },
                                onError = { error ->
                                    onError("Error fetching ranks data: $error")
                                }
                            )
                        } catch (e: Exception) {
                            onError("Error parsing summoner data: ${e.message}")
                        }
                    },
                    onError = { error ->
                        onError("Error fetching summoner data: $error")
                    }
                )
            } catch (e: Exception) {
                onError("Error parsing account data: ${e.message}")
            }
        },
        onError = { error ->
            onError("Error fetching account data: $error")
        }
    )
}

fun saveFriendList(context: Context, friendList: List<AccountInfo>) {
    val sharedPreferences = context.getSharedPreferences("FriendListPrefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val json = gson.toJson(friendList)
    editor.putString("FriendList", json)
    editor.apply()
}

fun loadFriendList(context: Context): MutableList<AccountInfo> {
    val sharedPreferences = context.getSharedPreferences("FriendListPrefs", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPreferences.getString("FriendList", null)
    val type = object : TypeToken<MutableList<AccountInfo>>() {}.type
    return gson.fromJson(json, type) ?: mutableListOf()
}