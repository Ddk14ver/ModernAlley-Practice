package dev.revere.alley.common.text;

import dev.revere.alley.AlleyPlugin;
import dev.revere.alley.core.locale.LocaleService;
import dev.revere.alley.core.locale.internal.impl.message.GlobalMessagesLocaleImpl;
import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * Enum formatting utility class.
 * 枚举格式化工具类。
 *
 * @author Emmy
 * @project alley-practice
 * @since 03/09/2025
 */
@UtilityClass
public class EnumFormatter {
    /**
     * Method to get and output all values of an enum as a formatted string.
     * 获取并以格式化字符串输出枚举的所有值的方法。
     * </br>
     * </br>
     * Example Enum: {@link dev.revere.alley.feature.arena.ArenaType}
     * 示例枚举：{@link dev.revere.alley.feature.arena.ArenaType}
     * </br>
     * Appearance: Invalid arena type. Available types are SHARED, STANDALONE, FFA.
     * 外观：Invalid arena type. Available types are SHARED, STANDALONE, FFA.
     *
     * @param enumClass The enum class to get the types from.
     *                  要获取类型的枚举类。
     * @return The available types of the enum.
     *         枚举的可用类型。
     */
    public String outputAvailableValues(Class<? extends Enum<?>> enumClass) {
        Enum<?>[] enumConstants = enumClass.getEnumConstants();
        String[] enumNames = Arrays.stream(enumConstants).map(Enum::name).toArray(String[]::new);

        String availableTypes = String.join(", ", enumNames);
        String readableName = enumClassToReadable(enumClass);

        String message = AlleyPlugin.getInstance().getService(LocaleService.class).getString(GlobalMessagesLocaleImpl.ERROR_INVALID_TYPE)
                .replace("{type}", readableName)
                .replace("{types}", availableTypes);

        return CC.translate(message);
    }

    /**
     * Method to format an enum class name to a more readable format.
     * 将枚举类名格式化为更可读的格式的方法。
     * </br>
     *
     * </br>
     * Example Enum: {@link dev.revere.alley.feature.arena.ArenaType}
     * 示例枚举：{@link dev.revere.alley.feature.arena.ArenaType}
     * </br>
     * Appearance: arena type
     * 外观：arena type
     *
     * @param enumClass The enum class to format the class name from.
     *                  要格式化类名的枚举类。
     * @return The formatted class name.
     *         格式化后的类名。
     */
    public String enumClassToReadable(Class<? extends Enum<?>> enumClass) {
        return String.join(" ", enumClass.getSimpleName().split("(?=[A-Z])")).toLowerCase();
    }
}