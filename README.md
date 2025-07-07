# 🚀 EasySell - Plateforme de Gestion Commerciale

## 📖 Description du projet

**EasySell** est une plateforme de gestion commerciale complète développée en Spring Boot, conçue pour faciliter la gestion des ventes, des stocks et des relations clients-fournisseurs.

### 🏗️ Architecture technique
- **Backend** : Spring Boot 3.x avec Java 17
- **Base de données** : MySQL 8.0
- **Sécurité** : JWT pour l'authentification
- **Email** : Intégration SMTP (Gmail)
- **Containerisation** : Docker & Docker Compose

## 📋 Prérequis

- **Docker Desktop** installé et démarré
- **Java 17** (pour le développement local)
- **Maven** (pour la compilation)

## 🐳 Lancement de l'application avec Docker

### 1. Clone et préparation
```bash
git clone <repo>
cd PA-5-AL2
```

### 2. Compilation du projet
```bash
# Compile le projet Maven
mvn clean package -DskipTests
```

### 3. Lancement Docker
```bash
# Lance les containers en arrière-plan
docker compose up --build -d
```

### 4. Vérification
```bash
# Vérifie que les containers tournent
docker ps

# Devrait afficher :
# - easysell-app (port 8081)
# - easysell-db (MySQL)
```

## 🌐 Accès aux services

| Service | URL | Description |
|---------|-----|-------------|
| **API Backend** | http://localhost:8081 | Spring Boot API |
| **Base de données** | `docker compose exec db mysql -u root -p` | MySQL (pas de mot de passe) |

## 🗄️ Base de données

### Connexion à MySQL
```bash
# Se connecter à la base via Docker
docker compose exec db mysql -u root -p
# Appuyer sur Entrée (pas de mot de passe)

# Utiliser la base EasySell
USE EasySell;

# Voir les tables
SHOW TABLES;
```

### Tables créées automatiquement
- `admin_user` - Utilisateurs administrateurs
- `client` - Clients de l'application
- `category` - Catégories de produits
- `product` - Produits
- `supplier` - Fournisseurs
- `stock_item` - Gestion des stocks
- `sale` - Ventes
- `sale_item` - Détails des ventes
- `payment` - Paiements
- `promotion` - Promotions
- `email` - Configuration emails
- `email_send` - Historique des emails
- `users` - Table utilisateurs générique

## 🛠️ Commandes utiles

### Gestion des containers
```bash
# Arrêter les containers
docker compose down

# Redémarrer après changement de code
docker compose up --build -d

# Voir les logs de l'application
docker compose logs easysell-app

# Voir les logs MySQL
docker compose logs easysell-db

# Logs en temps réel
docker compose logs -f easysell-app
```

### Nettoyage
```bash
# Arrêter et supprimer volumes (⚠️ supprime les données DB)
docker compose down --volumes

# Nettoyer les containers inutilisés
docker container prune -f

# Nettoyer les images inutilisées
docker image prune -f
```

## 🔧 Configuration

### Ports utilisés
- **8081** : Application Spring Boot
- **3306** : MySQL (accessible uniquement entre containers)

## ❌ Résolution de problèmes

### Port 3306 déjà utilisé
Si tu as MySQL installé localement :
```bash
# Arrêter MySQL local
net stop mysql80

# Ou changer le port dans docker-compose.yml
ports:
  - "3307:3306"  # Utilise le port 3307 à la place (j'ai été obligé de mettre 3307, mais si chez vous 3306 n'est pas utilisé, alors,vous pouvez changer)
```

### Container ne démarre pas
```bash
# Voir les erreurs détaillées
docker compose logs easysell-app

# Forcer la reconstruction
docker compose down
docker compose up --build --force-recreate -d
```

### Problème de connexion DB
```bash
# Vérifier que MySQL est prêt
docker compose exec db mysqladmin ping -h localhost -u root

# Redémarrer uniquement la DB
docker compose restart easysell-db
```

## 📁 Structure du projet

```
PA-5-AL2/
├── docker-compose.yml    # Configuration Docker
├── Dockerfile           # Image de l'application
├── src/                 # Code source Spring Boot
├── target/              # JAR compilé
└── README.md           # Ce fichier
```

## 🎯 Développement

Pour le développement local sans Docker :
1. Démarre uniquement MySQL avec Docker : `docker compose up db -d`
2. Lance Spring Boot depuis ton IDE
3. L'app se connectera au MySQL Docker sur le port 3306

