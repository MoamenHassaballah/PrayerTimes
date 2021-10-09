package com.moaapps.prayertimesdemo.utils

import android.content.Context
import com.moaapps.prayertimesdemo.utils.Constants.LANGUAGE
import java.util.*

object SetAppLocale {


    fun setAppLocale(context: Context){
        val lang = TinyDB(context).getString(LANGUAGE)
        val language = if (lang.isNullOrEmpty()){
            val defLanguage = Locale.getDefault().language
            TinyDB(context).putString(LANGUAGE, defLanguage)
            defLanguage
        } else lang

        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = context.resources.configuration
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }
}