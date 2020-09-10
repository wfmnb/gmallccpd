package com.ccpd.gmall0822.order.task;

import com.ccpd.gmall0822.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class CouponTask {

    @Autowired
    OrderService orderService;
    /*@Scheduled(cron = "0 30 2 21 * ?") //每月21号2点30分执行
    @Scheduled(cron = "0 0 * * * ?")   //每分钟执行
    @Scheduled(cron = "0 0/30 * * * ?")//30S执行一次
    @Scheduled(cron = "5 * * * * ?")   //每分钟执行一次，在每分钟的第5秒执行*/


    /*@Scheduled(cron = "0/5 * * * * ?") //每5秒执行一次
    public void work1() throws InterruptedException {
        System.out.println("thread1 = ==========" + Thread.currentThread());
        Thread.sleep(10 * 1000);
    }*/
    @Scheduled(cron = "0/5 * * * * ?")
    public void work1() throws InterruptedException {
        List<Integer> ids = orderService.checkExpiredCoupon();
        for (Integer id : ids) {
            orderService.handleExpiredCoupon(id);
        }
    }

    /*@Scheduled(cron = "0/2 * * * * ?") //每5秒执行一次
    public void work2() throws InterruptedException{
        System.out.println("thread2 = ==========" + Thread.currentThread());
    }*/

    //增加线程池，并行执行work1和work2
    @Bean
    public TaskScheduler taskScheduler(){
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        return taskScheduler;
    }
}
