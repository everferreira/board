package br.com.dio.ui;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.persistence.entity.CardEntity;
import br.com.dio.service.BoardColumnQueryService;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.CardQueryService;
import br.com.dio.service.CardService;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;
import static br.com.dio.util.ColorUtil.*;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    private final BoardEntity entity;

    public void execute() {
        try {
            printBox("Board #" + entity.getId() + " - " + entity.getName(), List.of(
                    BOLD + "Gerencie os cards deste board" + RESET
            ));
            var option = -1;
            while (option != 9) {
                System.out.println();
                System.out.println(CYAN + "┌──────────────────────────────────┐" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "1" + RESET + " - Criar um card          " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "2" + RESET + " - Mover um card          " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "3" + RESET + " - Bloquear um card       " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "4" + RESET + " - Desbloquear um card    " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "5" + RESET + " - Cancelar um card       " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "6" + RESET + " - Ver board              " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "7" + RESET + " - Ver coluna com cards   " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "8" + RESET + " - Ver card               " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "9" + RESET + " - Voltar                 " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "│" + RESET + "  " + BOLD + "10" + RESET + " - Sair                  " + CYAN + "  │" + RESET);
                System.out.println(CYAN + "└──────────────────────────────────┘" + RESET);
                System.out.print(GREEN + "▶ " + RESET + "Opção: ");
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> createCard();
                    case 2 -> moveCardToNextColumn();
                    case 3 -> blockCard();
                    case 4 -> unblockCard();
                    case 5 -> cancelCard();
                    case 6 -> showBoard();
                    case 7 -> showColumn();
                    case 8 -> showCard();
                    case 9 -> printInfo("Voltando para o menu anterior");
                    case 10 -> System.exit(0);
                    default -> printError("Opção inválida! Informe um número de 1 a 10.");
                }
            }
        }catch (SQLException ex){
            printError("Erro no banco de dados: " + ex.getMessage());
            System.exit(0);
        }
    }

    private void createCard() throws SQLException{
        var card = new CardEntity();
        printBox("CRIAR CARD", List.of("Preencha os dados do novo card"));
        System.out.print(YELLOW + "◆ " + RESET + "Título: ");
        card.setTitle(scanner.next());
        System.out.print(YELLOW + "◆ " + RESET + "Descrição: ");
        card.setDescription(scanner.next());
        card.setBoardColumn(entity.getInitialColumn());
        try(var connection = getConnection()){
            new CardService(connection).create(card);
        }
        printSuccess("Card \"" + card.getTitle() + "\" criado na coluna inicial!");
    }

    private void moveCardToNextColumn() throws SQLException {
        System.out.print(MAGENTA + "◆ " + RESET + "ID do card para mover: ");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
        } catch (RuntimeException ex){
            System.out.println(ex.getMessage());
        }
    }

    private void blockCard() throws SQLException {
        System.out.print(RED + "◆ " + RESET + "ID do card a bloquear: ");
        var cardId = scanner.nextLong();
        System.out.print(YELLOW + "◆ " + RESET + "Motivo do bloqueio: ");
        var reason = scanner.next();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
            printSuccess("Card " + cardId + " bloqueado!");
        } catch (RuntimeException ex){
            printError(ex.getMessage());
        }
    }

    private void unblockCard() throws SQLException {
        System.out.print(GREEN + "◆ " + RESET + "ID do card a desbloquear: ");
        var cardId = scanner.nextLong();
        System.out.print(YELLOW + "◆ " + RESET + "Motivo do desbloqueio: ");
        var reason = scanner.next();
        try(var connection = getConnection()){
            new CardService(connection).unblock(cardId, reason);
            printSuccess("Card " + cardId + " desbloqueado!");
        } catch (RuntimeException ex){
            printError(ex.getMessage());
        }
    }

    private void cancelCard() throws SQLException {
        System.out.print(RED + "◆ " + RESET + "ID do card a cancelar: ");
        var cardId = scanner.nextLong();
        var cancelColumn = entity.getCancelColumn();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).cancel(cardId, cancelColumn.getId(), boardColumnsInfo);
            printSuccess("Card " + cardId + " cancelado!");
        } catch (RuntimeException ex){
            printError(ex.getMessage());
        }
    }

    private void showBoard() throws SQLException {
        try(var connection = getConnection()){
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                printBox("BOARD #" + b.id() + " - " + b.name(), List.of());
                b.columns().forEach(c -> {
                    var kindColor = switch (c.kind()) {
                        case INITIAL -> CYAN;
                        case PENDING -> YELLOW;
                        case FINAL -> GREEN;
                        case CANCEL -> RED;
                    };
                    System.out.println(kindColor + "  ██ " + RESET + bold(c.name()) +
                            " (" + kindColor + c.kind() + RESET + ") - " +
                            bold(String.valueOf(c.cardsAmount())) + " cards");
                });
            });
        }
    }

    private void showColumn() throws SQLException {
        var columnsIds = entity.getBoardColumns().stream().map(BoardColumnEntity::getId).toList();
        var selectedColumnId = -1L;
        while (!columnsIds.contains(selectedColumnId)){
            printInfo("Escolha uma coluna do board \"" + entity.getName() + "\":");
            entity.getBoardColumns().forEach(c -> {
                var kindColor = switch (c.getKind()) {
                    case INITIAL -> CYAN;
                    case PENDING -> YELLOW;
                    case FINAL -> GREEN;
                    case CANCEL -> RED;
                };
                System.out.println(kindColor + "  [" + c.getId() + "] " + RESET + c.getName() + " (" + kindColor + c.getKind() + RESET + ")");
            });
            System.out.print(GREEN + "▶ " + RESET + "ID da coluna: ");
            selectedColumnId = scanner.nextLong();
        }
        try(var connection = getConnection()){
            var column = new BoardColumnQueryService(connection).findById(selectedColumnId);
            column.ifPresent(co -> {
                var kindColor = switch (co.getKind()) {
                    case INITIAL -> CYAN;
                    case PENDING -> YELLOW;
                    case FINAL -> GREEN;
                    case CANCEL -> RED;
                };
                printBox("COLUNA: " + co.getName(), List.of("Tipo: " + kindColor + co.getKind() + RESET + " | " + co.getCards().size() + " cards"));
                co.getCards().forEach(ca ->
                    System.out.println(CYAN + "  ❏ " + RESET + bold("#" + ca.getId()) +
                            " - " + ca.getTitle() + "\n" +
                            GRAY + "     " + ca.getDescription() + RESET)
                );
            });
        }
    }

    private void showCard() throws SQLException {
        System.out.print(MAGENTA + "◆ " + RESET + "ID do card: ");
        var selectedCardId = scanner.nextLong();
        try(var connection  = getConnection()){
            new CardQueryService(connection).findById(selectedCardId)
                    .ifPresentOrElse(
                            c -> {
                                printBox("CARD #" + c.id() + " - " + c.title(), List.of(c.description()));
                                System.out.println(CYAN + "  Coluna: " + RESET + bold(c.columnName()));
                                if (c.blocked()) {
                                    System.out.println(RED + "  ▌ BLOQUEADO" + RESET + " desde: " + c.blockedAt() + " | Motivo: " + c.blockReason());
                                } else {
                                    System.out.println(GREEN + "  ▌ Ativo" + RESET);
                                }
                                System.out.println(GRAY + "  Bloqueios: " + c.blocksAmount() + "x" + RESET);
                            },
                            () -> printError("Card com id " + selectedCardId + " não encontrado."));
        }
    }

}
