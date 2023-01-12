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
        replacements.get().each { replacement ->
            def file = project.file("$baseDir${replacement.name}.webp")
            def dir = file.parentFile
            dir.mkdirs()
            logger.error("开始下载图片文件:${replacement.value}\n" + "图片保存地址为:${file.path}")
            def byteStream = service.download(replacement.value).body().byteStream()
            service.webp(byteStream, file.path, replacement.width, replacement.height)
            def fileTree = project.fileTree(dir)
            fileTree.matching {
                include("${replacement.name}.*")
                exclude("${replacement.name}.webp")
            }.each {
                it.delete()
            }
        }
    }
}
