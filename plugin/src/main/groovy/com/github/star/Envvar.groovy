package com.github.star

class Envvar {
    String value = ""
    String description = ""

    def envvar(String... keys) {
        keys.find { key ->
            def env = System.getenv(key)
            if (env != null && env.trim() != "") {
                value(env)
            }
        }
        return this
    }

    def value(String value) {
        this.value = value
        return this
    }

    def description(String description) {
        this.description = description
        return this
    }

    boolean isPresent() {
        return value != null && value.trim() != ""
    }
}
