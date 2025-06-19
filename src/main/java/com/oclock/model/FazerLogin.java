package com.oclock.model;

import java.sql.*;
import com.oclock.dao.Conexao;

public class FazerLogin {

    private Criptografia criptografia = new Criptografia();

    public String fazerLoginWithCredentials(String email, String senha) {
        String hashedPassword = criptografia.gerarHash(senha);

        if (hashedPassword == null) {
            System.err.println("Erro ao gerar hash da senha para login.");
            return null;
        }

        try (Connection con = Conexao.conectar();
             PreparedStatement stmt = con.prepareStatement("SELECT permissao FROM usuarios WHERE email = ? AND senha_hash = ?")) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("permissao");
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de banco de dados durante o login: " + e.getMessage());
        }
        return null;
    }
}