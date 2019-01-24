package com.codingapi.txlcn.tc;

import com.codingapi.txlcn.commons.lock.DTXLocks;
import com.codingapi.txlcn.spi.message.dto.MessageDto;
import com.codingapi.txlcn.spi.message.exception.RpcException;
import com.codingapi.txlcn.tc.message.ReliableMessenger;
import com.codingapi.txlcn.tc.message.TMSearcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

/**
 * Description:
 * Date: 19-1-23 上午10:52
 *
 * @author ujued
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TCAutoConfiguration.class, MiniConfiguration.class})
public class RpcTest {

    @Autowired
    private ReliableMessenger messenger;

    @Test
    public void testLock() throws RpcException {

        // 第一个锁，True
        boolean result0 = messenger.acquireLocks("1", Sets.newSet("1"), DTXLocks.S_LOCK);
        Assert.assertTrue(result0);

        // 共享锁下可以加共享锁，　True
        boolean result1 = messenger.acquireLocks("2", Sets.newSet("1"), DTXLocks.S_LOCK);
        Assert.assertTrue(result1);

        //　共享锁下只准加共享锁，False
        boolean result = messenger.acquireLocks("3", Sets.newSet("1", "2"), DTXLocks.X_LOCK);
        Assert.assertFalse(result);

        messenger.releaseLocks(Sets.newSet("1"));

        // 锁被释放，True
        boolean result2 = messenger.acquireLocks("4", Sets.newSet("2", "3"), DTXLocks.X_LOCK);
        Assert.assertTrue(result2);

        // 同一个 DTX 下, True
        boolean result3 = messenger.acquireLocks("4", Sets.newSet("2"), DTXLocks.X_LOCK);
        Assert.assertTrue(result3);
    }

    /**
     * 多次重连TM Cluster
     */
    @Test
    public void testReconnect() {
        for (int i = 0; i < 100; i++) {
            TMSearcher.search();
        }
    }

    @Test
    public void testCountDown() {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 0; i < 110; i++) {
            System.out.println(countDownLatch.getCount());
            countDownLatch.countDown();
        }
    }

    @Test
    public void testCluster() throws RpcException {
        messenger.request(new MessageDto());
    }

}
