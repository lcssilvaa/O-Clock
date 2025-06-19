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
import com.oclock.model.ScreenManager;

public class MenuUserController implements Initializable {

    @FXML
    private Label relogioLabel, mensagemPonto;

    @FXML
    private ImageView baterPonto, botaoMenu;

    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane; // Sidebar para usuários comuns
    @FXML
    private AnchorPane sidebarAdminPane; // Sidebar para administradores

    private Timeline timeline;
    private String userEmail, userPermission;
    private BaterPonto baterPontoService = new BaterPonto();

    // Removido o sidebarVisible, pois a visibilidade é agora controlada pelas propriedades setVisible/setManaged

    /**
     * Inicializa os dados do usuário (email e permissão) passados de outra tela.
     * Atualiza a visibilidade do sidebar com base na permissão.
     * @param email O email do usuário logado.
     * @param role A permissão (função) do usuário logado (ex: "admin", "usuario").
     */
    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("E-mail: " + userEmail + ", Permissão: " + userPermission);
        updateSidebarVisibility(); // Chama após userPermission estar definido
    }

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * Configura o estado inicial dos painéis do sidebar e overlay, o relógio e o botão de bater ponto.
     * @param url A localização usada para resolver caminhos relativos para o objeto raiz, ou null se a localização não for conhecida.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // --- Configuração inicial do overlayPane ---
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setMouseTransparent(true); // *** CRUCIAL: Impede que capture cliques quando invisível ***
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
        }

        // --- Configuração inicial dos sidebars ---
        // Ambos devem estar fora da tela e não gerenciados no início
        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
            sidebarPane.setMouseTransparent(true); // Garante que não capture cliques quando fechado
        } else {
            System.err.println("ERRO: sidebarPane é NULO. Verifique o fx:id no FXML.");
        }

        if (sidebarAdminPane != null) {
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
            sidebarAdminPane.setMouseTransparent(true); // Garante que não capture cliques quando fechado
        } else {
            System.err.println("ERRO: sidebarAdminPane é NULO. Verifique o fx:id no FXML.");
        }

        // --- Configuração do botão de menu ---
        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        } else {
            System.err.println("ERRO: botaoMenu é NULO. Verifique o fx:id no FXML.");
        }

        // --- Outras inicializações ---
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

        // updateSidebarVisibility será chamado por initData quando os dados do usuário estiverem disponíveis.
        // Se este controlador for carregado de alguma forma sem initData (ex: SceneBuilder),
        // ele ainda terá um estado padrão.
    }

    /**
     * Atualiza a visibilidade e o gerenciamento de espaço dos sidebars com base na permissão do usuário.
     * O sidebar de admin é mostrado se o usuário for admin, caso contrário, o sidebar padrão é mostrado.
     */
    private void updateSidebarVisibility() {
        if (sidebarPane == null || sidebarAdminPane == null) {
            System.err.println("Erro: Painéis do sidebar FXML não injetados em MenuUserController.");
            return;
        }

        // Garante que ambos os sidebars estejam inicialmente fora da tela e não gerenciados
        // Re-ajusta a posição para o caso de ter sido aberto antes e fechado de forma abrupta
        sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
        sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());

        // Ambos invisíveis e não gerenciados por padrão
        sidebarAdminPane.setVisible(false);
        sidebarAdminPane.setManaged(false);
        sidebarAdminPane.setMouseTransparent(true); // Certifique-se de que estejam transparentes ao mouse
        sidebarPane.setVisible(false);
        sidebarPane.setManaged(false);
        sidebarPane.setMouseTransparent(true); // Certifique-se de que estejam transparentes ao mouse

        if (userPermission == null || userPermission.isEmpty()) {
            System.err.println("Erro: Permissão do usuário não definida em MenuUserController. Defaulting to usuario.");
            userPermission = "usuario"; // Define um padrão seguro
        }

        // Apenas o sidebar relevante é definido como visível/gerenciado quando necessário (pelo handleMenuButtonClick)
        // Não definimos Visible/Managed aqui, pois isso pode interferir na animação de abertura.
        // A lógica de setVisible/setManaged é tratada em handleMenuButtonClick.
    }

    /**
     * Retorna o AnchorPane do sidebar ativo com base na permissão do usuário.
     * @return O AnchorPane do sidebar de admin se o usuário for admin, caso contrário, o sidebar padrão.
     */
    private AnchorPane getActiveSidebarPane() {
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userPermission);
        return isAdmin ? sidebarAdminPane : sidebarPane;
    }

    /**
     * Lida com o evento de clique no botão de menu (geralmente um ícone).
     * Abre o sidebar com uma animação de deslizamento e escurece o overlay.
     * @param event O evento de clique do mouse.
     */
    private void handleMenuButtonClick(MouseEvent event) {
        AnchorPane activeSidebar = getActiveSidebarPane();

        if (activeSidebar == null) {
            System.err.println("ERRO: activeSidebar é NULO. Abortando animação do menu.");
            return;
        }

        // Torna o overlay visível e pronto para capturar cliques
        if (overlayPane != null) {
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false); // Permite que o overlay capture cliques para fechar o sidebar
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.4);
            fadeTransition.play();
        }

        // Torna o sidebar ativo visível, gerenciado e pronto para capturar cliques (botões internos)
        activeSidebar.setVisible(true);
        activeSidebar.setManaged(true);
        activeSidebar.setMouseTransparent(false); // Permite que os botões dentro do sidebar sejam clicáveis
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
        translateTransition.setToX(0);
        translateTransition.play();

        // Removido o sidebarVisible pois a visibilidade é controlada pelas propriedades do painel.
    }

    /**
     * Lida com o evento de clique no overlayPane (a área escurecida da tela).
     * Fecha o sidebar se ele estiver aberto.
     * @param event O evento de clique do mouse.
     */
    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

    /**
     * Fecha o sidebar ativo com uma animação de deslizamento e clareia o overlay.
     */
    private void closeSidebar() {
        AnchorPane activeSidebar = getActiveSidebarPane();

        if (overlayPane != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0);
            fadeTransition.play();
            fadeTransition.setOnFinished(event1 -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true); // *** CRUCIAL: Libera os cliques para elementos subjacentes ***
            });
        }

        if (activeSidebar != null) {
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(-activeSidebar.getPrefWidth());
            translateTransition.play();
            translateTransition.setOnFinished(event -> {
                activeSidebar.setVisible(false);
                activeSidebar.setManaged(false);
                activeSidebar.setMouseTransparent(true); // *** CRUCIAL: Libera os cliques para elementos subjacentes ***
            });
        }
        // Removido o sidebarVisible.
    }

    /**
     * Lida com o evento de clique no botão "Bater Ponto".
     * Tenta registrar um ponto para o usuário atual e exibe feedback.
     * @param event O evento de clique do mouse.
     */
    private void MarcarPontoClick(MouseEvent event) {
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
            System.err.println("Erro: Email do usuário é nulo ou vazio ao tentar marcar ponto.");
            return;
        }

        boolean pontoRegistradoComSucesso;
        String mensagemFeedback;
        String dataHoraAtual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        try {
            pontoRegistradoComSucesso = baterPontoService.baterPonto(userEmail);

            if (pontoRegistradoComSucesso) {
                mensagemFeedback = "Ponto marcado com sucesso em: " + dataHoraAtual;
                mensagemPonto.setStyle("-fx-text-fill: green;");
            } else {
                mensagemFeedback = "Falha ao registrar ponto. Verifique o console.";
                mensagemPonto.setStyle("-fx-text-fill: red;");
            }
        } catch (Exception e) {
            System.err.println("Erro inesperado ao marcar ponto: " + e.getMessage());
            e.printStackTrace();
            mensagemFeedback = "Erro inesperado ao marcar ponto.";
            mensagemPonto.setStyle("-fx-text-fill: red;");
            pontoRegistradoComSucesso = false; // Garante que o status seja falso em caso de exceção
        }

        if (mensagemPonto != null) {
            mensagemPonto.setText(mensagemFeedback);
            mensagemPonto.setVisible(true);
            mensagemPonto.setManaged(true);

            // Temporizador para esconder a mensagem
            Timeline hideMessageTimer = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                mensagemPonto.setVisible(false);
                mensagemPonto.setManaged(false);
            }));
            hideMessageTimer.setCycleCount(1);
            hideMessageTimer.play();
        }
    }

    /**
     * Inicia o relógio digital na interface do usuário, atualizando a cada segundo.
     */
    private void iniciarRelogio() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            LocalTime now = LocalTime.now();
            relogioLabel.setText(now.format(formatter));
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Para o relógio digital.
     */
    public void pararRelogio() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    // --- Métodos de Navegação (Menu Lateral/Sidebar) ---

    @FXML
    private void handleSair(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "TelaLogin.fxml", null, null);
        pararRelogio();
    }

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "RegistrosPonto.fxml", userEmail, userPermission);
        pararRelogio();
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Configuracoes.fxml", userEmail, userPermission);
        pararRelogio();
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "GestaoUsuarios.fxml", userEmail, userPermission);
        pararRelogio();
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "TelaCadastro.fxml", userEmail, userPermission);
        pararRelogio();
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", userEmail, userPermission);
        pararRelogio();
    }
}