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
    public Map<LocalDate, List<LocalDateTime>> getMarcacoesPorUsuarioComFiltro(
        String email, LocalDate startDate, LocalDate endDate) {

        // Para simulação, vamos usar os dados mockados existentes e aplicar o filtro
        // Em uma aplicação real, você faria uma consulta SQL com BETWEEN ou WHERE data >= ? AND data <= ?
        Map<LocalDate, List<LocalDateTime>> allMarcacoes = getMarcacoesPorUsuario(email); // Pega todos os dados mockados

        if (startDate == null && endDate == null) {
            return allMarcacoes; // Nenhum filtro aplicado, retorna todos
        }

        Map<LocalDate, List<LocalDateTime>> filteredMarcacoes = new java.util.HashMap<>();

        for (Map.Entry<LocalDate, List<LocalDateTime>> entry : allMarcacoes.entrySet()) {
            LocalDate date = entry.getKey();

            boolean matchesFilter = true;
            if (startDate != null && date.isBefore(startDate)) {
                matchesFilter = false;
            }
            if (endDate != null && date.isAfter(endDate)) {
                matchesFilter = false;
            }

            if (matchesFilter) {
                // Adiciona apenas as marcações que correspondem ao filtro
                filteredMarcacoes.put(date, entry.getValue());
            }
        }
        return filteredMarcacoes;
    }

    // Mantenha o método original para buscar todas as marcações, usado acima
    public Map<LocalDate, List<LocalDateTime>> getMarcacoesPorUsuario(String email) {
        // Seus dados mockados (ou chamada real ao banco)
        Map<LocalDate, List<LocalDateTime>> mockMarcacoes = new java.util.HashMap<>();
        // Adicione mais dados mockados para testar o filtro
        mockMarcacoes.put(LocalDate.now(), new java.util.ArrayList<>(List.of(
            LocalDateTime.now().withHour(8).withMinute(0).withSecond(0),
            LocalDateTime.now().withHour(12).withMinute(0).withSecond(0),
            LocalDateTime.now().withHour(13).withMinute(0).withSecond(0),
            LocalDateTime.now().withHour(17).withMinute(0).withSecond(0)
        )));
        mockMarcacoes.put(LocalDate.now().minusDays(1), new java.util.ArrayList<>(List.of(
            LocalDateTime.now().minusDays(1).withHour(9).withMinute(0).withSecond(0),
            LocalDateTime.now().minusDays(1).withHour(13).withMinute(0).withSecond(0),
            LocalDateTime.now().minusDays(1).withHour(14).withMinute(0).withSecond(0)
        )));
        mockMarcacoes.put(LocalDate.now().minusDays(5), new java.util.ArrayList<>(List.of(
            LocalDateTime.now().minusDays(5).withHour(8).withMinute(30).withSecond(0),
            LocalDateTime.now().minusDays(5).withHour(12).withMinute(30).withSecond(0)
        )));
        mockMarcacoes.put(LocalDate.now().minusDays(10), new java.util.ArrayList<>(List.of(
            LocalDateTime.now().minusDays(10).withHour(7).withMinute(0).withSecond(0),
            LocalDateTime.now().minusDays(10).withHour(16).withMinute(0).withSecond(0)
        )));
        mockMarcacoes.put(LocalDate.now().minusMonths(1), new java.util.ArrayList<>(List.of(
            LocalDateTime.now().minusMonths(1).withHour(9).withMinute(0).withSecond(0),
            LocalDateTime.now().minusMonths(1).withHour(17).withMinute(0).withSecond(0)
        )));
        mockMarcacoes.put(LocalDate.now().minusMonths(2), new java.util.ArrayList<>(List.of(
            LocalDateTime.now().minusMonths(2).withHour(8).withMinute(0).withSecond(0),
            LocalDateTime.now().minusMonths(2).withHour(12).withMinute(0).withSecond(0),
            LocalDateTime.now().minusMonths(2).withHour(13).withMinute(0).withSecond(0),
            LocalDateTime.now().minusMonths(2).withHour(17).withMinute(0).withSecond(0)
        )));
        return mockMarcacoes;
    }
}
