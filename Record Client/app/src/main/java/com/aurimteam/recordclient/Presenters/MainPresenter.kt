package com.aurimteam.recordclient.Presenters

import android.content.Context
import com.aurimteam.recordclient.IMainModel
import com.aurimteam.recordclient.IMainView
import com.aurimteam.recordclient.Models.MainModel

class MainPresenter(private var view: IMainView?, private val model: IMainModel?) :
    MainModel.OnFinishedListener {
    override fun onResultSuccess(path: String) {
        view?.setImage(path)
    }

    override fun onResultFail(strError: String?) {

    }

    fun getImage(context: Context?) {
        model?.getImageData(context, this)
    }

    fun attachView(view: IMainView?) {
        this.view = view
    }

    fun detachView() {
        view = null
    }

    fun onDestroy() {
        view = null
    }
}