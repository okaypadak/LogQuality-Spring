package dev.padak;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

import java.util.HashMap;
import java.util.Map;

public final class JoinPointUtil {
    private JoinPointUtil() {
        throw new IllegalStateException("Utility class!");
    }

    public static Map<String, Object> getParameters(JoinPoint joinPoint) {
        CodeSignature signature = (CodeSignature) joinPoint.getSignature();

        HashMap<String, Object> map = new HashMap<>();

        String[] parameterNames = signature.getParameterNames();

        for (int i = 0; i < parameterNames.length; i++) {
            map.put(parameterNames[i], joinPoint.getArgs()[i]);
        }

        return map;
    }

}
