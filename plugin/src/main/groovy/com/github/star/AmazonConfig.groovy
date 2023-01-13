package com.github.star

class AmazonConfig {
    Envvar key = new Envvar()
    Envvar secret = new Envvar()
    Envvar bucket = new Envvar()
    Envvar region = new Envvar()
    Envvar dirs = new Envvar()

    boolean isAvailable() {
        return key.present && secret.present && bucket.present && region.present && dirs.present
    }

    def error() {
        def builder = new StringBuilder()
        if (!key.present) {
            builder.append("缺少必须参数:key ")
        }
        if (!secret.present) {
            builder.append("缺少必须参数:secret ")
        }
        if (!bucket.present) {
            builder.append("缺少必须参数:bucket ")
        }
        if (!region.present) {
            builder.append("缺少必须参数:region ")
        }
        if (!dirs.present) {
            builder.append("缺少必须参数:dirs ")
        }
        return builder.toString()
    }
}
