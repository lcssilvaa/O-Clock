package com.oclock.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class BaterPonto {

    
    public boolean baterPonto(String emailUsuarioLogado) {
        LocalDateTime ponto = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String pontoFormatado = ponto.format(formatter);

        String url = "jdbc:mysql://localhost:3306/OnClock";
        String usuario = "root";
        String senha = "";

        Connection con = null;
        PreparedStatement stmt = null;
        boolean sucesso = false;

        try {
            con = DriverManager.getConnection(url, usuario, senha);
            String sql = "INSERT INTO registros_ponto (user_email, data_hora) VALUES (?, ?)";
            stmt = con.prepareStatement(sql);
            
            stmt.setString(1, emailUsuarioLogado);
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(ponto));
            
            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Ponto registrado com sucesso: " + pontoFormatado);
                sucesso = true;
            } else {
                System.out.println("Falha ao registrar ponto.");
                sucesso = false;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao conectar ou executar comando no banco: " + e.getMessage());
            sucesso = false;
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
        return sucesso;
    }
}