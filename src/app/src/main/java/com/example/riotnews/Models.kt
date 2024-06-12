package com.example.riotnews.models

data class AccountInfo(
    val accountName: String,
    val accountTag: String,
    val accountPuuid: String,
    val accountLvl: Int,
    val accountIcon: String,
    val accountElo: String,
    val accountLPs: Int
)