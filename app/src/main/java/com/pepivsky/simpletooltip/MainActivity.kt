package com.pepivsky.simpletooltip

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.grupogigante.officedepot.Common.CustomView.TooltipDialog
import com.pepivsky.simpletooltip.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        lateinit var binding: ActivityMainBinding

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        binding.tvHello.setOnClickListener {
            TooltipDialog.Builder(this)
                .onContentLoadedListener { view ->
                    (view as TextView).text = "Mostrando el Tooltip"
                }
                .build()
                .showTextTooltipDialog(it)
        }
    }
}