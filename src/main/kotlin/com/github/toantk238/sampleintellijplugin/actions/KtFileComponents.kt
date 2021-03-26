package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.util.findChildrenOfType
import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.google.common.base.CaseFormat
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*

class KtFileComponents(
    val project: Project,
    val ktFile: KtFile
) {

    private val logger by lazy { Logger.getInstance("ToanTK") }

    private val psiFactory by lazy { KtPsiFactory(project) }

    fun convertAllVariablesFromLowerUnderscoreToCamelCase() {
        val allVariables = ktFile.findChildrenOfType(KtNameReferenceExpression::class.java)
        val underscoreVars = allVariables.filter func@{
            val shouldAvoid = PsiTreeUtil.getParentOfType(
                it, KtPackageDirective::class.java,
                KtImportDirective::class.java
            ) != null
            if (shouldAvoid) return@func false

            val itsText = it.text ?: ""
            itsText.contains("_") &&
                    itsText.toUpperCase() != itsText // avoid constant variables
        }
        underscoreVars.forEach { oldVar ->
            val oldVarText = oldVar.text
            val newVarText = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, oldVarText)
            val newNameRefExpression = psiFactory.createExpression(newVarText)
            project.runWriteCommand { oldVar.replace(newNameRefExpression) }
            logger.info("ToanTK newVarText $newVarText")
        }
    }
}