package org.explement.jde.service;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.explement.jde.util.RegexUtils;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class SyntaxHighlighterService {

    // Java keywords
    private static final String[] KEYWORDS = {
            "abstract","assert","break","case","catch","class",
            "const","continue","default","do","else","enum","extends","final",
            "finally","for","goto","if","implements","import","instanceof",
            "interface","native","new","package","private","protected","public",
            "return","static","strictfp","super","switch","synchronized","this",
            "throw","throws","transient","try","void","volatile","while"
    };

    // Common Java types
    private static final String[] TYPES = {
            "boolean","char","byte","short","int","long","float","double",
            "String","Array","Class","Interface","Object","Enum"
    };

    // Regex patterns for keywords and types
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String TYPE_PATTERN = "\\b(" + String.join("|", TYPES) +  ")\\b";

    // Regex patterns for symbols, literals, and comments
    private static final String PAREN_PATTERN = RegexUtils.PAREN_PATTERN;
    private static final String BRACE_PATTERN = RegexUtils.BRACE_PATTERN;
    private static final String BRACKET_PATTERN = RegexUtils.BRACKET_PATTERN;
    private static final String SEMICOLON_PATTERN = RegexUtils.SEMICOLON_PATTERN;
    private static final String INTEGER_PATTERN = RegexUtils.INTEGER_PATTERN;
    private static final String COMMENT_PATTERN = RegexUtils.COMMENT_PATTERN;
    private static final String STRING_PATTERN = RegexUtils.STRING_PATTERN;

    // Combined regex pattern with named groups for syntax highlighting
    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<INTEGER>" + INTEGER_PATTERN + ")"
                    + "|(?<TYPE>" + TYPE_PATTERN + ")"
    );

    // Applies syntax highlighting to the given text
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = getString(matcher);

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd); // unstyled text
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start()); // styled match
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd); // remaining text
        return spansBuilder.create();
    }

    // Determines the style class based on regex group
    private static String getString(Matcher matcher) {
        String styleClass =
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("PAREN") != null ? "paren" :
                matcher.group("BRACE") != null ? "brace" :
                matcher.group("BRACKET") != null ? "bracket" :
                matcher.group("SEMICOLON") != null ? "semicolon" :
                matcher.group("STRING") != null ? "string" :
                matcher.group("COMMENT") != null ? "comment" :
                matcher.group("INTEGER") != null ? "integer" :
                matcher.group("TYPE") != null ? "type" : null;

        assert styleClass != null; // Should always match one
        return styleClass;
    }
}
