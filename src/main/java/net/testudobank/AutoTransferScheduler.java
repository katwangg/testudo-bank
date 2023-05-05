package net.testudobank;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.util.Calendar;

public class AutoTransferScheduler {
    private final Runnable transfer;

    // when
    private final Calendar startDate;
    private final Calendar endDate;
    Calendar nextRunDate;

    private final int startMonth;
    private final int dayOfStartMonth;
    private final int endMonth;
    private final int dayOfEndMonth;
    private final int hourOfDay;

    private final String frequency;

    // The current timer
    private Timer current = new Timer();// to avoid NPE

    public void cancelCurrent() {
        current.cancel();// cancel this execution;
        current.purge(); // removes the timertask so it can be gc'ed
    }

    // create a new instance
    public static AutoTransferScheduler schedule(Runnable runnable, Date startDate, Date endDate, String frequency) {
        Calendar transferStartDate = Calendar.getInstance();
        Calendar transferEndDate = Calendar.getInstance();
        transferStartDate.setTime(startDate);
        transferEndDate.setTime(endDate);

        int startMonth = transferStartDate.get(Calendar.MONTH);
        int dayOfStartMonth = transferStartDate.get(Calendar.DAY_OF_MONTH);
        int endMonth = transferEndDate.get(Calendar.MONTH);
        int dayOfEndMonth = transferEndDate.get(Calendar.DAY_OF_MONTH);
        int hourOfDay = transferStartDate.get(Calendar.HOUR_OF_DAY);

        return new AutoTransferScheduler(runnable, transferStartDate, transferEndDate, startMonth, dayOfStartMonth,
                endMonth, dayOfEndMonth, hourOfDay, frequency);
    }

    private AutoTransferScheduler(Runnable runnable, Calendar startDate, Calendar endDate, int startMonth,
            int dayOfStartMonth, int endMonth, int dayOfEndMonth, int hourOfDay, String frequency) {
        this.transfer = runnable;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextRunDate = null;
        this.startMonth = startMonth;
        this.dayOfStartMonth = dayOfStartMonth;
        this.endMonth = endMonth;
        this.dayOfEndMonth = dayOfEndMonth;
        this.hourOfDay = hourOfDay;
        this.frequency = frequency;
        schedule(frequency);
    }

    // Schedules the task for execution on next month or week.
    private void schedule(String frequency) {
        cancelCurrent();

        Calendar currentDate = Calendar.getInstance();

        if (currentDate.compareTo(endDate) < 0) { // before the endDate
            current = new Timer(); // assigning a new instance; will allow the previous Timer to be gc'ed

            if (frequency == "monthly") {
                current.schedule(new TimerTask() {
                    public void run() {
                        try {
                            if (currentDate.compareTo(startDate) >= 0) { // only run if it is currently between the
                                                                         // start and end date
                                transfer.run();
                            }
                        } finally {
                            if (currentDate.compareTo(endDate) < 0) {
                                System.out.println("schedule: schedule for the next month");
                                schedule(frequency); // schedule for the next month
                            }
                        }
                    }
                }, nextDateMonthly());
            } else {
                current.schedule(new TimerTask() {
                    public void run() {
                        try {
                            if (currentDate.compareTo(startDate) >= 0) { // only run if current date is between the
                                // start and end date
                                transfer.run();
                            }
                        } finally {
                            if (currentDate.compareTo(endDate) < 0) {
                                System.out.println("schedule: schedule for the next week");
                                schedule(frequency); // schedule for the next week
                            }
                        }
                    }
                }, nextDateWeekly());
            }
        }
    }

    // Calculates the next date for Monthly auto transfers
    private Date nextDateMonthly() {
        Calendar CURRENT_DATE = Calendar.getInstance();
        Calendar runDate = Calendar.getInstance();

        runDate.set(Calendar.MONTH, startMonth);
        runDate.set(Calendar.DAY_OF_MONTH, dayOfStartMonth);
        runDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        runDate.set(Calendar.MINUTE, 0);
        runDate.set(Calendar.SECOND, 0);

        if (CURRENT_DATE.compareTo(startDate) >= 0) { // if it is currently the start date or after
            runDate.add(Calendar.MONTH, 1); // set to next month
        }

        System.out.println("nextDateMonthly: " + runDate.getTime());

        nextRunDate = runDate;
        return runDate.getTime();
    }

    // Calculates the next date for Weekly auto trasfers
    private Date nextDateWeekly() {
        Calendar CURRENT_DATE = Calendar.getInstance();
        Calendar runDate = Calendar.getInstance();
        
        runDate.set(Calendar.MONTH, startMonth);
        runDate.set(Calendar.DAY_OF_MONTH, dayOfStartMonth);
        runDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
        runDate.set(Calendar.MINUTE, 0);
        runDate.set(Calendar.SECOND, 0);

        if (CURRENT_DATE.compareTo(startDate) >= 0) { // if it is currently the start date or after
            runDate.add(Calendar.DAY_OF_MONTH, 7); // set to next week
        }

        System.out.println("nextDateWeekly: " + runDate.getTime());
        
        nextRunDate = runDate;
        return runDate.getTime();
    }
}
