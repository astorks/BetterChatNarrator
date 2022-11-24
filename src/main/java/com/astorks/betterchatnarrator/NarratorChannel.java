package com.astorks.betterchatnarrator;

import java.util.regex.Pattern;

public class NarratorChannel {
    private Pattern compiledPattern;
    public String name;
    public int priority = 0;
    public String match;
    public String say;

    public Pattern getMatchPattern() {
        if(match != null && compiledPattern == null) {
            compiledPattern = Pattern.compile(match);
        }

        return compiledPattern;
    }
}
