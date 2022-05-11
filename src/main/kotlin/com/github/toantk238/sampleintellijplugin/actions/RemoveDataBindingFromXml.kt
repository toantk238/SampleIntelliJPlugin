package com.github.toantk238.sampleintellijplugin.actions

import com.github.toantk238.sampleintellijplugin.util.findChildrenOfType
import com.github.toantk238.sampleintellijplugin.util.runWriteCommand
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.xml.XmlToken

class RemoveDataBindingFromXml : AnAction() {

    private val logger by lazy { Logger.getInstance("ToanTK") }

    override fun actionPerformed(e: AnActionEvent) {

        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return

        val project = e.getData(CommonDataKeys.PROJECT) ?: return

        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        if (psiFile !is XmlFile) return

        // Watch current caret element
        val pos = editor.caretModel.offset
        val temp = psiFile.findElementAt(pos)

        val document = psiFile.document
        val tokens = document?.findChildrenOfType(XmlToken::class.java) ?: listOf()
        val tags = document?.findChildrenOfType(XmlTag::class.java) ?: listOf()
        val layoutTag = tags.firstOrNull {
            val firstTag = it.firstChild.nextSibling.text
            firstTag == "layout"
        } ?: return

        val attributes = layoutTag.attributes
        val children = layoutTag.children

        val endTagIndex = children.indexOfFirst {
            it.text == ">"
        }

        val contentFirstChild = children.toList().subList(endTagIndex + 1, children.size)
            .first { it is XmlTag }

        project.runWriteCommand {
            attributes.forEach { contentFirstChild.add(it) }
            layoutTag.replace(contentFirstChild)
        }

        logger.debug("ToanTK")
    }
}