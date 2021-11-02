package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.util.findChildOfType
import com.github.toantk238.sampleintellijplugin.util.findChildrenOfType
import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.idea.util.projectStructure.getModule
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ImportPath

class RefactorFragmentKotlin : AnAction() {

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

        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        val module = virtualFile?.getModule(project)

        // Watch current caret element
        val pos = editor.caretModel.offset
        val temp = ktFile.findElementAt(pos)

        updateInitFragmentTools()
        addImportBindingClass()
        updateGenericTypes()
        updateFuncSetupViews()
        convertAllVariablesFromLowerUnderscoreToCamelCase()

        logger.info("ToanTK got Kt File done")
    }

    private fun updateGenericTypes() {
        val ktClass = ktFile.findChildOfType(KtClass::class.java)
        val superTypes = ktClass?.findChildOfType(KtSuperTypeList::class.java) ?: return

        val comma = psiFactory.createComma()
        val typeEntry = psiFactory.createSuperTypeEntry(bindingClassName)
        val typeProjections = superTypes.findChildrenOfType(KtTypeProjection::class.java)
        val lastGenericType = typeProjections.getOrNull(typeProjections.size - 1)
        project.runWriteCommand {
            lastGenericType?.add(comma)
            lastGenericType?.add(typeEntry)
        }
    }

    private fun addImportBindingClass() {
        val importElement = ktFile.findChildOfType(KtImportDirective::class.java) ?: return

        val bindingClassPath = "com.base.databinding.$bindingClassName"
        val importPath = ImportPath(FqName(bindingClassPath), false, null)
        val bindingImportElement = psiFactory.createImportDirective(importPath)
        project.runWriteCommand {
            importElement.add(psiFactory.createNewLine())
            importElement.add(bindingImportElement)
        }
    }

    private fun updateInitFragmentTools() {
        val properties = ktFile.findChildrenOfType(KtProperty::class.java)
        val initFragmentToolProperty = properties.firstOrNull { it.name == "initFragmentTools" } ?: return
        val propertyText = initFragmentToolProperty.text
        val foundText = "R.layout."
        val index1 = propertyText.indexOf(foundText)
        val index2 = propertyText.indexOf(" ", index1)
        val layoutFileName = propertyText.substring(index1 + foundText.length, index2)
        val newString = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, layoutFileName)
        bindingClassName = "${newString}Binding"

        val layoutBlock = initFragmentToolProperty.findChildOfType(KtLambdaExpression::class.java)
        val bindClassExpression = psiFactory.createExpression("${bindingClassName}::class")
        project.runWriteCommand { layoutBlock?.replace(bindClassExpression) }
    }

    private fun updateFuncSetupViews() {
        val functions = ktFile.findChildrenOfType(KtNamedFunction::class.java)
        val setupViewFunction = functions.firstOrNull { it.name == "setupViews" } ?: return
        val bodyText = setupViewFunction.bodyExpression?.text ?: return

        val funcDeclaredText = "override val setupViews: (${bindingClassName}.() -> Unit)? ="
        val propertyText = funcDeclaredText + bodyText
        val property = psiFactory.createProperty(propertyText)
        project.runWriteCommand {
            setupViewFunction.replace(property)
        }

        logger.info("ToanTK updateFuncSetupViews")
    }

    private fun convertAllVariablesFromLowerUnderscoreToCamelCase() {
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