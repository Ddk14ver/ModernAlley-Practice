package dev.revere.alley.common.text;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    /**
     * A helper method to convert any enum constants name into a user-friendly, title-cased string.
     * 一个辅助方法，将任何枚举常量名称转换为用户友好的、首字母大写的字符串。
     * Example: SOME_ENUM_CONSTANT -> Some Enum Constant
     * 示例：SOME_ENUM_CONSTANT -> Some Enum Constant
     *
     * @param anEnum The enum constants to format.
     *               要格式化的枚举常量。
     * @return A formatted, title-cased string.
     *         格式化后的、首字母大写的字符串。
     */
    public String formatEnumName(Enum<?> anEnum) {
        String lowerCase = anEnum.name().replace('_', ' ').toLowerCase();

        String[] words = lowerCase.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        return result.toString().trim();
    }
}
