package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.util.LoggingManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service locator pattern implementation for managing service instances
 * This helps avoid creating multiple instances of the same service
 */
public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    private ServiceLocator() {}

    public static <T> T getService(Class<T> serviceClass) {
        return (T) services.computeIfAbsent(serviceClass, key -> {
            try {
                T service = serviceClass.getDeclaredConstructor().newInstance();
                LoggingManager.logSystemInfo("Created service: " + serviceClass.getSimpleName());
                return service;
            } catch (Exception e) {
                LoggingManager.logException("Error creating service: " + serviceClass.getSimpleName(), e);
                throw new RuntimeException("Error creating service: " + serviceClass.getSimpleName(), e);
            }
        });
    }
}