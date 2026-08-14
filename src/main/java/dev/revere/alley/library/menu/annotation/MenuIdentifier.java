package dev.revere.alley.library.menu.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Remi
 * @project alley-practice
 * @date 22/07/2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MenuIdentifier {
    /**
     * The identifier for the menu.
     * 菜单的标识符。
     * This should be unique across all menus.
     * 所有菜单中应保证唯一。
     *
     * @return the unique identifier for the menu
     *         菜单的唯一标识符
     */
    String value();
}
