package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.MyBundle
import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import org.jetbrains.kotlin.idea.KotlinIcons

class CreateFragmentAction : CreateFileFromTemplateAction(
    MyBundle.message("action.create_arch_fragment"), "Description", null
) {

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder.setTitle("MVVM Fragment").addKind("Kotlin file", KotlinIcons.FILE, "Kotlin file");
    }

    override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String {
        return "Create MaNaDrFragment Action Name"
    }
}