package model;

import db.DBHandler;

import java.sql.*;
import java.util.*;

public class Contato {
    private Integer id;
    private String nome;
    private List<String> emails = new ArrayList<>();
    private List<Telefone> telefones = new ArrayList<>();

    // Classe interna para representar telefone
    public static class Telefone {
        private String ddd;
        private String numero;

        public Telefone(String ddd, String numero) {
            this.ddd = ddd;
            this.numero = numero;
        }

        public String getDdd() { return ddd; }
        public String getNumero() { return numero; }
    }

    // Método para criar contato a partir da entrada do usuário
    public void criaContato(Scanner scanner) {
        System.out.println("___Adicione as informações de contato___");

        System.out.print("Nome: ");
        this.nome = scanner.nextLine();

        // Adicionar emails
        while (true) {
            this.emails.add(inputEmail(scanner));
            System.out.print("Deseja adicionar outro email? (S/N): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("S")) break;
        }

        // Adicionar telefones
        while (true) {
            this.telefones.add(inputTelefone(scanner));
            System.out.print("Deseja adicionar outro telefone? (S/N): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("S")) break;
        }
    }

    // Método para salvar contato no banco de dados
    public void salvarContato(DBHandler db) {
        try {
            db.getConn().setAutoCommit(false); // Iniciar transação

            // Inserir contato principal
            String colunasContato = "nome";
            int idContato = db.inserirItem("Contatos", colunasContato, this.nome);
            this.id = idContato;

            // Inserir emails
            String colunasEmail = db.getColunas().get("colunas_email");
            for (String email : this.emails) {
                db.inserirItem("Emails", colunasEmail, email, idContato);
            }

            // Inserir telefones
            String colunasTelefone = db.getColunas().get("colunas_telefone");
            for (Telefone telefone : this.telefones) {
                db.inserirItem("Telefones", colunasTelefone,
                        telefone.getDdd(), telefone.getNumero(), idContato);
            }

            db.getConn().commit(); // Confirmar transação
            System.out.println("___Contato adicionado com sucesso!___");

        } catch (SQLException e) {
            try {
                if (db.getConn() != null) db.getConn().rollback(); // Desfazer transação
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Erro ao salvar contato: " + e.getMessage());
        }
    }

    // Métodos estáticos auxiliares
    public static String inputEmail(Scanner scanner) {
        System.out.print("Email: ");
        return scanner.nextLine();
    }

    public static Telefone inputTelefone(Scanner scanner) {
        while (true) {
            System.out.print("DDD: ");
            String ddd = scanner.nextLine();
            System.out.print("Telefone: ");
            String numero = scanner.nextLine();

            try {
                validaTelefone(ddd, numero);
                return new Telefone(ddd, numero);
            } catch (IllegalArgumentException e) {
                System.out.println("Erro: " + e.getMessage());
            }
        }
    }

    public static void validaTelefone(String ddd, String numero) {
        if (ddd == null || numero == null) {
            throw new IllegalArgumentException("Valores nulos não são permitidos");
        }

        if (!ddd.matches("\\d{2}")) {
            throw new IllegalArgumentException("DDD deve ter 2 dígitos");
        }

        if (!numero.matches("\\d{9}")) {
            throw new IllegalArgumentException("Número deve ter 9 dígitos");
        }
    }
}