package com.oclock.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.oclock.dao.Conexao;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class HorasTrabalhadas {
	
	Conexao con = new Conexao();

    public Map<LocalDate, List<LocalDateTime>> getMarcacoesPorUsuario(String emailUsuario) {
        Map<LocalDate, List<LocalDateTime>> marcacoesPorDia = new TreeMap<>(); // TreeMap para manter a ordem das datas
        String sql = "SELECT data_hora FROM registros_ponto WHERE user_email = ? ORDER BY data_hora ASC";

        try (Connection conn = con.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, emailUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDateTime dataHora = rs.getTimestamp("data_hora").toLocalDateTime();
                LocalDate data = dataHora.toLocalDate();

                marcacoesPorDia.computeIfAbsent(data, k -> new ArrayList<>()).add(dataHora);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao buscar marcações de ponto: " + e.getMessage());
            e.printStackTrace();
        }
        return marcacoesPorDia;
    }
}