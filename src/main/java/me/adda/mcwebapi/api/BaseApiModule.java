package me.adda.mcwebapi.api;

import me.adda.mcwebapi.api.annotations.ApiMethod;
import me.adda.mcwebapi.api.annotations.ApiModule;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseApiModule {
    private final Map<String, Method> methodCache = new ConcurrentHashMap<>();
    private final String moduleName;

    public BaseApiModule() {
        ApiModule annotation = this.getClass().getAnnotation(ApiModule.class);
        this.moduleName = annotation != null ? annotation.value() :
                this.getClass().getSimpleName().toLowerCase().replace("module", "");
    }

    public String getModuleName() {
        return moduleName;
    }

    public Method getMethod(String methodName) {
        return methodCache.get(methodName.toLowerCase());
    }

    public void discoverMethods() {
        methodCache.clear();
        for (Method method : this.getClass().getMethods()) {
            if (method.isAnnotationPresent(ApiMethod.class)) {
                ApiMethod annotation = method.getAnnotation(ApiMethod.class);
                String name = annotation.value().isEmpty() ?
                        method.getName() : annotation.value();
                methodCache.put(name.toLowerCase(), method);

                System.out.println("Discovered method: " + moduleName + "." + name);
            }
        }
    }

    public Set<String> getAvailableMethods() {
        return methodCache.keySet();
    }
}