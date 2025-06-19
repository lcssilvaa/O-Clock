package com.oclock.controller;

import com.oclock.model.ScreenManager;
import com.oclock.dao.UserDAO;
import com.oclock.model.User;

import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition; // Importe FadeTransition
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
    private AnchorPane overlayPane; // Este será o painel que cobre a tela
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
        // Inicializa sidebars fora da tela e os oculta/desabilita o gerenciamento
        if (sidebarAdminPane != null) {
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
            sidebarAdminPane.setVisible(false);
            sidebarAdminPane.setManaged(false);
        } else {
            System.err.println("ERRO: sidebarAdminPane é NULO. Verifique o fx:id no FXML.");
        }
        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
            sidebarPane.setVisible(false);
            sidebarPane.setManaged(false);
        } else {
            System.err.println("ERRO: sidebarPane é NULO. Verifique o fx:id no FXML.");
        }

        // Inicializa o overlay
        if (overlayPane != null) {
            overlayPane.setVisible(false); // Começa invisível
            overlayPane.setOpacity(0.0);   // Começa totalmente transparente
            // O clique no overlay deve fechar o sidebar
            overlayPane.setOnMouseClicked(event -> toggleSidebar());
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
        }

        // Configuração das transições (Verificar nulidade para evitar NullPointerException)
        if (sidebarAdminPane != null) {
            slideInAdmin = new TranslateTransition(Duration.seconds(0.3), sidebarAdminPane);
            slideInAdmin.setToX(0);
            slideOutAdmin = new TranslateTransition(Duration.seconds(0.3), sidebarAdminPane);
            slideOutAdmin.setToX(-sidebarAdminPane.getPrefWidth());
        }

        if (sidebarPane != null) {
            slideInUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
            slideInUser.setToX(0);
            slideOutUser = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
            slideOutUser.setToX(-sidebarPane.getPrefWidth());
        }

        // Correção aqui: colunaEmail.setCellValueFactory (sem o .PropertyFactory)
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colunaEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colunaPermissao.setCellValueFactory(new PropertyValueFactory<>("permission"));

        tabelaUsuarios.setItems(listaDeUsuarios);

        userDao = new UserDAO();

        carregarUsuarios();

        // Inicia a mensagem de status invisível
        mensagemStatus.setText("");
        mensagemStatus.setVisible(false);

        btnPesquisar.setOnAction(this::handlePesquisarUsuario);
        btnEditarUsuario.setOnAction(this::handleEditarUsuario);
        btnRemoverUsuario.setOnAction(this::handleRemoverUsuario);
        btnAtualizarTabela.setOnAction(this::handleAtualizarTabela);
    }

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
            if (sidebarAdminPane != null) {
                sidebarAdminPane.setVisible(false);
                sidebarAdminPane.setManaged(false);
            }
            if (sidebarPane != null) {
                sidebarPane.setVisible(true);
                sidebarPane.setManaged(true);
            }
        }
        carregarUsuarios();
    }

    @FXML
    private void handleMenuButton(MouseEvent event) {
        toggleSidebar();
    }

    private void toggleSidebar() {
        if (overlayPane == null) {
            System.err.println("OverlayPane não está injetado. Verifique o FXML.");
            return; // Sai do método para evitar NullPointerException
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
            fadeOut.setToValue(0.0); // Totalmente transparente
            fadeOut.play();
            fadeOut.setOnFinished(event -> {
                overlayPane.setVisible(false); // Esconde o overlay após o fade
                overlayPane.setMouseTransparent(true); // Torna transparente a cliques
            });

        } else {
            // Lógica para ABRIR o sidebar
            overlayPane.toFront(); // Coloca o overlay na frente de tudo (exceto o sidebar)
            overlayPane.setVisible(true); // Torna o overlay visível
            overlayPane.setMouseTransparent(false); // Permite cliques no overlay para fechar

            // Animação de fade-in para o overlayPane
            FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeIn.setFromValue(overlayPane.getOpacity());
            fadeIn.setToValue(0.4); // Exemplo: 40% de opacidade para escurecer a tela
            fadeIn.play();

            if (currentSidebar != null && currentSlideIn != null) {
                currentSidebar.toFront(); // Coloca o sidebar na frente do overlay
                currentSlideIn.play();
            }
        }
        isSidebarOpen = !isSidebarOpen;
    }

    private void carregarUsuarios() {
        listaDeUsuarios.clear();
        try {
            List<User> usuariosDoBanco = userDao.getAllUsers();
            listaDeUsuarios.addAll(usuariosDoBanco);
            mensagemStatus.setText("Usuários carregados do banco de dados.");
            mensagemStatus.setStyle("-fx-text-fill: green;"); // Estilo para sucesso
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erro ao Carregar Usuários", "Não foi possível carregar os usuários: " + e.getMessage());
            mensagemStatus.setText("Erro ao carregar usuários: " + e.getMessage());
            mensagemStatus.setStyle("-fx-text-fill: red;"); // Estilo para erro
        } finally {
            tabelaUsuarios.refresh();
            mensagemStatus.setVisible(true); // Garante que a mensagem fique visível após a operação
        }
    }

    @FXML
    private void handlePesquisarUsuario(ActionEvent event) {
        String termoPesquisa = campoPesquisa.getText().toLowerCase();
        if (termoPesquisa.isEmpty()) {
            carregarUsuarios();
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
                    userDao.deleteUser(usuarioSelecionado.getId());
                    listaDeUsuarios.remove(usuarioSelecionado);
                    mensagemStatus.setText("Usuário " + usuarioSelecionado.getFullName() + " removido com sucesso.");
                    mensagemStatus.setStyle("-fx-text-fill: green;");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Erro ao Remover Usuário", "Não foi possível remover o usuário: " + e.getMessage());
                    mensagemStatus.setText("Erro ao remover usuário: " + e.getMessage());
                    mensagemStatus.setStyle("-fx-text-fill: red;");
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

    // --- Métodos de Navegação ---
    // Agora, para navegar, vamos simplesmente chamar toggleSidebar() antes de carregar uma nova tela
    // se o sidebar estiver aberto. Isso garante que ele se feche suavemente.
    private void closeSidebarIfOpen() {
        if (isSidebarOpen) {
            toggleSidebar(); // Chama o toggle para fechar
        }
    }

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        closeSidebarIfOpen(); // Fecha o sidebar antes de navegar
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        closeSidebarIfOpen(); // Fecha o sidebar antes de navegar
        ScreenManager.loadScreen((Node) event.getSource(), "RegistrarMarcacao.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        // Se já está na tela de gestão e o sidebar está aberto, apenas fecha-o.
        // Se está na tela de gestão e o sidebar está fechado, não faz nada.
        // Não é necessário carregar a mesma tela.
        if (isSidebarOpen) {
            toggleSidebar();
        }
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        closeSidebarIfOpen(); // Fecha o sidebar antes de navegar
        ScreenManager.loadScreen((Node) event.getSource(), "Cadastro.fxml", currentUserEmail, currentUserPermission);
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        closeSidebarIfOpen(); // Fecha o sidebar antes de navegar
        showAlert(Alert.AlertType.INFORMATION, "Funcionalidade", "Exportar Relatório ainda não implementado.");
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        closeSidebarIfOpen(); // Fecha o sidebar antes de navegar
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