/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.types

import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.types.KotlinType

interface FirResolvedType : FirTypeWithNullability {
    val type: ConeKotlinType

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R =
        visitor.visitResolvedType(this, data)
}

inline fun <reified T> FirType.coneTypeUnsafe() = (this as FirResolvedType).type as T