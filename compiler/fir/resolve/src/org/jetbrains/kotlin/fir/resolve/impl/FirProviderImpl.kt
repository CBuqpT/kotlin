/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.impl

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.resolve.FirProvider
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class FirProviderImpl(val session: FirSession) : FirProvider {

    fun recordFile(file: FirFile) {
        val packageName = file.packageFqName
        fileMap.merge(packageName, listOf(file)) { a, b -> a + b }

        file.acceptChildren(object : FirVisitorVoid() {
            override fun visitElement(element: FirElement) {}

            var containerFqName: FqName = FqName.ROOT

            override fun visitClass(klass: FirClass) {
                val fqName = containerFqName.child(klass.name)
                classifierMap[ClassId(packageName, fqName, false)] = klass

                containerFqName = fqName
                klass.acceptChildren(this)
                containerFqName = fqName.parent()
            }

            override fun visitTypeAlias(typeAlias: FirTypeAlias) {
                val fqName = containerFqName.child(typeAlias.name)
                classifierMap[ClassId(packageName, fqName, false)] = typeAlias
            }
        })
    }

    private val fileMap = mutableMapOf<FqName, List<FirFile>>()
    private val classifierMap = mutableMapOf<ClassId, FirMemberDeclaration>()

    override fun getFirFilesByPackage(fqName: FqName): List<FirFile> {
        return fileMap[fqName].orEmpty()
    }

    override fun getFirClassifierByFqName(fqName: ClassId): FirMemberDeclaration? {
        return classifierMap[fqName]
    }

    override fun getFirTypeParameterByFqName(fqName: ClassId, parameterName: Name): FirTypeParameter? {
        val typeParameterContainer = (getFirClassifierByFqName(fqName) as? FirTypeParameterContainer) ?: return null
        // TODO: Optimize search here
        return typeParameterContainer.typeParameters.find { it.name == parameterName }
    }
}