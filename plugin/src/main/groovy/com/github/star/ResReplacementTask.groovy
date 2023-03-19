package com.github.star

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileTree
import org.gradle.api.file.FileTree
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
            switch (replacement.resType) {
                case "image": {
                    def outputFile = "${file.path}.webp"
                    println("资源文件保存地址为:${outputFile}")
                    service.webp(byteStream, outputFile, replacement.width, replacement.height)
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
                    def fileExt = replacement.ext.trim()
                    String outputFile = ""
                    if (fileExt == "" || fileExt == "*") {
                        println("对于Raw类型的资源替换,无法推断出资源类型,尝试匹配中,如无法匹配,则替换失败")
                        def fileTree = project.fileTree(dir)
                        def matchedFiles = fileTree.matching {
                            include("${replacement.name}.*")
                        }
                        if (matchedFiles.files.size() > 0) {
                            println("匹配到可用资源类型:${matchedFiles.first().path}")
                            outputFile = matchedFiles.first().path
                        }
                    } else {
                        outputFile = project.file("$baseDir${replacement.name}.${fileExt}").path
                    }
                    println("资源文件保存地址为:${outputFile}")
                    service.saveFile(byteStream, outputFile)
                }
                    break
            }

        }
    }
}
