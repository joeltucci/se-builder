package com.sebuilder.interpreter;

import java.util.regex.Pattern;

/**
 * Different types of matchers, regex, straight text etc.  Useful for creating less fragile comparisons
 *
 * @author joel.tucci
 */
public enum MatcherType {

    REGEX("regexp:") {
        @Override
        public boolean matchText(Getter getter, TestRun ctx) {

            if(getter.cmpParamName() == null || !ctx.string(getter.cmpParamName()).startsWith(getTypePrefix()))
                throw new IllegalArgumentException("Regexes searches must be in the form of regexp:(expression)");

            String got = getter.get(ctx);

            //Need to strip off the regex part of the string, then create a matcher
            String pattern=ctx.string(getter.cmpParamName()).substring(getTypePrefix().length());

            boolean result = getter.cmpParamName() == null
                    ? Boolean.parseBoolean(got)
                    : Pattern.compile(pattern).matcher(got).matches();

            return result != ctx.currentStep().isNegated();
        }
    },
    PLAIN_TEXT(null) { //Just a straight up matcher, no prefix
        @Override
        public boolean matchText(Getter getter, TestRun ctx) {
            String got = getter.get(ctx);
            boolean result = getter.cmpParamName() == null
                    ? Boolean.parseBoolean(got)
                    : ctx.string(getter.cmpParamName()).equals(got);
            return result != ctx.currentStep().isNegated();
        }
    };

    private final String typePrefix; //the matcher string must start with the pattern prefix:text to

    private MatcherType(String typePrefix) {
        this.typePrefix=typePrefix;
    }

    public String getTypePrefix() {
        return typePrefix;
    }

    public abstract boolean matchText(Getter getter,TestRun ctx);


    public static MatcherType findMatcherType(Getter getter,TestRun ctx) {

        if(getter.cmpParamName() == null)
            return MatcherType.PLAIN_TEXT;

        String got =  ctx.string(getter.cmpParamName());

        for(MatcherType type : MatcherType.values()) {
            if(type.getTypePrefix() != null && got.startsWith(type.getTypePrefix()))
                return type;
        }
        return MatcherType.PLAIN_TEXT;  //Default matcher
    }

}
