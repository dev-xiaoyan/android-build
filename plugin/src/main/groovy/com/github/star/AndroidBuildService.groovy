package com.github.star

import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.nio.ImmutableImageLoader
import com.sksamuel.scrimage.webp.WebpWriter
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.BufferedSink
import okio.BufferedSource
import okio.Okio
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.internal.impldep.com.google.common.io.ByteSink

abstract class AndroidBuildService implements BuildService<BuildServiceParameters.None> {
    final BUILD_TOOL_TASK_GROUP = "android build"
    final ASSEMBLE_NOT_FOUND_TASK = "assembleNotFound"
    private final ImmutableImageLoader imageLoader
    private final OkHttpClient okHttpClient

    abstract Property<String> getFlavor()

    abstract Property<String> getServer()

    abstract DirectoryProperty getApkFileDir()

    abstract Property<Task> getAssembleTask()

    abstract Property<Task> getUploadTask()

    abstract Property<Task> getAndroidBuildTask()

    abstract Property<Task> getReplacementTask()

    abstract Property<Task> getReportTask()

    abstract Property<String> getPackageDownloadUrl()

    AndroidBuildService() {
        imageLoader = ImmutableImage.loader()
        okHttpClient = new OkHttpClient.Builder().build()
    }

    def webp(InputStream input, String output, int width = -1, int height = -1) {
        def immutableImage = imageLoader.fromStream(input)
        if (width > 0 && height > 0) {
            immutableImage = immutableImage.resizeTo(width, height)
        }
        immutableImage.output(new WebpWriter(), output)
    }

    static def saveFile(InputStream input, String output) {
        try {
            File file = new File(output)
            def bufferedSink = Okio.buffer(Okio.sink(file))
            bufferedSink.writeAll(Okio.source(input))
        } catch (Exception e) {
            println("保存文件失败:" + e.message)
        }
    }

    Response download(String url) {
        def request = new Request.Builder().url(url).get().build()
        return okHttpClient.newCall(request).execute()
    }

    OkHttpClient client() {
        return okHttpClient
    }

    boolean getReportEnabled() {
        def task = reportTask.get()
        boolean available = false
        try {
            available = task.config.available
        } catch (Exception e) {
        }
        return server.present && available
    }

    boolean getReplacementEnabled() {
        def task = replacementTask.get()
        boolean available = false
        try {
            available = task.replacements.present && task.replacements.get().findAll {
                it.present
            }.size() > 0
        } catch (Exception e) {
        }
        return assembleTask.present && flavor.present && available
    }

    boolean getUploadEnabled() {
        def upload = uploadTask.get()
        def assemble = assembleTask.get()
        boolean available = false
        try {
            available = upload.config.available
        } catch (Exception e) {
        }
        return assembleTaskFound
                && available
                && assemble.state.executed
                && assemble.state.failure == null
                && apkFileDir.asFileTree.matching { include("*.apk") }.size() == 1
    }

    boolean getAndroidBuildEnabled() {
        return assembleTaskFound && flavor.present && replacementTask.get().state.failure == null && reportEnabled
    }

    boolean getAssembleTaskFound() {
        return assembleTask.get().name != ASSEMBLE_NOT_FOUND_TASK
    }
}
