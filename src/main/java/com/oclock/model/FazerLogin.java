package com.oclock.model;

import java.sql.*;

public class FazerLogin {


    public String fazerLoginWithCredentials(String email, String senha) {

        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/OnClock", "root", "");
             PreparedStatement stmt = con.prepareStatement("SELECT permissao FROM usuarios WHERE email = ? AND senha_hash = ?")) {

            stmt.setString(1, email); 
            stmt.setString(2, senha);

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