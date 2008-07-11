/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.wizards;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;

import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.core.Messages;
import org.maven.ide.eclipse.project.ProjectImportConfiguration;


/**
 * Simple project wizard for creating a new Maven2 project.
 * <p>
 * The wizard provides the following functionality to the user:
 * <ul>
 * <li>Create the project in the workspace or at some external location.</li>
 * <li>Provide information about the Maven2 artifact to create.</li>
 * <li>Choose directories of the default Maven2 directory structure to create.</li>
 * <li>Choose a set of Maven2 dependencies for the project.</li>
 * </ul>
 * </p>
 * <p>
 * Once the wizard has finished, the following resources are created and configured:
 * <ul>
 * <li>A POM file containing the given artifact information and the chosen dependencies.</li>
 * <li>The chosen Maven2 directories.</li>
 * <li>The .classpath file is configured to hold appropriate entries for the Maven2 directories created as well as the
 * Java and Maven2 classpath containers.</li>
 * </ul>
 * </p>
 */
public class MavenProjectWizard extends Wizard implements INewWizard {
  /** The name of the default wizard page image. */
  private static final String DEFAULT_PAGE_IMAGE_NAME = "icons/new_m2_project_wizard.gif";

  /** The default wizard page image. */
  private static final ImageDescriptor DEFAULT_PAGE_IMAGE = MavenPlugin.getImageDescriptor(DEFAULT_PAGE_IMAGE_NAME);

  /** The wizard page for gathering general project information. */
  protected MavenProjectWizardLocationPage locationPage;

  /** The archetype selection page. */
  protected MavenProjectWizardArchetypePage archetypePage;

  /** The wizard page for gathering Maven2 project information. */
  protected MavenProjectWizardArtifactPage artifactPage;

  /** The wizard page for gathering archetype project information. */
  protected MavenProjectWizardArchetypeParametersPage parametersPage;

  /** The wizard page for choosing the Maven2 dependencies to use. */
  protected MavenDependenciesWizardPage dependenciesPage;

  ProjectImportConfiguration configuration;

  /**
   * Default constructor. Sets the title and image of the wizard.
   */
  public MavenProjectWizard() {
    super();
    setWindowTitle(Messages.getString("wizard.project.title"));
    setDefaultPageImageDescriptor(DEFAULT_PAGE_IMAGE);
    setNeedsProgressMonitor(true);
  }
  
  public void init(IWorkbench workbench, IStructuredSelection selection) {
    // do nothing
  }

  public void addPages() {
    configuration = new ProjectImportConfiguration();

    locationPage = new MavenProjectWizardLocationPage(configuration);
    archetypePage = new MavenProjectWizardArchetypePage(configuration);
    parametersPage = new MavenProjectWizardArchetypeParametersPage(configuration);
    artifactPage = new MavenProjectWizardArtifactPage(configuration);
    dependenciesPage = new MavenDependenciesWizardPage(configuration, Messages
        .getString("wizard.project.page.dependencies.title"), Messages
        .getString("wizard.project.page.dependencies.description"));
    dependenciesPage.setDependencies(new Dependency[0]);

    addPage(locationPage);
    addPage(archetypePage);
    addPage(parametersPage);
    addPage(artifactPage);
    addPage(dependenciesPage);
  }

  /** Adds the listeners after the page controls are created. */
  public void createPageControls(Composite pageContainer) {
    super.createPageControls(pageContainer);

    locationPage.addArchetypeSelectionListener(new SelectionAdapter() {
      public void widgetSelected(SelectionEvent e) {
        archetypePage.setUsed(!locationPage.isSimpleProject());
        parametersPage.setUsed(!locationPage.isSimpleProject());
        artifactPage.setUsed(locationPage.isSimpleProject());
        getContainer().updateButtons();
      }
    });
    
    archetypePage.addArchetypeSelectionListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent selectionchangedevent) {
        parametersPage.setArchetype(archetypePage.getArchetype());
        getContainer().updateButtons();
      }
    });

//    locationPage.addProjectNameListener(new ModifyListener() {
//      public void modifyText(ModifyEvent e) {
//        parametersPage.setProjectName(locationPage.getProjectName());
//        artifactPage.setProjectName(locationPage.getProjectName());
//      }
//    });
  }

  /** Returns the model. */
  public Model getModel() {
    if(locationPage.isSimpleProject()) {
      return artifactPage.getModel();
    }
    return parametersPage.getModel();
  }
  
  /**
   * To perform the actual project creation, an operation is created and run using this wizard as execution context.
   * That way, messages about the progress of the project creation are displayed inside the wizard.
   */
  public boolean performFinish() {
    // First of all, we extract all the information from the wizard pages.
    // Note that this should not be done inside the operation we will run
    // since many of the wizard pages' methods can only be invoked from within
    // the SWT event dispatcher thread. However, the operation spawns a new
    // separate thread to perform the actual work, i.e. accessing SWT elements
    // from within that thread would lead to an exception.

//    final IProject project = locationPage.getProjectHandle();
//    final String projectName = locationPage.getProjectName();

    // Get the location where to create the project. For some reason, when using
    // the default workspace location for a project, we have to pass null
    // instead of the actual location.
    final Model model = getModel();
    final String projectName = configuration.getProjectName(model);
    IStatus nameStatus = configuration.validateProjectName(model);
    if(!nameStatus.isOK()) {
      MessageDialog.openError(getShell(), Messages.getString("wizard.project.job.failed", projectName), nameStatus.getMessage());
      return false;
    }

    final IPath location = locationPage.isInWorkspace() ? null : locationPage.getLocationPath();
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IProject project = configuration.getProject(root, model);
    
    boolean pomExists = ( locationPage.isInWorkspace() ?
        root.getLocation().append(project.getName()) : location ).append(IMavenConstants.POM_FILE_NAME).toFile().exists();
    if ( pomExists ) {
      MessageDialog.openError(getShell(), Messages.getString("wizard.project.job.failed", projectName), Messages.getString("wizard.project.error.pomAlreadyExists"));
      return false;
    }
        

    final Job job;
    
    final MavenPlugin plugin = MavenPlugin.getDefault();

    if(locationPage.isSimpleProject()) {
      @SuppressWarnings("unchecked")
      List<Dependency> modelDependencies = model.getDependencies();
      modelDependencies.addAll(Arrays.asList(dependenciesPage.getDependencies()));

      final String[] folders = artifactPage.getFolders();

      job = new WorkspaceJob(Messages.getString("wizard.project.job.creatingProject", projectName)) {
        public IStatus runInWorkspace(IProgressMonitor monitor) {
          try {
            plugin.getProjectConfigurationManager().createSimpleProject(project, location, model, folders, //
                configuration.getResolverConfiguration(), monitor);
            return Status.OK_STATUS;
          } catch(CoreException e) {
            return e.getStatus();
          } finally {
            monitor.done();
          }
        }
      };

    } else {
      final Archetype archetype = archetypePage.getArchetype();
      
      final String groupId = model.getGroupId();
      final String artifactId = model.getArtifactId();
      final String version = model.getVersion();
      final String javaPackage = parametersPage.getJavaPackage();
      final Properties properties = parametersPage.getProperties();
      
      job = new WorkspaceJob(Messages.getString("wizard.project.job.creating", archetype.getArtifactId())) {
        public IStatus runInWorkspace(IProgressMonitor monitor) {
          try {
            plugin.getProjectConfigurationManager().createArchetypeProject(project, location, archetype, //
                groupId, artifactId, version, javaPackage, properties, configuration, monitor);
            return Status.OK_STATUS;
          } catch(CoreException e) {
            return e.getStatus();
          } finally {
            monitor.done();
          }
        }
      };
    }

    job.addJobChangeListener(new JobChangeAdapter() {
      public void done(IJobChangeEvent event) {
        final IStatus result = event.getResult();
        if(!result.isOK()) {
          Display.getDefault().asyncExec(new Runnable() {
            public void run() {
              MessageDialog.openError(getShell(), //
                  Messages.getString("wizard.project.job.failed", projectName), result.getMessage());
            }
          });
        }
      }
    });
    job.setRule(plugin.getProjectConfigurationManager().getRule());
    job.schedule();

    return true;
  }

}
