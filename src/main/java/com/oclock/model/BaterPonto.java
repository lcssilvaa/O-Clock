package com.oclock.model;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection; // Ainda necessário para buscar o ID do usuário
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.oclock.dao.Conexao; // Ainda necessário para buscar o ID do usuário

public class BaterPonto {

    // URL base da sua API Spring Boot
    private static final String API_BASE_URL = "http://localhost:8080/api/ponto";

    public boolean baterPonto(String userEmail) {
        // --- 1. Obter o idUsuario (essa parte ainda precisa do banco de dados) ---
        int idUsuario = -1;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = Conexao.conectar();
            String selectUserIdSql = "SELECT id_usuario FROM USUARIOS WHERE email = ?";
            stmt = con.prepareStatement(selectUserIdSql);
            stmt.setString(1, userEmail);
            rs = stmt.executeQuery();

            if (rs.next()) {
                idUsuario = rs.getInt("id_usuario");
            } else {
                System.err.println("Erro: Usuário com email " + userEmail + " não encontrado no banco de dados local.");
                return false; // Não pode bater ponto sem um ID de usuário válido
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL ao buscar ID do usuário: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Fechar recursos do JDBC
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão com o banco (busca de ID): " + e.getMessage());
            }
        }

        // Se o idUsuario não foi encontrado, encerra
        if (idUsuario == -1) {
            return false;
        }

        // Chamar a API Spring Boot para registrar o ponto ---
        LocalDateTime ponto = LocalDateTime.now();
        DateTimeFormatter apiFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dataHoraFormatadaParaAPI = ponto.format(apiFormatter);

        // O corpo da requisição JSON que a API espera
        String jsonInputString = "{\"dataHoraRegistro\": \"" + dataHoraFormatadaParaAPI + "\"}";

        HttpURLConnection conn = null;
        boolean sucessoAPI = false;

        try {
            // Constrói a URL para o endpoint de bater ponto da API
            URL url = new URL(API_BASE_URL + "/bater/" + idUsuario);
            conn = (HttpURLConnection) url.openConnection();

            // Configura a requisição POST
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json"); // Espera uma resposta JSON
            conn.setDoOutput(true); // Indica que vamos enviar um corpo na requisição

            // Envia o corpo da requisição
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lê a resposta da API
            int responseCode = conn.getResponseCode();
            System.out.println("DEBUG - Código de Resposta da API: " + responseCode);

            if (responseCode >= 200 && responseCode < 300) { // Códigos 2xx indicam sucesso
                System.out.println("Ponto registrado via API com sucesso para ID: " + idUsuario + ", Hora: " + dataHoraFormatadaParaAPI);
                sucessoAPI = true;
               
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine = null;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    System.out.println("DEBUG - Resposta da API: " + response.toString());
                }
            } else {
                // Erro da API
                System.err.println("Falha ao registrar ponto via API. Código: " + responseCode);
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine = null;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    System.err.println("DEBUG - Detalhes do erro da API: " + errorResponse.toString());
                }
                sucessoAPI = false;
            }

        } catch (Exception e) {
            System.err.println("Exceção ao tentar se conectar ou comunicar com a API: " + e.getMessage());
            e.printStackTrace();
            sucessoAPI = false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return sucessoAPI;
    }
}