package ru.cmc.web_prac.classes;

/**
 * Базовый интерфейс для всех сущностей
 * Определяет общие методы для работы с ID
 */
public interface CommonEntity<ID> {
    ID getId();
    void setId(ID id);
}
