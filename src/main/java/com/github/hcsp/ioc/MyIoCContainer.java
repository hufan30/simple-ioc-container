package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    Map<String, Object> beanMap = new HashMap<>();

    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    public static void main(String[] args) throws IOException {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        properties.forEach((beanName, beanClass) -> {
            try {
                beanMap.put((String) beanName, Class.forName((String) beanClass).getConstructor().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        //自动获取@Autowired并注入
        beanMap.forEach(this::AutowiedScan);

    }

    private void AutowiedScan(String beanName, Object beanInstance) {
        //获取具有@Autowired的filed
        List<Field> autoWired = Arrays.stream(beanInstance.getClass().getDeclaredFields()).filter(field -> field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());
        //根据filed注入值
        autoWired.forEach(field -> {
            field.setAccessible(true);
            Object di = beanMap.get(field.getName());
            String name = field.getDeclaringClass().getSimpleName();
            try {
                field.set(beanInstance, di);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beanMap.get(beanName);
    }
}
