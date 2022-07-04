package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.util.LANG_JAVA_ID
import com.github.toantk238.sampleintellijplugin.util.findChildrenOfType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.psi.*
import org.jetbrains.kotlin.idea.actions.JavaToKotlinAction

class ConvertJavaToKotlinAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        logger.info("ToantK action performed")

        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val module = e.getData(LangDataKeys.MODULE) ?: return

        if (psiFile !is PsiJavaFile) return

        /*
        Find the presenter if exist
         */
        val fClass = psiFile.findChildrenOfType(PsiClass::class.java).getOrNull(0) ?: return

        val genericTypes = fClass.findChildrenOfType(PsiReferenceList::class.java)
            .firstOrNull { it.text.contains("extends") }
        val presenterType = genericTypes?.findChildrenOfType(PsiTypeElement::class.java)?.getOrNull(0)
        val presenterRef = presenterType?.findChildrenOfType(PsiJavaCodeReferenceElement::class.java)?.getOrNull(0)

        val inputFiles = mutableListOf(psiFile)

        if (presenterRef != null
            && presenterRef.language.id == LANG_JAVA_ID
        ) {
            val containingFile = presenterRef.resolve()!!.containingFile as PsiJavaFile
            inputFiles.add(containingFile)
        }

        val results = JavaToKotlinAction.convertFiles(
            javaFiles = inputFiles, project = project,
            module = module, askExternalCodeProcessing = false,
            enableExternalCodeProcessing = false
        )
        logger.info("ToanTK action End")
    }
}