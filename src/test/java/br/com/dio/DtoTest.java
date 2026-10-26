package br.com.dio;

import br.com.dio.dto.BoardColumnDTO;
import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.dto.BoardDetailsDTO;
import br.com.dio.dto.CardDetailsDTO;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;

class DtoTest {

    @Test
    @DisplayName("BoardColumnDTO deve armazenar dados corretamente")
    void boardColumnDTO() {
        var dto = new BoardColumnDTO(1L, "A Fazer", BoardColumnKindEnum.INITIAL, 5);
        assertEquals(1L, dto.id());
        assertEquals("A Fazer", dto.name());
        assertEquals(BoardColumnKindEnum.INITIAL, dto.kind());
        assertEquals(5, dto.cardsAmount());
    }

    @Test
    @DisplayName("BoardColumnInfoDTO deve armazenar dados corretamente")
    void boardColumnInfoDTO() {
        var dto = new BoardColumnInfoDTO(2L, 1, BoardColumnKindEnum.PENDING);
        assertEquals(2L, dto.id());
        assertEquals(1, dto.order());
        assertEquals(BoardColumnKindEnum.PENDING, dto.kind());
    }

    @Test
    @DisplayName("BoardDetailsDTO deve conter lista de colunas")
    void boardDetailsDTO() {
        var columns = List.of(
                new BoardColumnDTO(1L, "Início", BoardColumnKindEnum.INITIAL, 2),
                new BoardColumnDTO(2L, "Final", BoardColumnKindEnum.FINAL, 0)
        );
        var dto = new BoardDetailsDTO(10L, "Board Legal", columns);
        assertEquals(10L, dto.id());
        assertEquals("Board Legal", dto.name());
        assertEquals(2, dto.columns().size());
    }

    @Test
    @DisplayName("CardDetailsDTO deve representar card bloqueado")
    void cardDetailsDTO_blocked() {
        var now = OffsetDateTime.now();
        var dto = new CardDetailsDTO(
                1L, "Card Teste", "Descrição",
                true, now, "Motivo do bloqueio", 3,
                5L, "Em Andamento"
        );
        assertTrue(dto.blocked());
        assertEquals("Motivo do bloqueio", dto.blockReason());
        assertEquals(3, dto.blocksAmount());
        assertEquals(5L, dto.columnId());
        assertEquals("Em Andamento", dto.columnName());
    }

    @Test
    @DisplayName("CardDetailsDTO deve representar card não bloqueado")
    void cardDetailsDTO_unblocked() {
        var dto = new CardDetailsDTO(
                2L, "Card Livre", "Livre descrição",
                false, null, null, 0,
                1L, "Início"
        );
        assertFalse(dto.blocked());
        assertNull(dto.blockedAt());
        assertNull(dto.blockReason());
        assertEquals(0, dto.blocksAmount());
    }
}