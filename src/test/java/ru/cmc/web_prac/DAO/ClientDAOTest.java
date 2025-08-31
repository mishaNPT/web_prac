package ru.cmc.web_prac.DAO;

import org.junit.jupiter.api.Test;
import ru.cmc.web_prac.classes.*;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ClientDAOTest extends BaseDAOTest {
    @Test
    void testSave() {
        Client client = new Client();
        client.setFullName("Тестовый Алексей Сидоров");
        client.setEmail("test.alexey@example.com");
        client.setPhone("+7-900-555-55-55");
        client.setAddress("Екатеринбург, ул. Ленина, д. 5");
        client.setBonusMiles(500);

        clientDAO.save(client);
        entityManager.flush();

        assertNotNull(client.getId());
        assertTrue(client.getId() > 0);
    }

    @Test
    void testGetById() {
        Client foundClient = clientDAO.getById(testClient1.getId());

        assertNotNull(foundClient);
        assertEquals(testClient1.getId(), foundClient.getId());
        assertEquals("Тестовый Иван Иванович", foundClient.getFullName());
        assertEquals("test.ivan@example.com", foundClient.getEmail());
        assertEquals(1000, foundClient.getBonusMiles());
    }

    @Test
    void testGetByIdNonExistent() {
        Client foundClient = clientDAO.getById(999L);
        assertNull(foundClient);
    }

    @Test
    void testGetAll() {
        Collection<Client> allClients = clientDAO.getAll();

        assertNotNull(allClients);
        assertEquals(2, allClients.size());

        boolean found1 = allClients.stream().anyMatch(c -> c.getEmail().equals("test.ivan@example.com"));
        boolean found2 = allClients.stream().anyMatch(c -> c.getEmail().equals("test.maria@example.com"));
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    void testUpdate() {
        testClient1.setPhone("+7-911-111-11-11");
        testClient1.setBonusMiles(1500);
        testClient1.setAddress("Москва, ул. Арбат, д. 10");

        clientDAO.update(testClient1);
        entityManager.flush();
        entityManager.clear();

        Client updatedClient = clientDAO.getById(testClient1.getId());
        assertEquals("+7-911-111-11-11", updatedClient.getPhone());
        assertEquals(1500, updatedClient.getBonusMiles());
        assertEquals("Москва, ул. Арбат, д. 10", updatedClient.getAddress());
    }

    @Test
    void testDelete() {
        Long airlineId = testAirline1.getId();

        // Сначала удаляем связанные рейсы и бронирования
        bookingDAO.delete(testBooking1);
        flightDAO.delete(testFlight1);

        // Теперь можем удалить авиакомпанию
        airlineDAO.delete(testAirline1);

        Airline deletedAirline = airlineDAO.getById(airlineId);
        assertNull(deletedAirline);

        Collection<Airline> allAirlines = airlineDAO.getAll();
        assertEquals(1, allAirlines.size());
        assertEquals("Test S7", allAirlines.iterator().next().getName());
    }

    @Test
    void testGetByEmail() {
        Client foundClient = clientDAO.getByEmail("test.ivan@example.com");

        assertNotNull(foundClient);
        assertEquals("test.ivan@example.com", foundClient.getEmail());
        assertEquals("Тестовый Иван Иванович", foundClient.getFullName());
        assertEquals(testClient1.getId(), foundClient.getId());
    }

    @Test
    void testGetByEmailNotFound() {
        Client foundClient = clientDAO.getByEmail("notfound@example.com");
        assertNull(foundClient);
    }

    @Test
    void testGetByNameContaining() {
        List<Client> clients = clientDAO.getByNameContaining("Иван");

        assertEquals(1, clients.size());
        assertTrue(clients.get(0).getFullName().contains("Иван"));
    }

    @Test
    void testGetByNameContainingCaseInsensitive() {
        List<Client> clients = clientDAO.getByNameContaining("мария");

        assertEquals(1, clients.size());
        assertTrue(clients.get(0).getFullName().toLowerCase().contains("мария"));
    }

    @Test
    void testGetByNameContainingNotFound() {
        List<Client> clients = clientDAO.getByNameContaining("НеСуществующее");
        assertTrue(clients.isEmpty());
    }

    @Test
    void testSearchByNameOrEmail() {
        // Поиск по имени
        List<Client> clientsByName = clientDAO.searchByNameOrEmail("Иван");
        assertEquals(1, clientsByName.size());

        // Поиск по email
        List<Client> clientsByEmail = clientDAO.searchByNameOrEmail("maria@example");
        assertEquals(1, clientsByEmail.size());

        // Поиск по части и того и другого
        List<Client> clientsByBoth = clientDAO.searchByNameOrEmail("test");
        assertEquals(2, clientsByBoth.size());
    }

    @Test
    void testSearchByNameOrEmailNotFound() {
        List<Client> clients = clientDAO.searchByNameOrEmail("НеНайдено");
        assertTrue(clients.isEmpty());
    }

    @Test
    void testGetByBonusMilesGreaterThan() {
        List<Client> richClients = clientDAO.getByBonusMilesGreaterThan(2000);

        assertEquals(1, richClients.size());
        assertEquals("test.maria@example.com", richClients.get(0).getEmail());
        assertTrue(richClients.get(0).getBonusMiles() > 2000);
    }

    @Test
    void testGetByBonusMilesGreaterThanEmpty() {
        List<Client> richClients = clientDAO.getByBonusMilesGreaterThan(10000);
        assertTrue(richClients.isEmpty());
    }

    @Test
    void testGetByBonusMilesGreaterThanSorted() {
        // Создаем дополнительного клиента
        Client client = new Client();
        client.setFullName("Богатый Клиент");
        client.setEmail("rich@example.com");
        client.setBonusMiles(5000);
        clientDAO.save(client);
        entityManager.flush();

        List<Client> richClients = clientDAO.getByBonusMilesGreaterThan(1000);

        assertEquals(2, richClients.size());
        // Проверяем сортировку по убыванию миль
        assertTrue(richClients.get(0).getBonusMiles() >= richClients.get(1).getBonusMiles());
    }

    @Test
    void testUpdateBonusMiles() {
        Long clientId = testClient1.getId();
        Integer newMiles = 3000;

        clientDAO.updateBonusMiles(clientId, newMiles);
        entityManager.flush();
        entityManager.clear();

        Client updatedClient = clientDAO.getById(clientId);
        assertEquals(newMiles, updatedClient.getBonusMiles());
    }

    @Test
    void testAddBonusMiles() {
        Long clientId = testClient1.getId();
        Integer initialMiles = testClient1.getBonusMiles();
        Integer milesToAdd = 500;

        clientDAO.addBonusMiles(clientId, milesToAdd);
        entityManager.flush();
        entityManager.clear();

        Client updatedClient = clientDAO.getById(clientId);
        assertEquals(initialMiles + milesToAdd, updatedClient.getBonusMiles());
    }

    @Test
    void testDeductBonusMiles() {
        Long clientId = testClient2.getId();
        Integer initialMiles = testClient2.getBonusMiles();
        Integer milesToDeduct = 1000;

        clientDAO.deductBonusMiles(clientId, milesToDeduct);
        entityManager.flush();
        entityManager.clear();

        Client updatedClient = clientDAO.getById(clientId);
        assertEquals(initialMiles - milesToDeduct, updatedClient.getBonusMiles());
    }

    @Test
    void testDeductBonusMilesInsufficientFunds() {
        Long clientId = testClient1.getId();
        Integer initialMiles = testClient1.getBonusMiles();
        Integer milesToDeduct = initialMiles + 1000; // Больше чем есть

        clientDAO.deductBonusMiles(clientId, milesToDeduct);
        entityManager.flush();
        entityManager.clear();

        Client updatedClient = clientDAO.getById(clientId);
        // Мили не должны были измениться, так как недостаточно средств
        assertEquals(initialMiles, updatedClient.getBonusMiles());
    }

    @Test
    void testSaveCollection() {
        Client client1 = new Client();
        client1.setFullName("Коллекция Первый");
        client1.setEmail("collection1@example.com");
        client1.setPhone("+7-900-111-11-11");
        client1.setAddress("Тестовый адрес 1");
        client1.setBonusMiles(750);

        Client client2 = new Client();
        client2.setFullName("Коллекция Второй");
        client2.setEmail("collection2@example.com");
        client2.setPhone("+7-900-222-22-22");
        client2.setAddress("Тестовый адрес 2");
        client2.setBonusMiles(1200);

        List<Client> clientsToSave = List.of(client1, client2);
        clientDAO.saveCollection(clientsToSave);
        entityManager.flush();

        assertNotNull(client1.getId());
        assertNotNull(client2.getId());

        Collection<Client> allClients = clientDAO.getAll();
        assertEquals(4, allClients.size());
    }

    @Test
    void testUniqueEmailConstraint() {
        Client client = new Client();
        client.setFullName("Дубликат Email");
        client.setEmail("test.ivan@example.com"); // Дублируем email
        client.setBonusMiles(0);

        assertThrows(Exception.class, () -> {
            clientDAO.save(client);
            entityManager.flush();
        });
    }

    @Test
    void testDefaultBonusMiles() {
        Client client = new Client();
        client.setFullName("Без Миль");
        client.setEmail("no.miles@example.com");
        // Не устанавливаем bonusMiles

        clientDAO.save(client);
        entityManager.flush();

        assertNotNull(client.getId());
        assertEquals(0, client.getBonusMiles()); // Значение по умолчанию
    }

    @Test
    void testNullEmail() {
        Client client = new Client();
        client.setFullName("Без Email");
        client.setEmail(null);
        client.setBonusMiles(100);

        clientDAO.save(client);
        entityManager.flush();

        assertNotNull(client.getId());
        assertNull(client.getEmail());
    }

    @Test
    void testFullNameLength() {
        Client client = new Client();
        // Имя длиннее 100 символов
        client.setFullName("Очень Очень Очень Очень Очень Очень Очень Длинное Полное Имя Клиента Которое Превышает Лимит В Сто Символов Установленный");
        client.setEmail("long.name@example.com");

        assertThrows(Exception.class, () -> {
            clientDAO.save(client);
            entityManager.flush();
        });
    }

    @Test
    void testPhoneLength() {
        Client client = new Client();
        client.setFullName("Длинный Телефон");
        client.setEmail("long.phone@example.com");
        client.setPhone("+7-900-123-456-789-012"); // Длиннее 20 символов

        assertThrows(Exception.class, () -> {
            clientDAO.save(client);
            entityManager.flush();
        });
    }
}