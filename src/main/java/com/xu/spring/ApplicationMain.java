package com.xu.spring;

import com.xu.server.Mediator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationMain {
    private static volatile boolean running = true;
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.xu");

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                context.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

            synchronized (ApplicationMain.class) {
                running = false;
                ApplicationMain.class.notify();
            }
        }));

        context.start();

        System.out.println("服务器已启动======");
        System.out.println(Mediator.methodBeanMap);
        synchronized (ApplicationMain.class) {
            while (running) {
                try {
                    ApplicationMain.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
