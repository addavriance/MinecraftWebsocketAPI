package me.adda.mcwebapi.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectiveApiDispatcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, BaseApiModule> modules = new ConcurrentHashMap<>();
    private final Set<String> pendingRequests = ConcurrentHashMap.newKeySet();

    public Message dispatch(Message request) {
        return dispatchInternal(request, null);
    }

    public Message dispatchWithModule(Message request, BaseApiModule specificModule) {
        return dispatchInternal(request, specificModule);
    }

    private Message dispatchInternal(Message request, BaseApiModule specificModule) {
        String requestId = request.getRequestId();

        // Валидация requestId
        if (requestId == null || requestId.length() > 4 || !isValidHex(requestId)) {
            return createErrorResponse(request, "INVALID_REQUEST_ID",
                    "Request ID must be 1-4 character hex string");
        }

        // Защита от дубликатов
        if (!pendingRequests.add(requestId)) {
            return createErrorResponse(request, "DUPLICATE_REQUEST",
                    "Request with this ID is already processing");
        }

        try {
            String moduleName = request.getModule();
            String methodName = request.getMethod();

            BaseApiModule module = specificModule != null ? specificModule : modules.get(moduleName);
            if (module == null) {
                return createErrorResponse(request, "MODULE_NOT_FOUND",
                        "Module not found: " + moduleName, getAvailableModules());
            }

            Method method = module.getMethod(methodName);
            if (method == null) {
                return createErrorResponse(request, "METHOD_NOT_FOUND",
                        "Method not found: " + methodName, module.getAvailableMethods());
            }

            Object[] args = convertArguments(request.getArgs(), method.getParameterTypes());

            Object result = method.invoke(module, args);

            return createSuccessResponse(request, result);

        } catch (IllegalArgumentException e) {
            return createErrorResponse(request, "INVALID_ARGUMENTS",
                    "Argument type mismatch: " + e.getMessage());
        } catch (Exception e) {
            Throwable target = e.getCause() != null ? e.getCause() : e;
            LOGGER.error("Error dispatching request", target);
            return createErrorResponse(request, "EXECUTION_ERROR",
                    "Method execution failed: " + target.getMessage());
        } finally {
            pendingRequests.remove(requestId);
        }
    }

    private Object[] convertArguments(Object[] rawArgs, Class<?>[] paramTypes) {
        if (rawArgs == null || rawArgs.length == 0) {
            return new Object[0];
        }

        Object[] converted = new Object[Math.min(rawArgs.length, paramTypes.length)];
        for (int i = 0; i < converted.length; i++) {
            converted[i] = convertArgument(rawArgs[i], paramTypes[i]);
        }
        return converted;
    }

    private Object convertArgument(Object rawArg, Class<?> targetType) {
        if (rawArg == null) {
            return null;
        }

        // Если типы совместимы
        if (targetType.isInstance(rawArg)) {
            return rawArg;
        }

        try {
            return mapper.convertValue(rawArg, targetType);
        } catch (Exception e) {
            return convertFromString(rawArg.toString(), targetType);
        }
    }

    private Object convertFromString(String value, Class<?> targetType) {
        try {
            if (targetType == String.class) return value;
            if (targetType == int.class || targetType == Integer.class) return Integer.parseInt(value);
            if (targetType == double.class || targetType == Double.class) return Double.parseDouble(value);
            if (targetType == boolean.class || targetType == Boolean.class) return Boolean.parseBoolean(value);
            if (targetType == float.class || targetType == Float.class) return Float.parseFloat(value);
            if (targetType == long.class || targetType == Long.class) return Long.parseLong(value);
            if (targetType == UUID.class) return UUID.fromString(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert '" + value + "' to " + targetType.getSimpleName());
        }

        throw new IllegalArgumentException("Unsupported target type: " + targetType.getSimpleName());
    }

    private boolean isValidHex(String str) {
        return str.matches("[0-9a-fA-F]{1,4}");
    }

    public void registerModule(BaseApiModule module) {
        module.discoverMethods();
        modules.put(module.getModuleName(), module);
        LOGGER.info("Registered API module: {}", module.getModuleName());
    }

    private Message createSuccessResponse(Message request, Object data) {
        Message response = new Message("RESPONSE", request.getRequestId());
        response.setStatus("SUCCESS");
        response.setData(data);
        return response;
    }

    private Message createErrorResponse(Message request, String errorCode, String message) {
        return createErrorResponse(request, errorCode, message, null);
    }

    private Message createErrorResponse(Message request, String errorCode,
                                        String message, Collection<String> suggestions) {
        Message response = new Message("ERROR", request.getRequestId());
        response.setStatus("ERROR");

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("code", errorCode);
        errorData.put("message", message);
        if (suggestions != null) {
            errorData.put("suggestions", new ArrayList<>(suggestions));
        }

        response.setData(errorData);
        return response;
    }

    private List<String> getAvailableModules() {
        return new ArrayList<>(modules.keySet());
    }
}