package com.github.toantk238.sampleintellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import org.jetbrains.kotlin.psi.KtFile

class ConvertLowUnderToCamelCase : AnAction() {

    private lateinit var ktFile: KtFile

    override fun actionPerformed(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        if (psiFile !is KtFile) return

        ktFile = psiFile

        val pos = editor.caretModel.offset
        val temp = ktFile.findElementAt(pos)

        val ktFileComponents = KtFileComponents(project, psiFile)
        ktFileComponents.convertAllVariablesFromLowerUnderscoreToCamelCase()
    }
}

