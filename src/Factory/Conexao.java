package Factory;
import java.sql.*;
import java.util.*;

public class Conexao {
    public static Connection conexao() {
        Connection conn = null;
        String driver = "com.mysql.cj.jdbc.Driver";
        String url = "jdbc:mysql://localhost:3306/contatos_db?useSSL=false&serverTimezone=UTC";
        String usuario = "naoeroot";
        String senha = "senha123";

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

    public int inserirItem(String tabela, String colunas, Object... valores) throws SQLException {
        String[] colunasArray = colunas.split(",");
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < valores.length; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tabela, colunas, placeholders);

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) {
                pstmt.setObject(i + 1, valores[i]);
            }

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Falha ao obter ID gerado.");
            }
        }
    }

    public List<Map<String, Object>> buscarContatos() throws SQLException {
        String sql = """
            SELECT Contatos.id, Contatos.nome,
                   GROUP_CONCAT(DISTINCT Emails.email) AS emails,
                   GROUP_CONCAT(DISTINCT CONCAT('(', Telefones.ddd, ') ', Telefones.numero)) AS telefones
            FROM Contatos 
            LEFT JOIN Emails ON Contatos.id = Emails.contato_id
            LEFT JOIN Telefones ON Contatos.id = Telefones.contato_id
            GROUP BY Contatos.id, Contatos.nome;
        """;

        List<Map<String, Object>> resultados = new ArrayList<>();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> linha = new HashMap<>();
                linha.put("id", rs.getInt("id"));
                linha.put("nome", rs.getString("nome").toUpperCase());
                linha.put("emails", rs.getString("emails"));
                linha.put("telefones", rs.getString("telefones"));
                resultados.add(linha);
            }
        }

        return resultados;
    }

    public void deletarItem(String tabela, int id) throws SQLException {
        String sql = "DELETE FROM ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tabela);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public void atualizarItem(String tabela, String campo, Object valor, int id) throws SQLException {
        String sql = String.format("UPDATE %s SET %s = ? WHERE id = ?", tabela, campo);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, valor);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    public Map<String, String> getColunas() {
        Map<String, String> colunas = new HashMap<>();
        colunas.put("colunas_telefone", "ddd, numero, contato_id");
        colunas.put("colunas_email", "email, contato_id");
        colunas.put("campos_telefone_update", "ddd = ?, numero = ?");
        return colunas;
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