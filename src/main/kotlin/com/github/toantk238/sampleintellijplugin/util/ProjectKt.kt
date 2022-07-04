package com.github.toantk238.sampleintellijplugin.util

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project

fun Project.runWriteCommand(block: () -> Unit) {
    WriteCommandAction.runWriteCommandAction(this) {
        block.invoke()
    }
}

const val LANG_JAVA_ID = "JAVA"