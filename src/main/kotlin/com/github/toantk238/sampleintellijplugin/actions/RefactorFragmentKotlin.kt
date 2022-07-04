package com.github.toantk238.sampleintellijplugin.actions

import com.android.tools.idea.npw.project.getPackageForApplication
import com.github.toantk238.sampleintellijplugin.util.findChildOfType
import com.github.toantk238.sampleintellijplugin.util.findChildrenOfType
import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.google.common.base.CaseFormat
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.android.facet.AndroidFacet
import org.jetbrains.kotlin.android.model.AndroidModuleInfoProvider
import org.jetbrains.kotlin.idea.util.projectStructure.getModule
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.ImportPath

class RefactorFragmentKotlin : AnAction() {

    private var virtualFile: VirtualFile? = null

    private lateinit var ktFile: KtFile

    private lateinit var psiFactory: KtPsiFactory

    private lateinit var bindingClassName: String

    private lateinit var project: Project

    private var packageName: String = ""

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

        virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
        testVirtualFiles()


        // Watch current caret element
        val pos = editor.caretModel.offset
        val temp = ktFile.findElementAt(pos)

        updateInitFragmentTools()
        addImportBindingClass()
        deleteSyntheticImports()
        updateImportBaseClass()
        updateGenericTypes()
        updateFuncSetupViews()
        convertAllVariablesFromLowerUnderscoreToCamelCase()

        logger.info("ToanTK got Kt File done")
    }

    private fun testVirtualFiles() {
        val module = virtualFile?.getModule(project) ?: return
        val sourceProvider = AndroidModuleInfoProvider.getInstance(module)
        val facet = AndroidFacet.getInstance(module)
        val moduleManager = ModuleManager.getInstance(project)
        val p = ProjectRootManager.getInstance(project)
        packageName = facet?.getPackageForApplication() ?: ""

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

        val bindingClassPath = "$packageName.databinding.$bindingClassName"
        val importPath = ImportPath(FqName(bindingClassPath), false, null)
        val bindingImportElement = psiFactory.createImportDirective(importPath)
        project.runWriteCommand {
            importElement.add(psiFactory.createNewLine())
            importElement.add(bindingImportElement)
        }
    }

    private fun deleteSyntheticImports() {
        val imports = ktFile.findChildrenOfType(KtImportDirective::class.java)
        val kotlinImports = imports.filter { it.text.contains("kotlinx.android.synthetic") }
        project.runWriteCommand {
            kotlinImports.forEach { it.delete() }
        }
    }

    private fun updateImportBaseClass() {
        val imports = ktFile.findChildrenOfType(KtImportDirective::class.java)
        val baseClassImport =
            imports.filter { it.text.contains("com.mhealth.core.mvvm.BaseMVVMFragment") }.firstOrNull() ?: return
        val newBaseClassPath = "com.mhealth.core.mvvm.v2.BaseMVVMFragment"
        val importPath = ImportPath(FqName(newBaseClassPath), false, null)
        val newImport = psiFactory.createImportDirective(importPath)
        project.runWriteCommand {
            baseClassImport.replace(newImport)
        }
    }

    private fun updateInitFragmentTools() {
        val properties = ktFile.findChildrenOfType(KtFunction::class.java)
        val initFragmentToolProperty = properties.firstOrNull { it.name == "initFragmentTools" } ?: return
        val propertyText = initFragmentToolProperty.text
        val foundText = "R.layout."
        val index1 = propertyText.indexOf(foundText)
        val index2 = propertyText.indexOf(" ", index1)
        val layoutFileName = propertyText.substring(index1 + foundText.length, index2)
        val newString = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, layoutFileName)
        bindingClassName = "${newString}Binding"

        project.runWriteCommand { initFragmentToolProperty.delete() }
    }

    private fun updateFuncSetupViews() {
        val functions = ktFile.findChildrenOfType(KtNamedFunction::class.java)
        val setupViewFunction = functions.firstOrNull { it.name == "setupViews" } ?: return
        val bodyText = setupViewFunction.bodyExpression?.text ?: return

        val funcDeclaredText = "override val setupViews: (${bindingClassName}.() -> Unit) ="
        val propertyText = funcDeclaredText + bodyText
        val property = psiFactory.createProperty(propertyText)
        project.runWriteCommand {
            setupViewFunction.replace(property)
        }

        logger.info("ToanTK updateFuncSetupViews")
    }

    private fun convertAllVariablesFromLowerUnderscoreToCamelCase() {
        val ktFileComponents = KtFileComponents(project, ktFile)
        ktFileComponents.convertAllVariablesFromLowerUnderscoreToCamelCase()
    }
}