package com.oclock.controller;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.oclock.model.BaterPonto;

public class MenuUserController implements Initializable {

    @FXML
    private Label relogioLabel, mensagemPonto;

    @FXML
    private ImageView baterPonto, botaoMenu;
    
    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
   
    private Timeline timeline;
    private String emailUsuarioLogado;
    private BaterPonto baterPontoService = new BaterPonto();

    private boolean sidebarVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        overlayPane.setVisible(false);
        overlayPane.setOpacity(0.0);

        sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());

        botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);

        overlayPane.setOnMouseClicked(this::handleOverlayClick);
    	
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
    
    public void setUserEmail(String useremail) {
        this.emailUsuarioLogado = useremail;
        System.out.println("Email do usuário logado recebido no MenuUserController: " + emailUsuarioLogado);
    }

    private void handleMenuButtonClick(MouseEvent event) {
    	
    	System.out.println("handleMenuButtonClick: Botão de menu clicado!");
        overlayPane.setVisible(true);
        FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), overlayPane);
        fadeTransition1.setFromValue(overlayPane.getOpacity());
        fadeTransition1.setToValue(0.4);
        fadeTransition1.play();

        TranslateTransition translateTransition1 = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
        translateTransition1.setToX(0);
        translateTransition1.play();
        sidebarVisible = true;
    }

    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }
    
    private void closeSidebar() {
        FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), overlayPane);
        fadeTransition1.setFromValue(overlayPane.getOpacity());
        fadeTransition1.setToValue(0);
        fadeTransition1.play();
        fadeTransition1.setOnFinished(event1 -> {
            overlayPane.setVisible(false);
        });

        TranslateTransition translateTransition1 = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
        translateTransition1.setToX(-sidebarPane.getPrefWidth());
        translateTransition1.play();
        sidebarVisible = false;
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
            System.out.println("Chamando baterPontoService.baterPonto para: " + emailUsuarioLogado);
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

    @FXML
    private void SairButtonAction(ActionEvent event) {
        System.out.println("Clicou em Sair");
        closeSidebar();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/TelaLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Login");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar TelaLogin.fxml ao sair: " + e.getMessage());
            e.printStackTrace();
        }
    }
@FXML
private void ListarHorasButtonAction(ActionEvent event) {
    System.out.println("Clicou em Registro de Ponto");
    closeSidebar();

    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/RegistrosPonto.fxml"));
        Parent root = loader.load();


        HorasTrabalhadasController registroController = loader.getController();
        if (registroController != null) {
            registroController.initData(emailUsuarioLogado);
        } else {
            System.err.println("Erro: Controlador da RegistroPonto.fxml não encontrado.");
        }


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("OnClock - Registro de Ponto");
        stage.show();

    } catch (IOException e) {
        System.err.println("Erro ao carregar RegistroPonto.fxml: " + e.getMessage());
        e.printStackTrace();
    }
  }
}