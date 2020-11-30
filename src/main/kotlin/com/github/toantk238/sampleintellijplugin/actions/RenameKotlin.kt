package com.github.toantk238.sampleintellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.psi.KtFile

class RenameKotlin : AnAction() {

    private val logger by lazy { Logger.getInstance("ToanTK") }

    override fun actionPerformed(e: AnActionEvent) {
        logger.info("ToantK action performed")

        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        if (psiFile !is KtFile) return
        val ktFile: KtFile = psiFile

//        ktFile.accept(object : KtVisitorVoid() {
//            override fun visitElement(element: PsiElement) {
//                super.visitElement(element)
//            }
//        })

//        val project = e.project ?: return
//        val tempFile = editingFile.toPsiFile(project)
//        if (tempFile !is KtFile) return
//        val ktFile = tempFile as KtFile
        logger.info("ToanTK got Kt File done")
    }
}