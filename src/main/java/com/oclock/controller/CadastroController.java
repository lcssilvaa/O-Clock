package com.oclock.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.event.ActionEvent;

import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

import com.oclock.model.Cadastro;
import com.oclock.model.ScreenManager;

public class CadastroController implements Initializable {

    // --- Elementos FXML injetados ---
    @FXML
    private ImageView botaoMenu;

    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private AnchorPane sidebarAdminPane;

    @FXML
    private TextField campoNome;
    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoSenha;
    @FXML
    private TextField campoCPF;
    @FXML
    private RadioButton userUsuarioRadio;
    @FXML
    private RadioButton userAdminRadio;
    @FXML
    private Label mensagemErro;
    @FXML
    private ToggleGroup permissaoToggleGroup;

    // --- Serviços e estados do controlador ---
    private Cadastro cadastroService = new Cadastro();
    private String userEmail, userPermission;
    private boolean isSidebarOpen = false;

    // --- Transições de animação para o sidebar ---
    // Instanciadas no initialize para garantir que os panes não sejam nulos
    private TranslateTransition slideInAdmin;
    private TranslateTransition slideOutAdmin;
    private TranslateTransition slideInUser;
    private TranslateTransition slideOutUser;

    /**
     * Construtor padrão da classe CadastroController.
     * Inicializa a instância do serviço de cadastro.
     */
    public CadastroController() {
        this.cadastroService = new Cadastro();
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
        updateSidebarVisibility(); // Chama após userPermission estar definido
    }

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * Configura o estado inicial dos painéis do sidebar e overlay, e as transições de animação.
     * Também configura os listeners de eventos e a validação inicial dos campos.
     * @param url A localização usada para resolver caminhos relativos para o objeto raiz, ou null se a localização não for conhecida.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa o overlayPane
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setMouseTransparent(true); // *** CRUCIAL: Impede que capture cliques quando invisível/fechado ***
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
        }

        // Inicializa sidebarPane (usuário)
        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
            sidebarPane.setMouseTransparent(true); // *** CRUCIAL: Impede que capture cliques quando fechado ***
            slideInUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
            slideInUser.setToX(0);
            slideOutUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
            slideOutUser.setToX(-sidebarPane.getPrefWidth());
        } else {
            System.err.println("ERRO: sidebarPane é NULO. Verifique o fx:id no FXML.");
        }

        // Inicializa sidebarAdminPane (administrador)
        if (sidebarAdminPane != null) {
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
            sidebarAdminPane.setMouseTransparent(true); // *** CRUCIAL: Impede que capture cliques quando fechado ***
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

        // Configura o ToggleGroup para os RadioButtons
        if (userUsuarioRadio != null && userAdminRadio != null) {
            permissaoToggleGroup = new ToggleGroup();
            userUsuarioRadio.setToggleGroup(permissaoToggleGroup);
            userAdminRadio.setToggleGroup(permissaoToggleGroup);
            userUsuarioRadio.setSelected(true); // Define um valor padrão
        } else {
            System.err.println("ERRO: RadioButtons de permissão não estão injetados. Verifique o fx:id no FXML.");
        }

        // Oculta a mensagem de erro no início
        if (mensagemErro != null) {
            mensagemErro.setVisible(false);
            mensagemErro.setManaged(false);
        }
    }

    // --- Métodos de Controle do Sidebar ---

    /**
     * Atualiza a visibilidade e o gerenciamento de espaço dos sidebars com base na permissão do usuário.
     * O sidebar de admin é mostrado se o usuário for admin, caso contrário, o sidebar padrão é mostrado.
     * NOTA: Este método agora apenas configura qual sidebar DEVE ser visível.
     * A visibilidade e `mouseTransparent` durante a abertura/fechamento são gerenciadas em `toggleSidebar`.
     */
    private void updateSidebarVisibility() {
        if (sidebarPane == null || sidebarAdminPane == null) {
            System.err.println("Erro: Painéis do sidebar FXML não injetados em CadastroController.");
            return;
        }

        // Garante que ambos os sidebars estejam fora da tela e não gerenciados por padrão
        sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
        sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());

        sidebarAdminPane.setVisible(false);
        sidebarAdminPane.setManaged(false);
        sidebarAdminPane.setMouseTransparent(true); // Certifique-se de que estejam transparentes ao mouse
        sidebarPane.setVisible(false);
        sidebarPane.setManaged(false);
        sidebarPane.setMouseTransparent(true); // Certifique-se de que estejam transparentes ao mouse

        if (userPermission == null) {
            System.err.println("Erro: Permissão do usuário não definida em CadastroController para configurar sidebar. Defaulting to user.");
            userPermission = "usuario"; // Define um padrão seguro
        }
        // A visibilidade real e "mouseTransparent" será definida em toggleSidebar
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

        if (isSidebarOpen) { 
            currentSlideTransition = "admin".equalsIgnoreCase(userPermission) ? slideOutAdmin : slideOutUser;

            if (currentSlideTransition != null) {
                currentSlideTransition.play();
                currentSlideTransition.setOnFinished(e -> {
                    activeSidebar.setVisible(false);
                    activeSidebar.setManaged(false);
                    activeSidebar.setMouseTransparent(true); // *** CRUCIAL: Libera os cliques no sidebar fechado ***
                });
            }

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeOut.setFromValue(overlayPane.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.play();
            fadeOut.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true); // *** CRUCIAL: Libera os cliques na tela principal ***
            });

        } else { // Se o sidebar está fechado, abre
            // Garante que o overlay e o sidebar venham para a frente
            overlayPane.toFront();
            activeSidebar.toFront(); // Certifique-se que o sidebar ativo também venha para frente

            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false); // Permite que o overlay capture cliques para fechar o sidebar

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeIn.setFromValue(overlayPane.getOpacity());
            fadeIn.setToValue(0.4);
            fadeIn.play();

            currentSlideTransition = "admin".equalsIgnoreCase(userPermission) ? slideInAdmin : slideInUser;

            if (activeSidebar != null && currentSlideTransition != null) {
                activeSidebar.setVisible(true);
                activeSidebar.setManaged(true);
                activeSidebar.setMouseTransparent(false); // Permite que os botões dentro do sidebar sejam clicáveis
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

    // --- Métodos de Ação do Formulário de Cadastro ---

    /**
     * Lida com o evento de clique no botão "Cadastrar Usuário" no formulário principal.
     * Coleta os dados dos campos, valida-os e tenta cadastrar um novo usuário no banco de dados.
     * Exibe mensagens de sucesso ou erro ao usuário.
     * @param event O evento de ação.
     */
    @FXML
    private void cadastrarUsuario(ActionEvent event) {
        // Assegura que todos os campos FXML estão carregados antes de continuar
        if (campoNome == null || campoEmail == null || campoSenha == null || campoCPF == null ||
            userUsuarioRadio == null || userAdminRadio == null || mensagemErro == null ||
            permissaoToggleGroup == null) { // Adicionado permissaoToggleGroup ao null check
            System.err.println("ERRO: Nem todos os campos de cadastro estão mapeados no FXML. Verifique os fx:id.");
            exibirMensagem("Erro interno: Campos não carregados. Contate o suporte.", Color.RED);
            return;
        }

        String nome = campoNome.getText();
        String email = campoEmail.getText();
        String senha = campoSenha.getText();
        String cpf = campoCPF.getText();

        String permissao = "";

        // Verifica qual RadioButton foi selecionado
        if (permissaoToggleGroup.getSelectedToggle() == userUsuarioRadio) {
            permissao = "usuario";
        } else if (permissaoToggleGroup.getSelectedToggle() == userAdminRadio) {
            permissao = "admin";
        } else {
            exibirMensagem("Selecione um tipo de usuário (Usuário ou Administrador).", Color.RED);
            return;
        }

        // Validações de entrada
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpf.isEmpty()) {
            exibirMensagem("Todos os campos são obrigatórios!", Color.RED);
            return;
        }

        if (!cpf.matches("\\d{11}")) {
            exibirMensagem("CPF deve conter exatamente 11 dígitos numéricos.", Color.RED);
            return;
        }

        if (senha.length() < 8 || senha.length() > 15) {
            exibirMensagem("A senha deve conter entre 8 e 15 caracteres.", Color.RED);
            return;
        }

        if (!senha.matches(".*[A-Z].*")) {
            exibirMensagem("A senha deve conter pelo menos uma letra maiúscula.", Color.RED);
            return;
        }
        if (!senha.matches(".*[a-z].*")) {
            exibirMensagem("A senha deve conter pelo menos uma letra minúscula.", Color.RED);
            return;
        }
        if (!senha.matches(".*\\d.*")) {
            exibirMensagem("A senha deve conter pelo menos um número.", Color.RED);
            return;
        }

        if (!senha.matches(".*[!@#$%^&*()\\-+=].*")) {
            exibirMensagem("A senha deve conter pelo menos um caractere especial (!@#$%^&*()-+=).", Color.RED);
            return;
        }

        if (!email.matches("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$")) {
            exibirMensagem("Formato de e-mail inválido.", Color.RED);
            return;
        }

        try {
            cadastroService.cadastrarUsuario(nome, senha, email, permissao, cpf);
            exibirMensagem("Usuário '" + nome + "' cadastrado com sucesso!", Color.LIMEGREEN);
            limparCampos();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                if (e.getMessage().contains("'email'")) {
                    exibirMensagem("Erro: E-mail já cadastrado.", Color.RED);
                } else if (e.getMessage().contains("'cpf'")) {
                    exibirMensagem("Erro: CPF já cadastrado.", Color.RED);
                } else {
                    exibirMensagem("Erro de duplicidade no banco de dados.", Color.RED);
                }
            } else {
                exibirMensagem("Erro ao cadastrar: " + e.getMessage(), Color.RED);
            }
            System.err.println("Erro SQL ao cadastrar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Exibe uma mensagem de status (sucesso ou erro) na interface do usuário.
     * @param mensagem O texto da mensagem a ser exibida.
     * @param cor A cor do texto da mensagem.
     */
    private void exibirMensagem(String mensagem, Color cor) {
        if (mensagemErro != null) {
            mensagemErro.setText(mensagem);
            mensagemErro.setTextFill(cor);
            mensagemErro.setVisible(true);
            mensagemErro.setManaged(true);
        }
    }

    /**
     * Limpa todos os campos de entrada do formulário de cadastro.
     * Oculta a mensagem de erro/sucesso após a limpeza.
     */
    private void limparCampos() {
        if (campoNome != null) campoNome.clear();
        if (campoEmail != null) campoEmail.clear();
        if (campoSenha != null) campoSenha.clear();
        if (campoCPF != null) campoCPF.clear();
        if (userUsuarioRadio != null) userUsuarioRadio.setSelected(true);
        if (userAdminRadio != null) userAdminRadio.setSelected(false);
        if (mensagemErro != null) {
            mensagemErro.setVisible(false);
            mensagemErro.setManaged(false);
        }
    }

    // --- Métodos de Navegação (Menu Lateral/Sidebar) ---

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "RegistrosPonto.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "Configuracoes.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "GestaoUsuarios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        if (isSidebarOpen) {
            toggleSidebar();
        }
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleSair(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Você está prestes a sair.");
        alert.setContentText("Deseja realmente sair da aplicação?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ScreenManager.loadScreen((Node) event.getSource(), "TelaLogin.fxml", null, null);
        }
    }
}