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
import javafx.scene.control.CheckBox;
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
import javafx.scene.control.Button;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.oclock.model.BaterPonto;
import com.oclock.model.Cadastro;

public class CadastroController implements Initializable {

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

    private Cadastro cadastroService = new Cadastro();
    
    private Timeline timeline;
    private String userEmail, userPermission;

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
    
    public CadastroController() {
    	this.cadastroService = new Cadastro();
    }

    @FXML
    private void cadastrarUsuario(ActionEvent event) {
    	System.out.println("DEBUG: botão cadastrar apertado!");
       
        if (campoNome == null || campoEmail == null || campoSenha == null || campoCPF == null ||
            userUsuarioRadio == null || userAdminRadio == null || mensagemErro == null) {
            System.err.println("ERRO: Nem todos os campos de cadastro estão mapeados no FXML. Verifique os fx:id.");
            exibirMensagem("Erro interno: Campos não carregados. Contate o suporte.", Color.RED);
            return;
        }

        String nome = campoNome.getText();
        String email = campoEmail.getText();
        String senha = campoSenha.getText();
        String cpf = campoCPF.getText();

        String permissao = "";
        
        if (permissaoToggleGroup.getSelectedToggle() == userUsuarioRadio) {
            permissao = "usuario"; 
        } else if (permissaoToggleGroup.getSelectedToggle() == userAdminRadio) {
            permissao = "admin";
        } else {
            exibirMensagem("Selecione um tipo de usuário (Usuário ou Administrador).", Color.RED);
            return;
        }

        
        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || cpf.isEmpty()) {
            exibirMensagem("Todos os campos são obrigatórios!", Color.RED);
            return;
        }

        if (!cpf.matches("\\d{11}")) {
            exibirMensagem("CPF deve conter exatamente 11 dígitos numéricos.", Color.RED);
            return;
        }
        
        if(senha.length() > 8 && senha.length() < 15) {
        	exibirMensagem("A senha deve conter mais que 8 caracteres e menos que 15", Color.RED);
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
        
        if (!senha.matches(".*[!@#$%^&*()-+=].*")) {
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

    private void exibirMensagem(String mensagem, Color cor) {
        if (mensagemErro != null) {
            mensagemErro.setText(mensagem);
            mensagemErro.setTextFill(cor);
            mensagemErro.setVisible(true);
            mensagemErro.setManaged(true);
        }
    }

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
    
    @FXML
    private void handleCadastrar(ActionEvent event) {
        System.out.println("Clicou em Consultar Horas Trabalhadas (já nesta tela).");
        closeSidebar();
 
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        System.out.println("Teste");
        closeSidebar();
    }
}