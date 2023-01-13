package com.github.star

class ServerConfig {
    Envvar id = new Envvar()
    Envvar api = new Envvar()

    def error() {
        def builder = new StringBuilder()
        if (!id.present) {
            builder.append("缺少必须参数:id ")
        }
        if (!api.present) {
            builder.append("缺少必须参数:api ")
        }
        return builder.toString()
    }

    boolean isAvailable() {
        return id.present && api.present
    }
}
