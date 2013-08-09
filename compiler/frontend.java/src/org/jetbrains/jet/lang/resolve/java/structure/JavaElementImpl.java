/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.lang.resolve.java.structure;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class JavaElementImpl implements JavaElement {
    private final PsiElement psiElement;

    protected JavaElementImpl(@NotNull PsiElement psiElement) {
        this.psiElement = psiElement;
    }

    @NotNull
    @Override
    public PsiElement getPsi() {
        return psiElement;
    }

    @NotNull
    @Override
    public JavaElementFactory getFactory() {
        return new JavaElementFactory(psiElement.getManager());
    }

    @Override
    public int hashCode() {
        return getPsi().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JavaElement && getPsi().equals(((JavaElement) obj).getPsi());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getPsi();
    }
}
