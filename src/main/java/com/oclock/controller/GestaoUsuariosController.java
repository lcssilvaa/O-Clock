package com.oclock.controller;

import com.oclock.model.ScreenManager;
import com.oclock.dao.UserDAO;
import com.oclock.model.User;

import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestaoUsuariosController implements Initializable {

    // --- Elementos FXML injetados ---
    @FXML
    private AnchorPane mainContentPane;
    @FXML
    private AnchorPane sidebarAdminPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private AnchorPane overlayPane;
    @FXML
    private ImageView botaoMenu;
    @FXML
    private Label mensagemStatus;

    @FXML
    private TableView<User> tabelaUsuarios;
    @FXML
    private TableColumn<User, String> colunaNome;
    @FXML
    private TableColumn<User, String> colunaEmail;
    @FXML
    private TableColumn<User, String> colunaPermissao;
    @FXML
    private TextField campoPesquisa;
    @FXML
    private Button btnPesquisar;
    @FXML
    private Button btnEditarUsuario;
    @FXML
    private Button btnRemoverUsuario;
    @FXML
    private Button btnAtualizarTabela;

    // --- Variáveis de estado do controlador ---
    private String currentUserEmail;
    private String currentUserPermission;
    private boolean isSidebarOpen = false;

    // --- Transições de animação para o sidebar ---
    private TranslateTransition slideInAdmin;
    private TranslateTransition slideOutAdmin;
    private TranslateTransition slideInUser;
    private TranslateTransition slideOutUser;

    private ObservableList<User> listaDeUsuarios = FXCollections.observableArrayList();
    private UserDAO userDao;

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * Configura o estado inicial dos painéis do sidebar e overlay, e as transições de animação.
     * Configura as células da tabela, inicializa o DAO e carrega os usuários.
     * Também configura os listeners de eventos para os botões da tela.
     * @param url A localização usada para resolver caminhos relativos para o objeto raiz, ou null se a localização não for conhecida.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializa sidebars fora da tela e os oculta/desabilita o gerenciamento
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

        // Inicializa o overlay
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setOnMouseClicked(event -> toggleSidebar());
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
        }

        // Define o handler para o botão de menu
        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButton);
        } else {
            System.err.println("ERRO: botaoMenu é NULO. Verifique o fx:id no FXML.");
        }

        // Configuração das colunas da tabela de usuários
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colunaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colunaPermissao.setCellValueFactory(new PropertyValueFactory<>("permission"));

        tabelaUsuarios.setItems(listaDeUsuarios);

        userDao = new UserDAO();

        carregarUsuarios(); // Carrega os usuários ao inicializar a tela

        // Inicia a mensagem de status invisível
        if (mensagemStatus != null) {
            mensagemStatus.setText("");
            mensagemStatus.setVisible(false);
        } else {
            System.err.println("ERRO: mensagemStatus é NULO. Verifique o fx:id no FXML.");
        }


        // Configura os listeners de eventos para os botões da interface principal
        if (btnPesquisar != null) btnPesquisar.setOnAction(this::handlePesquisarUsuario);
        if (btnEditarUsuario != null) btnEditarUsuario.setOnAction(this::handleEditarUsuario);
        if (btnRemoverUsuario != null) btnRemoverUsuario.setOnAction(this::handleRemoverUsuario);
        if (btnAtualizarTabela != null) btnAtualizarTabela.setOnAction(this::handleAtualizarTabela);
    }

    /**
     * Recebe e inicializa os dados do usuário (e-mail e permissão) passados de outra tela.
     * Ativa o sidebar correto com base na permissão e carrega os usuários.
     * @param email O e-mail do usuário logado.
     * @param permission A permissão (função) do usuário logado (ex: "admin", "usuario").
     */
    public void initData(String email, String permission) {
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
            // Caso não seja admin, esconde o sidebar de admin e exibe o de usuário padrão (se existir)
            if (sidebarAdminPane != null) {
                sidebarAdminPane.setVisible(false);
                sidebarAdminPane.setManaged(false);
            }
            if (sidebarPane != null) { // Garante que o sidebar de usuário esteja visível, se aplicável
                sidebarPane.setVisible(true);
                sidebarPane.setManaged(true);
            }
            // Opcional: Redirecionar para uma tela permitida caso o acesso seja negado
            ScreenManager.loadScreen((Node) tabelaUsuarios, "MenuUser.fxml", currentUserEmail, currentUserPermission);
        }
        carregarUsuarios();
    }

    // --- Métodos de Controle do Sidebar ---

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

        TranslateTransition currentSlideOut = null;
        TranslateTransition currentSlideIn = null;
        AnchorPane currentSidebar = null;

        if ("admin".equalsIgnoreCase(currentUserPermission)) {
            currentSlideOut = slideOutAdmin;
            currentSlideIn = slideInAdmin;
            currentSidebar = sidebarAdminPane;
        } else {
            currentSlideOut = slideOutUser;
            currentSlideIn = slideInUser;
            currentSidebar = sidebarPane;
        }

        if (currentSidebar == null) {
            System.err.println("Sidebar atual é nulo. Permissão ou FXML inválido.");
            return;
        }

        if (isSidebarOpen) {
            // Lógica para FECHAR o sidebar
            if (currentSlideOut != null) {
                currentSlideOut.play();
            }

            // Animação de fade-out para o overlayPane
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeOut.setFromValue(overlayPane.getOpacity());
            fadeOut.setToValue(0.0);
            fadeOut.play();
            fadeOut.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
            });

        } else {
            // Lógica para ABRIR o sidebar
            overlayPane.toFront();
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false);

            // Animação de fade-in para o overlayPane
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeIn.setFromValue(overlayPane.getOpacity());
            fadeIn.setToValue(0.4);
            fadeIn.play();

            if (currentSidebar != null && currentSlideIn != null) {
                currentSidebar.toFront();
                currentSidebar.setVisible(true); // Garante que o sidebar esteja visível antes de deslizar
                currentSidebar.setManaged(true); // Garante que o sidebar ocupe espaço
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

    // --- Métodos de Ação da Tela de Gestão de Usuários ---

    /**
     * Carrega todos os usuários do banco de dados e os exibe na tabela.
     * Exibe mensagens de status (sucesso ou erro).
     */
    private void carregarUsuarios() {
        listaDeUsuarios.clear();
        try {
            List<User> usuariosDoBanco = userDao.getAllUsers();
            listaDeUsuarios.addAll(usuariosDoBanco);
            mensagemStatus.setText("Usuários carregados do banco de dados.");
            mensagemStatus.setStyle("-fx-text-fill: green;");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro ao Carregar Usuários", "Não foi possível carregar os usuários: " + e.getMessage());
            mensagemStatus.setText("Erro ao carregar usuários: " + e.getMessage());
            mensagemStatus.setStyle("-fx-text-fill: red;");
        } finally {
            if (tabelaUsuarios != null) tabelaUsuarios.refresh();
            if (mensagemStatus != null) mensagemStatus.setVisible(true);
        }
    }

    /**
     * Lida com o evento de pesquisa de usuários.
     * Filtra a lista de usuários exibida na tabela com base no termo de pesquisa.
     * @param event O evento de ação.
     */
    @FXML
    private void handlePesquisarUsuario(ActionEvent event) {
        String termoPesquisa = campoPesquisa.getText().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            carregarUsuarios(); // Se o campo de pesquisa estiver vazio, carrega todos os usuários
            return;
        }

        ObservableList<User> resultadosFiltrados = FXCollections.observableArrayList();
        for (User user : listaDeUsuarios) {
            if (user.getFullName().toLowerCase().contains(termoPesquisa) ||
                user.getEmail().toLowerCase().contains(termoPesquisa) ||
                user.getPermission().toLowerCase().contains(termoPesquisa)) {
                resultadosFiltrados.add(user);
            }
        }
        tabelaUsuarios.setItems(resultadosFiltrados);
        mensagemStatus.setText(resultadosFiltrados.size() + " usuários encontrados.");
        mensagemStatus.setVisible(true);
    }

    /**
     * Lida com o evento de edição de usuário.
     * Exibe um alerta informando que a funcionalidade de edição deve ser implementada.
     * @param event O evento de ação.
     */
    @FXML
    private void handleEditarUsuario(ActionEvent event) {
    	User usuarioSelecionado = tabelaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSelecionado != null) {
            try {

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                ScreenManager.loadScreen(currentStage, "EditarUsuario.fxml", usuarioSelecionado, currentUserEmail, currentUserPermission);

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erro ao Abrir Edição", "Não foi possível abrir a tela de edição: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhum Usuário Selecionado", "Por favor, selecione um usuário para editar.");
        }
    }
    /**
     * Lida com o evento de remoção de usuário.
     * Exibe um alerta de confirmação e, se confirmado, remove o usuário do banco de dados e da tabela.
     * @param event O evento de ação.
     */
    @FXML
    private void handleRemoverUsuario(ActionEvent event) {
        User usuarioSelecionado = tabelaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSelecionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmação de Remoção");
            alert.setHeaderText("Remover Usuário");
            alert.setContentText("Tem certeza que deseja remover o usuário " + usuarioSelecionado.getFullName() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    userDao.deleteUser(usuarioSelecionado.getId());
                    listaDeUsuarios.remove(usuarioSelecionado); // Remove da ObservableList
                    mensagemStatus.setText("Usuário " + usuarioSelecionado.getFullName() + " removido com sucesso.");
                    mensagemStatus.setStyle("-fx-text-fill: green;");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erro ao Remover Usuário", "Não foi possível remover o usuário: " + e.getMessage());
                    mensagemStatus.setText("Erro ao remover usuário: " + e.getMessage());
                    mensagemStatus.setStyle("-fx-text-fill: red;");
                } finally {
                    if (mensagemStatus != null) mensagemStatus.setVisible(true);
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhum Usuário Selecionado", "Por favor, selecione um usuário para remover.");
        }
    }

    /**
     * Lida com o evento de atualização da tabela.
     * Recarrega todos os usuários do banco de dados.
     * @param event O evento de ação.
     */
    @FXML
    private void handleAtualizarTabela(ActionEvent event) {
        carregarUsuarios();
        mensagemStatus.setText("Tabela de usuários atualizada.");
        mensagemStatus.setVisible(true);
    }

    /**
     * Exibe um alerta simples ao usuário.
     * @param type O tipo de alerta (INFO, WARNING, ERROR, etc.).
     * @param title O título do alerta.
     * @param message A mensagem a ser exibida no alerta.
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
            // Se confirmar, fecha a janela atual e redireciona para a tela de login
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            ScreenManager.loadScreen(null, "TelaLogin.fxml", null, null);
        }
    }
}