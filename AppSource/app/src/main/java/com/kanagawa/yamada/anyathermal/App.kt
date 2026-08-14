package com.kanagawa.yamada.anyathermal

import android.app.Application
import com.topjohnwu.superuser.Shell

class App : Application() {
    init {
        Shell.setDefaultBuilder(
            Shell.Builder.create()
                .setFlags(Shell.FLAG_REDIRECT_STDERR)
                .setTimeout(10)
        )
    }
}
