package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.actions.mvptomvvm.PresenterRefactor
import com.github.toantk238.sampleintellijplugin.util.LANG_JAVA_ID
import com.github.toantk238.sampleintellijplugin.util.findChildOfType
import com.github.toantk238.sampleintellijplugin.util.findChildrenOfType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.psi.*
import org.jetbrains.kotlin.idea.actions.JavaToKotlinAction
import org.jetbrains.kotlin.psi.KtClass

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

        val viewType = genericTypes?.findChildrenOfType(PsiTypeElement::class.java)?.getOrNull(1)
        val viewRef = viewType?.findChildrenOfType(PsiJavaCodeReferenceElement::class.java)?.getOrNull(0)

        val viewMethods = viewRef?.let {
            val viewFile = viewRef.resolve()!!.containingFile as PsiJavaFile
            val viewClass = viewFile.findChildrenOfType(PsiClass::class.java)[0]
            viewClass.methods
        }?.toList()

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

        val newPresenterFile = results.firstOrNull {
            val fileClass = it.findChildOfType(KtClass::class.java)
            fileClass?.name == presenterRef?.referenceName
        }

        if (newPresenterFile != null) {
            val presenterRefactor = PresenterRefactor(newPresenterFile, viewMethods ?: listOf())
            presenterRefactor.update()
        }

        logger.info("ToanTK action End")
    }
}