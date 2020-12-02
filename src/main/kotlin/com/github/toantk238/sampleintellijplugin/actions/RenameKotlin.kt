package com.github.toantk238.sampleintellijplugin.actions

import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*

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

        val pos = editor.caretModel.offset
        val temp = ktFile.findElementAt(pos)

        try {
            val node = ktFile.node
        } catch (e: Exception) {
            logger.info("ToanTK That ko Exception : ${e.message}")
        }

        val psiFactory = KtPsiFactory(project)
        val properties = PsiTreeUtil.findChildrenOfType(ktFile, KtProperty::class.java)
        val initFragmentToolProperty = properties.first { it.name == "initFragmentTools" }
        val propertyText = initFragmentToolProperty.text
        val foundText = "R.layout."
        val index1 = propertyText.indexOf(foundText)
        val index2 = propertyText.indexOf(" ", index1)
        val layoutFileName = propertyText.substring(index1 + foundText.length, index2)
        val newString = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, layoutFileName)
        val bindingClassName = "${newString}Binding"

        val layoutBlock = PsiTreeUtil.findChildOfType(initFragmentToolProperty, KtFunctionLiteral::class.java)
        val type = psiFactory.createExpression("${bindingClassName}::class.java")
        WriteCommandAction.runWriteCommandAction(project) {
            layoutBlock?.replace(type)
        }

        ktFile.accept(classRecursiveVisitor {
            val name = it.name
            val superTypes = it.getColon()?.nextSibling?.nextSibling
            if (superTypes is KtSuperTypeList) {
                val comma = psiFactory.createComma()
                val type = psiFactory.createSuperTypeEntry(bindingClassName)
                val lastGenericType = superTypes.firstChild.firstChild.firstChild.firstChild.lastChild.children[1]
                WriteCommandAction.runWriteCommandAction(project) {
                    lastGenericType.add(comma)
                    lastGenericType.add(type)
                }
            }
            val generics = it.getColon()?.nextSibling?.nextSibling?.children?.getOrNull(0)
//                    as KtSuperTypeCallEntry?)?.typeArgumentList?.arguments
            if (generics is KtSuperTypeCallEntry) {
                val a = psiFactory.createComma()
//                WriteCommandAction.runWriteCommandAction(project) {
//                    generics.add(a)
//                }
                val arguments = generics.typeArguments
                logger.info("ToanTK got Field Declaration: $name")
            }
//            val query = ClassInheritorsSearch.search(it)
        })

//        val project = e.project ?: return
//        val tempFile = editingFile.toPsiFile(project)
//        if (tempFile !is KtFile) return
//        val ktFile = tempFile as KtFile
        logger.info("ToanTK got Kt File done")
    }
}