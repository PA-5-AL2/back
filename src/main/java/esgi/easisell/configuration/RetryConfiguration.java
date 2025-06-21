package esgi.easisell.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ NOUVEAU FICHIER À CRÉER
 * Configuration pour la gestion des retry automatiques
 */
@Configuration
@EnableRetry
@EnableTransactionManagement
public class RetryConfiguration {

    /**
     * Template de retry personnalisé pour les conflits de stock
     */
    @Bean
    public RetryTemplate stockRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // Configuration des exceptions à retry
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(OptimisticLockingFailureException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3, retryableExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        // Configuration du backoff exponentiel
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(100); // 100ms initial
        backOffPolicy.setMultiplier(2.0); // Multiplier par 2 à chaque tentative
        backOffPolicy.setMaxInterval(1000); // Maximum 1 seconde

        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}