package esgi.easisell.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : BackendController.java
 * @description : Contrôleur de base pour les endpoints racine
 * @author      : Votre nom
 * @version     : v1.0.0
 * @date        : 19/04/2025
 * @package     : esgi.easisell.controller
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 *
 * Ce contrôleur expose les endpoints de base du système :
 * - Endpoint de santé / vérification du service
 * - Page d'accueil de l'API
 */
@RestController
public class BackendController {

    /**
     * Endpoint de vérification du service
     * GET /
     *
     * Page d'accueil de l'API - vérification que le service est actif
     *
     * @return message de bienvenue
     */
    @GetMapping("/")
    public String sayHello() {
        return "Hello World !";
    }
}