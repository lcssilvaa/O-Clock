package com.oclock.controller;

import com.oclock.dao.UserDAO;
import com.oclock.model.ScreenManager;
import com.oclock.model.User;
import com.oclock.model.Criptografia; // Importe sua classe de criptografia

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

public class EditarController implements Initializable {

    // --- Elementos FXML injetados ---
    @FXML private AnchorPane mainContentPane;
    @FXML private AnchorPane sidebarAdminPane;
    @FXML private AnchorPane sidebarPane;
    @FXML private AnchorPane overlayPane;
    @FXML private ImageView botaoMenu;

    @FXML private TextField campoNome;
    @FXML private TextField campoEmail;
    @FXML private PasswordField campoSenha;
    @FXML private TextField campoCPF;
    @FXML private RadioButton userUsuarioRadio;
    @FXML private RadioButton userAdminRadio;
    @FXML private ToggleGroup permissaoToggleGroup;
    @FXML private Label mensagemErro;
    @FXML private Button btnCadastrarUser; // Este botão será usado para "Atualizar"

    // --- Variáveis de estado do controlador ---
    private User userToEdit;
    private UserDAO userDao;
    private Criptografia criptografia; // Instância da sua classe de criptografia
    private String currentUserEmail;
    private String currentUserPermission;
    private boolean isSidebarOpen = false;

    // --- Transições de animação para o sidebar ---
    private TranslateTransition slideInAdmin;
    private TranslateTransition slideOutAdmin;
    private TranslateTransition slideInUser;
    private TranslateTransition slideOutUser;

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * Configura o estado inicial dos painéis do sidebar e overlay, e as transições de animação.
     * Inicializa o DAO e a classe de criptografia.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userDao = new UserDAO();
        criptografia = new Criptografia(); // Inicializa sua instância de criptografia

        // Configuração inicial do sidebar
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

        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setOnMouseClicked(event -> toggleSidebar());
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
        }

        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButton);
        } else {
            System.err.println("ERRO: botaoMenu é NULO. Verifique o fx:id no FXML.");
        }

        // Garante que a mensagem de erro esteja inicialmente invisível
        if (mensagemErro != null) {
            mensagemErro.setText("");
            mensagemErro.setVisible(false);
        }
        
        // Define o handler para o botão de "Confirmar" (que será "Atualizar")
        if (btnCadastrarUser != null) {
            btnCadastrarUser.setOnAction(this::handleAtualizarUsuario);
        }
    }

    /**
     * Recebe os dados do usuário a ser editado e as credenciais do usuário logado.
     * Este método é chamado pelo ScreenManager após o carregamento do FXML.
     * Preenche os campos da interface com os dados do usuário a ser editado.
     * @param user O objeto User a ser editado.
     * @param email O e-mail do usuário logado (para controle de navegação).
     * @param permission A permissão do usuário logado (para controle de sidebar).
     */
    public void initData(User user, String email, String permission) {
        this.userToEdit = user;
        this.currentUserEmail = email;
        this.currentUserPermission = permission;

        // Ativa o sidebar correto com base na permissão
        if ("admin".equalsIgnoreCase(permission)) {
            if (sidebarAdminPane != null) {
                sidebarAdminPane.setVisible(true);
                sidebarAdminPane.setManaged(true);
            }
            if (sidebarPane != null) {
                sidebarPane.setVisible(false);
                sidebarPane.setManaged(false);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Acesso Negado", "Você não tem permissão para acessar esta tela.");
            ScreenManager.loadScreen((Node) mainContentPane, "MenuUser.fxml", currentUserEmail, currentUserPermission);
            return;
        }

        // Preenche os campos com os dados do usuário a ser editado
        if (userToEdit != null) {
            campoNome.setText(userToEdit.getFullName());
            campoEmail.setText(userToEdit.getEmail());
            campoCPF.setText(userToEdit.getCpf());
            // A senha NÃO deve ser pré-preenchida por segurança.

            // Seleciona o RadioButton correto para a permissão
            if ("admin".equalsIgnoreCase(userToEdit.getPermission())) {
                userAdminRadio.setSelected(true);
            } else {
                userUsuarioRadio.setSelected(true);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Erro", "Nenhum usuário foi selecionado para edição.");
            handleGestao(null);
        }
    }

    /**
     * Lida com o evento de clique no botão de menu (geralmente um ícone).
     * Chama o método para alternar o estado do sidebar (abrir/fechar).
     * @param event O evento de clique do mouse.
     */
    @FXML
    private void handleMenuButton(MouseEvent event) {
        toggleSidebar();
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

        TranslateTransition currentSlideOut;
        TranslateTransition currentSlideIn;
        AnchorPane currentSidebar;

        // Sempre usar o sidebar de admin para esta tela
        currentSlideOut = slideOutAdmin;
        currentSlideIn = slideInAdmin;
        currentSidebar = sidebarAdminPane;

        if (currentSidebar == null) {
            System.err.println("Sidebar atual é nulo. FXML inválido.");
            return;
        }

        if (isSidebarOpen) {
            if (currentSlideOut != null) {
                currentSlideOut.play();
            }

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeOut.setFromValue(overlayPane.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.play();
            fadeOut.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
            });

        } else {
            overlayPane.toFront();
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false);

            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeIn.setFromValue(overlayPane.getOpacity());
            fadeIn.setToValue(0.4);
            fadeIn.play();

            if (currentSidebar != null && currentSlideIn != null) {
                currentSidebar.toFront();
                currentSidebar.setVisible(true);
                currentSidebar.setManaged(true);
                currentSlideIn.play();
            }
        }
        isSidebarOpen = !isSidebarOpen;
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

    /**
     * Lida com a ação de atualização dos dados do usuário.
     * Coleta os dados dos campos, valida-os e chama o DAO para atualizar no banco de dados.
     * @param event O evento de ação (clique no botão "Confirmar").
     */
    @FXML
    private void handleAtualizarUsuario(ActionEvent event) {
        // Validação de entrada
        String nome = campoNome.getText().trim();
        String email = campoEmail.getText().trim();
        String senha = campoSenha.getText().trim();
        String cpf = campoCPF.getText().trim();
        RadioButton selectedRadioButton = (RadioButton) permissaoToggleGroup.getSelectedToggle();
        String permissao = (selectedRadioButton != null) ? selectedRadioButton.getText() : "";
        
        if ("administrador".equalsIgnoreCase(permissao)) {
            permissao = "admin";
        } else if ("usuario".equalsIgnoreCase(permissao)) {
            permissao = "usuario";
        }

        if (nome.isEmpty() || email.isEmpty() || cpf.isEmpty() || permissao.isEmpty()) {
            mostrarMensagemErro("Por favor, preencha todos os campos obrigatórios (exceto a senha, se não for alterar).");
            return;
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            mostrarMensagemErro("Por favor, insira um e-mail válido.");
            return;
        }

        if (!cpf.matches("\\d{11}")) {
            mostrarMensagemErro("O CPF deve conter exatamente 11 dígitos.");
            return;
        }

        try {
            String passwordHash;
            if (senha.isEmpty()) {
                // Se o campo de senha estiver vazio, mantém a senha hash existente.
                passwordHash = userToEdit.getPasswordHash();
            } else {
                // Caso contrário, gera um novo hash para a nova senha usando sua classe Criptografia.
                passwordHash = criptografia.gerarHash(senha);
                if (passwordHash == null) {
                    mostrarMensagemErro("Erro ao gerar hash da nova senha.");
                    return;
                }
            }

            User userAtualizado = new User(
                userToEdit.getId(),
                email,
                passwordHash,
                nome,
                cpf,
                permissao.toLowerCase(),
                userToEdit.isActive(),
                userToEdit.getCreatedAt(),
                LocalDateTime.now()
            );

            userDao.updateUser(userAtualizado);

            showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Usuário atualizado com sucesso!");
            handleGestao(event);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarMensagemErro("Erro ao atualizar usuário no banco de dados: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarMensagemErro("Erro inesperado durante a atualização: " + e.getMessage());
        }
    }

    /**
     * Exibe uma mensagem de erro na label `mensagemErro`.
     * @param mensagem A mensagem de erro a ser exibida.
     */
    private void mostrarMensagemErro(String mensagem) {
        if (mensagemErro != null) {
            mensagemErro.setText(mensagem);
            mensagemErro.setStyle("-fx-text-fill: red;");
            mensagemErro.setVisible(true);
        }
    }

    /**
     * Exibe um alerta padrão do JavaFX.
     * @param type O tipo de alerta (INFO, WARNING, ERROR, etc.).
     * @param title O título do alerta.
     * @param message A mensagem a ser exibida.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Métodos de Navegação (Menu Lateral/Sidebar) ---

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "RegistrosPonto.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "GestaoUsuarios.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "TelaCadastro.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        closeSidebarIfOpen();
        ScreenManager.loadScreen((Node) event.getSource(), "Configuracoes.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleSair(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Saída");
        alert.setHeaderText("Você está prestes a sair.");
        alert.setContentText("Deseja realmente sair da aplicação?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            ScreenManager.loadLoginScreen((Node) event.getSource());
        }
    }
}