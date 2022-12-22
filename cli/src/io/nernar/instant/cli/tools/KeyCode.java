package io.nernar.instant.cli.tools;

import io.nernar.instant.cli.CLI;
import java.io.IOException;

public class KeyCode {
    public static void main(String[] args) {
        CLI.Shell cli = CLI.byPlatform();
        try {
            cli.capture();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (CLI.interactive()) {
            try {
                int codePoint = cli.read(true);
                if (codePoint == CLI.INVALID_KEY) {
                    continue;
                }
                if (Character.isValidCodePoint(codePoint)) {
                    System.out.print(codePoint + ". ");
                    String unescaped = String.valueOf(Character.toChars(codePoint));
                    if (!Character.isISOControl(codePoint)) {
                        System.out.print("'" + unescaped + "' ");
                    }
                    if (codePoint != 32) {
                        System.out.print(encode(unescaped) + " ");
                    }
                    System.out.println("(" + Character.getName(codePoint) + ")");
                }
                if (codePoint == 3 || codePoint == 26) {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
    }

    public static String encode(String text) {
        if (text == null) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (char ch : text.toCharArray()) {
            sb.append(encode(ch));
        }
        return sb.toString();
    }

    public static String encode(char ch) {
        if (ch < 32 || ch > '\u007f') {
            StringBuilder sb = new StringBuilder();
            sb.append("\\u");
            StringBuffer hex = new StringBuffer(Integer.toHexString(ch));
            hex.reverse();
            int length = 4 - hex.length();
            for (int j = 0; j < length; j++) {
                hex.append('0');
            }
            for (int j = 0; j < 4; j++) {
                sb.append(hex.charAt(3 - j));
            }
            return sb.toString();
        } else {
            return Character.toString(ch);
        }
    }
}
