package br.com.dio.ui;

import br.com.dio.persistence.entity.BoardColumnEntity;
import br.com.dio.persistence.entity.BoardColumnKindEnum;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;
import br.com.dio.service.BoardService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.FINAL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.INITIAL;
import static br.com.dio.persistence.entity.BoardColumnKindEnum.PENDING;
import static br.com.dio.util.ColorUtil.*;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");

    public void execute() throws SQLException {
        printBox("Bem vindo ao gerenciador de boards", List.of(
                bold("Sistema de Gerenciamento Kanban") + " - " + CYAN + "DIO" + RESET
        ));
        var option = -1;
        while (true){
            System.out.println();
            System.out.println(CYAN + "┌──────────────────────────┐" + RESET);
            System.out.println(CYAN + "│" + RESET + "  " + BOLD + "1" + RESET + " - Criar um novo board    " + CYAN + "│" + RESET);
            System.out.println(CYAN + "│" + RESET + "  " + BOLD + "2" + RESET + " - Selecionar board        " + CYAN + "│" + RESET);
            System.out.println(CYAN + "│" + RESET + "  " + BOLD + "3" + RESET + " - Excluir um board        " + CYAN + "│" + RESET);
            System.out.println(CYAN + "│" + RESET + "  " + BOLD + "4" + RESET + " - Sair                    " + CYAN + "│" + RESET);
            System.out.println(CYAN + "└──────────────────────────┘" + RESET);
            System.out.print(GREEN + "▶ " + RESET + "Opção: ");
            option = scanner.nextInt();
            switch (option){
                case 1 -> createBoard();
                case 2 -> selectBoard();
                case 3 -> deleteBoard();
                case 4 -> System.exit(0);
                default -> printError("Opção inválida! Informe um número de 1 a 4.");
            }
        }
    }

    private void createBoard() throws SQLException {
        var entity = new BoardEntity();
        printBox("CRIAR NOVO BOARD", List.of("Digite os dados solicitados abaixo"));
        System.out.print(YELLOW + "◆ " + RESET + "Nome do board: ");
        entity.setName(scanner.next());

        System.out.print(YELLOW + "◆ " + RESET + "Colunas extras (além de Inicial | Final | Cancelar): ");
        var additionalColumns = scanner.nextInt();

        List<BoardColumnEntity> columns = new ArrayList<>();

        System.out.print(CYAN + "┃ " + RESET + "Nome da coluna " + BOLD + "INICIAL" + RESET + ": ");
        var initialColumnName = scanner.next();
        var initialColumn = createColumn(initialColumnName, INITIAL, 0);
        columns.add(initialColumn);

        for (int i = 0; i < additionalColumns; i++) {
            System.out.print(CYAN + "┃ " + RESET + "Nome da coluna " + YELLOW + "PENDENTE " + (i+1) + RESET + ": ");
            var pendingColumnName = scanner.next();
            var pendingColumn = createColumn(pendingColumnName, PENDING, i + 1);
            columns.add(pendingColumn);
        }

        System.out.print(CYAN + "┃ " + RESET + "Nome da coluna " + GREEN + "FINAL" + RESET + ": ");
        var finalColumnName = scanner.next();
        var finalColumn = createColumn(finalColumnName, FINAL, additionalColumns + 1);
        columns.add(finalColumn);

        System.out.print(CYAN + "┃ " + RESET + "Nome da coluna " + RED + "CANCELAR" + RESET + ": ");
        var cancelColumnName = scanner.next();
        var cancelColumn = createColumn(cancelColumnName, CANCEL, additionalColumns + 2);
        columns.add(cancelColumn);

        entity.setBoardColumns(columns);
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            service.insert(entity);
        }
        printSuccess("Board \"" + entity.getName() + "\" criado com sucesso!");

    }

    private void selectBoard() throws SQLException {
        System.out.print(MAGENTA + "◆ " + RESET + "ID do board: ");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var queryService = new BoardQueryService(connection);
            var optional = queryService.findById(id);
            optional.ifPresentOrElse(
                    b -> new BoardMenu(b).execute(),
                    () -> printError("Board com id " + id + " não encontrado.")
            );
        }
    }

    private void deleteBoard() throws SQLException {
        System.out.print(RED + "◆ " + RESET + "ID do board a excluir: ");
        var id = scanner.nextLong();
        try(var connection = getConnection()){
            var service = new BoardService(connection);
            if (service.delete(id)){
                printSuccess("Board " + id + " excluído com sucesso!");
            } else {
                printError("Board com id " + id + " não encontrado.");
            }
        }
    }

    private BoardColumnEntity createColumn(final String name, final BoardColumnKindEnum kind, final int order){
        var boardColumn = new BoardColumnEntity();
        boardColumn.setName(name);
        boardColumn.setKind(kind);
        boardColumn.setOrder(order);
        return boardColumn;
    }

}
