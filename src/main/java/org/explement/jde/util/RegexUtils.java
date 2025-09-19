package org.explement.jde.util;

import java.util.regex.Pattern;

public class RegexUtils {

    private RegexUtils() {} // Create private constructor

    // Regex patterns for different token types [e.g. comments (//), strings ("")]
    public static final String PAREN_PATTERN = "[()]";
    public static final String BRACE_PATTERN = "[{}]";
    public static final String BRACKET_PATTERN = "[\\[\\]]";
    public static final String SEMICOLON_PATTERN = ";";
    public static final String INTEGER_PATTERN = "\\b\\d+\\b";
    public static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    public static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    public static final String WHITESPACE_PATTERN = "^\\s+";
}
