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
    	if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
            System.out.println("DEBUG: overlayPane configurado.");
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
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
            System.out.println("DEBUG (MenuUserController): sidebarAdminPane inicializado (largura: " + sidebarAdminPane.getPrefWidth() + ").");
        }

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
        System.out.println("DEBUG: É Admin? " + isAdmin);

        if (isAdmin) {
        	System.out.println("DEBUG: Ativando sidebarAdminPane.");
            sidebarAdminPane.setVisible(true);
            sidebarAdminPane.setManaged(true);;

            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
        } else {
        	System.out.println("DEBUG: Ativando sidebarPane.");
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
        }
        System.out.println("DEBUG: updateSidebarVisibility() finalizado.");
    }

    private AnchorPane getActiveSidebarPane() {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userPermission);
        return isAdmin ? sidebarAdminPane : sidebarPane;
    }

    private void handleMenuButtonClick(MouseEvent event) {
    	System.out.println("--- handleMenuButtonClick: INÍCIO ---");
        System.out.println("DEBUG (handleMenuButtonClick): Botão de menu clicado!");
        
        AnchorPane activeSidebar = getActiveSidebarPane();

        if (activeSidebar == null) {
            System.err.println("ERRO (handleMenuButtonClick): activeSidebar é NULO APÓS getActiveSidebarPane(). Abortando animação.");
            return; 
        }

        System.out.println("DEBUG: activeSidebar detectado: " + (activeSidebar == sidebarAdminPane ? "sidebarAdminPane" : "sidebarPane"));
        System.out.println("DEBUG: Propriedades INICIAIS do activeSidebar antes da animação:");
        System.out.println("   Visible: " + activeSidebar.isVisible());
        System.out.println("   Managed: " + activeSidebar.isManaged());
        System.out.println("   TranslateX: " + activeSidebar.getTranslateX());

        if (overlayPane != null) {
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false); 
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.4);
            fadeTransition.play();
            System.out.println("DEBUG (handleMenuButtonClick): Animação do overlay iniciada.");
        } else {
            System.err.println("ERRO (handleMenuButtonClick): overlayPane é NULO. Animação do overlay falhou.");
        }

        activeSidebar.setVisible(true); 
        activeSidebar.setManaged(true);
        System.out.println("DEBUG (handleMenuButtonClick): activeSidebar definido como Visible=true, Managed=true.");

        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
        translateTransition.setToX(0);
        translateTransition.play();
        
        translateTransition.setOnFinished(e -> {
            System.out.println("DEBUG (handleMenuButtonClick): Animação da sidebar CONCLUÍDA.");
            System.out.println("DEBUG (handleMenuButtonClick): Propriedades FINAIS do activeSidebar APÓS ANIMAÇÃO:");
            System.out.println("   Visible: " + activeSidebar.isVisible());
            System.out.println("   Managed: " + activeSidebar.isManaged());
            System.out.println("   TranslateX: " + activeSidebar.getTranslateX());
        });
        
        System.out.println("DEBUG (handleMenuButtonClick): Animação da sidebar INICIADA.");
        sidebarVisible = true;
        System.out.println("--- handleMenuButtonClick: FIM ---");
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

    private void MarcarPontoClick(MouseEvent event) {
        System.out.println("Botão Marcar Ponto clicado dentro do handler!");

        if (mensagemPonto != null) {
            mensagemPonto.setVisible(false);
            mensagemPonto.setManaged(false);
            mensagemPonto.setText("");
        }

        if (userEmail == null || userEmail.isEmpty()) {
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
            System.out.println("Chamando baterPontoService.baterPonto para: " + userEmail);
            pontoRegistradoComSucesso = baterPontoService.baterPonto(userEmail);

            if (pontoRegistradoComSucesso) {
                mensagemFeedback = "Ponto marcado com sucesso em: " + dataHoraAtual + " para " + userEmail;
                mensagemPonto.setStyle("-fx-text-fill: green;");
            } else {
                mensagemFeedback = "Falha ao registrar ponto para " + userEmail + ". Verifique o console.";
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
    private void handleSair(ActionEvent event) {
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
    private void handleConsultarHoras(ActionEvent event) {
        System.out.println("Clicou em Registro de Ponto");
        closeSidebar();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/RegistrosPonto.fxml"));
            Parent root = loader.load();

            HorasTrabalhadasController registroController = loader.getController();
            if (registroController != null) {
                registroController.initData(userEmail, userPermission);
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

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        System.out.println("Clicou em Configurações");
        closeSidebar();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/Configuracoes.fxml"));
            Parent root = loader.load();

            ConfiguracoesController controller = loader.getController();
            if (controller != null) {
                controller.initData(userEmail, userPermission);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Configurações");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar Configurações.fxml para registrar marcação: " + e.getMessage());
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