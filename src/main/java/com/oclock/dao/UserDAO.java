package com.oclock.dao;

import com.oclock.model.User; 
import com.oclock.dao.Conexao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // Método para buscar um usuário pelo email (usado no login)
    public User getUserByEmail(String email) throws SQLException {
        String sql = "SELECT id_usuario, email, senha_hash, nome_completo, cpf, permissao, ativo, data_criacao, data_atualizacao FROM usuarios WHERE email = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id_usuario"),
                    rs.getString("email"),
                    rs.getString("senha_hash"),
                    rs.getString("nome_completo"),
                    rs.getString("cpf"),
                    rs.getString("permissao"),
                    rs.getBoolean("ativo"), 
                    rs.getTimestamp("data_criacao").toLocalDateTime(),
                    rs.getTimestamp("data_atualizacao").toLocalDateTime()
                );
            }
        }
        return null;
    }

    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO usuarios (email, senha_hash, nome_completo, cpf, permissao, ativo, data_criacao, data_atualizacao) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getCpf());
            stmt.setString(5, user.getPermission());
            stmt.setBoolean(6, user.isActive()); 
            stmt.executeUpdate();
        }
    }

    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE usuarios SET email = ?, senha_hash = ?, nome_completo = ?, cpf = ?, permissao = ?, ativo = ?, data_atualizacao = NOW() WHERE id_usuario = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash()); 
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getCpf());
            stmt.setString(5, user.getPermission());
            stmt.setBoolean(6, user.isActive());
            stmt.setInt(7, user.getId());
            stmt.executeUpdate();
        }
    }
    
    public void updateUserDetails(User user) throws SQLException {
        String sql = "UPDATE usuarios SET email = ?, nome_completo = ?, cpf = ?, permissao = ?, ativo = ?, data_atualizacao = NOW() WHERE id_usuario = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getFullName());
            stmt.setString(3, user.getCpf());
            stmt.setString(4, user.getPermission());
            stmt.setBoolean(5, user.isActive());
            stmt.setInt(6, user.getId());
            stmt.executeUpdate();
        }
    }


    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }


    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id_usuario, email, senha_hash, nome_completo, cpf, permissao, ativo, data_criacao, data_atualizacao FROM usuarios";
        try (Connection conn = Conexao.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id_usuario"),
                    rs.getString("email"),
                    rs.getString("senha_hash"),
                    rs.getString("nome_completo"),
                    rs.getString("cpf"),
                    rs.getString("permissao"),
                    rs.getBoolean("ativo"),
                    rs.getTimestamp("data_criacao").toLocalDateTime(),
                    rs.getTimestamp("data_atualizacao").toLocalDateTime()
                ));
            }
        }
        return users;
    }
    
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT id_usuario, email, senha_hash, nome_completo, cpf, permissao, ativo, data_criacao, data_atualizacao FROM usuarios WHERE id_usuario = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                    rs.getInt("id_usuario"),
                    rs.getString("email"),
                    rs.getString("senha_hash"),
                    rs.getString("nome_completo"),
                    rs.getString("cpf"),
                    rs.getString("permissao"),
                    rs.getBoolean("ativo"),
                    rs.getTimestamp("data_criacao").toLocalDateTime(),
                    rs.getTimestamp("data_atualizacao").toLocalDateTime()
                );
            }
        }
        return null;
    }
}