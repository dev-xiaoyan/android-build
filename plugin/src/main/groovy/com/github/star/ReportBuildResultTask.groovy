package com.github.star

import groovy.json.JsonBuilder
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class ReportBuildResultTask extends DefaultTask {
    @Internal
    abstract Property<AndroidBuildService> getService()
    @Internal
    ServerConfig config = new ServerConfig()

    @TaskAction
    def doReportAction() {
        def srv = service.get()
        def server = srv.server.get()
        if (!server.endsWith("/")) {
            server = "$server/"
        }
        def client = srv.client()
        def status = srv.packageDownloadUrl.present ? "1" : "2"
        def packageDownloadUrl = srv.packageDownloadUrl.getOrElse("")
        def params = new JsonBuilder(["id"                : "${config.id.value}",
                                      "packageDownloadUrl": "${packageDownloadUrl}",
                                      "status"            : "$status"]).toPrettyString()
        logger.error("上报请求参数为:${params}")
        def body = RequestBody.create(MediaType.get("application/json"), params)
        def request = new Request.Builder().url("$server${config.api.value}").put(body).build()
        def call = client.newCall(request)
        def response = call.execute()
        def bodyString = response.body().string()
        logger.error("上报结果为:${bodyString}")
        if (response.successful) {
            logger.error("打包结果上报成功🚀🚀🚀🚀🚀")
        } else {
            logger.error("打包结果上报失败,错误信息为:${bodyString}")
        }
    }
}
