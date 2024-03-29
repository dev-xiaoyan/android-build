/*
 * This Groovy source file was generated by the Gradle 'init' task.
 */
package com.github.star

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.Task

class AndroidBuildPlugin implements Plugin<Project> {
    void apply(Project project) {
        def buildService = project.gradle.sharedServices.registerIfAbsent("android build service", AndroidBuildService) {
        }
        def service = buildService.get()
        def extension = project.extensions.create("android_build", AndroidBuildExtension)
        service.reportTask.convention(project.tasks.register("report_build_result", ReportBuildResultTask) { task ->
            task.group = service.BUILD_TOOL_TASK_GROUP
            task.description = ""
            task.usesService(buildService)
            task.service.set(buildService)
            task.onlyIf {
                return service.reportEnabled
            }
        })
        service.replacementTask.convention(project.tasks.register("res_replacement", ResReplacementTask) { task ->
            task.group = service.BUILD_TOOL_TASK_GROUP
            task.description = ""
            task.usesService(buildService)
            task.service.set(buildService)
            task.finalizedBy(service.reportTask)
            task.onlyIf {
                return service.replacementEnabled
            }
        })
        service.uploadTask.convention(project.tasks.register("upload_amazon", AmazonUploadTask) { task ->
            task.group = service.BUILD_TOOL_TASK_GROUP
            task.description = ""
            task.usesService(buildService)
            task.service.set(buildService)
            task.finalizedBy(service.reportTask)
            task.mustRunAfter(service.assembleTask)
            task.onlyIf {
                return service.uploadEnabled
            }
        })
        service.androidBuildTask.convention(project.tasks.register("build_android") { task ->
            task.group = service.BUILD_TOOL_TASK_GROUP
            task.description = ""
            task.dependsOn(service.replacementTask)
            task.finalizedBy(service.assembleTask, service.uploadTask, service.reportTask)
            task.onlyIf {
                return service.androidBuildEnabled
            }
        })
        service.assembleTask.convention(project.tasks.register(service.ASSEMBLE_NOT_FOUND_TASK) { task ->
            group = service.BUILD_TOOL_TASK_GROUP
            task.doFirst {
                println("构建任务不可用,将跳过本次构建")
            }
        })
        project.extensions.configure(AndroidBuildExtension) { ext ->
            extension.service = buildService
            extension.replacements.metaClass.dir = { String dir, Closure cl ->
                cl.delegate = new Object() {
                    @Override
                    Object getProperty(String propertyName) {
                        try {
                            return super.getProperty(propertyName)
                        } catch (e) {
                            return ext.replacements.maybeCreate("$dir/$propertyName")
                        }
                    }

                    @Override
                    Object invokeMethod(String name, Object args) {
                        try {
                            return super.invokeMethod(name, args)
                        } catch (e) {
                            def replacement = ext.replacements.maybeCreate("$dir/$name")
                            if (args != null && args instanceof Object[] && args.length > 0) {
                                def arg = args.first()
                                if (arg instanceof Closure) {
                                    try {
                                        arg.delegate = replacement
                                        arg.call()
                                    } catch (Exception ce) {
                                        println("dsl error:${ce.message}")
                                    }
                                }
                            }
                            return replacement
                        }
                    }
                }
                cl()
            }
        }
        project.afterEvaluate {
            extension.props.each { props ->
                if (props.present) {
                    println("正在设置项目属性:${props.description},key:${props.name},value:${props.value}")
                    project.allprojects.each { proj -> proj.ext[props.name] = props.value }
                }
            }
            def app = project.evaluationDependsOn(":app")
            if (app.plugins.hasPlugin("com.android.application")) {
                app.android.applicationVariants.all { variant ->
                    if (variant.buildType.name == "release") {
                        variant.productFlavors.each { flavor ->
                            def buildConfigFields = flavor.buildConfigFields
                            def channel = buildConfigFields.CHANNEL.value.replace("\"", "")
                            String flavorName = flavor.name
                            def assembleTask = app.tasks.findByName("assemble${flavorName.capitalize()}Release")
                            if (channel == extension.channel.value && assembleTask != null) {
                                String host = buildConfigFields.HOST.value.replace("\"", "")
                                println("找到可打包配置:${flavor.name},服务器:${host}")
                                service.server.set(host)
                                service.flavor.set(flavorName)
                                boolean reportAvailable = false
                                boolean uploadAvailable = false
                                try {
                                    reportAvailable = service.reportTask.get().config.available
                                } catch (Exception e) {

                                }
                                try {
                                    uploadAvailable = service.uploadTask.get().config.available
                                } catch (Exception e) {

                                }
                                if (!reportAvailable) {
                                    println("上报任务不可用,请检查参数配置")
                                } else if (!uploadAvailable) {
                                    println("上传任务不可用,请检查参数配置")
                                } else {
                                    println("构建任务已设置:${assembleTask.name}")
                                    service.assembleTask.set(assembleTask)
                                }
                                service.apkFileDir.set(app.layout.buildDirectory.dir("outputs/apk/$flavorName/release/"))
                            }
                        }
                    }
                }
            } else {
                println("当前不是Application工程,打包插件不可用!!!")
            }
        }
    }
}
