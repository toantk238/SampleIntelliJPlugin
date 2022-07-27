package com.github.toantk238.sampleintellijplugin.toolwindow

import com.github.toantk238.sampleintellijplugin.actions.logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.wm.ToolWindow
import javax.swing.*

class TestToolWindow(
    private val toolWindow: ToolWindow,
    private val project: Project
) {

    private lateinit var mainPanel: JPanel

    private lateinit var vmClassField: JTextField

    private lateinit var vmLabel: JLabel

    private lateinit var frgClassField: JTextField

    private lateinit var hasVMCheck: JCheckBox

    private lateinit var frgBuilderModule: JTextField

    private lateinit var selectVMBtn: JButton

    private lateinit var vmModuleField: JTextField

    private lateinit var selectVMModuleBtn: JButton

    private lateinit var createAllBtn: JButton

    fun getMainPanel() = mainPanel

    init {
        setup()
    }

    private fun setup() {
        selectVMBtn.addActionListener { onClickSelectVMClass() }
        selectVMModuleBtn.addActionListener { onClickSelectVMModuleClass() }
        createAllBtn.addActionListener { onClickCreateAll() }

        val state = TestToolState.INSTANCE
        vmModuleField.text = state.vmModuleClass
    }

    private fun onClickSelectVMModuleClass() {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("kt")
        descriptor.setRoots(project.guessProjectDir())
        val selectFile = FileChooser.chooseFile(descriptor, project, null)
        logger.debug("")
    }

    private fun onClickSelectVMClass() {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("kt")
        descriptor.setRoots(project.guessProjectDir())
        val selectFile = FileChooser.chooseFile(descriptor, project, null)
        logger.debug("")
    }

    private fun onClickCreateAll() {
        val state = TestToolState.INSTANCE
        state.vmModuleClass = vmModuleField.text.trim()
    }
}