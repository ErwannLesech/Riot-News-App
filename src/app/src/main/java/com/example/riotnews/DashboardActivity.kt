package com.example.riotnews

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.riotnews.ui.theme.RiotNewsTheme
import com.example.riotnews.ui.DashboardGreeting
import com.example.riotnews.models.AccountInfo
import com.example.riotnews.utils.loadFriendList
import com.example.riotnews.utils.saveFriendList

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiotNewsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardGreeting(
                        modifier = Modifier.padding(innerPadding),
                        loadFriendList = { loadFriendList(this) },
                        saveFriendList = { friendList -> saveFriendList(this, friendList) }
                    )
                }
            }
        }
    }
}