package com.example.riotnews

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.riotnews.ui.theme.RiotNewsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiotNewsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                            name = "Riot News",
                            modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
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
        Image(
            painter = painterResource(id = R.drawable.riot_logo),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = if (isLandscape) Modifier.padding(8.dp) else Modifier.padding(32.dp)
        )
        Text(
            text = "Welcome to ${name}",
            fontSize = 32.sp,
            color = Color(0xFF0000FF),
            modifier = if (isLandscape) Modifier.padding(8.dp) else Modifier.padding(32.dp)
        )
        Button(
            onClick = {
                context.startActivity(Intent(context, DashboardActivity::class.java))
            }) {
            Text(
                text = "Access app",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        Text(
            text = "Made by Alexandra Wolter & Erwann Lesech",
            fontSize = 18.sp,
            modifier = Modifier.padding(32.dp)
        )
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RiotNewsTheme {
        Greeting("Riot News")
    }
}