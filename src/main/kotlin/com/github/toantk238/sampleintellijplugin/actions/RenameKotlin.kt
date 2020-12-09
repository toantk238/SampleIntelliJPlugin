package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ImportPath

class RenameKotlin : AnAction() {

    private val logger by lazy { Logger.getInstance("ToanTK") }

    private lateinit var ktFile: KtFile

    private lateinit var psiFactory: KtPsiFactory

    private lateinit var bindingClassName: String

    private lateinit var project: Project

    override fun actionPerformed(e: AnActionEvent) {
        logger.info("ToantK action performed")

        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        project = e.getData(CommonDataKeys.PROJECT) ?: return
        logger.info("ToanTK before cast object")
        if (psiFile !is KtFile) {
            logger.info("ToanTK this file isn't KtFile")
            return
        }

        ktFile = psiFile
        psiFactory = KtPsiFactory(project)

        // Watch current caret element
        val pos = editor.caretModel.offset
        val temp = ktFile.findElementAt(pos)

        updateInitFragmentTools()
        addImportBindingClass()
        updateGenericTypes()

        logger.info("ToanTK got Kt File done")
    }

    private fun updateGenericTypes() {
        val ktClass = PsiTreeUtil.findChildOfType(ktFile, KtClass::class.java)
        val superTypes = PsiTreeUtil.findChildOfType(ktClass, KtSuperTypeList::class.java)
        if (superTypes is KtSuperTypeList) {
            val comma = psiFactory.createComma()
            val typeEntry = psiFactory.createSuperTypeEntry(bindingClassName)
            val typeProjections = PsiTreeUtil.findChildrenOfType(superTypes, KtTypeProjection::class.java).toList()
            val lastGenericType = typeProjections.getOrNull(typeProjections.size - 1)
            project.runWriteCommand {
                lastGenericType?.add(comma)
                lastGenericType?.add(typeEntry)
            }
        }
    }

    private fun addImportBindingClass() {
        val importElement = PsiTreeUtil.findChildOfType(ktFile, KtImportDirective::class.java)
        val bindingClassPath = "com.base.databinding.$bindingClassName"
        val importPath = ImportPath(FqName(bindingClassPath), false, null)
        val bindingImportElement = psiFactory.createImportDirective(importPath)
        project.runWriteCommand {
            importElement?.add(psiFactory.createNewLine())
            importElement?.add(bindingImportElement)
        }
    }

    private fun updateInitFragmentTools() {
        val properties = PsiTreeUtil.findChildrenOfType(ktFile, KtProperty::class.java)
        val initFragmentToolProperty = properties.first { it.name == "initFragmentTools" } ?: return
        val propertyText = initFragmentToolProperty.text
        val foundText = "R.layout."
        val index1 = propertyText.indexOf(foundText)
        val index2 = propertyText.indexOf(" ", index1)
        val layoutFileName = propertyText.substring(index1 + foundText.length, index2)
        val newString = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, layoutFileName)
        bindingClassName = "${newString}Binding"

        val layoutBlock = PsiTreeUtil.findChildOfType(initFragmentToolProperty, KtLambdaExpression::class.java)
        val bindClassExpression = psiFactory.createExpression("${bindingClassName}::class")
        project.runWriteCommand { layoutBlock?.replace(bindClassExpression) }
    }
}