package Factory;
import java.sql.*;
import java.util.*;

public class Conexao {

    private static Connection conn;

    private static final String driver = "com.mysql.cj.jdbc.Driver";
    private static final String url = "jdbc:mysql://localhost:3306/contatos_db?useSSL=false&serverTimezone=UTC";
    private static final String usuario = "naoeroot";
    private static final String senha = "senha123";

    public static Connection conector() {
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, usuario, senha);

            if (conn != null) {
                System.out.println("Conexão estabelecida! Criando tabelas...");
                criaTabelas();
            }
            return conn;

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Erro na conexão com o banco: " + e.getMessage());
            closeConnection();
            throw new RuntimeException(e);
        }
    }

    private static void criaTabelas() throws SQLException {
        String criarContatos = """
            CREATE TABLE IF NOT EXISTS Contatos (
                id INT PRIMARY KEY AUTO_INCREMENT,
                nome VARCHAR(100) NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        String criarTelefones = """
            CREATE TABLE IF NOT EXISTS Telefones (
                id INT PRIMARY KEY AUTO_INCREMENT,
                ddd VARCHAR(2) NOT NULL,
                numero VARCHAR(9) NOT NULL,
                contato_id INT,
                FOREIGN KEY (contato_id) REFERENCES Contatos(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        String criarEmails = """
            CREATE TABLE IF NOT EXISTS Emails (
                id INT PRIMARY KEY AUTO_INCREMENT,
                email VARCHAR(100) NOT NULL,
                contato_id INT,
                FOREIGN KEY (contato_id) REFERENCES Contatos(id) ON DELETE CASCADE
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(criarContatos);
            stmt.execute(criarTelefones);
            stmt.execute(criarEmails);
        }
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.out.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}