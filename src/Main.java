//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import Factory.Conexao;
import Model.Contato;

import java.sql.*;
import java.util.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Conexao db = new Conexao();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            mostrarMenu();
            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1":
                    criarContato(db, scanner);
                    break;
                case "2":
                    exibirContatos(db);
                    break;
                case "3":
                    atualizarContato(db, scanner);
                    break;
                case "4":
                    deletarContato(db, scanner);
                    break;
                case "5":
                    db.closeConnection();
                    System.out.println("Saindo...");
                    return;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n--- Gerenciador de Contatos ---");
        System.out.println("1. Criar contato");
        System.out.println("2. Exibir contatos");
        System.out.println("3. Atualizar contato");
        System.out.println("4. Deletar contato");
        System.out.println("5. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private static void criarContato(Conexao db, Scanner scanner) {
        Contato contato = new Contato();
        contato.criaContato(scanner);
        contato.salvarContato(db);
    }

    private static void exibirContatos(Conexao db) {
        try {
            List<Map<String, Object>> contatos = db.buscarContatos();

            System.out.println("\n--- Lista de Contatos ---");
            System.out.printf("%-5s %-20s %-30s %-30s%n", "ID", "Nome", "Emails", "Telefones");
            System.out.println("----------------------------------------------------------------------------------------------");

            for (Map<String, Object> contato : contatos) {
                System.out.printf("%-5d %-20s %-30s %-30s%n",
                        contato.get("id"),
                        contato.get("nome"),
                        contato.get("emails") == null ? "-" : contato.get("emails"),
                        contato.get("telefones") == null ? "-" : contato.get("telefones"));
            }
            System.out.println("----------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.out.println("Erro ao exibir contatos: " + e.getMessage());
        }
    }

    private static void atualizarContato(Conexao db, Scanner scanner) {
        System.out.print("Digite o ID do contato a ser atualizado: ");
        int idContato = Integer.parseInt(scanner.nextLine());

        System.out.println("Escolha o campo a ser atualizado:");
        System.out.println("1. Nome");
        System.out.println("2. Email");
        System.out.println("3. Telefone");
        System.out.print("Opção: ");

        int opcao = Integer.parseInt(scanner.nextLine());
        String tabela = "";
        String campo = "";

        switch (opcao) {
            case 1:
                tabela = "Contatos";
                campo = "nome";
                break;
            case 2:
                tabela = "Emails";
                campo = "email";
                break;
            case 3:
                tabela = "Telefones";
                campo = "numero";
                break;
            default:
                System.out.println("Opção inválida!");
                return;
        }

        System.out.print("Digite o novo valor: ");
        String novoValor = scanner.nextLine();

        try {
            db.atualizarItem(tabela, campo, novoValor, idContato);
            System.out.println("___Contato atualizado!___");
        } catch (SQLException e) {
            System.out.println("Erro ao atualizar contato: " + e.getMessage());
        }
    }

    private static void deletarContato(Conexao db, Scanner scanner) {
        System.out.print("Digite o ID do contato a ser deletado: ");
        int idContato = Integer.parseInt(scanner.nextLine());

        try {
            String nomeExcluido = "";
            // Busca o nome do contato para confirmação
            String sql = "SELECT nome FROM Contatos WHERE id = ?";
            try (PreparedStatement pstmt = db.getConn().prepareStatement(sql)) {
                pstmt.setInt(1, idContato);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        nomeExcluido = rs.getString("nome");
                    }
                }
            }

            System.out.printf("Deseja excluir o contato de %s? (SIM/NAO): ", nomeExcluido);
            String confirmacao = scanner.nextLine().trim().toUpperCase();

            if ("SIM".equals(confirmacao)) {
                db.deletarItem("Contatos", idContato);
                System.out.println("___Contato excluído com sucesso!___");
            } else if ("NAO".equals(confirmacao)) {
                System.out.println("Operação cancelada.");
            } else {
                System.out.println("Opção inválida!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao deletar contato: " + e.getMessage());
        }
    }
}