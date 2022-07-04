package com.github.toantk238.sampleintellijplugin.actions.mvptomvvm

import com.github.toantk238.sampleintellijplugin.util.findChildOfType
import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.intellij.psi.PsiMethod
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory

class PresenterRefactor(
    private val file: KtFile,
    private val viewMethods: List<PsiMethod>
) {

    private val psiFactory = KtPsiFactory(file.project)

    fun update() {
        addLiveData()
    }

    private fun addLiveData() {
        val project = file.project
        val fileClass = file.findChildOfType(KtClass::class.java) ?: return

        for (method in viewMethods) {
            val parameters = method.parameterList.parameters
            val genericType = if (parameters.isEmpty()) "Boolean"
            else if (parameters.size == 1) parameters[0].typeElement?.text ?: ""
            else ""

            val propertyText = "internal val ${method.name}LD = SingleLiveEvent<$genericType>()"

            val liveData = psiFactory.createProperty(propertyText)

            project.runWriteCommand {
                val body = fileClass.body!!
                val firstChild = body.children[0]
                body.addAfter(liveData, firstChild)
            }
        }
    }
}