package org.example.jde.service;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class SyntaxHighlighterService {

    // List of Java keywords (e.g., class, public, return)
    private static final String[] KEYWORDS = {
            "abstract","assert","break","case","catch","class",
            "const","continue","default","do","else","enum","extends","final",
            "finally","for","goto","if","implements","import","instanceof",
            "interface","native","new","package","private","protected","public",
            "return","static","strictfp","super","switch","synchronized","this",
            "throw","throws","transient","try","void","volatile","while"
    };

    // Common Java types (primitives + core classes)
    private static final String[] TYPES = {
            "boolean","char","byte","short","int","long","float","double",
            "String","Array","Class","Interface","Object","Enum"
    };

    // Regex patterns for different token types
    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "[()]";
    private static final String BRACE_PATTERN = "[{}]";
    private static final String BRACKET_PATTERN = "[\\[\\]]";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String INTEGER_PATTERN = "\\b\\d+\\b";
    private static final String TYPE_PATTERN = "\\b(" + String.join("|", TYPES) +  ")\\b";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";

    // Combined regex pattern with named groups
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

    //  Applies syntax highlighting to the given text
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass = getString(matcher);

            // Add unstyled text between matches
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            // Add styled match
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        // Add any remaining unstyled text at the end
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
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
