package com.github.toantk238.sampleintellijplugin.services

import com.intellij.openapi.project.Project
import com.github.toantk238.sampleintellijplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
