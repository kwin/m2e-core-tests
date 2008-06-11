/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.editor.pom;

import org.eclipse.swt.graphics.Image;
import org.maven.ide.eclipse.editor.MavenEditorImages;
import org.maven.ide.eclipse.editor.composites.StringLabelProvider;
import org.maven.ide.eclipse.project.MavenProjectFacade;

public class ModulesLabelProvider extends StringLabelProvider {
  
  private final EMFEditorPage editorPage;

  public ModulesLabelProvider(EMFEditorPage editorPage) {
    super(MavenEditorImages.IMG_JAR);
    this.editorPage = editorPage;
  }

  @Override
  public Image getImage(Object element) {
    if(element instanceof String) {
      String moduleName = (String) element;
      MavenProjectFacade projectFacade = editorPage.findModuleProject(moduleName);
      if(projectFacade!=null) {
        return MavenEditorImages.IMG_PROJECT;
      }
    }
    return super.getImage(element);
  }
}