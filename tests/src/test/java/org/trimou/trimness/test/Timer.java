package org.trimou.trimness.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

/**
 * Delays thread execution for specified time or unless conditions are met.
 */
public class Timer {

    private static final long DEFAULT_SLEEP_INTERVAL = 100L;

    public static Timer of(long interval) {
        return new Timer().setInterval(interval);
    }

    private long interval;

    private long sleepInterval;

    private boolean failIfElapsed;

    private List<Condition> conditions;

    private ConditionLogic logic;

    public Timer() {
        reset();
    }

    /**
     * @param interval
     *            The delay in milliseconds
     * @return self
     */
    public Timer setInterval(long interval) {
        return setInterval(interval, TimeUnit.MILLISECONDS);
    }

    /**
     *
     * @param interval
     * @param timeUnit
     * @return self
     */
    public Timer setInterval(long interval, TimeUnit timeUnit) {
        if (interval <= 0) {
            throw new IllegalArgumentException(
                    "Delay must be greater than zero");
        }
        this.interval = timeUnit.toMillis(interval);
        return this;
    }

    /**
     * Set new sleep interval value.
     *
     * @param sleepInterval
     * @return self
     */
    public Timer setSleepInterval(long sleepInterval) {
        this.sleepInterval = sleepInterval;
        return this;
    }

    /**
     * @param conditionLogic
     * @return self
     */
    public Timer setLogic(ConditionLogic conditionLogic) {
        this.logic = conditionLogic;
        return this;
    }

    /**
     * Add new stop condition.
     *
     * @param condition
     * @return self
     */
    public Timer stopIf(Condition condition) {
        conditions.add(condition);
        return this;
    }

    /**
     *
     * @param failIfElapsed
     * @return self
     */
    public Timer setFailIfElapsed(boolean failIfElapsed) {
        this.failIfElapsed = failIfElapsed;
        return this;
    }

    /**
     * Start the timer.
     *
     * @throws InterruptedException
     */
    public Timer countDown() throws InterruptedException {
        checkConfiguration();
        if (conditions == null || conditions.isEmpty()) {
            Thread.sleep(interval);
        } else {
            long start = System.currentTimeMillis();
            while (isSleepy(start)) {
                Thread.sleep(sleepInterval);
            }
        }
        return this;
    }

    /**
     * Reset to default values.
     */
    public void reset() {
        this.interval = -1;
        this.sleepInterval = DEFAULT_SLEEP_INTERVAL;
        this.logic = ConditionLogic.DISJUNCTION;
        this.conditions = new ArrayList<>();
    }

    /**
     * @return the current interval in ms
     */
    public long getInterval() {
        return interval;
    }

    /**
     * @return the current sleep interval in ms
     */
    public long getSleepInterval() {
        return sleepInterval;
    }

    private boolean isSleepy(long start) {
        if (hasConditionsMet()) {
            return false;
        }
        if (isFinished(start)) {
            return false;
        }
        return true;
    }

    private void checkConfiguration() {
        if (interval < 0 || sleepInterval < 0) {
            throw new IllegalStateException("Invalid timer configuration");
        }
    }

    private boolean hasConditionsMet() {
        switch (logic) {
        case DISJUNCTION:
            return hasAtLeastOneConditionsSatisfied();
        case CONJUNCTION:
            return hasAllConditionsSatisfied();
        default:
            throw new IllegalStateException(
                    "Unsupported condition resolution logic");
        }
    }

    private boolean hasAtLeastOneConditionsSatisfied() {
        for (Condition condition : conditions) {
            if (condition.isMet()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAllConditionsSatisfied() {
        for (Condition condition : conditions) {
            if (!condition.isMet()) {
                return false;
            }
        }
        return true;
    }

    private boolean isFinished(long start) {
        boolean finished = (System.currentTimeMillis() - start) >= interval;
        if (finished && failIfElapsed) {
            Assert.fail("Timer [interval: " + interval
                    + "] counts down and conditions not met");
        }
        return finished;
    }

    /**
     *
     */
    @FunctionalInterface
    public interface Condition {

        /**
         * @return <code>true</code> if stop condition satisfied,
         *         <code>false</code> otherwise
         */
        boolean isMet();

    }

    public static enum ConditionLogic {

        /**
         * At least one condition must be satisfied
         */
        DISJUNCTION,
        /**
         * All conditions must be satisfied
         */
        CONJUNCTION,;

    }

}