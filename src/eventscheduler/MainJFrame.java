package eventscheduler;


import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalTime;
import static java.time.temporal.ChronoUnit.HOURS;

public class MainJFrame extends JFrame {

	private JPanel contentPane;
	private JTextField txtEventName;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainJFrame frame = new MainJFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainJFrame() {
		setTitle("Event Scheduler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 626, 332);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		DatePicker startDatePicker = new DatePicker();
		startDatePicker.setBounds(127, 75, 220, 29);
		startDatePicker.setDateToToday();
		contentPane.add(startDatePicker);

		DatePicker endDatePicker = new DatePicker();
		endDatePicker.setBounds(365, 75, 220, 29);
		endDatePicker.setDate(java.time.LocalDate.now().plusMonths(1));
		contentPane.add(endDatePicker);

		TimePicker startTimePicker = new TimePicker();
		startTimePicker.setBounds(127, 205, 145, 29);
		startTimePicker.setTime(LocalTime.parse("09:00:00"));
		contentPane.add(startTimePicker);

		TimePicker endTimePicker = new TimePicker();
		endTimePicker.setBounds(127, 250, 145, 29);
		endTimePicker.setTime(LocalTime.parse("19:00:00"));
		contentPane.add(endTimePicker);

		JLabel lblDateRange = new JLabel("Date Range");
		lblDateRange.setBounds(43, 81, 72, 16);
		contentPane.add(lblDateRange);

		JLabel lblFrom = new JLabel("From");
		lblFrom.setBounds(81, 211, 40, 16);
		contentPane.add(lblFrom);

		JLabel lblTo = new JLabel("To");
		lblTo.setBounds(94, 256, 21, 16);
		contentPane.add(lblTo);

		JCheckBox chkExHoliday = new JCheckBox("Exclude Public Holidays");
		chkExHoliday.setSelected(true);
		chkExHoliday.setBounds(44, 135, 192, 23);
		contentPane.add(chkExHoliday);

		txtEventName = new JTextField();
		txtEventName.setBounds(127, 26, 389, 26);
		contentPane.add(txtEventName);
		txtEventName.setColumns(10);

		JLabel lblEventName = new JLabel("Event Name");
		lblEventName.setBounds(43, 31, 82, 16);
		contentPane.add(lblEventName);

		JLabel lblLengthOfEvent = new JLabel("Length of Event");
		lblLengthOfEvent.setBounds(319, 139, 102, 16);
		contentPane.add(lblLengthOfEvent);

		Integer value = new Integer(2);
		Integer min = new Integer(1);
		Integer max = new Integer(12);
		Integer step = new Integer(1);
		SpinnerNumberModel model = new SpinnerNumberModel(value, min, max, step);
		JSpinner spnEventLength = new JSpinner();
		spnEventLength.setModel(model);
		spnEventLength.setBounds(433, 134, 63, 26);
		contentPane.add(spnEventLength);

		JCheckBox chkLimitTime = new JCheckBox("Limit Time Range Per Day");
		chkLimitTime.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				if (cb.isSelected()) {
					startTimePicker.setEnabled(true);
					endTimePicker.setEnabled(true);
				} else {
					startTimePicker.setEnabled(false);
					endTimePicker.setEnabled(false);
				}
			}
		});
		chkLimitTime.setSelected(true);
		chkLimitTime.setBounds(43, 170, 193, 23);
		contentPane.add(chkLimitTime);

		JLabel lblHours = new JLabel("hours");
		lblHours.setBounds(505, 139, 61, 16);
		contentPane.add(lblHours);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String eventName = txtEventName.getText();
				LocalDate startDate = startDatePicker.getDate();
				LocalDate endDate = endDatePicker.getDate();
				LocalTime startTime = startTimePicker.getTime();
				LocalTime endTime = endTimePicker.getTime();
				boolean exHoliday = chkExHoliday.isSelected();
				boolean limitTime = chkLimitTime.isSelected();
				try {
					spnEventLength.commitEdit();
				} catch (java.text.ParseException ex) {
				}
				int eventLength = (Integer) spnEventLength.getValue();
				
				
				if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
					JOptionPane.showMessageDialog(contentPane, "Invalid date entry.");
					return;
				}
				
				if (limitTime && (startTime == null || endTime == null || startTime.isAfter(endTime)
						|| startTime.equals(endTime) || HOURS.between(startTime, endTime) < eventLength)) {
					JOptionPane.showMessageDialog(contentPane, "Invalid time entry.");
					return;
				}
				
				Scheduler sch = new Scheduler();
				sch.seteventName(eventName);
				sch.setlengthHours(eventLength);
				sch.setlengthDays(eventLength/24);
				sch.setexHoliday(exHoliday);
				sch.setlimitTime(limitTime);
				sch.setstartDate(startDate);
				sch.setendDate(endDate);
				sch.setstartTime(startTime);
				sch.setendTime(endTime);
				sch.start();

			}
		});
		btnStart.setBounds(319, 251, 102, 29);
		contentPane.add(btnStart);

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		btnClose.setBounds(433, 251, 117, 29);
		contentPane.add(btnClose);

	}
}
