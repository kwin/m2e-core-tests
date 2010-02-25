/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.integration.tests.common;

import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;

public class SwtbotUtil {

	public static boolean waitForClose(SWTBotShell shell) {
		for (int i = 0; i < 50; i++) {
			if (!shell.isOpen()) {
				return true;
			}
			sleep(200);
		}
		shell.close();
		return false;
	}

	private static void sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException ex) {
			// ;)
		}
	}

	public static ICondition waitForLoad(final SWTBotTable table) {
		return new DefaultCondition() {
			public boolean test() throws Exception {
				return table.rowCount() != 0;
			}

			public String getFailureMessage() {
				return "Table still empty";
			}
		};

	}

}
