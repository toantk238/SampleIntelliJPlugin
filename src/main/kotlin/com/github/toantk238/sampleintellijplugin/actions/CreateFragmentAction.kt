package com.github.toantk238.sampleintellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class CreateFragmentAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        logger.debug("ASD")
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val project = e.project
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        e.presentation.isVisible = false
        e.presentation.isEnabled = false
        logger.debug("ASD")
    }
}