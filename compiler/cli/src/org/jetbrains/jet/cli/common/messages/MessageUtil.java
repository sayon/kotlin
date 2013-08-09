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

package org.jetbrains.jet.cli.common.messages;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.impl.jar.CoreJarVirtualFile;
import com.intellij.openapi.vfs.local.CoreLocalVirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.diagnostics.DiagnosticUtils;

import static com.intellij.openapi.util.io.FileUtil.toSystemDependentName;

public class MessageUtil {
    private MessageUtil() {}

    @NotNull
    public static CompilerMessageLocation psiElementToMessageLocation(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        DiagnosticUtils.LineAndColumn lineAndColumn = DiagnosticUtils.getLineAndColumnInPsiFile(file, element.getTextRange());
        return virtualFileToMessageLocation(file.getVirtualFile(), lineAndColumn.getLine(), lineAndColumn.getColumn());
    }

    @NotNull
    public static CompilerMessageLocation virtualFileToMessageLocation(VirtualFile virtualFile, int line, int column) {
        String path;
        if (virtualFile == null) {
            path = "<no path>";
        }
        else {
            path = virtualFile.getPath();
            if (virtualFile instanceof CoreLocalVirtualFile || virtualFile instanceof CoreJarVirtualFile) {
                path = toSystemDependentName(path);
            }
        }
        return CompilerMessageLocation.create(path, line, column);
    }
}
