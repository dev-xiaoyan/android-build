package com.github.star

class AmazonConfig {
    Envvar key = new Envvar()
    Envvar secret = new Envvar()
    Envvar bucket = new Envvar()
    Envvar region = new Envvar()
    Envvar dirs = new Envvar()

    boolean available() {
        return key.present && secret.present && bucket.present && region.present && dirs.present
    }
}
