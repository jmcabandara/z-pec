package com.zimbra.qa.selenium.projects.ajax.tests.preferences.importexport;

import org.testng.annotations.Test;

import com.zimbra.qa.selenium.framework.ui.Action;
import com.zimbra.qa.selenium.framework.util.HarnessException;
import com.zimbra.qa.selenium.framework.util.ZAssert;
import com.zimbra.qa.selenium.projects.ajax.core.AjaxCommonTest;
import com.zimbra.qa.selenium.projects.ajax.ui.preferences.TreePreferences.TreeItem;


public class ExportContacts extends AjaxCommonTest {

	public ExportContacts() {
		
		super.startingPage = app.zPagePreferences;
		super.startingAccountPreferences = null;
	}


	@Test(
			description = "Verify clicking on 'export contacts' radio button",
			groups = { "functional" }
			)
	public void ExportContacts_01() throws HarnessException {

		
		/**
		 * 
		 * TODO: Since selenium doesn't handle the
		 * 'download' system dialog, just execute
		 * as much as possible - click Contacts and
		 * verify the display.
		 * 
		 */
		
		
		// Navigate to preferences -> notifications
		app.zTreePreferences.zTreeItem(Action.A_LEFTCLICK, TreeItem.ImportExport);

		// TODO: See https://bugzilla.zimbra.com/show_bug.cgi?id=63289
		String locator = "css=div[id$='_TYPE_CSV_control'] input[type='radio']";
		app.zPagePreferences.sFocus(locator);
		app.zPagePreferences.sClick(locator);
		app.zPagePreferences.zWaitForBusyOverlay();

		// Verify the help hint
		String hint = app.zPagePreferences.sGetText("css=div[id$='_TYPE_HINT']");
		ZAssert.assertStringContains(hint, "You can export your contacts in the standard", "Verify help hint text changed");
		
	}
}
