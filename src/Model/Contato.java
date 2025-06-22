package Model;

import java.util.*;

public class Contato {

    private Integer id;
    private String nome;
    private List<String> emails = new ArrayList<>(); //Lista de emails por contato
    private List<Telefone> telefones = new ArrayList<>(); //Lista de telefones por contato

    public Integer getId() {
        return id; }
    public void setId(Integer id) {
        this.id = id; }
    public String getNome() {
        return nome; }
    public List<String> getEmails() {
        return emails; }
    public List<Telefone> getTelefones() {
        return telefones; }

    // Classe interna para representar telefone (DDD+Número)
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