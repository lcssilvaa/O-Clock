package com.oclock.controller;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent; // Import adicionado para MouseEvent

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.oclock.model.Cadastro; // Mantido, mas não usado diretamente neste controlador
import com.oclock.model.EmailValidator;
import com.oclock.model.FazerLogin;
import com.oclock.model.RedefinirSenha;
import com.oclock.model.ScreenManager;

public class RedefinirSenhaController implements Initializable {

    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoSenhaAtual;
    @FXML
    private PasswordField campoNovaSenha;

    @FXML
    private Label mensagemFeedback;

    @FXML
    private ImageView voltarLogin; // fx:id para a ImageView

    private FazerLogin fazerLoginService = new FazerLogin();
    private RedefinirSenha redefinirSenhaService = new RedefinirSenha();

    /**
     * Método de inicialização do controlador.
     * Não há necessidade de configurar um listener de clique para 'voltarLogin' aqui
     * se o método 'imagemVoltarLoginClick' estiver configurado no FXML.
     * @param url A localização usada para resolver caminhos relativos para o objeto raiz, ou null se a localização não for conhecida.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nada a inicializar explicitamente para a ImageView aqui,
        // pois o evento será tratado pelo método FXML onMouseClicked.
    }


    @FXML
    private void RedefinirSenhaButtonAction(ActionEvent event) {
        if (mensagemFeedback != null) {
            mensagemFeedback.setText("");
            mensagemFeedback.setStyle("-fx-text-fill: red;");
            mensagemFeedback.setVisible(true);
        }

        String email = campoEmail.getText();
        String senhaAtual = campoSenhaAtual.getText();
        String novaSenha = campoNovaSenha.getText();

        if (email.isEmpty() || senhaAtual.isEmpty() || novaSenha.isEmpty()) {
            if (mensagemFeedback != null) mensagemFeedback.setText("Todos os campos são obrigatórios.");
            return;
        }

        // Validações de senha
        if (novaSenha.length() < 4 || novaSenha.length() > 12) {
            if (mensagemFeedback != null) mensagemFeedback.setText("Erro: A senha deve ter entre 4 e 12 caracteres.");
            return;
        }
        if (!novaSenha.matches(".*[a-zA-Z].*")) {
            if (mensagemFeedback != null) mensagemFeedback.setText("Erro: A senha deve conter ao menos uma letra.");
            return;
        }
        if (!novaSenha.matches(".*[0-9].*")) {
            if (mensagemFeedback != null) mensagemFeedback.setText("Erro: A senha deve conter ao menos um número.");
            return;
        }
        if (!novaSenha.matches(".*[!@#$%^&()*_+=\\-\\[\\]{};':\"\\\\|,.<>/?].*")) { // Caracteres especiais expandidos
            if (mensagemFeedback != null) mensagemFeedback.setText("Erro: A senha deve conter ao menos um caractere especial.");
            return;
        }

        if (!EmailValidator.isValidEmail(email)) {
            if (mensagemFeedback != null) mensagemFeedback.setText("Erro: Email inválido.");
            return;
        }

        String permissao = fazerLoginService.fazerLoginWithCredentials(email, senhaAtual);

        if (permissao == null) {
            if (mensagemFeedback != null) mensagemFeedback.setText("Email ou senha atual incorretos.");
            return;
        }

        boolean sucessoAtualizacao = redefinirSenhaService.atualizarSenha(email, novaSenha);

        if (sucessoAtualizacao) {
            if (mensagemFeedback != null) {
                mensagemFeedback.setText("Senha redefinida com sucesso!");
                mensagemFeedback.setStyle("-fx-text-fill: green;");
            }

            campoEmail.clear();
            campoSenhaAtual.clear();
            campoNovaSenha.clear();

            // Volta para a tela de login após 2 segundos
            PauseTransition pause = new PauseTransition(Duration.seconds(2));
            pause.setOnFinished(e -> voltarParaLogin(event));
            pause.play();

        } else {
            if (mensagemFeedback != null) mensagemFeedback.setText("Erro ao redefinir a senha. Tente novamente.");
        }
    }

    @FXML
    private void voltarParaLogin(ActionEvent event) {
        // Usando ScreenManager para consistência na navegação
        ScreenManager.loadScreen((Node) event.getSource(), "TelaLogin.fxml", null, null);
    }


    @FXML
    private void imagemVoltarLoginClick(MouseEvent event) {
        // Cria um ActionEvent sintético para chamar o método que espera ActionEvent
        voltarParaLogin(new ActionEvent(event.getSource(), event.getTarget()));
    }
}