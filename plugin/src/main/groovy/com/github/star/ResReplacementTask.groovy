package com.github.star

import org.apache.commons.io.FilenameUtils
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
            def file = project.file("$baseDir${replacement.name}")
            def dir = file.parentFile
            dir.mkdirs()
            if (replacement.description != "") {
                println("开始下载资源文件:${replacement.description}")
            }
            println("开始下载资源文件:${replacement.value}")
            InputStream byteStream = service.download(replacement.value).body().byteStream()
            def fileExt = ""
            def outputFile = ""
            switch (replacement.resType) {
                case "image": {
                    fileExt = "webp"
                    outputFile = "${file.path}.${fileExt}"
                    service.webp(byteStream, outputFile, replacement.width, replacement.height)
                }
                    break
                case "raw": {
                    try {
                        fileExt = FilenameUtils.getExtension(replacement.value)
                    } catch (Exception e) {
                        println("获取文件类型失败:${e.message}")
                        println("资源地址为:${replacement.value}")
                    }
                    if (fileExt.trim() == "" && replacement.ext != "") {
                        fileExt = replacement.ext
                    }
                    if (fileExt.trim() == "") {
                        fileExt = "raw"
                    }
                    outputFile = "${file.path}.${fileExt}"
                    service.saveFile(byteStream, outputFile)
                }
                    break
            }
            println("资源文件保存地址为:${outputFile}")
            def fileTree = project.fileTree(dir)
            fileTree.matching {
                include("${replacement.name}.*")
                exclude("${replacement.name}.${fileExt}")
            }.each {
                it.delete()
                println("正在删除冲突资源文件:${it.path}")
            }
        }
    }


}
