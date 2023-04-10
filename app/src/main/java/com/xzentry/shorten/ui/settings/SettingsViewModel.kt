package com.xzentry.shorten.ui.settings

import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.ui.base.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    sharedPrefs: PreferencesHelper
) : BaseViewModel<SettingsActivity>() {


}
