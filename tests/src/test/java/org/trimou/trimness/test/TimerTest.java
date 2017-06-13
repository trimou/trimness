package org.trimou.trimness.test;

import org.junit.Assert;
import org.junit.Test;

public class TimerTest {

    @Test
    public void testInterval() throws InterruptedException {
        long start = System.currentTimeMillis();
        Timer.of(60).countDown();
        Assert.assertTrue((System.currentTimeMillis() - start) >= 60);
    }

    @Test
    public void testCondition() throws InterruptedException {
        long start = System.currentTimeMillis();
        Timer.of(200).setSleepInterval(50).stopIf(() -> true).countDown();
        Assert.assertTrue((System.currentTimeMillis() - start) < 200);
    }

    @Test(expected = AssertionError.class)
    public void testFailIfElapsed() throws InterruptedException {
        Timer.of(60).stopIf(() -> false).setFailIfElapsed(true).countDown();
    }

}
