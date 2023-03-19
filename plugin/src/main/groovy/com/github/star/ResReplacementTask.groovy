package com.github.star

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class ResReplacementTask extends DefaultTask {
    @Internal
    abstract Property<AndroidBuildService> getService()

    @Input
    abstract ListProperty<ResReplacement> getReplacements()

    @TaskAction
    def doReplacement() {
        def service = service.get()
        def baseDir = "app/src/${service.flavor.get()}/res/"
        replacements.get().findAll {
            it.present
        }.each { ResReplacement replacement ->
            def file = project.file("$baseDir${replacement.name}.webp")
            def dir = file.parentFile
            dir.mkdirs()
            if (replacement.description != "") {
                println("开始下载资源文件:${replacement.description}")
            }
            println("开始下载资源文件:${replacement.value}")
            println("资源文件保存地址为:${file.path}")
            InputStream byteStream = service.download(replacement.value).body().byteStream()
            switch (replacement.resType) {
                case "image": {
                    service.webp(byteStream, file.path, replacement.width, replacement.height)
                    def fileTree = project.fileTree(dir)
                    fileTree.matching {
                        include("${replacement.name}.*")
                        exclude("${replacement.name}.webp")
                    }.each {
                        it.delete()
                        println("正在删除冲突资源文件:${it.path}")
                    }
                }
                    break
                case "raw": {
                    service.saveFile(byteStream, file.path)
                }
                    break
            }

        }
    }
}
