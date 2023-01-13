package com.github.star

class Envvar {
    String value = ""

    def envvar(String... keys) {
        keys.find { key ->
            def env = System.getenv(key)
            if (env != null) {
                value(env)
            }
        }
        return this
    }

    def value(String value) {
        this.value = value
        return this
    }

    boolean isPresent() {
        return value != null && value.trim() != ""
    }
}
