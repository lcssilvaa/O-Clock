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
    private AnchorPane sidebarAdminPane; 
    @FXML
    private ImageView botaoMenu;

    private String userEmail, userPermission;
    private boolean sidebarVisible = false;
    private String emailUsuarioLogado;

    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("E-mail: " + userEmail + ", Permissão: " + userPermission);
        
        updateSidebarVisibility(); 
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ConfiguraçõesController: Inicializado.");
        
        botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);

        overlayPane.setOnMouseClicked(this::handleOverlayClick);

        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
        }

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

        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        }
        if (overlayPane != null) {
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        }
    }

    private void updateSidebarVisibility() {
        
        boolean isAdmin = "admin".equalsIgnoreCase(userPermission);

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
        
        return "admin".equalsIgnoreCase(userPermission) ? sidebarAdminPane : sidebarPane;
    }
        
    private void handleMenuButtonClick(MouseEvent event) {
    	System.out.println("handleMenuButtonClick: Botão de menu clicado em HorasTrabalhadas!");
        AnchorPane activeSidebar = getActiveSidebarPane();

        if (overlayPane != null) {
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false);
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.4);
            fadeTransition.play();
        }

        if (activeSidebar != null) { 
            
            activeSidebar.setVisible(true);
            activeSidebar.setManaged(true); 
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(0);
            translateTransition.play();
        }
        sidebarVisible = true;
    }

    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

    private void closeSidebar() {
    	if (overlayPane != null) {
            
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
                controller.initData(userEmail, userPermission);
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
                controller.initData(userEmail, userPermission);
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

@FXML
private void handleGestao(ActionEvent event) {
	System.out.println("Teste");
	closeSidebar();
	}

@FXML
private void handleCadastrar(ActionEvent event) {
	System.out.println("Clicou em Cadastro");
    closeSidebar();
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/TelaCadastro.fxml"));
        Parent root = loader.load();

        CadastroController controller = loader.getController();
        if (controller != null) {
            controller.initData(userEmail, userPermission);
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("OnClock - Cadastro de Usuários");
        stage.show();
    } catch (IOException e) {
        System.err.println("Erro ao carregar TelaCadastro.fxml para registrar marcação: " + e.getMessage());
        e.printStackTrace();
    }
}

@FXML
private void handleRelatorio(ActionEvent event) {
	System.out.println("Teste");
	closeSidebar();
	}
}
