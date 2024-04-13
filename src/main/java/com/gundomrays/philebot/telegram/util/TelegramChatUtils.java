package com.gundomrays.philebot.telegram.util;

public class TelegramChatUtils {

    public static String makePingUrl(final String username) {
        return String.format("tg://user?id=%s", username);
    }

    public static String wrapLink(final String url, final String text) {
        return String.format("<a href='%s'>%s</a>", url, text);
    }

    public static String createUserLink(final String tgId, final String tgName) {
        String userPingUrl = makePingUrl(String.valueOf(tgId));
        return wrapLink(userPingUrl, "@" + tgName);
    }

}
