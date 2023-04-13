package com.example.jpgtopng_mvp.ui

interface Contract {

    interface View {
        fun showProcess(inProcess: Boolean)
        fun showResult(message: String)
    }

    interface Presenter {
        fun buttonClicked(imagePath: String?)
    }

}