/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : BackendControllerTest.java
 * @description : Tests unitaires pour le contrôleur backend de base
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 10/07/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour le contrôleur backend de base
 *
 * Tests des méthodes :
 * - sayHello()
 */
@ExtendWith(MockitoExtension.class)
class BackendControllerTest {

    @InjectMocks
    private BackendController backendController;

    /**
     * Test de l'endpoint racine - retourne Hello World
     */
    @Test
    @DisplayName("sayHello() - Retourne message de bienvenue")
    void testSayHello() {
        // When
        String result = backendController.sayHello();

        // Then
        assertNotNull(result);
        assertEquals("Hello World !", result);
    }
}