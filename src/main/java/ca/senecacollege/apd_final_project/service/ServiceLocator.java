package ca.senecacollege.apd_final_project.service;

import ca.senecacollege.apd_final_project.util.LoggingManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Service locator pattern implementation for managing service instances
 * This helps avoid creating multiple instances of the same service
 */
public class ServiceLocator {
    private static final Map<Class<?>, Object> services = new HashMap<>();

    // Private constructor to prevent instantiation
    private ServiceLocator() {}

    /**
     * Get or create a service instance
     *
     * @param serviceClass The class of the service to get
     * @param <T> The type of the service
     * @return The service instance
     */
    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceClass) {
        // Check if we already have an instance
        if (services.containsKey(serviceClass)) {
            return (T) services.get(serviceClass);
        }

        // Create a new instance
        try {
            T service = serviceClass.getDeclaredConstructor().newInstance();
            services.put(serviceClass, service);
            LoggingManager.logSystemInfo("Created service: " + serviceClass.getSimpleName());
            return service;
        } catch (Exception e) {
            LoggingManager.logException("Error creating service: " + serviceClass.getSimpleName(), e);
            throw new RuntimeException("Error creating service: " + serviceClass.getSimpleName(), e);
        }
    }

    /**
     * Register a service instance
     *
     * @param serviceClass The class of the service to register
     * @param serviceInstance The service instance
     * @param <T> The type of the service
     */
    public static <T> void registerService(Class<T> serviceClass, T serviceInstance) {
        services.put(serviceClass, serviceInstance);
        LoggingManager.logSystemInfo("Registered service: " + serviceClass.getSimpleName());
    }

    /**
     * Clear all registered services
     * Useful for testing or when shutting down the application
     */
    public static void clearServices() {
        services.clear();
        LoggingManager.logSystemInfo("Cleared all services");
    }
}