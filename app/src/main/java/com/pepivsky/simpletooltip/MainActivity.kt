package com.pepivsky.simpletooltip

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//import com.pepivsky.simpletooltip.TooltipDialog
import com.pepivsky.simpletooltip.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        lateinit var binding: ActivityMainBinding
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.tvHello.setOnClickListener {
            /*com.pepivsky.simpletooltip.TooltipDialog.Builder(this)
                .onContentLoadedListener { view ->
                    (view as TextView).text = "Mostrando el Tooltip"
                }
                .build()
                .showTextTooltipDialog(it)*/

            TooltipDialog.Builder(this)
                .onContentLoadedListener { view ->
                    (view as TextView).text = "Hello I am a Tooltip"
                }
                .build()
                .showTextTooltipDialog(it)
        }
    }
}