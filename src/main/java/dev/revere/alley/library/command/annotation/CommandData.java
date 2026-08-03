package dev.revere.alley.library.command.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Command Framework - Command <br>
 * 命令框架 - 命令注解 <br>
 * The command annotation used to designate methods as commands. All methods
 * 用于将方法指定为命令的命令注解。所有被注解的方法
 * should have a single CommandArgs argument
 * 应该有一个 CommandArgs 类型的参数。
 *
 * @author minnymin3
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandData {

    String name();

    String permission() default "";

    String[] aliases() default {};

    String description();

    String usage() default "";

    boolean inGameOnly() default true;

    boolean isAdminOnly() default false;

    int cooldown() default 0;
}