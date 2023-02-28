package com.rhys.spring.IoC;

import java.util.List;
import java.util.Map;

/**
 * @author Rhys.Ni
 * @version 1.0
 * @date 2023/2/7 12:08 AM
 */
public interface BeanFactory {
    /**
     * 获取Bean实例
     *
     * @param beanName bean名称
     * @return java.lang.Object
     * @author Rhys.Ni
     * @date 2023/2/16
     */
    Object getBean(String beanName) throws Exception;

    /**
     * 根据beanName获取Type
     *
     * @param beanName
     * @return java.lang.Class<?>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    Class<?> getType(String beanName) throws Exception;

    /**
     * 根据Type获取bean实例
     *
     * @param c
     * @return T
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    <T> T getBean(Class<T> c) throws Exception;

    /**
     * 根据Type获取bean实例
     *
     * @param c
     * @return java.util.Map<java.lang.String, T>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    <T> Map<String, T> getBeanOfType(Class<T> c) throws Exception;

    /**
     * 根据Type获取bean集合
     *
     * @param c
     * @return java.util.List<T>
     * @author Rhys.Ni
     * @date 2023/3/1
     */
    <T> List<T> getBeanListOfType(Class<T> c) throws Exception;
}
