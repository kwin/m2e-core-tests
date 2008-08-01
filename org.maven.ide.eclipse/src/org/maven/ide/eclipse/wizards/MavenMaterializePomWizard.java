/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import org.apache.maven.model.Dependency;

import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.actions.SelectionUtil;
import org.maven.ide.eclipse.embedder.ArtifactKey;
import org.maven.ide.eclipse.index.IndexedArtifactFile;
import org.maven.ide.eclipse.project.MavenProjectPomScanner;
import org.maven.ide.eclipse.project.MavenProjectScmInfo;
import org.maven.ide.eclipse.project.ProjectImportConfiguration;


/**
 * A wizard used to import projects for Maven artifacts 
 * 
 * @author Eugene Kuleshov
 */
public class MavenMaterializePomWizard extends Wizard implements IImportWizard, INewWizard {

  ProjectImportConfiguration importConfiguration;
  
  MavenDependenciesWizardPage selectionPage;
  
  // TODO replace with ArtifactKey
  private Dependency[] dependencies;

  private MavenProjectWizardLocationPage locationPage;

  private IStructuredSelection selection;


  public MavenMaterializePomWizard() {
    importConfiguration = new ProjectImportConfiguration();
    setNeedsProgressMonitor(true);
    setWindowTitle("Import Maven Projects");
  }

  public void setDependencies(Dependency[] dependencies) {
    this.dependencies = dependencies;
  }
  
  public Dependency[] getDependencies() {
    return dependencies;
  }

  public void init(IWorkbench workbench, IStructuredSelection selection) {
    this.selection = selection;
    
    importConfiguration.setWorkingSet(SelectionUtil.getSelectedWorkingSet(selection));
    
    ArrayList<Dependency> dependencies = new ArrayList<Dependency>();

    for(Iterator<?> it = selection.iterator(); it.hasNext();) {
      Object element = it.next();
      if(element instanceof IPackageFragmentRoot) {
        ArtifactKey a = SelectionUtil.getType(element, ArtifactKey.class);
        if(a!=null) {
          Dependency d = new Dependency();
          d.setGroupId(a.getGroupId());
          d.setArtifactId(a.getArtifactId());
          d.setVersion(a.getVersion());
          d.setClassifier(a.getClassifier());
          dependencies.add(d);
        }
      } else if(element instanceof IndexedArtifactFile) {
        dependencies.add(((IndexedArtifactFile) element).getDependency());
      }
    }
    
    setDependencies(dependencies.toArray(new Dependency[dependencies.size()]));
  }

  public void addPages() {
    selectionPage = new MavenDependenciesWizardPage(importConfiguration, //
        "Select Maven projects", //
        "Select Maven artifacts to import");
    selectionPage.setDependencies(dependencies);
    
    locationPage = new MavenProjectWizardLocationPage(importConfiguration, //
        "Select project location", 
        "Select project location and working set");
    locationPage.setLocationPath(SelectionUtil.getSelectedLocation(selection));
    
    addPage(selectionPage);
    addPage(locationPage);
  }
  
  public boolean canFinish() {
    return super.canFinish();
  }

  public boolean performFinish() {
    if(!canFinish()) {
      return false;
    }

    final Dependency[] dependencies = selectionPage.getDependencies();
    
    final boolean checkoutAllProjects = selectionPage.isCheckoutAllProjects();
    final boolean developer = selectionPage.isDeveloperConnection();
    
    MavenProjectCheckoutJob job = new MavenProjectCheckoutJob(importConfiguration, checkoutAllProjects) {
      protected List<MavenProjectScmInfo> getProjects(IProgressMonitor monitor) throws InterruptedException {
        MavenPlugin plugin = MavenPlugin.getDefault();
        MavenProjectPomScanner<MavenProjectScmInfo> scanner = new MavenProjectPomScanner<MavenProjectScmInfo>(developer, dependencies, //
            plugin.getMavenModelManager(), plugin.getMavenEmbedderManager(), //
            plugin.getIndexManager(), plugin.getConsole());
        scanner.run(monitor);
        // XXX handle errors/warnings
        
        return scanner.getProjects();
      }
    };
    
    if(!locationPage.isInWorkspace()) {
      job.setLocation(locationPage.getLocationPath().toFile());
    }
    
    job.schedule();

    return true;
  }
  
//  public Scm[] getScms(IProgressMonitor monitor) {
//    ArrayList scms = new ArrayList();
//    
//    MavenPlugin plugin = MavenPlugin.getDefault();
//    MavenEmbedderManager embedderManager = plugin.getMavenEmbedderManager();
//    IndexManager indexManager = plugin.getMavenRepositoryIndexManager();
//    MavenConsole console = plugin.getConsole();
//        
//    MavenEmbedder embedder = embedderManager.getWorkspaceEmbedder();
//
//    for(int i = 0; i < dependencies.length; i++ ) {
//      try {
//        Dependency d = dependencies[i];
//        
//        Artifact artifact = embedder.createArtifact(d.getGroupId(), //
//            d.getArtifactId(), d.getVersion(), null, "pom");
//        
//        List remoteRepositories = indexManager.getArtifactRepositories(null, null);
//        
//        embedder.resolve(artifact, remoteRepositories, embedder.getLocalRepository());
//        
//        File file = artifact.getFile();
//        if(file != null) {
//          MavenProject project = embedder.readProject(file);
//          
//          Scm scm = project.getScm();
//          if(scm == null) {
//            String msg = project.getId() + " doesn't specify SCM info";
//            console.logError(msg);
//            continue;
//          }
//          
//          String connection = scm.getConnection();
//          String devConnection = scm.getDeveloperConnection();
//          String tag = scm.getTag();
//          String url = scm.getUrl();
//
//          console.logMessage(project.getArtifactId());
//          console.logMessage("Connection: " + connection);
//          console.logMessage("       dev: " + devConnection);
//          console.logMessage("       url: " + url);
//          console.logMessage("       tag: " + tag);
//          
//          if(connection==null) {
//            if(devConnection==null) {
//              String msg = project.getId() + " doesn't specify SCM connection";
//              console.logError(msg);
//              continue;
//            }
//            scm.setConnection(devConnection);
//          }
//
//          // connection: scm:svn:https://svn.apache.org/repos/asf/incubator/wicket/branches/wicket-1.2.x/wicket
//          //        dev: scm:svn:https://svn.apache.org/repos/asf/incubator/wicket/branches/wicket-1.2.x/wicket
//          //        url: http://svn.apache.org/viewvc/incubator/wicket/branches/wicket-1.2.x/wicket
//          //        tag: HEAD  
//
//          // TODO add an option to select all modules/projects and optimize scan 
//          
//          scms.add(scm);
//          
////          if(!connection.startsWith(SCM_SVN_PROTOCOL)) {
////            String msg = project.getId() + " SCM type is not supported " + connection;
////            console.logError(msg);
////            addError(new Exception(msg));
////          } else {
////            String svnUrl = connection.trim().substring(SCM_SVN_PROTOCOL.length());
////          }
//        }
//
//      } catch(Exception ex) {
//        console.logError(ex.getMessage());
//      }
//    }
//    
//    return (Scm[]) scms.toArray(new Scm[scms.size()]);
//  }

}
