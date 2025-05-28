package com.oclock.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.oclock.model.BaterPonto;

public class MenuAdminController implements Initializable {

    @FXML
    private Label relogioLabel;
    @FXML
    private Label mensagemPonto;

    private Timeline timeline;
    
    @FXML
    private ImageView baterPonto;
    
    private String emailUsuarioLogado;
    
    private BaterPonto baterPontoService = new BaterPonto();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        if (relogioLabel != null) {
            iniciarRelogio();
        }

        
        if (baterPonto != null) {
            baterPonto.setOnMouseClicked(this::MarcarPontoClick);
        }

        if (mensagemPonto != null) {
            mensagemPonto.setVisible(false);
            mensagemPonto.setManaged(false);
        }

       
    }
    
    public void setUserEmail(String adminEmail) {
        this.emailUsuarioLogado = adminEmail;
        System.out.println("Email do usuário logado recebido " + emailUsuarioLogado);
    }

    private void MarcarPontoClick(MouseEvent event) {
        System.out.println("Botão Marcar Ponto clicado dentro do handler!");

        if (mensagemPonto != null) {
            mensagemPonto.setVisible(false);
            mensagemPonto.setManaged(false);
            mensagemPonto.setText("");
        }

        if (emailUsuarioLogado == null || emailUsuarioLogado.isEmpty()) {
            if (mensagemPonto != null) {
                mensagemPonto.setText("Erro: Usuário não identificado. Faça login novamente.");
                mensagemPonto.setStyle("-fx-text-fill: orange;");
                mensagemPonto.setVisible(true);
                mensagemPonto.setManaged(true);
            }
            System.err.println("Erro: Email é nulo ou vazio.");
            return;
        }

        boolean pontoRegistradoComSucesso = false;
        String mensagemFeedback = "";
        String dataHoraAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        try {
            System.out.println("Chamando baterPontoService.baterPonto para: " + emailUsuarioLogado); // Debug antes da chamada
            pontoRegistradoComSucesso = baterPontoService.baterPonto(emailUsuarioLogado);

            if (pontoRegistradoComSucesso) {
                mensagemFeedback = "Ponto marcado com sucesso em: " + dataHoraAtual + " para " + emailUsuarioLogado;
                mensagemPonto.setStyle("-fx-text-fill: green;");
            } else {
                mensagemFeedback = "Falha ao registrar ponto para " + emailUsuarioLogado + ". Verifique o console.";
                mensagemPonto.setStyle("-fx-text-fill: red;");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mensagemFeedback = "Erro inesperado ao marcar ponto: " + e.getMessage();
            mensagemPonto.setStyle("-fx-text-fill: red;");
            pontoRegistradoComSucesso = false;
        }

        if (mensagemPonto != null) {
            mensagemPonto.setText(mensagemFeedback);
            mensagemPonto.setVisible(true);
            mensagemPonto.setManaged(true);

            Timeline hideMessageTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                mensagemPonto.setVisible(false);
                mensagemPonto.setManaged(false);
            }));
            hideMessageTimer.setCycleCount(1);
            hideMessageTimer.play();
        }
    }
    	

    private void iniciarRelogio() {
    	
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            
            LocalTime now = LocalTime.now();
            
            relogioLabel.setText(now.format(formatter));
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        
        timeline.play();
    }

    public void pararRelogio() {
        if (timeline != null) {
            timeline.stop();
        }
    }
}