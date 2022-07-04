package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.util.findChildrenOfType
import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.google.common.base.CaseFormat
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*

class KtFileComponents(
    private val project: Project,
    private val ktFile: KtFile
) {

    private val psiFactory by lazy { KtPsiFactory(project) }

    fun convertAllVariablesFromLowerUnderscoreToCamelCase() {
        val allVariables = ktFile.findChildrenOfType(KtNameReferenceExpression::class.java)
        allVariables.forEach { convertVarToLowerCamel(it) }
    }

    fun shouldAvoidAnElement(it: KtNameReferenceExpression): Boolean {
        val insideImport = PsiTreeUtil.getParentOfType(
            it, KtPackageDirective::class.java,
            KtImportDirective::class.java
        ) != null

        if (insideImport) return true

        val itsText = it.text ?: ""

        if (!itsText.contains("_")) return true
        if (itsText.toUpperCase() == itsText) return true // avoid constant variables

        // Don't convert those references to Android resource ( R.layout, R.drawable, so on )
        val dotParent = PsiTreeUtil.getParentOfType(it, KtDotQualifiedExpression::class.java)
        if (dotParent != null) {
            val dotParentText = dotParent.text
            if (dotParentText.startsWith("R.") || dotParentText.contains(".R.")) return true
        }

        return false
    }

    fun convertVarToLowerCamel(oldVar: KtNameReferenceExpression) {
        if (shouldAvoidAnElement(oldVar)) return

        val oldVarText = oldVar.text
        val newVarText = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, oldVarText)
        val newNameRefExpression = psiFactory.createExpression(newVarText)
        project.runWriteCommand { oldVar.replace(newNameRefExpression) }
        logger.info("ToanTK newVarText $newVarText")
    }
}