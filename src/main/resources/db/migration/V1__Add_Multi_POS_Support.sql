-- ============================================
-- FICHIER: src/main/resources/db/migration/V2__Add_Multi_POS_Support.sql
-- Migration pour le support multi-caisses stoïque
-- ============================================

-- 1. Ajouter les colonnes de versioning optimiste à STOCK_ITEM
ALTER TABLE STOCK_ITEM
    ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0 NOT NULL,
    ADD COLUMN IF NOT EXISTS last_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 2. Initialiser les données existantes
UPDATE STOCK_ITEM
SET version = 0, last_modified = CURRENT_TIMESTAMP
WHERE version IS NULL OR version = 0;

-- 3. Index critiques pour performances multi-caisses
CREATE INDEX IF NOT EXISTS idx_stock_item_version ON STOCK_ITEM(version);
CREATE INDEX IF NOT EXISTS idx_stock_product_client ON STOCK_ITEM(product_id, client_id, quantity);
CREATE INDEX IF NOT EXISTS idx_stock_expiration_purchase ON STOCK_ITEM(product_id, client_id, expiration_date, purchase_date);

-- 4. Table d'audit pour traçabilité des modifications
CREATE TABLE IF NOT EXISTS stock_audit_log (
                                               log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               stock_item_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    client_id VARCHAR(36) NOT NULL,
    operation_type ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    old_quantity INT,
    new_quantity INT,
    old_version BIGINT,
    new_version BIGINT,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_stock_audit_time (modified_at),
    INDEX idx_stock_audit_product (product_id, client_id)
    );

-- 5. Trigger pour audit automatique
DROP TRIGGER IF EXISTS stock_item_audit_update;
DELIMITER //
CREATE TRIGGER stock_item_audit_update
    AFTER UPDATE ON STOCK_ITEM
    FOR EACH ROW
BEGIN
    INSERT INTO stock_audit_log (
        stock_item_id, product_id, client_id, operation_type,
        old_quantity, new_quantity, old_version, new_version
    ) VALUES (
                 NEW.stock_item_id, NEW.product_id, NEW.client_id, 'UPDATE',
                 OLD.quantity, NEW.quantity, OLD.version, NEW.version
             );
END //
DELIMITER ;

-- 6. Trigger pour audit des créations
DROP TRIGGER IF EXISTS stock_item_audit_insert;
DELIMITER //
CREATE TRIGGER stock_item_audit_insert
    AFTER INSERT ON STOCK_ITEM
    FOR EACH ROW
BEGIN
    INSERT INTO stock_audit_log (
        stock_item_id, product_id, client_id, operation_type,
        old_quantity, new_quantity, old_version, new_version
    ) VALUES (
                 NEW.stock_item_id, NEW.product_id, NEW.client_id, 'INSERT',
                 NULL, NEW.quantity, NULL, NEW.version
             );
END //
DELIMITER ;

-- 7. Vue pour monitoring du stock en temps réel
CREATE OR REPLACE VIEW realtime_stock_view AS
SELECT
    p.product_id,
    p.name as product_name,
    p.barcode,
    c.user_id as client_id,
    c.name as client_name,
    COALESCE(SUM(si.quantity), 0) as total_stock,
    COUNT(si.stock_item_id) as stock_lots,
    MIN(si.expiration_date) as earliest_expiration,
    MAX(si.last_modified) as last_stock_update,
    CASE
        WHEN COALESCE(SUM(si.quantity), 0) = 0 THEN 'OUT_OF_STOCK'
        WHEN COALESCE(SUM(si.quantity), 0) <= MIN(si.reorder_threshold) THEN 'LOW_STOCK'
        ELSE 'IN_STOCK'
        END as stock_status
FROM PRODUCT p
         JOIN CLIENT c ON p.client_id = c.user_id
         LEFT JOIN STOCK_ITEM si ON p.product_id = si.product_id AND si.client_id = c.user_id
GROUP BY p.product_id, p.name, p.barcode, c.user_id, c.name;

-- 8. Vue pour monitoring des performances multi-caisses
CREATE OR REPLACE VIEW multi_pos_performance AS
SELECT
    DATE(sal.modified_at) as activity_date,
    HOUR(sal.modified_at) as activity_hour,
    sal.client_id,
    COUNT(DISTINCT sal.stock_item_id) as concurrent_modifications,
    AVG(sal.new_version - sal.old_version) as avg_version_increment,
    MAX(sal.new_version) - MIN(sal.old_version) as version_spread,
    COUNT(*) as total_operations
FROM stock_audit_log sal
WHERE sal.modified_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
GROUP BY DATE(sal.modified_at), HOUR(sal.modified_at), sal.client_id
HAVING concurrent_modifications > 1
ORDER BY activity_date DESC, activity_hour DESC, version_spread DESC;