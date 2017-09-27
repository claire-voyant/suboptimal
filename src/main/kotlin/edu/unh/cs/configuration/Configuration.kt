package edu.unh.cs.configuration

data class Configuration(val domain: String, val algorithm: String, val bound: String,
                         val height: String, val width: String, val problem: List<String>)