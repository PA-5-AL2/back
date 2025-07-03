/**
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * PROJET EASISELL - PLATEFORME DE GESTION COMMERCIALE
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * @file        : StockAuditLog.java
 * @description : Entité audit des modifications de stock
 * @author      : Chancy MOUYABI
 * @version     : v1.0.0
 * @date        : 03/07/2025
 * @package     : esgi.easisell.entity
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 */
package esgi.easisell.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * ✅ TABLE D'AUDIT pour traçabilité des modifications de stock
 * Sera créée automatiquement par JPA
 */
@Entity
@Table(name = "stock_audit_log", indexes = {
        @Index(name = "idx_stock_audit_time", columnList = "modified_at"),
        @Index(name = "idx_stock_audit_product", columnList = "product_id, client_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "stock_item_id", nullable = false)
    private UUID stockItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    @Column(name = "old_quantity")
    private Integer oldQuantity;

    @Column(name = "new_quantity")
    private Integer newQuantity;

    @Column(name = "old_version")
    private Long oldVersion;

    @Column(name = "new_version")
    private Long newVersion;

    @CreationTimestamp
    @Column(name = "modified_at", nullable = false)
    private Timestamp modifiedAt;

    public enum OperationType {
        INSERT, UPDATE, DELETE
    }
}