package com.github.star

import org.gradle.api.Named

abstract class ResReplacement extends Envvar implements Named {
    protected int width = -1, height = -1

    def size(int width, int height) {
        this.width = width
        this.height = height
        return this
    }
}
