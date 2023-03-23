package com.github.star

import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.StopExecutionException
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
            String fileExt = ""
            String outputFile = ""
            switch (replacement.resType) {
                case "image": {
                    InputStream byteStream = service.download(replacement.value).body().byteStream()
                    fileExt = "webp"
                    outputFile = "${file.path}.${fileExt}"
                    service.webp(byteStream, outputFile, replacement.width, replacement.height)
                }
                    break
                case "raw": {
                    InputStream byteStream = service.download(replacement.value).body().byteStream()
                    fileExt = candidate(fileExtension(replacement.value), replacement.ext, "raw")
                    outputFile = "${file.path}.${fileExt}"
                    service.saveFile(byteStream, outputFile)
                }
                    break
                case "video": {
                    fileExt = candidate(replacement.ext, "webm", fileExtension(replacement.value), "mp4")
                    outputFile = "${file.path}.${fileExt}"
                    def result = project.exec {
                        executable 'ffmpeg'
                        args("-i", replacement.value)
                        if (replacement.width > 0 && replacement.height > 0) {
                            def width = "\'ceil(min(${replacement.width},iw)/2)*2\'"
                            def height = "\'ceil(min(${replacement.height},ih)/2)*2\'"
                            args("-vf", "scale=$width:$height:force_original_aspect_ratio=decrease")
                        }
                        args("-y", outputFile)
                        println("正在执行FFMPEG命令:\n${commandLine.join(" ")}")
                    }
                    if (result.exitValue != 0) {
                        throw new StopExecutionException("转换视屏失败:${result.exitValue}")
                    }
                }
                    break
                default: {
                    throw new StopExecutionException("不支持的资源类型:${replacement.resType}")
                }
            }
            println("资源文件保存地址为:${outputFile}")
            def fileTree = project.fileTree(dir)
            fileTree.matching {
                include("${file.name}.*")
                exclude("${file.name}.${fileExt}")
            }.each {
                it.delete()
                println("正在删除冲突资源文件:${it.path}")
            }
        }
    }

    static String fileExtension(String url) {
        try {
            return FilenameUtils.getExtension(url)
        } catch (Exception e) {
            println("获取文件类型失败:${e.message}")
            println("资源地址为:${url}")
            return ""
        }
    }

    static String candidate(String... candidate) {
        if (candidate != null && candidate.length > 0) {
            def filterCandidate = candidate.findAll { it != null && it.trim() != "" }
            if (filterCandidate.size() > 0) {
                return filterCandidate.first()
            }
        }
        return ""
    }
}
