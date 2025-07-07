/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * 🚀 PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StockAuditLogTest.java
 * @description : Tests unitaires pour l'entité StockAuditLog
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 03/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Stock audit log test.
 */
class StockAuditLogTest {

    private StockAuditLog auditLog;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        auditLog = new StockAuditLog();
    }

    /**
     * Test builder constructor.
     */
    @Test
    @DisplayName("✅ Constructeur Builder")
    void testBuilderConstructor() {
        Long logId = 1L;
        UUID stockItemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        StockAuditLog.OperationType operationType = StockAuditLog.OperationType.UPDATE;
        Integer oldQuantity = 50;
        Integer newQuantity = 40;
        Long oldVersion = 1L;
        Long newVersion = 2L;

        StockAuditLog newAuditLog = StockAuditLog.builder()
                .logId(logId)
                .stockItemId(stockItemId)
                .productId(productId)
                .clientId(clientId)
                .operationType(operationType)
                .oldQuantity(oldQuantity)
                .newQuantity(newQuantity)
                .oldVersion(oldVersion)
                .newVersion(newVersion)
                .build();

        assertEquals(logId, newAuditLog.getLogId());
        assertEquals(stockItemId, newAuditLog.getStockItemId());
        assertEquals(productId, newAuditLog.getProductId());
        assertEquals(clientId, newAuditLog.getClientId());
        assertEquals(operationType, newAuditLog.getOperationType());
        assertEquals(oldQuantity, newAuditLog.getOldQuantity());
        assertEquals(newQuantity, newAuditLog.getNewQuantity());
        assertEquals(oldVersion, newAuditLog.getOldVersion());
        assertEquals(newVersion, newAuditLog.getNewVersion());
    }

    /**
     * Test operation types.
     */
    @Test
    @DisplayName("✅ Types d'opération")
    void testOperationTypes() {
        StockAuditLog.OperationType[] operationTypes = {
                StockAuditLog.OperationType.INSERT,
                StockAuditLog.OperationType.UPDATE,
                StockAuditLog.OperationType.DELETE
        };

        for (StockAuditLog.OperationType type : operationTypes) {
            auditLog.setOperationType(type);
            assertEquals(type, auditLog.getOperationType());
        }
    }

    /**
     * Test audit log getters setters.
     */
    @Test
    @DisplayName("✅ Getters/Setters audit log")
    void testAuditLogGettersSetters() {
        UUID stockItemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        StockAuditLog.OperationType operationType = StockAuditLog.OperationType.INSERT;
        Integer newQuantity = 100;
        Long newVersion = 1L;

        auditLog.setStockItemId(stockItemId);
        auditLog.setProductId(productId);
        auditLog.setClientId(clientId);
        auditLog.setOperationType(operationType);
        auditLog.setNewQuantity(newQuantity);
        auditLog.setNewVersion(newVersion);

        assertEquals(stockItemId, auditLog.getStockItemId());
        assertEquals(productId, auditLog.getProductId());
        assertEquals(clientId, auditLog.getClientId());
        assertEquals(operationType, auditLog.getOperationType());
        assertEquals(newQuantity, auditLog.getNewQuantity());
        assertEquals(newVersion, auditLog.getNewVersion());
    }

    /**
     * Test required fields.
     */
    @Test
    @DisplayName("✅ Champs obligatoires")
    void testRequiredFields() {
        auditLog.setStockItemId(UUID.randomUUID());
        auditLog.setProductId(UUID.randomUUID());
        auditLog.setClientId(UUID.randomUUID());
        auditLog.setOperationType(StockAuditLog.OperationType.UPDATE);

        assertNotNull(auditLog.getStockItemId());
        assertNotNull(auditLog.getProductId());
        assertNotNull(auditLog.getClientId());
        assertNotNull(auditLog.getOperationType());
    }

    /**
     * Test creation timestamp.
     */
    @Test
    @DisplayName("✅ CreationTimestamp automatique")
    void testCreationTimestamp() {
        // Le @CreationTimestamp est géré par Hibernate
        // On teste juste que le champ existe
        auditLog.setModifiedAt(Timestamp.valueOf(LocalDateTime.now()));

        assertNotNull(auditLog.getModifiedAt());
    }
}