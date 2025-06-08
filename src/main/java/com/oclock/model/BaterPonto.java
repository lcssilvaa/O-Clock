package com.oclock.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.oclock.dao.Conexao;

public class BaterPonto {

    public boolean baterPonto(String userEmail) {
        LocalDateTime ponto = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String pontoFormatado = ponto.format(formatter);

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean sucesso = false;
        int idUsuario = -1;

        try {
            con = Conexao.conectar();

            String selectUserIdSql = "SELECT id_usuario FROM USUARIOS WHERE email = ?";
            stmt = con.prepareStatement(selectUserIdSql);
            stmt.setString(1, userEmail);
            rs = stmt.executeQuery();

            if (rs.next()) {
                idUsuario = rs.getInt("id_usuario");
            } else {
                System.out.println("Erro: Usuário com email " + userEmail + " não encontrado.");
                return false;
            }
            rs.close();
            stmt.close();

            String insertPointSql = "INSERT INTO REGISTROS_PONTO (id_usuario, data_hora_registro) VALUES (?, ?)";
            stmt = con.prepareStatement(insertPointSql);
            
            stmt.setInt(1, idUsuario);
            stmt.setTimestamp(2, Timestamp.valueOf(ponto));
            
            int linhas = stmt.executeUpdate();

            if (linhas > 0) {
                System.out.println("Ponto registrado com sucesso para " + userEmail + ": " + pontoFormatado);
                sucesso = true;
            } else {
                System.out.println("Falha ao registrar ponto para " + userEmail + ".");
                sucesso = false;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao conectar ou executar comando no banco: " + e.getMessage());
            e.printStackTrace();
            sucesso = false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.out.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
        return sucesso;
    }
}