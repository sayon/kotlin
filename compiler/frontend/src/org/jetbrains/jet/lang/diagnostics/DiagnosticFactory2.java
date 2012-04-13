/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.lang.diagnostics;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.diagnostics.rendering.Renderer;
import org.jetbrains.jet.lang.diagnostics.rendering.Renderers;

/**
 * @author abreslav
 */
public class DiagnosticFactory2<E extends PsiElement, A, B> extends DiagnosticFactoryWithMessageFormat<E> {

    @NotNull
    public ParametrizedDiagnostic<E> on(@NotNull E element, @NotNull A a, @NotNull B b) {
        return new DiagnosticWithParameters2<E, A, B>(element, a, b, this, severity);
    }


    private DiagnosticFactory2(Severity severity, String message, PositioningStrategy<? super E> positioningStrategy, Renderer<? super A> rendererForA, Renderer<? super B> rendererForB) {
        super(severity, message, positioningStrategy);
    }

    public static <T extends PsiElement, A, B> DiagnosticFactory2<T, A, B> create(Severity severity, String messageStub, PositioningStrategy<? super T> positioningStrategy, Renderer<? super A> rendererForA, Renderer<? super B> rendererForB) {
        return new DiagnosticFactory2<T, A, B>(severity, messageStub, positioningStrategy, rendererForA, rendererForB);
    }

    public static <T extends PsiElement, A, B> DiagnosticFactory2<T, A, B> create(Severity severity, String messageStub, PositioningStrategy<? super T> positioningStrategy) {
        return new DiagnosticFactory2<T, A, B>(severity, messageStub, positioningStrategy, Renderers.TO_STRING, Renderers.TO_STRING);
    }

    public static <T extends PsiElement, A, B> DiagnosticFactory2<T, A, B> create(Severity severity, String messageStub, Renderer<? super A> rendererForA, Renderer<? super B> rendererForB) {
        return new DiagnosticFactory2<T, A, B>(severity, messageStub, PositioningStrategies.DEFAULT, rendererForA, rendererForB);
    }

    public static <T extends PsiElement, A, B> DiagnosticFactory2<T, A, B> create(Severity severity, String messageStub) {
        return new DiagnosticFactory2<T, A, B>(severity, messageStub, PositioningStrategies.DEFAULT, Renderers.TO_STRING, Renderers.TO_STRING);
    }

}
