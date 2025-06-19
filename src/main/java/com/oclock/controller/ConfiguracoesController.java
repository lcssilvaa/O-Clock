package com.oclock.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import com.oclock.model.ScreenManager;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ConfiguracoesController implements Initializable {

    // --- Elementos FXML injetados ---
    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private AnchorPane sidebarAdminPane;
    @FXML
    private ImageView botaoMenu;

    // --- Variáveis de estado do controlador ---
    private String userEmail, userPermission;
    private boolean isSidebarOpen = false;

    // --- Transições de animação para o sidebar ---
    private TranslateTransition slideInAdmin;
    private TranslateTransition slideOutAdmin;
    private TranslateTransition slideInUser;
    private TranslateTransition slideOutUser;

    /**
     * Construtor padrão da classe ConfiguracoesController.
     */
    public ConfiguracoesController() {
        // Nada específico para inicializar aqui além do padrão
    }

    /**
     * Recebe e inicializa os dados do usuário (e-mail e permissão) passados de outra tela.
     * Também chama o método para atualizar a visibilidade do sidebar.
     * @param email O e-mail do usuário logado.
     * @param role A permissão (função) do usuário logado (ex: "admin", "usuario").
     */
    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("E-mail: " + userEmail + ", Permissão: " + userPermission);
        updateSidebarVisibility();
    }

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * Configura o estado inicial dos painéis do sidebar e overlay, e as transições de animação.
     * Também configura os listeners de eventos.
     * @param url A localização usada para resolver caminhos relativos para o objeto raiz, ou null se a localização não for conhecida.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ConfiguracoesController: Inicializado.");

        // Configuração inicial do overlayPane
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
        }

        // Inicializa sidebarPane (usuário) e suas transições
        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
            slideInUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
            slideInUser.setToX(0);
            slideOutUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
            slideOutUser.setToX(-sidebarPane.getPrefWidth());
        } else {
            System.err.println("ERRO: sidebarPane é NULO. Verifique o fx:id no FXML.");
        }

        // Inicializa sidebarAdminPane (administrador) e suas transições
        if (sidebarAdminPane != null) {
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
            slideInAdmin = new TranslateTransition(Duration.seconds(0.3), sidebarAdminPane);
            slideInAdmin.setToX(0);
            slideOutAdmin = new TranslateTransition(Duration.seconds(0.3), sidebarAdminPane);
            slideOutAdmin.setToX(-sidebarAdminPane.getPrefWidth());
        } else {
            System.err.println("ERRO: sidebarAdminPane é NULO. Verifique o fx:id no FXML.");
        }

        // Define o handler para o botão de menu
        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        } else {
            System.err.println("ERRO: botaoMenu é NULO. Verifique o fx:id no FXML.");
        }
    }

    // --- Métodos de Controle do Sidebar ---

    /**
     * Atualiza a visibilidade e o gerenciamento de espaço dos sidebars com base na permissão do usuário.
     * O sidebar de admin é mostrado se o usuário for admin, caso contrário, o sidebar padrão é mostrado.
     */
    private void updateSidebarVisibility() {
        if (sidebarPane == null || sidebarAdminPane == null) {
            System.err.println("Erro: Painéis do sidebar FXML não injetados em ConfiguracoesController.");
            return;
        }

        if (userPermission == null) {
            System.err.println("Erro: Permissão do usuário não definida em ConfiguracoesController para configurar sidebar.");
            // Default para usuário comum se a permissão não for definida
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
            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
        } else {
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
            sidebarPane.setVisible(true);
            sidebarPane.setManaged(true);
        }
    }

    /**
     * Retorna o AnchorPane do sidebar ativo com base na permissão do usuário.
     * @return O AnchorPane do sidebar de admin se o usuário for admin, caso contrário, o sidebar padrão.
     */
    private AnchorPane getActiveSidebarPane() {
        return "ADMIN".equalsIgnoreCase(userPermission) ? sidebarAdminPane : sidebarPane;
    }

    /**
     * Alterna a visibilidade e animação de deslizamento do sidebar e o fade do overlay.
     * Se o sidebar estiver aberto, ele fecha; se estiver fechado, ele abre.
     */
    private void toggleSidebar() {
        if (overlayPane == null) {
            System.err.println("OverlayPane não está injetado. Verifique o FXML.");
            return;
        }

        AnchorPane activeSidebar = getActiveSidebarPane();
        TranslateTransition currentSlideTransition = null;

        if (activeSidebar == null) {
            System.err.println("ERRO: activeSidebar é NULO. Permissão ou FXML inválido.");
            return;
        }

        if (isSidebarOpen) { // Se o sidebar está aberto, fecha
            currentSlideTransition = "admin".equalsIgnoreCase(userPermission) ? slideOutAdmin : slideOutUser;

            if (currentSlideTransition != null) {
                currentSlideTransition.play();
                currentSlideTransition.setOnFinished(e -> {
                    activeSidebar.setVisible(false);
                    activeSidebar.setManaged(false);
                });
            }

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeOut.setFromValue(overlayPane.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.play();
            fadeOut.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
            });

        } else { // Se o sidebar está fechado, abre
            overlayPane.toFront();
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeIn.setFromValue(overlayPane.getOpacity());
            fadeIn.setToValue(0.4);
            fadeIn.play();

            currentSlideTransition = "admin".equalsIgnoreCase(userPermission) ? slideInAdmin : slideInUser;

            if (activeSidebar != null && currentSlideTransition != null) {
                activeSidebar.toFront();
                activeSidebar.setVisible(true);
                activeSidebar.setManaged(true);
                currentSlideTransition.play();
            }
        }
        isSidebarOpen = !isSidebarOpen;
    }

    /**
     * Lida com o evento de clique no botão de menu (geralmente um ícone).
     * Chama o método para alternar o estado do sidebar (abrir/fechar).
     * @param event O evento de clique do mouse.
     */
    @FXML
    private void handleMenuButtonClick(MouseEvent event) {
        System.out.println("handleMenuButtonClick: Botão de menu clicado em Configurações.");
        toggleSidebar();
    }

    /**
     * Lida com o evento de clique no overlayPane (a área escurecida da tela).
     * Fecha o sidebar se ele estiver aberto.
     * @param event O evento de clique do mouse.
     */
    private void handleOverlayClick(MouseEvent event) {
        if (isSidebarOpen) {
            toggleSidebar();
        }
    }

    /**
     * Método auxiliar que verifica se o sidebar está aberto e o fecha.
     * Utilizado antes de navegar para uma nova tela para garantir uma transição suave.
     */
    private void closeSidebarIfOpen() {
        if (isSidebarOpen) {
            toggleSidebar();
        }
    }

    // --- Métodos de Navegação (Menu Lateral/Sidebar) ---

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        System.out.println("Clicou em Registrar Marcação.");
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        System.out.println("Clicou em Consultar Horas.");
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "RegistrosPonto.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        System.out.println("Clicou em Configurações (já nesta tela).");
        closeSidebarIfOpen();
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        System.out.println("Clicou em Gestão de Usuários.");
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "GestaoUsuarios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        System.out.println("Clicou em Cadastro.");
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "TelaCadastro.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        System.out.println("Clicou em Relatório.");
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleSair(ActionEvent event) {
        System.out.println("Clicou em Sair.");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Você está prestes a sair.");
        alert.setContentText("Deseja realmente sair da aplicação?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ScreenManager.loadScreen((Node) event.getSource(), "TelaLogin.fxml", null, null);
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
        }
    }
}