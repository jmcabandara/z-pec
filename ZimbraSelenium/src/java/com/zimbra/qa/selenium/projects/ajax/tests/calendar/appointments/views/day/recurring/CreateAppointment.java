package com.zimbra.qa.selenium.projects.ajax.tests.calendar.appointments.views.day.recurring;

import java.util.Calendar;
import java.util.HashMap;
import org.testng.annotations.Test;
import com.zimbra.qa.selenium.framework.items.AppointmentItem;
import com.zimbra.qa.selenium.framework.ui.Button;
import com.zimbra.qa.selenium.framework.util.*;
import com.zimbra.qa.selenium.projects.ajax.core.AjaxCommonTest;
import com.zimbra.qa.selenium.projects.ajax.ui.calendar.FormApptNew;

public class CreateAppointment extends AjaxCommonTest {


	public CreateAppointment() {
		logger.info("New "+ CreateAppointment.class.getCanonicalName());

		// All tests start at the Calendar page
		super.startingPage = app.zPageCalendar;

		// Make sure we are using an account with message view
		super.startingAccountPreferences = new HashMap<String, String>() {
			private static final long serialVersionUID = -2913827779459595178L;
		{
		    put("zimbraPrefCalendarInitialView", "day");
		}};
	}

	
	@Test(	
			description = "Create basic recurring appointment (every day) in day view", 
			groups = { "smoke" } )
	public void CreateRecurringAppointment_01() throws HarnessException {
		
		//-- Data Setup
		
		// Appointment data
		ZDate startTime, endTime;
		AppointmentItem appt = new AppointmentItem();
		Calendar now = Calendar.getInstance();
		
		startTime = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 12, 0, 0);
		endTime = new ZDate(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH), 14, 0, 0);
		
		appt.setSubject("appointment" + ZimbraSeleniumProperties.getUniqueString());
		appt.setAttendees(ZimbraAccount.AccountA().EmailAddress);
		appt.setContent("content" + ZimbraSeleniumProperties.getUniqueString());
		appt.setStartTime(startTime);
		appt.setEndTime(endTime);
		appt.setRecurring("EVERYDAY", "");

		
		//-- GUI steps
		
		
		
		// Create series appointment
		FormApptNew apptForm = (FormApptNew) app.zPageCalendar.zToolbarPressButton(Button.B_NEW);
		apptForm.zFill(appt);
		apptForm.zSubmit();
		
		
		//-- Data Verification
		
		// Verify the new appointment exists on the server
		AppointmentItem actual = AppointmentItem.importFromSOAP(app.zGetActiveAccount(), "subject:("+ appt.getSubject() +")", appt.getStartTime().addDays(-7), appt.getEndTime().addDays(7));
		ZAssert.assertNotNull(actual, "Verify the new appointment is created");
		ZAssert.assertEquals(actual.getSubject(), appt.getSubject(), "Subject: Verify the appointment data");
		ZAssert.assertEquals(app.zGetActiveAccount().soapSelectValue("//mail:s", "d"), startTime.toYYYYMMDDTHHMMSS(), "Verify recurring appointment start time and date");
		ZAssert.assertEquals(app.zGetActiveAccount().soapSelectValue("//mail:e", "d"), endTime.toYYYYMMDDTHHMMSS(), "Verify recurring appointment end time and date");
		
//		Move this verification to GetAppointment or ViewAppointment
//		
//		// Open instance and verify corresponding UI
//		app.zPageCalendar.zToolbarPressButton(Button.B_REFRESH);
//        app.zPageCalendar.zListItem(Action.A_DOUBLECLICK, appt.getSubject());
//        DialogOpenRecurringItem dlgConfirm = new DialogOpenRecurringItem(DialogOpenRecurringItem.Confirmation.OPENRECURRINGITEM, app, ((AppAjaxClient) app).zPageCalendar);
//		dlgConfirm.zClickButton(Button.B_OK);
//		ZAssert.assertEquals(app.zPageCalendar.sIsElementPresent(Locators.RepeatDisabled), true, "Verify 'Every Week' menu item is disabled");
//		SleepUtil.sleepMedium();
//		app.zPageCalendar.zToolbarPressButton(Button.B_CLOSE);
//		
//		// Open entire series and verify corresponding UI
//		app.zPageCalendar.zListItem(Action.A_DOUBLECLICK, appt.getSubject());
//        dlgConfirm = new DialogOpenRecurringItem(DialogOpenRecurringItem.Confirmation.OPENRECURRINGITEM, app, ((AppAjaxClient) app).zPageCalendar);
//        app.zPageCalendar.zCheckRadioButton(Button.B_OPEN_THE_SERIES);
//		dlgConfirm.zClickButton(Button.B_OK);
//		ZAssert.assertEquals(app.zPageCalendar.sIsElementPresent(Locators.RepeatEnabled), true, "Verify 'Every Week' menu item is enabled");
//		SleepUtil.sleepMedium();
//		app.zPageCalendar.zToolbarPressButton(Button.B_CLOSE);
		
	}

}
