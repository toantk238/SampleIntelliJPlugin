package com.github.toantk238.sampleintellijplugin.toolwindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(
    name = "org.intellij.sdk.settings.TestToolState",
    storages = [Storage("rock_test.xml")]
)
class TestToolState : PersistentStateComponent<TestToolState> {

    var vmModuleClass: String = ""

    override fun getState(): TestToolState = this

    override fun loadState(state: TestToolState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {

        val INSTANCE by lazy { ApplicationManager.getApplication().getService(TestToolState::class.java) }

    }
}