/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.project.configurator;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.maven.ide.eclipse.project.IMavenProjectFacade;

/**
 * LifecycleMapping
 *
 * @author igor
 */
public interface ILifecycleMapping {
  String getName();
  
  List<String> getPotentialMojoExecutionsForBuildKind(IMavenProjectFacade projectFacade, int kind, IProgressMonitor progressMonitor);
  
  void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException;

  void unconfigure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException;

  List<AbstractBuildParticipant> getBuildParticipants(IMavenProjectFacade facade, IProgressMonitor monitor) throws CoreException;

  List<AbstractProjectConfigurator> getProjectConfigurators(IMavenProjectFacade facade, IProgressMonitor monitor) throws CoreException;
}
