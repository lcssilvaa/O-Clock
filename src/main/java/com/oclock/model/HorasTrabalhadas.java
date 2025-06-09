package com.oclock.model;

import com.oclock.dao.Conexao; // Sua classe de conexão com o banco

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HorasTrabalhadas {

    private int idRegistro;
    private String emailUsuario; 
    private LocalDateTime dataHoraRegistro;

    public HorasTrabalhadas(int idRegistro, String emailUsuario, LocalDateTime dataHoraRegistro) {
        this.idRegistro = idRegistro;
        this.emailUsuario = emailUsuario;
        this.dataHoraRegistro = dataHoraRegistro;
    }

    public int getIdRegistro() { return idRegistro; }
    public String getEmailUsuario() { return emailUsuario; }
    public LocalDateTime getDataHoraRegistro() { return dataHoraRegistro; }

    public String getDataHoraRegistroFormatado() {
        return dataHoraRegistro != null ? dataHoraRegistro.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) : "";
    }


    // Busca e calcula horas trabalhadas, retornando RegistroDiario ---
    public List<RegistroDiario> buscarRegistrosDiariosPorEmail(String email) {
        List<RegistroDiario> registrosDiarios = new ArrayList<>();
        Map<LocalDate, List<LocalDateTime>> pontosPorDia = new TreeMap<>(); // Usa TreeMap para ordenar por data

        int idUsuario = -1;
        // 1. Obter o id_usuario a partir do email
        String selectUserIdSql = "SELECT id_usuario FROM USUARIOS WHERE email = ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(selectUserIdSql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("id_usuario");
                } else {
                    System.err.println("Usuário não encontrado com o email: " + email + " ao buscar registros.");
                    return registrosDiarios;
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar ID do usuário para buscar registros: " + e.getMessage());
            e.printStackTrace();
            return registrosDiarios;
        }

        if (idUsuario == -1) {
            System.err.println("ID de usuário inválido após consulta para buscar registros.");
            return registrosDiarios;
        }
        
        String selectPointsSql = "SELECT data_hora_registro FROM REGISTROS_PONTO WHERE id_usuario = ? ORDER BY data_hora_registro ASC";
        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(selectPointsSql)) {

            pstmt.setInt(1, idUsuario);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime dataHora = rs.getTimestamp("data_hora_registro").toLocalDateTime();
                    pontosPorDia.computeIfAbsent(dataHora.toLocalDate(), k -> new ArrayList<>()).add(dataHora);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar registros de ponto por ID de usuário: " + e.getMessage());
            e.printStackTrace();
            return registrosDiarios;
        }

        // 3. Processar os pontos por dia para calcular horas
        for (Map.Entry<LocalDate, List<LocalDateTime>> entry : pontosPorDia.entrySet()) {
            LocalDate data = entry.getKey();
            List<LocalDateTime> pontosDoDia = entry.getValue();

            // Garantir que os pontos estejam ordenados (útil se não houver ORDER BY no SQL ou para segurança)
            pontosDoDia.sort(Comparator.naturalOrder());

            if (pontosDoDia.isEmpty()) {
                continue;
            }

            LocalDateTime entrada = null;
            LocalDateTime saida = null;

            if (!pontosDoDia.isEmpty()) {
                entrada = pontosDoDia.get(0); 
            }

            if (pontosDoDia.size() > 1) {
                saida = pontosDoDia.get(pontosDoDia.size() - 1); 
            } else if (pontosDoDia.size() == 1) {
                saida = null; 
            }

            Duration duracaoTrabalhada = Duration.ZERO;
            if (entrada != null && saida != null) {
                duracaoTrabalhada = Duration.between(entrada, saida);
            }
            
            long totalSeconds = duracaoTrabalhada.getSeconds();
            long hours = totalSeconds / 3600;
            long minutes = (totalSeconds % 3600) / 60;
            long seconds = totalSeconds % 60;
            String horasTrabalhadasFormatadas = String.format("%02d:%02d:%02d", hours, minutes, seconds);

            registrosDiarios.add(new RegistroDiario(data, entrada, saida, horasTrabalhadasFormatadas));
        }

        return registrosDiarios;
    }

    // --- CLASSE INTERNA para representar um registro diário agregado ---
    public static class RegistroDiario {
        private LocalDate data;
        private LocalDateTime entrada;
        private LocalDateTime saida;
        private String horasTrabalhadas;

        public RegistroDiario(LocalDate data, LocalDateTime entrada, LocalDateTime saida, String horasTrabalhadas) {
            this.data = data;
            this.entrada = entrada;
            this.saida = saida;
            this.horasTrabalhadas = horasTrabalhadas;
        }

        public LocalDate getData() { return data; }
        public LocalDateTime getEntrada() { return entrada; } // NOVO GETTER
        public LocalDateTime getSaida() { return saida; }     // NOVO GETTER
        public String getHorasTrabalhadas() { return horasTrabalhadas; }

        // Getters formatados para exibição (se ainda precisar deles para TableView direto)
        public String getEntradaFormatada() { return entrada != null ? entrada.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-"; }
        public String getSaidaFormatada() { return saida != null ? saida.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-"; }
    }
}
