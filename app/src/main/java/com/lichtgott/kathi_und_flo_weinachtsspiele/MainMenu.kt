package com.lichtgott.kathi_und_flo_weinachtsspiele

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.lichtgott.kathi_und_flo_weinachtsspiele.mine_sweeper.MineSweeperActivity

class MainMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_menu)

        findViewById<Button>(R.id.btnActivityA).setOnClickListener {
            val intent = Intent(this, WordleActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnActivityB).setOnClickListener {
            val intent = Intent(this, MineSweeperActivity::class.java)
            startActivity(intent)
        }

        findViewById<ImageButton>(R.id.btnExit).setOnClickListener {
            finishAffinity()
        }
    }
}