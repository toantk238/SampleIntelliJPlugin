package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ImportPath

class RenameKotlin : AnAction() {

    private val logger by lazy { Logger.getInstance("ToanTK") }

    override fun actionPerformed(e: AnActionEvent) {
        logger.info("ToantK action performed")

        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        logger.info("ToanTK before cast object")
        if (psiFile !is KtFile) {
            logger.info("ToanTK this file isn't KtFile")
            return
        }
        val ktFile: KtFile = psiFile
        val psiFactory = KtPsiFactory(project)

        // Watch current caret element
        val pos = editor.caretModel.offset
        val temp = ktFile.findElementAt(pos)

        val properties = PsiTreeUtil.findChildrenOfType(ktFile, KtProperty::class.java)
        val initFragmentToolProperty = properties.first { it.name == "initFragmentTools" } ?: return
        val propertyText = initFragmentToolProperty.text
        val foundText = "R.layout."
        val index1 = propertyText.indexOf(foundText)
        val index2 = propertyText.indexOf(" ", index1)
        val layoutFileName = propertyText.substring(index1 + foundText.length, index2)
        val newString = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, layoutFileName)
        val bindingClassName = "${newString}Binding"

        val importElement = PsiTreeUtil.findChildOfType(ktFile, KtImportDirective::class.java)
        val bindingClassPath = "com.base.databinding.$bindingClassName"
        val importPath = ImportPath(FqName(bindingClassPath), false, null)
        val bindingImportElement = psiFactory.createImportDirective(importPath)
        project.runWriteCommand {
            importElement?.add(psiFactory.createNewLine())
            importElement?.add(bindingImportElement)
        }

        val layoutBlock = PsiTreeUtil.findChildOfType(initFragmentToolProperty, KtLambdaExpression::class.java)
        val bindClassExpression = psiFactory.createExpression("${bindingClassName}::class.java")
        project.runWriteCommand { layoutBlock?.replace(bindClassExpression) }

        ktFile.accept(classRecursiveVisitor {
            val superTypes = it.getColon()?.nextSibling?.nextSibling
            if (superTypes is KtSuperTypeList) {
                val comma = psiFactory.createComma()
                val typeEntry = psiFactory.createSuperTypeEntry(bindingClassName)
                val lastGenericType = superTypes.firstChild.firstChild.firstChild.firstChild.lastChild.children[1]
                project.runWriteCommand {
                    lastGenericType.add(comma)
                    lastGenericType.add(typeEntry)
                }
            }
        })

        logger.info("ToanTK got Kt File done")
    }
}