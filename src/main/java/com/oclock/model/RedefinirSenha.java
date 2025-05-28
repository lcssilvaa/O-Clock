package com.oclock.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.oclock.model.Criptografia;

public class RedefinirSenha {

  
    public boolean atualizarSenha(String email, String novaSenhaPura) {
        Criptografia crip = new Criptografia();
        String novaSenhaHash = crip.gerarHash(novaSenhaPura);

        String url = "jdbc:mysql://localhost:3306/OnClock";
        String usuarioDB = "root";
        String senhaDB = "";

        Connection con = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        System.out.println("DEBUG: Tentando atualizar senha para o email: " + email);
        System.out.println("DEBUG: Hash da nova senha gerado: " + novaSenhaHash);

        try {
            con = DriverManager.getConnection(url, usuarioDB, senhaDB);
            System.out.println("DEBUG: Conexão com o banco estabelecida.");

            String sql = "UPDATE usuarios SET senha = ? WHERE email = ?";
            stmt = con.prepareStatement(sql);

            stmt.setString(1, novaSenhaHash);
            stmt.setString(2, email);

            int linhasAfetadas = stmt.executeUpdate();
            System.out.println("DEBUG: Linhas afetadas pelo UPDATE: " + linhasAfetadas); 

            if (linhasAfetadas > 0) {
                System.out.println("DEBUG: Senha do usuário " + email + " atualizada com sucesso!");
                sucesso = true;
            } else {
                System.out.println("DEBUG: Falha ao atualizar senha do usuário " + email + ". Email pode não existir ou dados iguais.");
                
            }

        } catch (SQLException e) {
            System.err.println("ERRO SQL: Erro ao conectar ou executar comando no banco ao atualizar senha: " + e.getMessage());
            e.printStackTrace();
            sucesso = false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();
                System.out.println("DEBUG: Recursos do banco fechados.");
            } catch (SQLException e) {
                System.err.println("ERRO: Erro ao fechar recursos de atualização de senha: " + e.getMessage());
            }
        }
        return sucesso;
    }
}