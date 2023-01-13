package com.github.star

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

abstract class AmazonUploadTask extends DefaultTask {
    @Internal
    abstract Property<AndroidBuildService> getService()
    @Internal
    AmazonConfig config = new AmazonConfig()

    @TaskAction
    def doUploadAction() {
        def srv = service.get()
        def credentials = AwsBasicCredentials.create(config.key.value, config.secret.value)
        def s3Client = S3Client.builder().credentialsProvider { credentials }.region(Region.of(config.region.value)).build()
        def apkFile = srv.apkFileDir.asFileTree.matching { include("*.apk") }.singleFile
        def ossKey = "${config.dirs.value}/${apkFile.name}"
        def bucket = config.bucket.value
        logger.info("开始上传apk文件到amazon OSS,文件地址为:${ossKey}")
        logger.info("本地apk文件路径为:${apkFile.path}")
        PutObjectRequest request = PutObjectRequest.builder().bucket(bucket).key(ossKey).build()
        s3Client.putObject(request, apkFile.toPath())
        def downloadUrl = "https://${config.bucket.value}.s3.amazonaws.com/${ossKey}"
        logger.info("amazon OSS文件上传成功,文件KEY:${ossKey}")
        srv.packageDownloadUrl.set(downloadUrl)
    }
}
