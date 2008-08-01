/*******************************************************************************
 * Copyright (c) 2007, 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.actions;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.project.MavenProjectManager;
import org.maven.ide.eclipse.project.MavenUpdateRequest;


public class RefreshMavenModelsAction implements IWorkbenchWindowActionDelegate, IExecutableExtension {

  public static final String ID = "org.maven.ide.eclipse.refreshMavenModelsAction";

  public static final String ID_SNAPSHOTS = "org.maven.ide.eclipse.refreshMavenSnapshotsAction";
  
  private IProject[] projects;

  private boolean updateSnapshots = false;

  private boolean offline = false;  // should respect global settings

  public RefreshMavenModelsAction() {
  }
  
  public RefreshMavenModelsAction(boolean updateSnapshots) {
    this.updateSnapshots = updateSnapshots;
  }

  public void run(IAction action) {
    MavenProjectManager projectManager = MavenPlugin.getDefault().getMavenProjectManager();

    if(projects != null) {
      projectManager.refresh(new MavenUpdateRequest(projects, offline, updateSnapshots));
    } else {
      projectManager.refresh(new MavenUpdateRequest(ResourcesPlugin.getWorkspace().getRoot().getProjects(), //
          offline, updateSnapshots));
    }
  }

  public void selectionChanged(IAction action, ISelection selection) {
    projects = null;
    
    if(selection instanceof IStructuredSelection) {
      ArrayList<IProject> projectList = new ArrayList<IProject>(); 
      for(Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
        Object o = it.next();
        if(o instanceof IProject) {
          projectList.add((IProject) o);
        }
      }
      if(projectList.size()>0) {
        projects = projectList.toArray(new IProject[projectList.size()]);
      }
    }
  }

  public void dispose() {
  }

  public void init(IWorkbenchWindow window) {
  }

  public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
    if("snapshots".equals(data)) {
      this.updateSnapshots = true;
    }
  }

}

