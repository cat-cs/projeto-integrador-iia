package Dao;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Factory.Conexao;
import Model.Contato;

import static Factory.Conexao.conn;

public class ContatoDao {
//METODO UNIFICADO PARA INSERRIR NO BANCO DE DADOS (PODE INSERIR QUALQUER ITEM)
    public int inserirItem(String tabela, String colunas, Object... valores) throws SQLException {
        String[] colunasArray = colunas.split(",");
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < valores.length; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tabela, colunas, placeholders);

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < valores.length; i++) {
                pstmt.setObject(i + 1, valores[i]);
            }

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int idInserido = rs.getInt(1);
                    return idInserido;
                }
                throw new SQLException("Falha ao obter ID gerado.");
            }
        }
    }
    //METODO PARA BUSCAR NO BANCO DE DADOS (MAIS DE UM TELEFONE E EMAIL)
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

    //METODO UNIFICADO PARA DELETAR NO BANCO DE DADOS (PODE DELETAR QUALQUER ITEM)
    public void deletarItem(String tabela, int id) throws SQLException {
        String sql = "DELETE FROM " + tabela + " WHERE id = ?";
        try (PreparedStatement pstmt = Conexao.conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            }
    }

    //METODO UNIFICADO PARA ATUALIZAR NO BANCO DE DADOS (PODE INSERIR QUALQUER ITEM)
    public void atualizarItem(String tabela, String campo, Object valor, int id) throws SQLException {
        String sql = String.format("UPDATE %s SET %s = ? WHERE id = ?", tabela, campo);
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, valor);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }

    //Dicionário de colunas
    public Map<String, String> getColunas() {
        Map<String, String> colunas = new HashMap<>();
        colunas.put("colunas_telefone", "ddd, numero, contato_id");
        colunas.put("colunas_email", "email, contato_id");
        colunas.put("campos_telefone_update", "ddd = ?, numero = ?");
        return colunas;
    }

    // Metodo para salvar contato no banco de dados
    public void salvarContato(Contato contato) {
        try {
            Conexao.conn.setAutoCommit(false); // Iniciar transação

            // Inserir contato principal
            String colunasContato = "nome";
            int idContato = inserirItem("Contatos", colunasContato, contato.getNome());
            contato.setId(idContato);

            // Inserir emails
            String colunasEmail = getColunas().get("colunas_email");
            for (String email : contato.getEmails()) {
                inserirItem("Emails", colunasEmail, email, idContato);
            }

            // Inserir telefones
            String colunasTelefone = getColunas().get("colunas_telefone");
            for (Contato.Telefone telefone : contato.getTelefones()) {
                inserirItem("Telefones", colunasTelefone,
                        telefone.getDdd(), telefone.getNumero(), idContato);
            }

            conn.commit(); // Confirmar transação
            System.out.println("___Contato adicionado com sucesso!___");

        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Desfazer transação
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.out.println("Erro ao salvar contato: " + e.getMessage());
        }
    }

}
