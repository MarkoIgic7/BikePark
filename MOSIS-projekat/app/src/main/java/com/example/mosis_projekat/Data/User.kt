package com.example.mosis_projekat.Data

data class User(val uid:String?, val email:String, val password: String, val name: String?= null, val surname: String ?= null, val number: String ?= null,
                var points: Int, val imgName:String, val mySpots: ArrayList<String> = ArrayList<String>())
