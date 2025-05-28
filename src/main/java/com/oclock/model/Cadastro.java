package com.oclock.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
// import java.util.Scanner; // Remove Scanner

public class Cadastro {


    public void cadastrarUsuario(String nome, String senha, String email, String permiss) throws SQLException { 
        Criptografia crip = new Criptografia(); 
        String senhaHash = crip.gerarHash(senha); 

    
        try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/OnClock", "root", ""); 
             PreparedStatement stmt = con.prepareStatement("INSERT INTO usuarios (nome, senha, email, permissao) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, nome); 
            stmt.setString(2, senhaHash); 
            stmt.setString(3, email); 
            stmt.setString(4, permiss); 

            stmt.executeUpdate(); 
            
        } catch (SQLException e) { 
            
            throw new SQLException("Erro ao cadastrar: " + e.getMessage(), e); 
        }
    }
}