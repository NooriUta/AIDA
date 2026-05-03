package com.mimir.log;

import com.mimir.byok.CredentialMasker;
import io.quarkus.logging.LoggingFilter;
import org.jboss.logmanager.ExtLogRecord;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Last-resort defence: any log line that escapes the typed-Secret discipline
 * still gets BYOK keys masked before hitting the console / file appender.
 *
 * <p>Patterns matched:
 * <ul>
 *   <li>{@code sk-…} — OpenAI / DeepSeek / Anthropic style (8+ word chars after dash)</li>
 *   <li>{@code Bearer …} — JWT-ish auth headers (20+ chars)</li>
 * </ul>
 *
 * <p>Wired via {@code quarkus.log.filter.credential-mask.class} in
 * application.properties.
 */
@LoggingFilter(name = "credential-mask")
public class CredentialMaskingFilter implements Filter {

    private static final Pattern KEY_PATTERN = Pattern.compile(
            "(sk-[a-zA-Z0-9_-]{8,})|(Bearer\\s+[\\w.~+/-]{20,})");

    @Override
    public boolean isLoggable(LogRecord record) {
        if (record == null) return true;
        String msg = record.getMessage();
        if (msg != null && containsCandidate(msg)) {
            String masked = mask(msg);
            // ExtLogRecord exposes a setMessage that survives Logmanager dispatch;
            // for plain LogRecord we still call setMessage which the formatters honour.
            if (record instanceof ExtLogRecord ext) {
                ext.setMessage(masked, ext.getFormatStyle());
            } else {
                record.setMessage(masked);
            }
        }
        Object[] params = record.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof String s && containsCandidate(s)) {
                    params[i] = mask(s);
                }
            }
        }
        return true;
    }

    private static boolean containsCandidate(String s) {
        return s.contains("sk-") || s.contains("Bearer ");
    }

    static String mask(String s) {
        Matcher m = KEY_PATTERN.matcher(s);
        StringBuilder out = new StringBuilder(s.length());
        while (m.find()) {
            String hit = m.group();
            String masked = hit.startsWith("Bearer")
                    ? "Bearer " + CredentialMasker.mask(hit.substring(7).trim())
                    : CredentialMasker.mask(hit);
            m.appendReplacement(out, Matcher.quoteReplacement(masked));
        }
        m.appendTail(out);
        return out.toString();
    }
}
