package com.example.riotnews.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.riotnews.R
import com.example.riotnews.models.AccountInfo
import com.example.riotnews.utils.addFriend

@Composable
fun DashboardGreeting(
    modifier: Modifier = Modifier,
    loadFriendList: () -> MutableList<AccountInfo>,
    saveFriendList: (List<AccountInfo>) -> Unit
) {
    val context = LocalContext.current
    var pseudo by remember { mutableStateOf("Darkouh") }
    var tag by remember { mutableStateOf("EUW") }
    val friendList = remember { mutableStateListOf<AccountInfo>().apply { addAll(loadFriendList()) } }

    Image(
        painter = painterResource(id = R.drawable.bg_main),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = modifier.fillMaxSize()
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Find your friends",
            fontSize = 24.sp,
            color = Color(0xFFFF0000),
            modifier = Modifier.padding(16.dp)
        )
        Row {
            TextField(value = pseudo, onValueChange = { input -> pseudo = input })
            TextField(value = tag, onValueChange = { input -> tag = input })
        }
        Button(
            onClick = {
                addFriend(context, pseudo, tag) { acc ->
                    if (acc != null) {
                        if (!friendList.contains(acc)) {
                            friendList.add(acc)
                            saveFriendList(friendList)
                            Toast.makeText(context, "${pseudo}#${tag} added to your friend list", Toast.LENGTH_SHORT).show()
                        }
                        for (index in friendList.indices) {
                            println("friend $index : ${friendList[index].accountName}")
                        }
                        println("list")
                        println(friendList)
                    } else {
                        println("Failed to add friend")
                        Toast.makeText(context, "${pseudo}#${tag} doesn't exist", Toast.LENGTH_SHORT).show()
                    }
                }
            }) {
            Text(
                text = "Add friend",
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )
        }
        FriendListDisplay(friendList = friendList, onRemoveFriend = { friend -> friendList.remove(friend); saveFriendList(friendList) })
    }
}

@Composable
fun FriendListDisplay(friendList: List<AccountInfo>, onRemoveFriend: (AccountInfo) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        items(friendList) { friend ->
            FriendItem(friend, onRemoveFriend)
        }
    }
}

@Composable
fun FriendItem(friend: AccountInfo, onRemoveFriend: (AccountInfo) -> Unit) {
    println(friend.accountElo)
    val textContent: String = if (friend.accountElo == "unknown unknown") {
        " ${friend.accountName} - lvl ${friend.accountLvl} - Unranked"
    } else {
        " ${friend.accountName} - lvl ${friend.accountLvl} - ${friend.accountElo} ${friend.accountLPs}"
    }
    println(friend.accountIcon)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .border(2.dp, Color.Black)
            //.clickable { expanded = !expanded }
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(friend.accountIcon)
                    .error(R.drawable.pp) // Your local image resource
                    .build(),
                contentDescription = "profile pricture",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.size(50.dp)//.padding(end = 8.dp)
            )
            Text(
                text = textContent,
                modifier = Modifier.padding(end = 8.dp),
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = { onRemoveFriend(friend) },
            ) {
                Text(
                    text = "X",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}