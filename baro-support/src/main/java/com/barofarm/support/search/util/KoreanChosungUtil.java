package com.barofarm.support.search.util;

public final class KoreanChosungUtil {

    private static final char[] CHOSUNG = {
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ',
        'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ',
        'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ',
        'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    private KoreanChosungUtil() {}

    public static String extract(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        StringBuilder result = new StringBuilder();

        for (char ch : text.toCharArray()) {
            // 공백은 그대로 유지
            if (ch == ' ') {
                result.append(' ');
                continue;
            }

            // 한글 음절 범위
            if (ch >= 0xAC00 && ch <= 0xD7A3) {
                int index = (ch - 0xAC00) / (21 * 28);
                result.append(CHOSUNG[index]);
            } else {
                // 한글 아닌 문자는 그대로
                result.append(ch);
            }
        }

        return result.toString();
    }
}
