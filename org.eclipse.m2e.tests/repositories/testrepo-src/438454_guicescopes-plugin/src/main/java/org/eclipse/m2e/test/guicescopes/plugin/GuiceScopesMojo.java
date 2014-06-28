
package org.eclipse.m2e.test.guicescopes.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;


@Mojo(name = "guicescopes")
public class GuiceScopesMojo extends AbstractMojo {

  @Component
  private MojoExecutionScopedComponent executionScoped;

  public void execute() throws MojoExecutionException, MojoFailureException {

  }

}
