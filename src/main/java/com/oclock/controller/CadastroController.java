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

public class CadastroController implements Initializable {

    @FXML
    private Label relogioLabel, mensagemPonto;

    @FXML
    private ImageView baterPonto, botaoMenu;

    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private AnchorPane sidebarAdminPane;

    private Timeline timeline;
    private String userEmail, userPermission;
    private BaterPonto baterPontoService = new BaterPonto();

    private boolean sidebarVisible = false;

    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("E-mail: " + userEmail + ", Permissão: " + userPermission);
        updateSidebarVisibility();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        overlayPane.setVisible(false);
        overlayPane.setOpacity(0.0);

        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
        }
        if (sidebarAdminPane != null) {
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
        }

        botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        overlayPane.setOnMouseClicked(this::handleOverlayClick);
    }

    private void updateSidebarVisibility() {
        if (sidebarPane == null || sidebarAdminPane == null) {
            System.err.println("Erro: Painéis do sidebar FXML não injetados em MenuUserController.");
            return;
        }

        if (userPermission == null) {
            System.err.println("Erro: Permissão do usuário não definida em MenuUserController para configurar sidebar.");
            sidebarPane.setVisible(true);
            sidebarPane.setManaged(true);
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
            return;
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userPermission);

        if (isAdmin) {
            sidebarAdminPane.setVisible(true);
            sidebarAdminPane.setManaged(true);
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());

            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
        } else {
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);

            sidebarPane.setVisible(true);
            sidebarPane.setManaged(true);
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
        }
    }

    private AnchorPane getActiveSidebarPane() {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userPermission);
        return isAdmin ? sidebarAdminPane : sidebarPane;
    }

    private void handleMenuButtonClick(MouseEvent event) {
        System.out.println("handleMenuButtonClick: Botão de menu clicado!");
        AnchorPane activeSidebar = getActiveSidebarPane();

        overlayPane.setVisible(true);
        FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), overlayPane);
        fadeTransition1.setFromValue(overlayPane.getOpacity());
        fadeTransition1.setToValue(0.4);
        fadeTransition1.play();

        if (activeSidebar != null) {
            activeSidebar.setVisible(true);
            activeSidebar.setManaged(true);
            TranslateTransition translateTransition1 = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition1.setToX(0);
            translateTransition1.play();
        }
        sidebarVisible = true;
    }

    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

    private void closeSidebar() {
        AnchorPane activeSidebar = getActiveSidebarPane();

        FadeTransition fadeTransition1 = new FadeTransition(Duration.seconds(0.3), overlayPane);
        fadeTransition1.setFromValue(overlayPane.getOpacity());
        fadeTransition1.setToValue(0);
        fadeTransition1.play();
        fadeTransition1.setOnFinished(event1 -> {
            overlayPane.setVisible(false);
        });

        if (activeSidebar != null) {
            TranslateTransition translateTransition1 = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition1.setToX(-activeSidebar.getPrefWidth());
            translateTransition1.play();
            translateTransition1.setOnFinished(event -> {
                activeSidebar.setVisible(false);
                activeSidebar.setManaged(false);
            });
        }
        sidebarVisible = false;
    }
}