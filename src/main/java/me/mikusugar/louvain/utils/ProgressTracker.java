package me.mikusugar.louvain.utils;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * The ProgressTracker class is used to track the progress of a task.
 * @author FangJie
 * @version 1.0, 2023/10/09
 */
public class ProgressTracker
{

    private long startTime;

    private final long total;

    /**
     * Current progress
     */
    private long current;

    /**
     * Stores the timestamp of the last update made.
     */
    private long lastUpdateTime;

    private boolean isStart;

    /**
     * Constructs a new ProgressTracker object with the given sum.
     *
     * @param total the sum of the progress that needs to be tracked
     * @throws IllegalArgumentException if the sum is less than or equal to zero
     */
    public ProgressTracker(long total)
    {
        if (total < 0)
        {
            throw new IllegalArgumentException("Sum must be greater than or equal to 0");
        }
        this.total = total;
        this.isStart = false;
    }

    /**
     * Starts the operation.
     * <p>
     * This method sets the start time, initializes the current value to 0, and sets the flag indicating that the operation has started.
     *
     * @throws IllegalStateException if the start method is called more than once
     */
    public void start()
    {
        if (isStart)
        {
            throw new IllegalStateException("The start method can only be executed once.");
        }
        this.startTime = System.currentTimeMillis();
        this.lastUpdateTime = startTime;
        this.current = 0;
        this.isStart = true;
    }

    /**
     * Sets the current value of the operation.
     * <p>
     * This method updates the current value and the last update time of the operation.
     *
     * @param current the new value for the current position
     * @throws IllegalArgumentException if the start method has not been called yet, or if the current value is greater than the sum of the operation, or if the current value is less than 0
     */
    public void setCurrent(long current)
    {
        if (!isStart)
        {
            throw new IllegalArgumentException("The start method must be executed at the beginning.");
        }
        if (current > total)
        {
            throw new IllegalArgumentException("Current must be less than or equal to sum.");
        }
        if (current < 0)
        {
            throw new IllegalArgumentException("Current must be greater than 0.");
        }
        this.current = current;
        this.lastUpdateTime = System.currentTimeMillis();
    }

    /**
     * Returns the progress of the operation.
     * <p>
     * The progress is calculated by dividing the current value by the sum of the operation.
     *
     * @return the progress of the operation as a decimal value between 0 and 1
     */
    public double getProgress()
    {
        if (total == 0)
        {
            return 1;
        }
        return (double)current / total;
    }

    /**
     * Returns the progress of the operation in a human-friendly format.
     * <p>
     * The progress is calculated by dividing the current value by the sum of the operation.
     * The progress is then formatted as a string with the format "current/sum percentage%".
     *
     * @return the progress of the operation in a human-friendly format
     */
    public String getHumanFriendlyProgress()
    {
        final double percentage = getProgress() * 100;
        return current + "/" + total + " " + String.format("%.2f", percentage) + "%";
    }

    /**
     * Returns the elapsed time of the operation in milliseconds.
     * <p>
     * The elapsed time is calculated by subtracting the start time of the operation from the last update time.
     *
     * @return the elapsed time of the operation in milliseconds
     */
    public long getElapsedTime()
    {
        return lastUpdateTime - startTime;
    }

    /**
     * Returns the elapsed time between the start time and the last update time
     * in a human-friendly format.
     *
     * @return the elapsed time in a human-friendly format
     */
    public String getHumanFriendlyElapsedTime()
    {
        return getHumanFriendlyTime(getElapsedTime());
    }

    /**
     * Returns the speed of progress in a task.
     *
     * @return the speed of progress
     */
    public double getSpeed()
    {
        long elapsedTime = getElapsedTime();
        if (elapsedTime == 0)
        {
            return 0;
        }
        double progressDelta = current;
        return progressDelta / elapsedTime;
    }

    /**
     * Calculates the estimated time remaining (ETC) to complete a task based on the current progress and speed.
     *
     * @return The estimated time remaining in milliseconds, or -1 if the speed is 0.
     */
    public long getEtcTime()
    {
        final long num = total - current;
        if (getSpeed() == 0)
        {
            return -1;
        }
        return (long)(num / getSpeed());
    }

    /**
     * Returns a human-friendly string representation of the estimated time to completion (ETC).
     *
     * @return A human-friendly string representation of the ETC, or an empty string if the ETC cannot be calculated.
     */
    public String getHumanFriendlyEtcTime()
    {
        final long etcTime = getEtcTime();
        return getHumanFriendlyTime(etcTime);
    }

    /**
     * Converts a given time in milliseconds to a human-friendly format.
     *
     * @param time The time in milliseconds to be converted.
     * @return The time converted to a human-friendly format.
     */
    private static String getHumanFriendlyTime(long time)
    {
        if (time == -1)
        {
            return "unknow";
        }
        if (time <= 1000)
        {
            return time + "ms";
        }
        if (time <= 1000 * 60)
        {
            return TimeUnit.MILLISECONDS.toSeconds(time) + "s";
        }
        long minutes = time / 60000;
        long seconds = (time % 60000) / 1000;
        return minutes + "m" + seconds + "s";
    }

    public long getCurrent()
    {
        return current;
    }

    public static void main(String[] args) throws InterruptedException
    {
        Random random = new Random();
        int count = 100;
        final ProgressTracker progressTracker = new ProgressTracker(count);
        progressTracker.start();
        System.out.println(progressTracker.getHumanFriendlyEtcTime());
        for (int i = 0; i < count; i++)
        {
            Thread.sleep(random.nextInt(4 * 1000));
            progressTracker.setCurrent(i + 1);
            System.out.println(
                    progressTracker.getHumanFriendlyElapsedTime() + " " + progressTracker.getHumanFriendlyProgress() + " " + progressTracker.getHumanFriendlyEtcTime());

        }
    }

}
