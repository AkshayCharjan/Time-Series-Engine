package com.TSEngine.TSEngine.utils;

import java.util.List;

public class ErrorUtils {
    private static final StackWalker WALKER = StackWalker.getInstance();
    private static final List<String> SKIP_PREFIXES = List.of(
            ErrorUtils.class.getName(),
            "com.TSEngine.TSEngine.exception.ApiError$Builder",
            "java.lang.reflect.",
            "sun.reflect.",
            "org.springframework",
            "jdk.internal.reflect"
    );

    public static String getCallerLocation(){
        return WALKER.walk(frames -> frames
                .filter(f->{
                    String cls = f.getClassName();
                    for(String p : SKIP_PREFIXES){
                        if(cls.equals(p) || cls.startsWith(p)) return false;
                    }
                    return true;
                })
                .findFirst()
                .map(StackWalker.StackFrame::getMethodName)
                .orElse("UnknownMethod"));
    }
}
