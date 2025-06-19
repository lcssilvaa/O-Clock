package com.oclock.controller;

import com.oclock.model.User; 
import com.oclock.dao.UserDAO;
import com.oclock.model.ScreenManager; 

import javafx.animation.FadeTransition;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Optional;
import java.util.ResourceBundle;

public class GestaoUsuariosController implements Initializable {

    @FXML private AnchorPane rootPane;
    @FXML private AnchorPane sidebarPane;
    @FXML private AnchorPane sidebarAdminPane;
    @FXML private AnchorPane overlayPane;
    @FXML private ImageView botaoMenu;
    @FXML private Label mensagemPonto;

    // Elementos da TableView
    @FXML private TableView<User> usersTableView;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> nameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> permissionColumn;

    // Botões da sidebar (mantidos como estavam)
    @FXML private Button btnConsultarHoras;
    @FXML private Button btnRegistrarMarcacao;
    @FXML private Button btnConfiguracoes;
    @FXML private Button btnSair;
    @FXML private Button btnGestao;
    @FXML private Button btnCadastrar;
    @FXML private Button btnRelatorio; // Note que você tem btnRelatorio no FXML, eu usei handleRelatorios no código.

    private UserDAO userDAO;
    private String userEmail;
    private String userPermission;
    private boolean sidebarVisible = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userDAO = new UserDAO();

        // Configura as colunas da TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        permissionColumn.setCellValueFactory(new PropertyValueFactory<>("permission"));

        // Adiciona o evento de clique ao ImageView botaoMenu para abrir a sidebar
        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        }
        
        // Garante que as sidebars e o overlay começam escondidos e fora da tela
        // Seus FXML agora já definem layoutX="-277.0" e visible="false",
        // mas é bom ter a lógica no controller também.
        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false); // Não ocupa espaço no layout quando invisível
        }
        if (sidebarAdminPane != null) {
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
        }
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setMouseTransparent(true); // Clicks pass through when invisible
        }
    }

    // Método chamado pelo ScreenManager para passar os dados do usuário logado
    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("DEBUG (GestaoUsuariosController): E-mail: " + userEmail + ", Permissão: " + userPermission);

        updateSidebarVisibility(); // Configura qual sidebar é visível
        loadUsers(); // Carrega os usuários na tabela
    }

    private void loadUsers() {
        try {
            ObservableList<User> users = FXCollections.observableArrayList(userDAO.getAllUsers());
            usersTableView.setItems(users);
        } catch (SQLException e) {
            System.err.println("Erro ao carregar usuários: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível carregar os usuários.");
            e.printStackTrace();
        }
    }

    // --- Métodos de Ação dos Botões de Gestão ---

    // handleAddUser não é mais necessário para este FXML, pois o botão foi removido.
    // Se você o tiver em outro controlador, mantenha-o lá.

    @FXML
    private void handleEditUser(ActionEvent event) {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            System.out.println("Botão Editar Usuário clicado. Usuário selecionado: " + selectedUser.getEmail());
            showAlert(Alert.AlertType.INFORMATION, "Editar Usuário", "Funcionalidade de edição de usuário para " + selectedUser.getFullName() + " será implementada aqui.");
            // Ex: Abrir um novo stage (janela) com os dados do usuário para edição
            // Você pode criar um novo FXML para o formulário de edição (ex: EditarUsuarioForm.fxml)
            // ScreenManager.loadScreen((Node) event.getSource(), "EditarUsuarioForm.fxml", userEmail, userPermission, selectedUser);
            // O ScreenManager precisará de um método para passar um objeto complexo como 'selectedUser'
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhum Usuário Selecionado", "Por favor, selecione um usuário para editar.");
        }
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = usersTableView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Evita que o próprio usuário logado se exclua.
            // Adapte esta lógica se o email for único e usado como identificador.
            if (selectedUser.getEmail().equals(this.userEmail)) {
                showAlert(Alert.AlertType.WARNING, "Operação Não Permitida", "Você não pode excluir sua própria conta através desta interface.");
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Excluir Usuário?");
            alert.setContentText("Tem certeza que deseja excluir o usuário " + selectedUser.getFullName() + "? Esta ação não pode ser desfeita.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    userDAO.deleteUser(selectedUser.getId()); // Use o ID para excluir
                    loadUsers(); // Recarrega a lista após a exclusão
                    showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Usuário excluído com sucesso!");
                } catch (SQLException e) {
                    System.err.println("Erro ao excluir usuário: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erro de Banco de Dados", "Não foi possível excluir o usuário.");
                    e.printStackTrace();
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhum Usuário Selecionado", "Por favor, selecione um usuário para excluir.");
        }
    }

    // --- Métodos de Controle da Sidebar (MESMOS DOS OUTROS CONTROLADORES) ---

    @FXML
    private void handleMenuButtonClick(MouseEvent event) {
        System.out.println("handleMenuButtonClick: Botão de menu clicado em GestaoUsuarios!");
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
            activeSidebar.setMouseTransparent(false);
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(0); // Desliza para a posição 0 (visível)
            translateTransition.play();
        }
        sidebarVisible = true;
    }

    @FXML
    private void closeSidebar() {
        AnchorPane activeSidebar = getActiveSidebarPane();

        if (activeSidebar != null) {
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(-activeSidebar.getPrefWidth()); // Desliza para fora da tela
            translateTransition.setOnFinished(event -> {
                activeSidebar.setVisible(false);
                activeSidebar.setManaged(false);
                activeSidebar.setMouseTransparent(true);
            });
            translateTransition.play();
        }

        if (overlayPane != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.0);
            fadeTransition.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
            });
            fadeTransition.play();
        }
        sidebarVisible = false;
    }

    // Retorna a sidebar ativa com base na permissão do usuário
    private AnchorPane getActiveSidebarPane() {
        // USE A CAPITALIZAÇÃO CORRETA DA SUA PERMISSÃO (EX: "ADMIN" ou "admin")
        // Se o log mostrar "admin" minúsculo, use "admin".equals(userPermission) ou "admin".equalsIgnoreCase(userPermission)
        if (userPermission != null && userPermission.equalsIgnoreCase("ADMIN")) { // Usando equalsIgnoreCase para flexibilidade
            return sidebarAdminPane;
        } else {
            return sidebarPane;
        }
    }

    // Configura a visibilidade inicial das sidebars
    private void updateSidebarVisibility() {
        if (sidebarPane == null || sidebarAdminPane == null) {
            System.err.println("Sidebars não injetadas no FXML de GestaoUsuariosController.");
            return;
        }

        // Primeiro, esconde ambas as sidebars
        sidebarPane.setVisible(false);
        sidebarPane.setManaged(false);
        sidebarPane.setMouseTransparent(true);
        sidebarPane.setTranslateX(-sidebarPane.getPrefWidth()); // Garante que está fora da tela

        sidebarAdminPane.setVisible(false);
        sidebarAdminPane.setManaged(false);
        sidebarAdminPane.setMouseTransparent(true);
        sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth()); // Garante que está fora da tela

        // Depois, mostra a sidebar correta
        if (userPermission != null && userPermission.equalsIgnoreCase("ADMIN")) {
            sidebarAdminPane.setVisible(true);
            sidebarAdminPane.setManaged(true);
            sidebarAdminPane.setMouseTransparent(false);
        } else { // Para usuários comuns
            sidebarPane.setVisible(true);
            sidebarPane.setManaged(true);
            sidebarPane.setMouseTransparent(false);
        }
    }

    // Método auxiliar para exibir alertas
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // --- Métodos de Navegação da Sidebar (copie de outros controladores) ---
    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "HorasTrabalhadas.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", userEmail, userPermission);
    }
    
    @FXML
    private void handleGestao(ActionEvent event) {
        closeSidebar();
        // Já estamos na tela de gestão de usuários, não precisamos recarregá-la
        // Apenas fecha a sidebar se clicado nela mesma.
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        closeSidebar();
        // Este botão ainda existe na sidebar do admin e deve levar para a tela de cadastro
        ScreenManager.loadScreen((Node) event.getSource(), "Cadastro.fxml", userEmail, userPermission);
    }
    
    @FXML
    private void handleRelatorios(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", userEmail, userPermission);
    }
    
    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Configuracoes.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleSair(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Login.fxml", null, null);
    }
}