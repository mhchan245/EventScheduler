package eventscheduler;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.parameter.Value;

import java.text.ParseException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Scheduler {

	private LocalDate startDate;
	private LocalDate endDate;
	private LocalTime startTime;
	private LocalTime endTime;
	private String eventName;
	private int eventLength;
	private boolean exHoliday;
	private boolean limitTime;

	// A candidate period of interest
	private Date periodStart;
	private Date periodEnd;

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public void setEventLength(int eventLength) {
		this.eventLength = eventLength;
	}

	public void setLimitTime(boolean limitTime) {
		this.limitTime = limitTime;
	}

	public void setExHoliday(boolean exHoliday) {
		this.exHoliday = exHoliday;
	}

	public void setDateRange(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public void setTimeRange(LocalTime startTime, LocalTime endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	private void preprocessInput() {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");

		String sd = startDate.format(dateFormatter);
		String ed = endDate.format(dateFormatter);

		String st = "000000";
		String et = "000000";

		if (limitTime) {
			st = startTime.format(timeFormatter);
			et = endTime.format(timeFormatter);
		}

		try {
			periodStart = new DateTime(sd + "T" + st);
			periodEnd = new DateTime(ed + "T" + et);
			System.out.println("period start: " + periodStart + ", period end: " + periodEnd);
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
	}

	public void start() {
		preprocessInput();
		File[] files = new File("data").listFiles();
		for (File file : files) {
			if (file.isFile() && file.getName().endsWith(".ics")) {
				runCalendarFile(file.getPath());
			}
		}
	}

	public static Date readDateProperty(Property property) throws ParseException {
		String value = property.getValue();
		Parameter param = property.getParameter(Parameter.VALUE);
		Date dt;
		if (param != null && param.getValue().equals("DATE"))
			dt = new DateTime(value + "T000000");
		else
			dt = new DateTime(value);
		return dt;
	}
	
	public static Date addHours(Date dt, int hours) {
		java.util.Calendar cal = java.util.Calendar.getInstance(); 	// creates a calendar utility
		cal.setTime(dt);												// sets calendar time/date
		cal.add(java.util.Calendar.HOUR_OF_DAY, hours); 				// adds hours
		Date newDt = new DateTime();
		newDt.setTime(cal.getTime().getTime());
		return newDt;
	}
	
	public void runCalendarFile(String filePath) {
		try {
			FileInputStream fin = new FileInputStream(filePath);
			System.out.println("\nProcessing " + filePath + " ... ");

			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(fin);

			String calName = calendar.getProperty("X-WR-CALNAME").getValue();
			String studentInfo = calendar.getProperty("X-WR-CALDESC").getValue();
			String[] studentData = studentInfo.split("\n");

			for (String s : studentData) {
				System.out.println(s);
			}

			// The current time slot to check against the event
			Date slotStart = periodStart;
			Date slotEnd = addHours(periodStart, 1);
			System.out.println("Current time slot: " + slotStart + " to " + slotEnd);

			for (Iterator<CalendarComponent> i = calendar.getComponents().iterator(); i.hasNext();) {
				Component component = (Component) i.next();

				if (!component.getName().equals(Component.VEVENT))
					continue;

				Property summary = component.getProperty(Property.SUMMARY);
				System.out.println("<Event>: " + summary.getValue());

				Property description = component.getProperty(Property.DESCRIPTION);
				if (description != null) {
					System.out.println("\tDescription: " + description.getValue());
				}
				
				Property dtStart = component.getProperty(Property.DTSTART);
				Date evtStart = readDateProperty(dtStart);
				System.out.println("\tEvent Start: " + evtStart);
				
				Property dtEnd = component.getProperty(Property.DTEND);
				Date evtEnd = readDateProperty(dtEnd);
				System.out.println("\tEvent End:   " + evtEnd);
				
				Property rRule = component.getProperty(Property.RRULE);
				if (rRule != null) {
					RRule r = new RRule(rRule.getValue());
					Date seed = evtStart;
					DateList list = r.getRecur().getDates(seed, periodStart, periodEnd, Value.DATE_TIME);
					if (0 == list.size()) 
						System.out.println("\tThe event does not occur in the specified period.");
					else 
						System.out.println("\tThe event occurs on the dates below in the specified period.");
					for (int n = 0; n < list.size(); n++) {
						DateTime rDateStart = (DateTime) list.get(n);
						DateTime rDateEnd = new DateTime(rDateStart.toString().substring(0, 8) + "T" + evtEnd.toString().substring(9,15));
						System.out.print("\t" + rDateStart + " compares with " + slotStart + ": " + rDateStart.compareTo(slotStart));
						System.out.println(", " + rDateEnd   + " compares with " + slotEnd   + ": " + rDateEnd.compareTo(slotEnd));
					}
				} else {
					System.out.println("\t" + evtStart + " compares with " + slotStart + ": " + evtStart.compareTo(slotStart));
					System.out.println("\t" + evtEnd   + " compares with " + slotEnd   + ": " + evtEnd.compareTo(slotEnd));
				}
			}
		} catch (IOException | ParseException | ParserException ex) {
			ex.printStackTrace();
		}
	}
}
	