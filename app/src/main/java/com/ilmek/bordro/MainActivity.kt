package com.ilmek.bordro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ilmek.bordro.ui.nav.BordroNavHost
import com.ilmek.bordro.ui.theme.BordroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as BordroApplication).repository
        setContent {
            BordroTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BordroNavHost(repository)
                }
            }
        }
    }
}
