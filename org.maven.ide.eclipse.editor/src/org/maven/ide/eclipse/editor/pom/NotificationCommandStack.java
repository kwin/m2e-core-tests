package org.maven.ide.eclipse.editor.pom;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;

public class NotificationCommandStack extends BasicCommandStack {

  private List<EMFEditorPage> pages;

  public NotificationCommandStack(List<EMFEditorPage> pages) {
    this.pages = pages;
  }
  
  @Override
  public void execute(Command command) {
    super.execute(command);
    processCommand(command);
  }

  private void processCommand(Command command) {
    if (command instanceof CompoundCommand) {
      CompoundCommand compoundCommand = (CompoundCommand) command;
      Iterator<Command> commands = compoundCommand.getCommandList().iterator();
      while (commands.hasNext())
        processCommand(commands.next());
    }
    
    if (command instanceof AddCommand) {
      AddCommand addCommand = (AddCommand) command;
      Iterator<?> it = addCommand.getCollection().iterator();
      while (it.hasNext()) {
        addListeners(it.next());
      }
    }

    if (command instanceof SetCommand) {
      SetCommand setCommand = (SetCommand) command;
      addListeners(setCommand.getValue());
    }

    if (command instanceof RemoveCommand) {
      RemoveCommand addCommand = (RemoveCommand) command;
      Iterator<?> it = addCommand.getCollection().iterator();
      while (it.hasNext()) {
        Object next = it.next();
        if (next instanceof EObject) {
          EObject object = (EObject) next;
          for (int i=0; i<pages.size(); i++)
            object.eAdapters().remove(pages.get(i));
        }
      }
    }
  }

  private void addListeners(Object next) {
    if (next instanceof EObject) {
      EObject object = (EObject) next;
      for (int i=0; i<pages.size(); i++)
        if (!object.eAdapters().contains(pages.get(i)))
          object.eAdapters().add(pages.get(i));
    }
  }
  
  @Override
  public void redo() {
    // TODO Auto-generated method stub
    super.redo();
  }

  @Override
  public void undo() {
    // TODO Auto-generated method stub
    super.undo();
  }

}
