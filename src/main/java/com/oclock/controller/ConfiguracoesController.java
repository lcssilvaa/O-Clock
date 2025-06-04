package com.oclock.controller;

import javafx.util.Duration;
import java.util.Comparator;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import com.oclock.model.HorasTrabalhadas;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ConfiguracoesController implements Initializable {

  
    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private ImageView botaoMenu;

    private boolean sidebarVisible = false;

    private String emailUsuarioLogado;

    public void initData(String email) {
        this.emailUsuarioLogado = email;
        System.out.println("Email do usuário recebido: " + emailUsuarioLogado);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ConfiguraçõesController: Inicializado.");

        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
        }

        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
        }

        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        }
        if (overlayPane != null) {
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        }
       
    }   
        
    private void handleMenuButtonClick(MouseEvent event) {
    	System.out.println("handleMenuButtonClick: Botão de menu clicado em HorasTrabalhadas!");
        if (overlayPane != null) {
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false); // Torna o overlay interativo para que possa ser clicado
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane); // Define a duração e o nó
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.4);
            fadeTransition.play();
        }

        if (sidebarPane != null) {
            sidebarPane.setMouseTransparent(false); // Torna o sidebar interativo
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), sidebarPane); // Define a duração e o nó
            translateTransition.setToX(0); // Move para a posição visível
            translateTransition.play();
        }
        sidebarVisible = true;
    }

    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

    private void closeSidebar() {
    	if (overlayPane != null) {
            // Use javafx.util.Duration explicitamente aqui
            FadeTransition fadeTransition = new FadeTransition(javafx.util.Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0);
            fadeTransition.play();
            fadeTransition.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
            });
        }

        if (sidebarPane != null) {
            
            TranslateTransition translateTransition = new TranslateTransition(javafx.util.Duration.seconds(0.3), sidebarPane);
            translateTransition.setToX(-sidebarPane.getPrefWidth());
            translateTransition.play();
            translateTransition.setOnFinished(event -> {
                sidebarPane.setMouseTransparent(true);
            });
        }
        sidebarVisible = false;
    }

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
    	System.out.println("Clicou em Consultar Horas (no sidebar da tela de Horas Trabalhadas).");
        closeSidebar();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/RegistrosPonto.fxml"));
            Parent root = loader.load();

            HorasTrabalhadasController controller = loader.getController();
            if (controller != null) {
                controller.initData(emailUsuarioLogado);
            } else {
                System.err.println("Erro: Consultar Horas é nulo ao voltar.");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Horas Trabalhadas");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar MenuUser.fxml para registrar marcação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        System.out.println("Clicou em Registrar Marcação (no sidebar da tela de Horas Trabalhadas).");
        closeSidebar();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/MenuUser.fxml"));
            Parent root = loader.load();

            MenuUserController controller = loader.getController();
            if (controller != null) {
                controller.setUserEmail(emailUsuarioLogado);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Bater Ponto");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar MenuUser.fxml para registrar marcação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
    	System.out.println("Clicou em Configurações (já nesta tela).");
        closeSidebar();
    }
    @FXML
    private void handleSair(ActionEvent event) {
        System.out.println("Clicou em Sair.");
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
}