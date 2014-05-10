package net.onrc.onos.api.rest;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * Utility class used for formatting Rest Error descriptions.
 */
public final class RestErrorFormatter {

    /**
     * Hide default constructor for utility classes.
     */
    private RestErrorFormatter() { }

    /**
     * Takes a RestError template and formats the description using a supplied
     * list of replacement parameters.
     *
     * @param error the RestError to format
     * @param parameters parameter list to use as positional parameters in the
     *                   result string
     *
     * @return the String object for the formatted message.
     */
    static String formatErrorMessage(final RestError error,
                                     final Object... parameters) {
        final FormattingTuple formattingResult =
                MessageFormatter.arrayFormat(error.getDescription(), parameters);
        return formattingResult.getMessage();
    }
}
