package com.github.toantk238.sampleintellijplugin.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil

fun <T : PsiElement> PsiElement.findChildOfType(type: Class<T>): T? {
    return PsiTreeUtil.findChildOfType(this, type)
}

fun <T : PsiElement> PsiElement.findChildrenOfType(type: Class<T>): List<T> {
    return PsiTreeUtil.findChildrenOfType(this, type).toList()
}