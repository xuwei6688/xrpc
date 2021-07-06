package com.xu.server;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.lang.reflect.Method;
import java.util.Map;

@Component
public class InitLoadRemote implements ApplicationListener<ContextRefreshedEvent>, Ordered {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        ApplicationContext context = contextRefreshedEvent.getApplicationContext();
        Map<String, Object> beansMap = context.getBeansWithAnnotation(Controller.class);
        beansMap.values().forEach(bean ->{
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Remote.class)) {
                    Mediator.MethodBean methodBean = new Mediator.MethodBean();
                    methodBean.setBean(bean);
                    methodBean.setMethod(method);

                    Remote remote = method.getAnnotation(Remote.class);
                    Mediator.methodBeanMap.put(remote.value(), methodBean);
                }
            }
        });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
