Modèle de Données (Diagramme Entité-Relation)
Voici le diagramme Entité-Relation (ERD) représentant la structure de la base de données pour le projet EasySell, généré avec Mermaid :mermaid

````mermaid
erDiagram
    AdminUser {
        INT adminUserId PK
        VARCHAR email UK "Email unique pour la connexion admin"
        VARCHAR password
        VARCHAR firstName
        VARCHAR userName
    }
    
    Client {
        INT clientId PK
        VARCHAR name "Nom de la supérette/propriétaire"
        VARCHAR email UK "Email unique pour la connexion client"
        VARCHAR password
        VARCHAR address
        VARCHAR contractStatus "e.g., Actif/Résilié"
        VARCHAR currencyPreference "e.g., EUR, USD, CAD / Dévive par défaut euros"
    }

    Email {
        INT emailId PK
        TEXT content "Contenu du modèle"
        TIMESTAMP createdAt
        VARCHAR subject "Sujet"
        VARCHAR type "Type (Rappel, Promotion)"
    }

    EmailSend {
        INT emailSendId PK
        INT emailId FK
        INT clientId FK
        TIMESTAMP sentAt
        VARCHAR status
        VARCHAR emailType "BACKUP|PROMOTION|REMINDER"
    }

    Supplier {
        INT supplierId PK
        VARCHAR name
        VARCHAR contactInfo
        INT clientId FK "Fournisseur lié à un Client"
    }

    Category {
        INT categoryId PK
        VARCHAR name "e.g., Boissons, Boulangerie, Fromages"
        INT clientId FK "Catégorie liée à un Client"
    }

    Product {
        INT productId PK
        VARCHAR name "e.g., Pain baguette, Coca-Cola 1.5L, Fromage Emmental"
        VARCHAR description
        VARCHAR barcode UK "Nullable, Code-barres unique par client"
        VARCHAR brand "e.g., Soumam, Danone / Marque"
        DECIMAL unitPrice
        INT categoryId FK
        INT clientId FK "Products appartenant a un specific Client"
    }

    StockItem {
        INT stockItemId PK
        INT productId FK
        INT clientId FK "Stock appartenant a un specific Client"
        INT quantity "Current quantity in stock"
        INT reorderThreshold "Seuil de réapprovisionnement"
        TIMESTAMP purchaseDate "Optional: When the item was received"
        TIMESTAMP expirationDate "Crucial pour les alertes"
        DECIMAL purchasePrice "Optional: Cost price"
        INT supplierId FK "Optional: Link to supplier"
    }

    Sale {
        INT saleId PK
        INT clientId FK
        TIMESTAMP saleTimestamp
        DECIMAL totalAmount
        BOOLEAN isDeferred "Paiement différé"
    }

    SaleItem {
        INT saleItemId PK
        INT saleId FK
        INT productId FK
        INT quantitySold
        DECIMAL priceAtSale
    }

    Promotion {
        INT promotionId PK
        INT productId FK "Promotion applies to a specific Product"
        INT clientId FK "Promotion belongs to a specific Client"
        VARCHAR promotionCode "Code unique (ex: TOMATE20)"
        VARCHAR description "e.g., Vente flash Tomates"
        VARCHAR discountType "PERCENT or FIXED"
        DECIMAL discountValue
        TIMESTAMP startDate
        TIMESTAMP endDate
    }

    Payment {
        INT paymentId PK
        INT saleId FK
        VARCHAR type "Espèces, Carte..."
        DECIMAL amount
        VARCHAR currency "Devise utilisée (ex: EUR)"
    }

    AdminUser ||--o{ Client : "Gère"
    Client ||--o{ EmailSend : "Reçoit"
    Email ||--o{ EmailSend : "Utilisé_dans"
    Client ||--o{ Supplier : "Gère"
    Client ||--o{ Category : "Définit"
    Client ||--o{ Product : "Possède"
    Client ||--o{ StockItem : "Stocke"
    Client ||--o{ Sale : "Génère"
    Client ||--o{ Promotion : "Crée"
    Category ||--o{ Product : "Contient"
    Supplier ||--o{ StockItem : "Approvisionne"
    Product ||--o{ StockItem : "Lot en stock"
    Product ||--o{ SaleItem : "Vendu dans"
    Product ||--o{ Promotion : "Promotion applicable"
    Sale ||--o{ SaleItem : "Inclut"
    Sale ||--o{ Payment : "Paiements associés"
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