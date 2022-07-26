package com.github.toantk238.sampleintellijplugin.toolwindow

import com.intellij.openapi.wm.ToolWindow
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class TestToolWindow(
    val toolWindow: ToolWindow
) {

    private lateinit var mainPanel: JPanel

    private lateinit var vmClassField: JTextField

    private lateinit var vmLabel: JLabel

    private lateinit var frgClassField: JTextField

    private lateinit var hasVMCheck: JCheckBox

    private lateinit var frgBuilderModule: JTextField

    fun getMainPanel() = mainPanel
}