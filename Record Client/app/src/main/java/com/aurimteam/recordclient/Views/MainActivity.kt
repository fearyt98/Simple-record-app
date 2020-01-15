package com.aurimteam.recordclient.Views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import com.aurimteam.recordclient.IMainView
import com.aurimteam.recordclient.Models.MainModel
import com.aurimteam.recordclient.Presenters.MainPresenter
import com.aurimteam.recordclient.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity(), IMainView {

    private val presenter = MainPresenter(this, MainModel())
    private var hide = true
    private val context: Context = this
    private val timer = object : CountDownTimer(110000000000000, 250) {
        override fun onTick(millisUntilFinished: Long) {
            presenter.getImage(context)
        }
        override fun onFinish() {
            start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        //generalButton.setOnClickListener {  }
        timer.start()
    }

    override fun setImage(path: String) {
        if (!hide) hide = false
        videoContainer.visibility = View.VISIBLE
        val imgFile = File(path)
        if (imgFile.exists()) {
            val imageBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            videoContainer.setImageBitmap(imageBitmap);
        }
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
        presenter.onDestroy()
    }

    private fun clickBtn(){
        if (generalButton.text == "Начать") {
            timer.start()
            generalButton.text = "Остановить"
        }
        if (generalButton.text == "Начать") {
            timer.start()
            generalButton.text = "Остановить"
        }
    }
}
