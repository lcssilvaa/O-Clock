package com.oclock.controller;

import com.oclock.model.ScreenManager;
import com.oclock.dao.UserDAO; // Importe seu UserDao
import com.oclock.model.User; // Importe sua classe User (modelo)

import javafx.animation.TranslateTransition;
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

    private String currentUserEmail;
    private String currentUserPermission;

    private TranslateTransition slideInAdmin;
    private TranslateTransition slideOutAdmin;
    private TranslateTransition slideInUser;
    private TranslateTransition slideOutUser;
    private boolean isSidebarOpen = false;

    private ObservableList<User> listaDeUsuarios = FXCollections.observableArrayList();

    private UserDAO userDao;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
        sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());

        slideInAdmin = new TranslateTransition(Duration.seconds(0.3), sidebarAdminPane);
        slideInAdmin.setToX(0);
        slideOutAdmin = new TranslateTransition(Duration.seconds(0.3), sidebarAdminPane);
        slideOutAdmin.setToX(-sidebarAdminPane.getPrefWidth());

        slideInUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
        slideInUser.setToX(0);
        slideOutUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
        slideOutUser.setToX(-sidebarPane.getPrefWidth());

        overlayPane.setOnMouseClicked(event -> toggleSidebar());

        // CORREÇÃO AQUI: Usando "fullName" para a coluna de nome e "permission"
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("fullName")); // Seu User tem getFullName()
        colunaEmail.setCellValueFactory(new PropertyValueFactory<>("email")); // Seu User tem getEmail()
        colunaPermissao.setCellValueFactory(new PropertyValueFactory<>("permission")); // Seu User tem getPermission()

        tabelaUsuarios.setItems(listaDeUsuarios);

        userDao = new UserDAO(); // Instanciando seu UserDao

        carregarUsuarios(); // Carrega usuários ao iniciar a tela

        mensagemStatus.setText("");

        btnPesquisar.setOnAction(this::handlePesquisarUsuario);
        btnEditarUsuario.setOnAction(this::handleEditarUsuario);
        btnRemoverUsuario.setOnAction(this::handleRemoverUsuario);
        btnAtualizarTabela.setOnAction(this::handleAtualizarTabela);
    }

    public void initData(String email, String permission) {
        this.currentUserEmail = email;
        this.currentUserPermission = permission;

        if ("admin".equalsIgnoreCase(permission)) {
            sidebarAdminPane.setVisible(true);
            sidebarPane.setVisible(false);
        } else {
            showAlert(Alert.AlertType.ERROR, "Acesso Negado", "Você não tem permissão para acessar esta tela.");
            // Opcional: Redirecionar para uma tela permitida para o usuário comum
            // ScreenManager.loadScreen(mainContentPane, "MenuUser.fxml", currentUserEmail, currentUserPermission);
            sidebarAdminPane.setVisible(false);
            sidebarPane.setVisible(true);
        }
        carregarUsuarios(); // Recarrega os usuários toda vez que a tela é acessada
    }

    @FXML
    private void handleMenuButton(MouseEvent event) {
        toggleSidebar();
    }

    private void toggleSidebar() {
        if (isSidebarOpen) {
            if ("admin".equalsIgnoreCase(currentUserPermission)) {
                slideOutAdmin.play();
            } else {
                slideOutUser.play();
            }
            overlayPane.setVisible(false);
        } else {
            if ("admin".equalsIgnoreCase(currentUserPermission)) {
                sidebarAdminPane.toFront();
                slideInAdmin.play();
            } else {
                sidebarPane.toFront();
                slideInUser.play();
            }
            overlayPane.toFront();
            overlayPane.setVisible(true);
        }
        isSidebarOpen = !isSidebarOpen;
    }

    // ================================================================
    // MÉTODOS DE ACESSO AO BANCO DE DADOS ATRAVÉS DO UserDao
    // ================================================================

    private void carregarUsuarios() {
        listaDeUsuarios.clear();
        try {
            // CORREÇÃO AQUI: Chamando getAllUsers() do seu UserDao
            List<User> usuariosDoBanco = userDao.getAllUsers();
            listaDeUsuarios.addAll(usuariosDoBanco);
            mensagemStatus.setText("Usuários carregados do banco de dados.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro ao Carregar Usuários", "Não foi possível carregar os usuários: " + e.getMessage());
            mensagemStatus.setText("Erro ao carregar usuários: " + e.getMessage());
        } finally {
            tabelaUsuarios.refresh();
            mensagemStatus.setVisible(true);
        }
    }

    @FXML
    private void handlePesquisarUsuario(ActionEvent event) {
        String termoPesquisa = campoPesquisa.getText().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            carregarUsuarios();
            return;
        }

        // Idealmente, você implementaria um método de busca por termo no seu UserDao
        // Ex: List<User> resultados = userDao.searchUsers(termoPesquisa);
        // Por agora, filtramos na lista carregada, mas lembre-se da otimização para grandes dados.
        ObservableList<User> resultadosFiltrados = FXCollections.observableArrayList();
        for (User user : listaDeUsuarios) {
            if (user.getFullName().toLowerCase().contains(termoPesquisa) || // Usando getFullName
                user.getEmail().toLowerCase().contains(termoPesquisa) ||
                user.getPermission().toLowerCase().contains(termoPesquisa)) {
                resultadosFiltrados.add(user);
            }
        }
        tabelaUsuarios.setItems(resultadosFiltrados);
        mensagemStatus.setText(resultadosFiltrados.size() + " usuários encontrados.");
        mensagemStatus.setVisible(true);
    }

    @FXML
    private void handleEditarUsuario(ActionEvent event) {
        User usuarioSelecionado = tabelaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSelecionado != null) {
            showAlert(Alert.AlertType.INFORMATION, "Editar Usuário", "Funcionalidade de edição para: " + usuarioSelecionado.getFullName() + ".\nRedirecione para a tela de edição ou abra um diálogo.\n(Para editar, você precisaria carregar os dados do " + usuarioSelecionado.getEmail() + " e usar userDao.updateUserDetails ou updateUser)");
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhum Usuário Selecionado", "Por favor, selecione um usuário para editar.");
        }
    }

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
                    // CORREÇÃO AQUI: Chamando deleteUser(int userId) do seu UserDao
                    userDao.deleteUser(usuarioSelecionado.getId());
                    listaDeUsuarios.remove(usuarioSelecionado); // Remove da ObservableList local
                    mensagemStatus.setText("Usuário " + usuarioSelecionado.getFullName() + " removido com sucesso.");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erro ao Remover Usuário", "Não foi possível remover o usuário: " + e.getMessage());
                    mensagemStatus.setText("Erro ao remover usuário: " + e.getMessage());
                } finally {
                    mensagemStatus.setVisible(true);
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhum Usuário Selecionado", "Por favor, selecione um usuário para remover.");
        }
    }

    @FXML
    private void handleAtualizarTabela(ActionEvent event) {
        carregarUsuarios();
        mensagemStatus.setText("Tabela de usuários atualizada.");
        mensagemStatus.setVisible(true);
    }

    // ================================================================
    // Métodos dos botões da Sidebar
    // ================================================================

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        ScreenManager.loadScreen((Node) event.getSource(), "RegistrarMarcacao.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        if (isSidebarOpen) {
            toggleSidebar();
        }
        // Já estamos na tela de Gestão de Usuários
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        ScreenManager.loadScreen((Node) event.getSource(), "Cadastro.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Funcionalidade", "Exportar Relatório ainda não implementado.");
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
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
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            try {
                ScreenManager.loadScreen(null, "TelaLogin.fxml", null, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}