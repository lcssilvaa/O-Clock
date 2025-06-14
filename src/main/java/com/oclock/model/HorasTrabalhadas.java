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


    // Método existente: Busca e calcula horas trabalhadas PARA UM USUÁRIO ESPECÍFICO
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

            // Passa o email do usuário no construtor
            registrosDiarios.add(new RegistroDiario(data, entrada.toLocalTime(), saida != null ? saida.toLocalTime() : null, horasTrabalhadasFormatadas, email));
        }

        return registrosDiarios;
    }


    // NOVO MÉTODO: Busca e calcula horas trabalhadas PARA TODOS OS USUÁRIOS (ADMIN)
    public List<RegistroDiario> buscarTodosRegistrosDiarios() {
        List<RegistroDiario> todosRegistrosDiarios = new ArrayList<>();
        Map<String, Map<LocalDate, List<LocalDateTime>>> pontosPorUsuarioDia = new TreeMap<>(); // Usuario -> Data -> Pontos

        String selectAllPointsSql = "SELECT u.email, rp.data_hora_registro " +
                                    "FROM REGISTROS_PONTO rp " +
                                    "JOIN USUARIOS u ON rp.id_usuario = u.id_usuario " +
                                    "ORDER BY u.email, rp.data_hora_registro ASC";

        try (Connection conn = Conexao.conectar();
             PreparedStatement pstmt = conn.prepareStatement(selectAllPointsSql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String email = rs.getString("email");
                LocalDateTime dataHora = rs.getTimestamp("data_hora_registro").toLocalDateTime();

                pontosPorUsuarioDia
                    .computeIfAbsent(email, k -> new TreeMap<>())
                    .computeIfAbsent(dataHora.toLocalDate(), k -> new ArrayList<>())
                    .add(dataHora);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar todos os registros de ponto: " + e.getMessage());
            e.printStackTrace();
            return todosRegistrosDiarios;
        }

        // Processar os pontos por usuário e por dia
        for (Map.Entry<String, Map<LocalDate, List<LocalDateTime>>> userEntry : pontosPorUsuarioDia.entrySet()) {
            String emailUsuario = userEntry.getKey();
            Map<LocalDate, List<LocalDateTime>> pontosDoUsuario = userEntry.getValue();

            for (Map.Entry<LocalDate, List<LocalDateTime>> dayEntry : pontosDoUsuario.entrySet()) {
                LocalDate data = dayEntry.getKey();
                List<LocalDateTime> pontosDoDia = dayEntry.getValue();

                pontosDoDia.sort(Comparator.naturalOrder()); // Garantir ordenação

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

                // Passa o email do usuário no construtor
                todosRegistrosDiarios.add(new RegistroDiario(data, entrada.toLocalTime(), saida != null ? saida.toLocalTime() : null, horasTrabalhadasFormatadas, emailUsuario));
            }
        }

        return todosRegistrosDiarios;
    }


    // --- CLASSE INTERNA para representar um registro diário agregado ---
    public static class RegistroDiario {
        private LocalDate data;
        private java.time.LocalTime entrada; // Alterado para LocalTime
        private java.time.LocalTime saida; // Alterado para LocalTime
        private String horasTrabalhadas;
        private String emailUsuario; // NOVO CAMPO

        public RegistroDiario(LocalDate data, java.time.LocalTime entrada, java.time.LocalTime saida, String horasTrabalhadas, String emailUsuario) {
            this.data = data;
            this.entrada = entrada;
            this.saida = saida;
            this.horasTrabalhadas = horasTrabalhadas;
            this.emailUsuario = emailUsuario; // Inicializa o novo campo
        }

        public LocalDate getData() { return data; }
        public java.time.LocalTime getEntrada() { return entrada; }
        public java.time.LocalTime getSaida() { return saida; }
        public String getHorasTrabalhadas() { return horasTrabalhadas; }
        public String getEmailUsuario() { return emailUsuario; } // NOVO GETTER

        // Getters formatados para exibição (se ainda precisar deles para TableView direto)
        public String getEntradaFormatada() { return entrada != null ? entrada.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-"; }
        public String getSaidaFormatada() { return saida != null ? saida.format(DateTimeFormatter.ofPattern("HH:mm:ss")) : "-"; }
    }
}