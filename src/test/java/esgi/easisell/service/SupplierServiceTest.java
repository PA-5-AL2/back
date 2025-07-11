package esgi.easisell.service;

import esgi.easisell.dto.SupplierDTO;
import esgi.easisell.dto.SupplierResponseDTO;
import esgi.easisell.entity.Client;
import esgi.easisell.entity.Supplier;
import esgi.easisell.repository.ClientRepository;
import esgi.easisell.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private SupplierService supplierService;

    private UUID clientId;
    private UUID supplierId;
    private Client testClient;
    private Supplier testSupplier;
    private SupplierDTO testSupplierDTO;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        supplierId = UUID.randomUUID();

        testClient = new Client();
        testClient.setUserId(clientId);
        testClient.setFirstName("John");
        testClient.setUsername("john.doe@example.com");
        testClient.setName("Supérette John");
        testClient.setAddress("123 Rue de la Paix");
        testClient.setContractStatus("ACTIVE");
        testClient.setCurrencyPreference("EUR");

        testSupplier = new Supplier();
        testSupplier.setSupplierId(supplierId);
        testSupplier.setName("TechSupply");
        testSupplier.setFirstName("Jean");
        testSupplier.setDescription("Fournisseur d'équipements informatiques");
        testSupplier.setContactInfo("contact@techsupply.com");
        testSupplier.setPhoneNumber("0123456789");
        testSupplier.setClient(testClient);

        testSupplierDTO = new SupplierDTO();
        testSupplierDTO.setName("TechSupply");
        testSupplierDTO.setFirstName("Jean");
        testSupplierDTO.setDescription("Fournisseur d'équipements informatiques");
        testSupplierDTO.setContactInfo("contact@techsupply.com");
        testSupplierDTO.setPhoneNumber("0123456789");
    }

    @Test
    void getAllSuppliers_ShouldReturnAllSuppliers() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findAll()).thenReturn(suppliers);

        // When
        List<Supplier> result = supplierService.getAllSuppliers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSupplier, result.get(0));
        verify(supplierRepository, times(1)).findAll();
    }

    @Test
    void getSuppliersByClientId_ShouldReturnSupplierResponseDTOs() {
        // Given
        List<Supplier> suppliers = Arrays.asList(testSupplier);
        when(supplierRepository.findByClientUserId(clientId)).thenReturn(suppliers);

        // When
        List<SupplierResponseDTO> result = supplierService.getSuppliersByClientId(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSupplier.getName(), result.get(0).getName());
        assertEquals(testSupplier.getFirstName(), result.get(0).getFirstName());
        verify(supplierRepository, times(1)).findByClientUserId(clientId);
    }

    @Test
    void getSupplierById_ShouldReturnSupplier_WhenExists() {
        // Given
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(testSupplier));

        // When
        Optional<Supplier> result = supplierService.getSupplierById(supplierId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testSupplier, result.get());
        verify(supplierRepository, times(1)).findById(supplierId);
    }

    @Test
    void getSupplierById_ShouldReturnEmpty_WhenNotExists() {
        // Given
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // When
        Optional<Supplier> result = supplierService.getSupplierById(supplierId);

        // Then
        assertFalse(result.isPresent());
        verify(supplierRepository, times(1)).findById(supplierId);
    }

    @Test
    void createSupplier_ShouldReturnCreatedSupplier_WhenClientExists() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        Optional<Supplier> result = supplierService.createSupplier(clientId, testSupplierDTO);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testSupplier, result.get());
        verify(clientRepository, times(1)).findById(clientId);
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void createSupplier_ShouldReturnEmpty_WhenClientNotExists() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When
        Optional<Supplier> result = supplierService.createSupplier(clientId, testSupplierDTO);

        // Then
        assertFalse(result.isPresent());
        verify(clientRepository, times(1)).findById(clientId);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void updateSupplier_ShouldReturnUpdatedSupplierResponseDTO_WhenSupplierExists() {
        // Given
        SupplierDTO updateDTO = new SupplierDTO();
        updateDTO.setName("TechSupply Updated");
        updateDTO.setFirstName("Jean Updated");
        updateDTO.setDescription("Description mise à jour");
        updateDTO.setContactInfo("new-contact@techsupply.com");
        updateDTO.setPhoneNumber("0987654321");

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);

        // When
        Optional<SupplierResponseDTO> result = supplierService.updateSupplier(supplierId, updateDTO);

        // Then
        assertTrue(result.isPresent());
        verify(supplierRepository, times(1)).findById(supplierId);
        verify(supplierRepository, times(1)).save(testSupplier);
    }

    @Test
    void updateSupplier_ShouldReturnEmpty_WhenSupplierNotExists() {
        // Given
        SupplierDTO updateDTO = new SupplierDTO();
        updateDTO.setName("TechSupply Updated");

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // When
        Optional<SupplierResponseDTO> result = supplierService.updateSupplier(supplierId, updateDTO);

        // Then
        assertFalse(result.isPresent());
        verify(supplierRepository, times(1)).findById(supplierId);
        verify(supplierRepository, never()).save(any(Supplier.class));
    }

    @Test
    void getSuppliersByClientId_ShouldReturnEmptyList_WhenNoSuppliersForClient() {
        // Given
        when(supplierRepository.findByClientUserId(clientId)).thenReturn(Arrays.asList());

        // When
        List<SupplierResponseDTO> result = supplierService.getSuppliersByClientId(clientId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(supplierRepository, times(1)).findByClientUserId(clientId);
    }

    @Test
    void createSupplier_ShouldSetAllFieldsCorrectly() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier savedSupplier = invocation.getArgument(0);
            // Vérifier que tous les champs sont correctement définis
            assertEquals(testSupplierDTO.getName(), savedSupplier.getName());
            assertEquals(testSupplierDTO.getFirstName(), savedSupplier.getFirstName());
            assertEquals(testSupplierDTO.getDescription(), savedSupplier.getDescription());
            assertEquals(testSupplierDTO.getContactInfo(), savedSupplier.getContactInfo());
            assertEquals(testSupplierDTO.getPhoneNumber(), savedSupplier.getPhoneNumber());
            assertEquals(testClient, savedSupplier.getClient());
            assertNotNull(savedSupplier.getStockItems());
            return savedSupplier;
        });

        // When
        Optional<Supplier> result = supplierService.createSupplier(clientId, testSupplierDTO);

        // Then
        assertTrue(result.isPresent());
        verify(clientRepository, times(1)).findById(clientId);
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void createSupplier_ShouldHandleNullFields() {
        // Given
        SupplierDTO dtoWithNulls = new SupplierDTO();
        dtoWithNulls.setName("OnlyName");
        // Autres champs restent null

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Optional<Supplier> result = supplierService.createSupplier(clientId, dtoWithNulls);

        // Then
        assertTrue(result.isPresent());
        verify(clientRepository, times(1)).findById(clientId);
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    void getSuppliersByClientId_ShouldHandleMultipleSuppliers() {
        // Given
        Supplier supplier2 = new Supplier();
        supplier2.setSupplierId(UUID.randomUUID());
        supplier2.setName("SecondSupplier");
        supplier2.setFirstName("Marie");
        supplier2.setClient(testClient);

        List<Supplier> suppliers = Arrays.asList(testSupplier, supplier2);
        when(supplierRepository.findByClientUserId(clientId)).thenReturn(suppliers);

        // When
        List<SupplierResponseDTO> result = supplierService.getSuppliersByClientId(clientId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("TechSupply", result.get(0).getName());
        assertEquals("SecondSupplier", result.get(1).getName());
        verify(supplierRepository, times(1)).findByClientUserId(clientId);
    }
}