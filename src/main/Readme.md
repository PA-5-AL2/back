Modèle de Données (Diagramme Entité-Relation)
Voici le diagramme Entité-Relation (ERD) représentant la structure de la base de données pour le projet EasySell, généré avec Mermaid :mermaid

````mermaid
erDiagram
    User {
        UUID userId PK
        VARCHAR username UK "Email de connexion"
        VARCHAR password "Mot de passe hashé"
        VARCHAR firstName
        VARCHAR role "ADMIN|CLIENT"
        TIMESTAMP createdAt
    }

    AdminUser {
        UUID userId PK,FK "Hérite de User"
    }


    Client {
        UUID userId PK,FK "Hérite de User"
        VARCHAR name "Nom de la supérette"
        VARCHAR address
        VARCHAR phone "Recommandé"
        VARCHAR contractStatus "ACTIVE|SUSPENDED|CANCELLED"
        VARCHAR currencyPreference "EUR|USD|CAD"
        UUID adminUserId FK "Admin qui gère ce client"
    }

    Email {
        UUID emailId PK
        TEXT content "Contenu du modèle"
        TIMESTAMP createdAt
        VARCHAR subject "Sujet"
        VARCHAR type "REMINDER|PROMOTION|CANCELLATION"
    }

    EmailSend {
        UUID emailSendId PK
        UUID emailId FK
        UUID clientId FK
        TIMESTAMP sentAt
        VARCHAR status "SENT|FAILED|PENDING"
        VARCHAR emailType "BACKUP|PROMOTION|REMINDER"
    }

    Supplier {
        UUID supplierId PK
        VARCHAR name
        VARCHAR contactInfo
        VARCHAR phone "Recommandé"
        UUID clientId FK "Fournisseur lié à un Client"
    }

    Category {
        UUID categoryId PK
        VARCHAR name "e.g., Boissons, Boulangerie, Fromages"
        UUID clientId FK "Catégorie liée à un Client"
    }

    Product {
        UUID productId PK
        VARCHAR name "e.g., Pain baguette, Coca-Cola 1.5L"
        TEXT description
        VARCHAR barcode "Nullable, unique par client"
        VARCHAR brand "e.g., Soumam, Danone"
        DECIMAL unitPrice
        UUID categoryId FK
        UUID clientId FK "Produit appartenant à un client"
    }

    StockItem {
        UUID stockItemId PK
        UUID productId FK
        UUID clientId FK
        INT quantity "Quantité actuelle"
        INT reorderThreshold "Seuil de réapprovisionnement"
        TIMESTAMP purchaseDate "Date de réception"
        TIMESTAMP expirationDate "Date de péremption"
        DECIMAL purchasePrice "Prix d'achat"
        UUID supplierId FK "Fournisseur de ce lot"
    }

    Sale {
        UUID saleId PK
        UUID clientId FK
        TIMESTAMP saleTimestamp
        DECIMAL totalAmount
        BOOLEAN isDeferred "Paiement différé"
    }

    SaleItem {
        UUID saleItemId PK
        UUID saleId FK
        UUID productId FK
        INT quantitySold
        DECIMAL priceAtSale
    }

    Promotion {
        UUID promotionId PK
        UUID productId FK "Produit concerné"
        UUID clientId FK "Client propriétaire"
        VARCHAR promotionCode UK "Code unique"
        VARCHAR description "e.g., Vente flash Tomates"
        VARCHAR discountType "PERCENT|FIXED"
        DECIMAL discountValue
        TIMESTAMP startDate
        TIMESTAMP endDate
    }

    Payment {
        UUID paymentId PK
        UUID saleId FK
        VARCHAR type "CASH|CARD|CHECK"
        DECIMAL amount
        VARCHAR currency "EUR|USD|CAD"
        TIMESTAMP paymentDate "Recommandé"
    }

%% Héritage
    User ||--|| AdminUser : "est un"
    User ||--|| Client : "est un"

%% Relations
    AdminUser ||--o{ Client : "gère"
    Client ||--o{ EmailSend : "reçoit"
    Email ||--o{ EmailSend : "utilisé dans"
    Client ||--o{ Supplier : "possède"
    Client ||--o{ Category : "définit"
    Client ||--o{ Product : "vend"
    Client ||--o{ StockItem : "stocke"
    Client ||--o{ Sale : "effectue"
    Client ||--o{ Promotion : "crée"
    Category ||--o{ Product : "contient"
    Supplier ||--o{ StockItem : "fournit"
    Product ||--o{ StockItem : "en stock"
    Product ||--o{ SaleItem : "vendu dans"
    Product ||--o{ Promotion : "bénéficie de"
    Sale ||--o{ SaleItem : "inclut"
    Sale ||--o{ Payment : "payé par"
````

AdminUser -- manages --> Client : "Manages (Logical Link)"

### Explication des Entités Principales

*   **AdminUser**: Utilisateurs administratifs (Samira, Maram, Chancy).
*   **Client**: Les supérettes clientes utilisant le logiciel. Contient les informations de connexion et de préférence (comme la devise).
*   **Supplier**: Fournisseurs associés à une supérette spécifique.
*   **Category**: Catégories de produits définies par chaque supérette.
*   **Product**: Les produits vendus par une supérette, liés à une catégorie et potentiellement à une marque. Le code-barres est unique par client s'il est présent.
*   **StockItem**: Représente un lot spécifique d'un produit en stock, avec sa quantité et sa date de péremption. Essentiel pour les alertes.
*   **Sale**: Enregistre une transaction de vente, incluant le montant total et le statut (ex: paiement différé).
*   **SaleItem**: Détaille les produits spécifiques inclus dans une vente, avec la quantité et le prix au moment de la vente.
*   **Promotion**: Définit les promotions applicables à certains produits pour une période donnée.

- PK : Primary Key (Clé Primaire). Dans votre diagramme, les colonnes marquées PK (comme adminUserId dans la table AdminUser ou clientId dans la table Client) servent d'identifiant unique pour chaque enregistrement de cette table.
- UK : Unique Key (Clé Unique). Dans votre diagramme, les colonnes marquées UK (comme email dans AdminUser et Client, ou barcode dans Product) doivent contenir des valeurs uniques au sein de leur table respective. Par exemple, chaque administrateur doit avoir une adresse e-mail différente.
- FK : Foreign Key (Clé Etrangère). Dans votre diagramme, les colonnes marquées FK (comme clientId dans Supplier, Category, Product, StockItem et Sale, ou categoryId dans Product, productId dans StockItem, SaleItem et Promotion, ou supplierId dans StockItem, saleId dans SaleItem, et promotionId dans Promotion) indiquent une relation avec une autre table. Par exemple, la colonne clientId dans la table Supplier est une clé étrangère qui fait référence à la clé primaire clientId de la table Client, indiquant à quel client appartient ce fournisseur.

````mermaid
classDiagram
    class AdminUser {
        +int adminUserId
        +String email
        +String password
        +String firstName
        +String userName
        +List~Client~ managedClients
    }

    class Client {
        +int clientId
        +String name
        +String email
        +String password
        +String address
        +String contractStatus
        +String currencyPreference
        +List~Supplier~ suppliers
        +List~Category~ categories
        +List~Product~ products
        +List~StockItem~ stockItems
        +List~Sale~ sales
        +List~Promotion~ promotions
        +List~BackupLog~ backupLogs
        +List~EmailSend~ emailSends
    }

    class Email {
        +int emailId
        +String subject
        +String content
        +String type
        +Timestamp createdAt
    }

    class EmailSend {
        +UUID emailSendId
        +Email email
        +Client client
        +Timestamp sentAt
        +String status
        +String emailType "Nouveau champ"
    }

    class Supplier {
        +int supplierId
        +String name
        +String contactInfo
        +Client client
    }

    class Category {
        +int categoryId
        +String name
        +Client client
        +List~Product~ products
    }

    class Product {
        +int productId
        +String name
        +String description
        +String barcode
        +String brand
        +BigDecimal unitPrice
        +Category category
        +Client client
        +List~StockItem~ stockItems
        +List~SaleItem~ saleItems
        +List~Promotion~ promotions
    }

    class StockItem {
        +int stockItemId
        +Product product
        +Client client
        +int quantity
        +int reorderThreshold
        +Timestamp purchaseDate
        +Timestamp expirationDate
        +BigDecimal purchasePrice
        +Supplier supplier
    }

    class Sale {
        +int saleId
        +Client client
        +Timestamp saleTimestamp
        +BigDecimal totalAmount
        +boolean isDeferred
        +List~SaleItem~ saleItems
        +List~Payment~ payments
    }

    class SaleItem {
        +int saleItemId
        +Sale sale
        +Product product
        +int quantitySold
        +BigDecimal priceAtSale
    }

    class Promotion {
        +int promotionId
        +Product product
        +Client client
        +String promotionCode
        +String description
        +String discountType
        +BigDecimal discountValue
        +Timestamp startDate
        +Timestamp endDate
    }

    class Payment {
        +int paymentId
        +Sale sale
        +String type
        +BigDecimal amount
        +String currency
    }

    AdminUser --|> Object : (Implicit Java Inheritance)
    Client --|> Object
    Supplier --|> Object
    Category --|> Object
    Product --|> Object
    StockItem --|> Object
    Sale --|> Object
    SaleItem --|> Object
    Promotion --|> Object

    AdminUser "1" -- "*" Client : Gère
    
    Client "1" -- "*" Supplier : Gère
    Client "1" -- "*" Category : Définit
    Client "1" -- "*" Product : Possède
    Client "1" -- "*" StockItem : Stocke
    Client "1" -- "*" Sale : Génère
    Client "1" -- "*" Promotion : Crée
    Client "1" -- "*" EmailSend : "Reçoit"

    Email "1" -- "*" EmailSend : "Modèle"

    Category "1" -- "*" Product : Contient
    Supplier "1" -- "*" StockItem : Approvisionne
    
    Product "1" -- "*" StockItem : Lots
    Product "1" -- "*" SaleItem : Vendu dans
    Product "1" -- "*" Promotion : "Promotions applicables"


    Sale "1" -- "*" SaleItem : Inclut
    Sale "1" -- "*" Payment : Paiements

    %% Note: AdminUser managing Client is more of a service-level relationship,
    %% not typically shown as a direct class association unless AdminUser holds a list of Clients.
````