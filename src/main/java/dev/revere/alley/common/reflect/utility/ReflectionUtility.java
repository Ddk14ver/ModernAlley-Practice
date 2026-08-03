package dev.revere.alley.common.reflect.utility;

import dev.revere.alley.common.logger.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <b>ReflectionUtils</b>
 * <p>
 * This class provides useful methods which makes dealing with reflect much easier, especially when working with Bukkit
 * 此类提供了一些有用的方法，使反射操作更加简单，尤其是在使用 Bukkit 时
 * <p>
 * You are welcome to use it, modify it and redistribute it under the following conditions:
 * 欢迎您使用、修改和重新分发它，但需遵守以下条件：
 * <ul>
 * <li>Don't claim this class as your own
 *     不要声称此类为您自己的作品
 * <li>Don't remove this disclaimer
 *     不要移除此免责声明
 * </ul>
 * <p>
 * <i>It would be nice if you provide credit to me if you use this class in a published project</i>
 * <i>如果您在发布的项目中使用此类，请注明出处，我将不胜感激</i>
 *
 * @author DarkBlade12
 * @version 1.1
 */
public final class ReflectionUtility {
    // Prevent accidental construction
    // 防止意外构造
    private ReflectionUtility() {
    }

    /**
     * Returns the constructor of a given class with the given parameter types
     * 返回具有给定参数类型的给定类的构造函数
     *
     * @param clazz          Target class
     *                       目标类
     * @param parameterTypes Parameter types of the desired constructor
     *                       所需构造函数的参数类型
     * @return The constructor of the target class with the specified parameter types
     *         具有指定参数类型的目标类的构造函数
     * @throws NoSuchMethodException If the desired constructor with the specified parameter types cannot be found
     *                               如果找不到具有指定参数类型的所需构造函数
     * @see DataType
     * @see DataType#getPrimitive(Class[])
     * @see DataType#compare(Class[], Class[])
     */
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (!DataType.compare(DataType.getPrimitive(constructor.getParameterTypes()), primitiveTypes)) {
                continue;
            }
            return constructor;
        }
        throw new NoSuchMethodException("There is no such constructor in this class with the specified parameter types");
    }

    /**
     * Returns the constructor of a desired class with the given parameter types
     * 返回具有给定参数类型的所需类的构造函数
     *
     * @param className      Name of the desired target class
     *                       所需目标类的名称
     * @param packageType    Package where the desired target class is located
     *                       所需目标类所在的包
     * @param parameterTypes Parameter types of the desired constructor
     *                       所需构造函数的参数类型
     * @return The constructor of the desired target class with the specified parameter types
     *         具有指定参数类型的所需目标类的构造函数
     * @throws NoSuchMethodException  If the desired constructor with the specified parameter types cannot be found
     *                                如果找不到具有指定参数类型的所需构造函数
     * @throws ClassNotFoundException ClassNotFoundException If the desired target class with the specified name and package cannot be found
     *                                如果找不到具有指定名称和包的所需目标类
     * @see #getConstructor(Class, Class...)
     */
    public static Constructor<?> getConstructor(String className, PackageType packageType, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getConstructor(packageType.getClass(className), parameterTypes);
    }

    /**
     * Returns an instance of a class with the given arguments
     * 返回具有给定参数的类的实例
     *
     * @param clazz     Target class
     *                  目标类
     * @param arguments Arguments which are used to construct an object of the target class
     *                  用于构造目标类对象的参数
     * @return The instance of the target class with the specified arguments
     *         具有指定参数的目标类的实例
     * @throws InstantiationException    If you cannot create an instance of the target class due to certain circumstances
     *                                   如果由于某些情况无法创建目标类的实例
     * @throws IllegalAccessException    If the desired constructor cannot be accessed due to certain circumstances
     *                                   如果由于某些情况无法访问所需的构造函数
     * @throws IllegalArgumentException  If the types of the arguments do not match the parameter types of the constructor (this should not occur since it searches for a constructor with the types of the arguments)
     *                                   如果参数类型与构造函数的参数类型不匹配（这不应该发生，因为它会搜索具有参数类型的构造函数）
     * @throws InvocationTargetException If the desired constructor cannot be invoked
     *                                   如果无法调用所需的构造函数
     * @throws NoSuchMethodException     If the desired constructor with the specified arguments cannot be found
     *                                   如果找不到具有指定参数的所需构造函数
     */
    public static Object instantiateObject(Class<?> clazz, Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getConstructor(clazz, DataType.getPrimitive(arguments)).newInstance(arguments);
    }

    /**
     * Returns an instance of a desired class with the given arguments
     * 返回具有给定参数的所需类的实例
     *
     * @param className   Name of the desired target class
     *                    所需目标类的名称
     * @param packageType Package where the desired target class is located
     *                    所需目标类所在的包
     * @param arguments   Arguments which are used to construct an object of the desired target class
     *                    用于构造所需目标类对象的参数
     * @return The instance of the desired target class with the specified arguments
     *         具有指定参数的所需目标类的实例
     * @throws InstantiationException    If you cannot create an instance of the desired target class due to certain circumstances
     *                                   如果由于某些情况无法创建所需目标类的实例
     * @throws IllegalAccessException    If the desired constructor cannot be accessed due to certain circumstances
     *                                   如果由于某些情况无法访问所需的构造函数
     * @throws IllegalArgumentException  If the types of the arguments do not match the parameter types of the constructor (this should not occur since it searches for a constructor with the types of the arguments)
     *                                   如果参数类型与构造函数的参数类型不匹配（这不应该发生，因为它会搜索具有参数类型的构造函数）
     * @throws InvocationTargetException If the desired constructor cannot be invoked
     *                                   如果无法调用所需的构造函数
     * @throws NoSuchMethodException     If the desired constructor with the specified arguments cannot be found
     *                                   如果找不到具有指定参数的所需构造函数
     * @throws ClassNotFoundException    If the desired target class with the specified name and package cannot be found
     *                                   如果找不到具有指定名称和包的所需目标类
     * @see #instantiateObject(Class, Object...)
     */
    public static Object instantiateObject(String className, PackageType packageType, Object... arguments) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return instantiateObject(packageType.getClass(className), arguments);
    }

    /**
     * Returns a method of a class with the given parameter types
     * 返回具有给定参数类型的类的方法
     *
     * @param clazz          Target class
     *                       目标类
     * @param methodName     Name of the desired method
     *                       所需方法的名称
     * @param parameterTypes Parameter types of the desired method
     *                       所需方法的参数类型
     * @return The method of the target class with the specified name and parameter types
     *         具有指定名称和参数类型的目标类的方法
     * @throws NoSuchMethodException If the desired method of the target class with the specified name and parameter types cannot be found
     *                               如果找不到具有指定名称和参数类型的目标类的所需方法
     * @see DataType#getPrimitive(Class[])
     * @see DataType#compare(Class[], Class[])
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?>[] primitiveTypes = DataType.getPrimitive(parameterTypes);
        for (Method method : clazz.getMethods()) {
            if (!method.getName().equals(methodName) || !DataType.compare(DataType.getPrimitive(method.getParameterTypes()), primitiveTypes)) {
                continue;
            }
            return method;
        }
        throw new NoSuchMethodException("There is no such method in this class with the specified name and parameter types");
    }

    /**
     * Returns a method of a desired class with the given parameter types
     * 返回具有给定参数类型的所需类的方法
     *
     * @param className      Name of the desired target class
     *                       所需目标类的名称
     * @param packageType    Package where the desired target class is located
     *                       所需目标类所在的包
     * @param methodName     Name of the desired method
     *                       所需方法的名称
     * @param parameterTypes Parameter types of the desired method
     *                       所需方法的参数类型
     * @return The method of the desired target class with the specified name and parameter types
     *         具有指定名称和参数类型的所需目标类的方法
     * @throws NoSuchMethodException  If the desired method of the desired target class with the specified name and parameter types cannot be found
     *                                如果找不到具有指定名称和参数类型的所需目标类的所需方法
     * @throws ClassNotFoundException If the desired target class with the specified name and package cannot be found
     *                                如果找不到具有指定名称和包的所需目标类
     * @see #getMethod(Class, String, Class...)
     */
    public static Method getMethod(String className, PackageType packageType, String methodName, Class<?>... parameterTypes) throws NoSuchMethodException, ClassNotFoundException {
        return getMethod(packageType.getClass(className), methodName, parameterTypes);
    }

    /**
     * Invokes a method on an object with the given arguments
     * 使用给定的参数在对象上调用方法
     *
     * @param instance   Target object
     *                   目标对象
     * @param methodName Name of the desired method
     *                   所需方法的名称
     * @param arguments  Arguments which are used to invoke the desired method
     *                   用于调用所需方法的参数
     * @return The result of invoking the desired method on the target object
     *         在目标对象上调用所需方法的结果
     * @throws IllegalAccessException    If the desired method cannot be accessed due to certain circumstances
     *                                   如果由于某些情况无法访问所需的方法
     * @throws IllegalArgumentException  If the types of the arguments do not match the parameter types of the method (this should not occur since it searches for a method with the types of the arguments)
     *                                   如果参数类型与方法的参数类型不匹配（这不应该发生，因为它会搜索具有参数类型的方法）
     * @throws InvocationTargetException If the desired method cannot be invoked on the target object
     *                                   如果无法在目标对象上调用所需的方法
     * @throws NoSuchMethodException     If the desired method of the class of the target object with the specified name and arguments cannot be found
     *                                   如果找不到具有指定名称和参数的目标对象类的所需方法
     * @see #getMethod(Class, String, Class...)
     * @see DataType#getPrimitive(Object[])
     */
    public static Object invokeMethod(Object instance, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(instance.getClass(), methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    /**
     * Invokes a method of the target class on an object with the given arguments
     * 使用给定的参数在对象上调用目标类的方法
     *
     * @param instance   Target object
     *                   目标对象
     * @param clazz      Target class
     *                   目标类
     * @param methodName Name of the desired method
     *                   所需方法的名称
     * @param arguments  Arguments which are used to invoke the desired method
     *                   用于调用所需方法的参数
     * @return The result of invoking the desired method on the target object
     *         在目标对象上调用所需方法的结果
     * @throws IllegalAccessException    If the desired method cannot be accessed due to certain circumstances
     *                                   如果由于某些情况无法访问所需的方法
     * @throws IllegalArgumentException  If the types of the arguments do not match the parameter types of the method (this should not occur since it searches for a method with the types of the arguments)
     *                                   如果参数类型与方法的参数类型不匹配（这不应该发生，因为它会搜索具有参数类型的方法）
     * @throws InvocationTargetException If the desired method cannot be invoked on the target object
     *                                   如果无法在目标对象上调用所需的方法
     * @throws NoSuchMethodException     If the desired method of the target class with the specified name and arguments cannot be found
     *                                   如果找不到具有指定名称和参数的目标类的所需方法
     * @see #getMethod(Class, String, Class...)
     * @see DataType#getPrimitive(Object[])
     */
    public static Object invokeMethod(Object instance, Class<?> clazz, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        return getMethod(clazz, methodName, DataType.getPrimitive(arguments)).invoke(instance, arguments);
    }

    /**
     * Invokes a method of a desired class on an object with the given arguments
     * 使用给定的参数在对象上调用所需类的方法
     *
     * @param instance    Target object
     *                    目标对象
     * @param className   Name of the desired target class
     *                    所需目标类的名称
     * @param packageType Package where the desired target class is located
     *                    所需目标类所在的包
     * @param methodName  Name of the desired method
     *                    所需方法的名称
     * @param arguments   Arguments which are used to invoke the desired method
     *                    用于调用所需方法的参数
     * @return The result of invoking the desired method on the target object
     *         在目标对象上调用所需方法的结果
     * @throws IllegalAccessException    If the desired method cannot be accessed due to certain circumstances
     *                                   如果由于某些情况无法访问所需的方法
     * @throws IllegalArgumentException  If the types of the arguments do not match the parameter types of the method (this should not occur since it searches for a method with the types of the arguments)
     *                                   如果参数类型与方法的参数类型不匹配（这不应该发生，因为它会搜索具有参数类型的方法）
     * @throws InvocationTargetException If the desired method cannot be invoked on the target object
     *                                   如果无法在目标对象上调用所需的方法
     * @throws NoSuchMethodException     If the desired method of the desired target class with the specified name and arguments cannot be found
     *                                   如果找不到具有指定名称和参数的所需目标类的所需方法
     * @throws ClassNotFoundException    If the desired target class with the specified name and package cannot be found
     *                                   如果找不到具有指定名称和包的所需目标类
     * @see #invokeMethod(Object, Class, String, Object...)
     */
    public static Object invokeMethod(Object instance, String className, PackageType packageType, String methodName, Object... arguments) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        return invokeMethod(instance, packageType.getClass(className), methodName, arguments);
    }

    /**
     * Returns a field of the target class with the given name
     * 返回具有给定名称的目标类的字段
     *
     * @param clazz     Target class
     *                  目标类
     * @param declared  Whether the desired field is declared or not
     *                  所需字段是否为已声明的字段
     * @param fieldName Name of the desired field
     *                  所需字段的名称
     * @return The field of the target class with the specified name
     *         具有指定名称的目标类的字段
     * @throws NoSuchFieldException If the desired field of the given class cannot be found
     *                              如果找不到给定类的所需字段
     * @throws SecurityException    If the desired field cannot be made accessible
     *                              如果无法使所需字段可访问
     */
    public static Field getField(Class<?> clazz, boolean declared, String fieldName) throws NoSuchFieldException, SecurityException {
        Field field = declared ? clazz.getDeclaredField(fieldName) : clazz.getField(fieldName);
        field.setAccessible(true);
        return field;
    }

    /**
     * Returns a field of a desired class with the given name
     * 返回具有给定名称的所需类的字段
     *
     * @param className   Name of the desired target class
     *                    所需目标类的名称
     * @param packageType Package where the desired target class is located
     *                    所需目标类所在的包
     * @param declared    Whether the desired field is declared or not
     *                    所需字段是否为已声明的字段
     * @param fieldName   Name of the desired field
     *                    所需字段的名称
     * @return The field of the desired target class with the specified name
     *         具有指定名称的所需目标类的字段
     * @throws NoSuchFieldException   If the desired field of the desired class cannot be found
     *                                如果找不到所需类的所需字段
     * @throws SecurityException      If the desired field cannot be made accessible
     *                                如果无法使所需字段可访问
     * @throws ClassNotFoundException If the desired target class with the specified name and package cannot be found
     *                                如果找不到具有指定名称和包的所需目标类
     * @see #getField(Class, boolean, String)
     */
    public static Field getField(String className, PackageType packageType, boolean declared, String fieldName) throws NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getField(packageType.getClass(className), declared, fieldName);
    }

    /**
     * Returns the value of a field of the given class of an object
     * 返回对象的给定类的字段值
     *
     * @param instance  Target object
     *                  目标对象
     * @param clazz     Target class
     *                  目标类
     * @param declared  Whether the desired field is declared or not
     *                  所需字段是否为已声明的字段
     * @param fieldName Name of the desired field
     *                  所需字段的名称
     * @return The value of field of the target object
     *         目标对象的字段值
     * @throws IllegalArgumentException If the target object does not feature the desired field
     *                                  如果目标对象不包含所需的字段
     * @throws IllegalAccessException   If the desired field cannot be accessed
     *                                  如果无法访问所需的字段
     * @throws NoSuchFieldException     If the desired field of the target class cannot be found
     *                                  如果找不到目标类的所需字段
     * @throws SecurityException        If the desired field cannot be made accessible
     *                                  如果无法使所需字段可访问
     * @see #getField(Class, boolean, String)
     */
    public static Object getValue(Object instance, Class<?> clazz, boolean declared, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getField(clazz, declared, fieldName).get(instance);
    }

    /**
     * Returns the value of a field of a desired class of an object
     * 返回对象的所需类的字段值
     *
     * @param instance    Target object
     *                    目标对象
     * @param className   Name of the desired target class
     *                    所需目标类的名称
     * @param packageType Package where the desired target class is located
     *                    所需目标类所在的包
     * @param declared    Whether the desired field is declared or not
     *                    所需字段是否为已声明的字段
     * @param fieldName   Name of the desired field
     *                    所需字段的名称
     * @return The value of field of the target object
     *         目标对象的字段值
     * @throws IllegalArgumentException If the target object does not feature the desired field
     *                                  如果目标对象不包含所需的字段
     * @throws IllegalAccessException   If the desired field cannot be accessed
     *                                  如果无法访问所需的字段
     * @throws NoSuchFieldException     If the desired field of the desired class cannot be found
     *                                  如果找不到所需类的所需字段
     * @throws SecurityException        If the desired field cannot be made accessible
     *                                  如果无法使所需字段可访问
     * @throws ClassNotFoundException   If the desired target class with the specified name and package cannot be found
     *                                  如果找不到具有指定名称和包的所需目标类
     * @see #getValue(Object, Class, boolean, String)
     */
    public static Object getValue(Object instance, String className, PackageType packageType, boolean declared, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        return getValue(instance, packageType.getClass(className), declared, fieldName);
    }

    /**
     * Returns the value of a field with the given name of an object
     * 返回具有给定名称的对象的字段值
     *
     * @param instance  Target object
     *                  目标对象
     * @param declared  Whether the desired field is declared or not
     *                  所需字段是否为已声明的字段
     * @param fieldName Name of the desired field
     *                  所需字段的名称
     * @return The value of field of the target object
     *         目标对象的字段值
     * @throws IllegalArgumentException If the target object does not feature the desired field (should not occur since it searches for a field with the given name in the class of the object)
     *                                  如果目标对象不包含所需的字段（不应该发生，因为它会在对象的类中搜索具有给定名称的字段）
     * @throws IllegalAccessException   If the desired field cannot be accessed
     *                                  如果无法访问所需的字段
     * @throws NoSuchFieldException     If the desired field of the target object cannot be found
     *                                  如果找不到目标对象的所需字段
     * @throws SecurityException        If the desired field cannot be made accessible
     *                                  如果无法使所需字段可访问
     * @see #getValue(Object, Class, boolean, String)
     */
    public static Object getValue(Object instance, boolean declared, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        return getValue(instance, instance.getClass(), declared, fieldName);
    }

    /**
     * Sets the value of a field of the given class of an object
     * 设置对象的给定类的字段值
     *
     * @param instance  Target object
     *                  目标对象
     * @param clazz     Target class
     *                  目标类
     * @param declared  Whether the desired field is declared or not
     *                  所需字段是否为已声明的字段
     * @param fieldName Name of the desired field
     *                  所需字段的名称
     * @param value     New value
     *                  新值
     * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
     *                                  如果值的类型与所需字段的类型不匹配
     * @throws IllegalAccessException   If the desired field cannot be accessed
     *                                  如果无法访问所需的字段
     * @throws NoSuchFieldException     If the desired field of the target class cannot be found
     *                                  如果找不到目标类的所需字段
     * @throws SecurityException        If the desired field cannot be made accessible
     *                                  如果无法使所需字段可访问
     * @see #getField(Class, boolean, String)
     */
    public static void setValue(Object instance, Class<?> clazz, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        getField(clazz, declared, fieldName).set(instance, value);
    }

    /**
     * Sets the value of a field of a desired class of an object
     * 设置对象的所需类的字段值
     *
     * @param instance    Target object
     *                    目标对象
     * @param className   Name of the desired target class
     *                    所需目标类的名称
     * @param packageType Package where the desired target class is located
     *                    所需目标类所在的包
     * @param declared    Whether the desired field is declared or not
     *                    所需字段是否为已声明的字段
     * @param fieldName   Name of the desired field
     *                    所需字段的名称
     * @param value       New value
     *                    新值
     * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
     *                                  如果值的类型与所需字段的类型不匹配
     * @throws IllegalAccessException   If the desired field cannot be accessed
     *                                  如果无法访问所需的字段
     * @throws NoSuchFieldException     If the desired field of the desired class cannot be found
     *                                  如果找不到所需类的所需字段
     * @throws SecurityException        If the desired field cannot be made accessible
     *                                  如果无法使所需字段可访问
     * @throws ClassNotFoundException   If the desired target class with the specified name and package cannot be found
     *                                  如果找不到具有指定名称和包的所需目标类
     * @see #setValue(Object, Class, boolean, String, Object)
     */
    public static void setValue(Object instance, String className, PackageType packageType, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException, ClassNotFoundException {
        setValue(instance, packageType.getClass(className), declared, fieldName, value);
    }

    /**
     * Sets the value of a field with the given name of an object
     * 设置具有给定名称的对象的字段值
     *
     * @param instance  Target object
     *                  目标对象
     * @param declared  Whether the desired field is declared or not
     *                  所需字段是否为已声明的字段
     * @param fieldName Name of the desired field
     *                  所需字段的名称
     * @param value     New value
     *                  新值
     * @throws IllegalArgumentException If the type of the value does not match the type of the desired field
     *                                  如果值的类型与所需字段的类型不匹配
     * @throws IllegalAccessException   If the desired field cannot be accessed
     *                                  如果无法访问所需的字段
     * @throws NoSuchFieldException     If the desired field of the target object cannot be found
     *                                  如果找不到目标对象的所需字段
     * @throws SecurityException        If the desired field cannot be made accessible
     *                                  如果无法使所需字段可访问
     * @see #setValue(Object, Class, boolean, String, Object)
     */
    public static void setValue(Object instance, boolean declared, String fieldName, Object value) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        setValue(instance, instance.getClass(), declared, fieldName, value);
    }

    /**
     * Represents an enumeration of dynamic packages of NMS and CraftBukkit
     * 表示 NMS 和 CraftBukkit 的动态包枚举
     * <p>
     * This class is part of the <b>ReflectionUtils</b> and follows the same usage conditions
     * 此类是 <b>ReflectionUtils</b> 的一部分，并遵循相同的使用条件
     *
     * @author DarkBlade12
     * @since 1.0
     */
    public enum PackageType {
        MINECRAFT_SERVER("net.minecraft.server." + getServerVersion()),
        CRAFTBUKKIT("org.bukkit.craftbukkit." + getServerVersion()),
        CRAFTBUKKIT_BLOCK(CRAFTBUKKIT, "block"),
        CRAFTBUKKIT_CHUNKIO(CRAFTBUKKIT, "chunkio"),
        CRAFTBUKKIT_COMMAND(CRAFTBUKKIT, "command"),
        CRAFTBUKKIT_CONVERSATIONS(CRAFTBUKKIT, "conversations"),
        CRAFTBUKKIT_ENCHANTMENS(CRAFTBUKKIT, "enchantments"),
        CRAFTBUKKIT_ENTITY(CRAFTBUKKIT, "entity"),
        CRAFTBUKKIT_EVENT(CRAFTBUKKIT, "event"),
        CRAFTBUKKIT_GENERATOR(CRAFTBUKKIT, "generator"),
        CRAFTBUKKIT_HELP(CRAFTBUKKIT, "help"),
        CRAFTBUKKIT_INVENTORY(CRAFTBUKKIT, "inventory"),
        CRAFTBUKKIT_MAP(CRAFTBUKKIT, "map"),
        CRAFTBUKKIT_METADATA(CRAFTBUKKIT, "metadata"),
        CRAFTBUKKIT_POTION(CRAFTBUKKIT, "potion"),
        CRAFTBUKKIT_PROJECTILES(CRAFTBUKKIT, "projectiles"),
        CRAFTBUKKIT_SCHEDULER(CRAFTBUKKIT, "scheduler"),
        CRAFTBUKKIT_SCOREBOARD(CRAFTBUKKIT, "scoreboard"),
        CRAFTBUKKIT_UPDATER(CRAFTBUKKIT, "updater"),
        CRAFTBUKKIT_UTIL(CRAFTBUKKIT, "util");

        private final String path;

        /**
         * Construct a new package type
         * 构造一个新的包类型
         *
         * @param path Path of the package
         *             包的路径
         */
        PackageType(String path) {
            this.path = path;
        }

        /**
         * Construct a new package type
         * 构造一个新的包类型
         *
         * @param parent Parent package of the package
         *               包的父包
         * @param path   Path of the package
         *               包的路径
         */
        PackageType(PackageType parent, String path) {
            this(parent + "." + path);
        }

        /**
         * Returns the path of this package type
         * 返回此包类型的路径
         *
         * @return The path
         *         路径
         */
        public String getPath() {
            return path;
        }

        /**
         * Returns the class with the given name
         * 返回具有给定名称的类
         *
         * @param className Name of the desired class
         *                  所需类的名称
         * @return The class with the specified name
         *         具有指定名称的类
         * @throws ClassNotFoundException If the desired class with the specified name and package cannot be found
         *                                如果找不到具有指定名称和包的所需类
         */
        public Class<?> getClass(String className) throws ClassNotFoundException {
            return Class.forName(this + "." + className);
        }

        // Override for convenience
        // 为方便起见进行重写
        @Override
        public String toString() {
            return path;
        }

        /**
         * Returns the version of your server
         * 返回您的服务器版本
         *
         * @return The server version
         *         服务器版本
         */
        public static String getServerVersion() {
            return Bukkit.getServer().getClass().getPackage().getName().substring(23);
        }
    }

    /**
     * Represents an enumeration of Java data types with corresponding classes
     * 表示 Java 数据类型及其对应类的枚举
     * <p>
     * This class is part of the <b>ReflectionUtils</b> and follows the same usage conditions
     * 此类是 <b>ReflectionUtils</b> 的一部分，并遵循相同的使用条件
     *
     * @author DarkBlade12
     * @since 1.0
     */
    public enum DataType {
        BYTE(byte.class, Byte.class),
        SHORT(short.class, Short.class),
        INTEGER(int.class, Integer.class),
        LONG(long.class, Long.class),
        CHARACTER(char.class, Character.class),
        FLOAT(float.class, Float.class),
        DOUBLE(double.class, Double.class),
        BOOLEAN(boolean.class, Boolean.class);

        private static final Map<Class<?>, DataType> CLASS_MAP = new HashMap<>();
        private final Class<?> primitive;
        private final Class<?> reference;

        // Initialize map for quick class lookup
        // 初始化映射以便快速查找类
        static {
            for (DataType type : values()) {
                CLASS_MAP.put(type.primitive, type);
                CLASS_MAP.put(type.reference, type);
            }
        }

        /**
         * Construct a new data type
         * 构造一个新的数据类型
         *
         * @param primitive Primitive class of this data type
         *                  此数据类型的原始类
         * @param reference Reference class of this data type
         *                  此数据类型的引用类
         */
        DataType(Class<?> primitive, Class<?> reference) {
            this.primitive = primitive;
            this.reference = reference;
        }

        /**
         * Returns the primitive class of this data type
         * 返回此数据类型的原始类
         *
         * @return The primitive class
         *         原始类
         */
        public Class<?> getPrimitive() {
            return primitive;
        }

        /**
         * Returns the reference class of this data type
         * 返回此数据类型的引用类
         *
         * @return The reference class
         *         引用类
         */
        public Class<?> getReference() {
            return reference;
        }

        /**
         * Returns the data type with the given primitive/reference class
         * 返回具有给定原始/引用类的数据类型
         *
         * @param clazz Primitive/Reference class of the data type
         *              数据类型的原始/引用类
         * @return The data type
         *         数据类型
         */
        public static DataType fromClass(Class<?> clazz) {
            return CLASS_MAP.get(clazz);
        }

        /**
         * Returns the primitive class of the data type with the given reference class
         * 返回具有给定引用类的数据类型的原始类
         *
         * @param clazz Reference class of the data type
         *              数据类型的引用类
         * @return The primitive class
         *         原始类
         */
        public static Class<?> getPrimitive(Class<?> clazz) {
            DataType type = fromClass(clazz);
            return type == null ? clazz : type.getPrimitive();
        }

        /**
         * Returns the reference class of the data type with the given primitive class
         * 返回具有给定原始类的数据类型的引用类
         *
         * @param clazz Primitive class of the data type
         *              数据类型的原始类
         * @return The reference class
         *         引用类
         */
        public static Class<?> getReference(Class<?> clazz) {
            DataType type = fromClass(clazz);
            return type == null ? clazz : type.getReference();
        }

        /**
         * Returns the primitive class array of the given class array
         * 返回给定类数组的原始类数组
         *
         * @param classes Given class array
         *                给定的类数组
         * @return The primitive class array
         *         原始类数组
         */
        public static Class<?>[] getPrimitive(Class<?>[] classes) {
            int length = classes == null ? 0 : classes.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getPrimitive(classes[index]);
            }
            return types;
        }

        /**
         * Returns the reference class array of the given class array
         * 返回给定类数组的引用类数组
         *
         * @param classes Given class array
         *                给定的类数组
         * @return The reference class array
         *         引用类数组
         */
        public static Class<?>[] getReference(Class<?>[] classes) {
            int length = classes == null ? 0 : classes.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getReference(classes[index]);
            }
            return types;
        }

        /**
         * Returns the primitive class array of the given object array
         * 返回给定对象数组的原始类数组
         *
         * @param objects Given object array
         *                给定的对象数组
         * @return The primitive class array
         *         原始类数组
         */
        public static Class<?>[] getPrimitive(Object[] objects) {
            int length = objects == null ? 0 : objects.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getPrimitive(objects[index].getClass());
            }
            return types;
        }

        /**
         * Returns the reference class array of the given object array
         * 返回给定对象数组的引用类数组
         *
         * @param objects Given object array
         *                给定的对象数组
         * @return The reference class array
         *         引用类数组
         */
        public static Class<?>[] getReference(Object[] objects) {
            int length = objects == null ? 0 : objects.length;
            Class<?>[] types = new Class<?>[length];
            for (int index = 0; index < length; index++) {
                types[index] = getReference(objects[index].getClass());
            }
            return types;
        }

        /**
         * Compares two class arrays on equivalence
         * 比较两个类数组是否等价
         *
         * @param primary   Primary class array
         *                  主类数组
         * @param secondary Class array which is compared to the primary array
         *                  与主数组进行比较的类数组
         * @return Whether these arrays are equal or not
         *         这些数组是否相等
         */
        public static boolean compare(Class<?>[] primary, Class<?>[] secondary) {
            if (primary == null || secondary == null || primary.length != secondary.length) {
                return false;
            }
            for (int index = 0; index < primary.length; index++) {
                Class<?> primaryClass = primary[index];
                Class<?> secondaryClass = secondary[index];
                if (primaryClass.equals(secondaryClass) || primaryClass.isAssignableFrom(secondaryClass)) {
                    continue;
                }
                return false;
            }
            return true;
        }
    }

    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static Class<?> getCraftBukkitClassFromName(String name) {
        try {
            return Class.forName("org.bukkit.craftbukkit." + getVersion() + "." + name);
        } catch (ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Gets the ping of a player.
     * 获取玩家的延迟。
     *
     * @param player The player to get the ping of.
     *               要获取延迟的玩家。
     * @return The ping of the player in milliseconds.
     *         玩家的延迟（以毫秒为单位）。
     * @author Emmy
     */
    public static int getPing(Player player) {
        return player.getPing();
    }

    /**
     * Sets the unbreakable state of an item using the modern Bukkit API.
     * 使用现代 Bukkit API 设置物品的不可破坏状态。
     *
     * @param item        The item to modify.
     *                    要修改的物品。
     * @param unbreakable Whether the item should be unbreakable.
     *                    物品是否应为不可破坏的。
     * @return The modified item with the unbreakable state set.
     *         设置了不可破坏状态的修改后的物品。
     * @author Emmy
     */
    public static @NotNull ItemStack setUnbreakable(ItemStack item, boolean unbreakable) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Sets the glowing state of an item using enchantment trick (cross-version compatible).
     * 使用附魔技巧设置物品的发光状态（跨版本兼容）。
     *
     * @param item The item to modify.
     *             要修改的物品。
     * @param glow Whether the item should glow.
     *             物品是否应该发光。
     * @return The modified item with the glowing state set.
     *         设置了发光状态的修改后的物品。
     * @author Emmy
     */
    public static ItemStack setGlowing(ItemStack item, boolean glow) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (glow) {
                meta.addEnchant(org.bukkit.enchantments.Enchantment.LURE, 1, true);
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            } else {
                meta.removeEnchant(org.bukkit.enchantments.Enchantment.LURE);
                meta.removeItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Creates a SkullMeta with a base64 texture.
     * 使用 base64 纹理创建 SkullMeta。
     *
     * @param itemStack     The ItemStack to modify.
     *                      要修改的 ItemStack。
     * @param base64Texture The base64 encoded texture string.
     *                      base64 编码的纹理字符串。
     * @return The modified SkullMeta with the specified texture.
     *         具有指定纹理的修改后的 SkullMeta。
     * @author Emmy
     */
    // Cache of PlayerProfiles to avoid duplicate creates for same skin URL
    private static final Map<String, PlayerProfile> profileCache = new HashMap<>();

    public static @NotNull SkullMeta createSkullMeta(ItemStack itemStack, String base64Texture) {
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();

        try {
            String decoded = new String(Base64.getDecoder().decode(base64Texture));
            int urlStart = decoded.indexOf("http://textures.minecraft.net/texture/");
            if (urlStart == -1) {
                Logger.error("Failed to extract texture URL from base64 string for skull");
                return meta;
            }
            int urlEnd = decoded.indexOf("\"", urlStart);
            String urlString = decoded.substring(urlStart, urlEnd);

            PlayerProfile profile = profileCache.get(urlString);
            if (profile == null) {
                profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "texture");
                URL skinUrl = new URL(urlString);
                profile.getTextures().setSkin(skinUrl);
                profileCache.put(urlString, profile);
            }
            meta.setOwnerProfile(profile);
        } catch (Exception exception) {
            Logger.logException("Failed to set skull texture", exception);
        }

        return meta;
    }
}