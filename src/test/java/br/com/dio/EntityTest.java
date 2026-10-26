package br.com.dio;

import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.CardEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EntityTest {

    @Test
    @DisplayName("Deve criar BoardEntity com dados básicos")
    void boardEntity() {
        var board = new BoardEntity();
        board.setId(1L);
        board.setName("Meu Board");

        assertEquals(1L, board.getId());
        assertEquals("Meu Board", board.getName());
        assertNotNull(board.getBoardColumns());
        assertTrue(board.getBoardColumns().isEmpty());
    }

    @Test
    @DisplayName("Deve criar BoardColumnEntity com tipo INITIAL")
    void boardColumnInitial() {
        var col = new BoardColumnEntity();
        col.setId(10L);
        col.setName("A Fazer");
        col.setOrder(0);
        col.setKind(BoardColumnKindEnum.INITIAL);

        assertEquals(10L, col.getId());
        assertEquals(BoardColumnKindEnum.INITIAL, col.getKind());
        assertEquals(0, col.getOrder());
    }

    @Test
    @DisplayName("Deve criar CardEntity e associar à coluna")
    void cardEntity() {
        var card = new CardEntity();
        card.setId(100L);
        card.setTitle("Tarefa Exemplo");
        card.setDescription("Descrição detalhada da tarefa");

        var column = new BoardColumnEntity();
        column.setId(1L);
        card.setBoardColumn(column);

        assertEquals(100L, card.getId());
        assertEquals("Tarefa Exemplo", card.getTitle());
        assertEquals("Descrição detalhada da tarefa", card.getDescription());
        assertEquals(1L, card.getBoardColumn().getId());
    }

    @Test
    @DisplayName("BoardEntity deve retornar coluna inicial e de cancelamento")
    void boardEntityColumns() {
        var board = new BoardEntity();
        board.setId(1L);
        board.setName("Board Teste");

        var initial = new BoardColumnEntity();
        initial.setName("Início");
        initial.setKind(BoardColumnKindEnum.INITIAL);
        initial.setOrder(0);

        var pending = new BoardColumnEntity();
        pending.setName("Em Andamento");
        pending.setKind(BoardColumnKindEnum.PENDING);
        pending.setOrder(1);

        var finalCol = new BoardColumnEntity();
        finalCol.setName("Concluído");
        finalCol.setKind(BoardColumnKindEnum.FINAL);
        finalCol.setOrder(2);

        var cancel = new BoardColumnEntity();
        cancel.setName("Cancelado");
        cancel.setKind(BoardColumnKindEnum.CANCEL);
        cancel.setOrder(3);

        board.getBoardColumns().add(initial);
        board.getBoardColumns().add(pending);
        board.getBoardColumns().add(finalCol);
        board.getBoardColumns().add(cancel);

        assertEquals(initial, board.getInitialColumn());
        assertEquals(cancel, board.getCancelColumn());
    }
}