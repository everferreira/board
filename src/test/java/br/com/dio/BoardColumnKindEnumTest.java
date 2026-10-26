package br.com.dio;

import br.com.dio.persistence.entity.BoardColumnKindEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BoardColumnKindEnumTest {

    @Test
    @DisplayName("Deve encontrar enum por nome")
    void findByName() {
        assertEquals(BoardColumnKindEnum.INITIAL, BoardColumnKindEnum.findByName("INITIAL"));
        assertEquals(BoardColumnKindEnum.PENDING, BoardColumnKindEnum.findByName("PENDING"));
        assertEquals(BoardColumnKindEnum.FINAL, BoardColumnKindEnum.findByName("FINAL"));
        assertEquals(BoardColumnKindEnum.CANCEL, BoardColumnKindEnum.findByName("CANCEL"));
    }

    @Test
    @DisplayName("Deve lançar exceção para nome inválido")
    void findByName_invalid() {
        assertThrows(RuntimeException.class,
                () -> BoardColumnKindEnum.findByName("INEXISTENTE"));
    }

    @Test
    @DisplayName("Deve ter exatamente 4 constantes")
    void countConstants() {
        assertEquals(4, BoardColumnKindEnum.values().length);
    }
}