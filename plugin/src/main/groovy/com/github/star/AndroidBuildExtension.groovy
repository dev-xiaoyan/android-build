package com.github.star

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Nested

abstract class AndroidBuildExtension {
    Envvar channel = new Envvar()

    protected Provider<AndroidBuildService> service

    @Nested
    abstract NamedDomainObjectContainer<PropsConfig> getProps()

    abstract NamedDomainObjectContainer<ResReplacement> getReplacements()

    def replace(Task task) {
        def srv = service.get()
        srv.replacementTask.set(task)
        task.group = srv.BUILD_TOOL_TASK_GROUP
        task.description = ""
        task.replacements = replacements
        task.usesService(service)
        task.service.set(service)
        task.finalizedBy(srv.reportTask)
        task.onlyIf {
            return srv.replacementEnabled
        }
    }

    Task res(Closure cl) {
        def srv = service.get()
        def task = srv.replacementTask.get()
        cl.delegate = replacements
        cl()
        return task
    }

    def upload(Task task) {
        def srv = service.get()
        srv.uploadTask.set(task)
        task.group = srv.BUILD_TOOL_TASK_GROUP
        task.description = ""
        task.usesService(service)
        task.service.set(service)
        task.finalizedBy(srv.reportTask)
        task.mustRunAfter(srv.assembleTask)
        task.onlyIf {
            return srv.uploadEnabled
        }
    }

    Task amazon(Action action) {
        def srv = service.get()
        def task = srv.uploadTask.get()
        action.execute(task.config)
        return task
    }

    def report(Task task) {
        def srv = service.get()
        task.group = srv.BUILD_TOOL_TASK_GROUP
        task.description = ""
        task.usesService(service)
        task.service.set(service)
        task.onlyIf {
            return srv.reportEnabled
        }
    }

    Task server(Action action) {
        def srv = service.get()
        def task = srv.reportTask.get()
        action.execute(task.config)
        return task
    }
}
