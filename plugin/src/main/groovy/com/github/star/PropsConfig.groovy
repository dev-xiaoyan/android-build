package com.github.star

import org.gradle.api.Named

abstract class PropsConfig extends Envvar implements Named {
    def value(String value) {
        this.value = value
        ext[name] = value
        return this
    }
}
