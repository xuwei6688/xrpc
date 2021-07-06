package com.xu.server;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Mediator 负责根据请求找到对应的方法执行请求</p>
 *
 * 1.准备：{@link InitLoadRemote} ApplicationListener 会在 Spring 容器就绪后遍历所有的 Bean，
 * 找到被 @Controller 修饰的类，然后找到被 @Remote 修饰的方法，将这些方法都存到 Mediator 的 methodBeanMap中，
 * key 是 Remote 注解的值，也就是请求路径；value 是 MethodBean 封装了 bean 和 Method。
 *
 * 2.请求分发：当 NettyServer 接收到请求后会取出 {@link RequestFuture} 中的 path，然后从 methodBeanMap 中取出对应的
 * MethodBean，这样就可以知道对应的 Method，然后把参数序列化，通过反射调用方法。
 */
public class Mediator {
    public static Map<String, MethodBean> methodBeanMap = new HashMap<>();

    public static Response process(RequestFuture requestFuture) {

        try {
            Object path = requestFuture.getUrl();
            MethodBean methodBean = methodBeanMap.get(path);
            if (methodBean == null) {
                throw new IllegalStateException();
            }

            Object request = requestFuture.getMsg();
            Method method = methodBean.getMethod();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> parameterType = parameterTypes[0];
            Object param = null;
            if (parameterType.isAssignableFrom(List.class)) {
                param  = JSONArray.parseArray(JSONArray.toJSONString(request));
            } else if (parameterType.getName().equals(String.class.getName())) {
                param = request;
            }else{
                param = JSONObject.parseObject(JSONObject.toJSONString(request), parameterType);
            }

            Object bean = methodBean.getBean();


            Object result = method.invoke(bean, param);
            Response response = new Response();
            response.setId(requestFuture.getId());
            response.setResult(result);
            return response;

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static class MethodBean{
        private Object bean;
        private Method method;

        public Object getBean() {
            return bean;
        }

        public void setBean(Object bean) {
            this.bean = bean;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }
    }
}
