package com.example.riotnews

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riotnews.ui.theme.RiotNewsTheme
import com.typesafe.config.ConfigFactory
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.Serializable
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiotNewsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardGreeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class AccountInfo(
    val accountName: String,
    val accountId: String,
    val accountLvl: Int,
    val accountIcon: String,
)

@Composable
fun DashboardGreeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    //var pseudo by remember { mutableStateOf("pseudo here...")}
    //var tag by remember { mutableStateOf("tag here...")}
    var pseudo by remember { mutableStateOf("LFT R One")}
    var tag by remember { mutableStateOf("EUWR1")}
    var friendAccount by remember { mutableStateOf<AccountInfo?>(null) }
    Image(
        painter = painterResource(id = R.drawable.bg_main),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxSize()
    )
    Column (
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "Find your friends",
            fontSize = 24.sp,
            color = Color(0xFF0000FF),
            modifier = Modifier.padding(16.dp)
        )
        Row {
            TextField(value = pseudo, onValueChange = {input -> pseudo = input})
            TextField(value = tag, onValueChange = {input -> tag = input})
        }
        Button(
            onClick = {
                Toast.makeText(context, "${pseudo}#${tag} adding to your friend list", Toast.LENGTH_SHORT).show()
                friendAccount = addFriend(context, pseudo, tag)
                Log.d("pizzaD", "test")
                friendAccount?.let { Log.d("pizzaD", it.accountName) }
            }) {
            Text(
                text = "Add friend",
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        friendAccount?.let { account ->
            Log.d("pizza", "test")
            Log.d("pizza", account.accountName);
            Text(
                text = "${account.accountName} is lvl : ${account.accountLvl}",
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardGreetingPreview() {
    RiotNewsTheme {
        DashboardGreeting()
    }
}

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

fun addFriend(context: Context, pseudo: String, tag: String): AccountInfo? {
    val lolApiKey = context.getString(R.string.lolAPIKey)
    var friendAccount: AccountInfo? = null

    getLolAccount(context, pseudo, tag, lolApiKey,
        onResponse = { friend ->
            friendAccount = friend
        },
        onError = { error ->
            Log.e("addFriend", "Error adding friend: $error")
        }
    )

    return friendAccount
}
fun getLolAccount(context: Context, gameName: String, tagLine: String, lolApiKey: String, onResponse: (friend: AccountInfo) -> Unit, onError: (error: String) -> Unit) {
    val accountUrl = "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/$gameName/$tagLine"
    makeVolleyRequest(context, accountUrl, lolApiKey,
        onResponse = { accountResponse ->
            try {
                val accountId = accountResponse?.getString("puuid") ?: ""
                val summonerUrl = "https://euw1.api.riotgames.com/lol/summoner/v4/summoners/by-puuid/$accountId"
                makeVolleyRequest(context, summonerUrl, lolApiKey,
                    onResponse = { summonerResponse ->
                        try {
                            if (summonerResponse != null) {
                                Log.e("request", summonerResponse.getString("name"))
                            }
                            else
                            {
                                Log.e("request", "null response")
                            }
                            val accountName = summonerResponse?.getString("name") ?: ""
                            val accountLvl = summonerResponse?.getInt("summonerLevel") ?: 0
                            val accountIcon = "http://ddragon.leagueoflegends.com/cdn/11.16.1/img/profileicon/${summonerResponse?.getInt("profileIconId")}.png"
                            val id = summonerResponse?.getString("id") ?: ""
                            val friend = AccountInfo(accountName, accountId, accountLvl, accountIcon)
                            onResponse(friend)
                        } catch (e: Exception) {
                            Log.e("request", "null response")
                            onError("Error parsing summoner data: ${e.message}")
                        }
                    },
                    onError = { error ->
                        Log.e("request", error)
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