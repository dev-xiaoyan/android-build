package com.github.star

class AmazonConfig {
    Envvar key = new Envvar()
    Envvar secret = new Envvar()
    Envvar bucket = new Envvar()
    Envvar region = new Envvar()
    Envvar dirs = new Envvar()

    boolean available() {
        return key.value != "" && secret.value != "" && bucket.value != "" && region.value != "" && dirs.value != ""
    }
}
