package com.shark.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(name="mockUser")
class User {
    private String name = "mockName";
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                '}';
    }
}

@Service(name="mockService")
class MockService {
    @Resource(name="mockUser")
    private User user;

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "MockService{" +
                "user=" + user +
                '}';
    }
}

/**
 * 注解处理类
 */
public class AnotaionProcessor {
    public static void main(String[] args) {
        List<Class<?>> classes = PackageScanner.getAllClassByPackageName("com.shark.annotation");
        Map<String, Object> beans = new HashMap<>();
        for (Class clazz : classes) {
            System.out.println("process: " + clazz.toString());
            processClass(clazz, beans);
        }

        for(Map.Entry<String, Object> entry : beans.entrySet()) {
            System.out.println("Name: " + entry.getKey());
            System.out.println("Object: " + entry.getValue().toString());
        }
    }

    /**
     * 处理特定的类
     * @param clazz
     * @param beans
     * @return
     */
    public static void processClass(Class clazz, Map<String, Object> beans) {
        Object obj=null;
        try {
            if (!clazz.isAnnotationPresent(Service.class)) {
                return;
            }

            obj = clazz.newInstance();
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Resource.class)) {
                    Resource annotation = field.getAnnotation(Resource.class);
                    String name = annotation.name();
                    Object bean = beans.get(name);
                    if (bean == null) {
                        //获取不到指定名字的Bean，可能此类还没有被扫描到，先扫描一下
                        processClass(field.getType(), beans);
                        bean = beans.get(name);
                    }
                    field.setAccessible(true);
                    field.set(obj, bean);
                }
            }

            Service service = (Service) clazz.getAnnotation(Service.class);
            beans.put(service.name(), obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
