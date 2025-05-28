package com.oclock.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView; 
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

import com.oclock.model.Cadastro;
import com.oclock.model.EmailValidator;
import com.oclock.model.FazerLogin;
import com.oclock.model.RedefinirSenha;

public class RedefinirSenhaController {

    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoSenhaAtual;
    @FXML
    private PasswordField campoNovaSenha;

    @FXML
    private Label mensagemFeedback;
    
    @FXML
    private ImageView voltarLogin;

    private FazerLogin fazerLoginService = new FazerLogin();
    private RedefinirSenha redefinirSenhaService = new RedefinirSenha();

    @FXML
    private void RedefinirSenhaButtonAction(ActionEvent event) {
        mensagemFeedback.setText("");
        mensagemFeedback.setStyle("-fx-text-fill: red;");

        String email = campoEmail.getText();
        String senhaAtual = campoSenhaAtual.getText();
        String novaSenha = campoNovaSenha.getText();

        if (email.isEmpty() || senhaAtual.isEmpty() || novaSenha.isEmpty()) {
            mensagemFeedback.setText("Todos os campos são obrigatórios.");
            return;
        }

   
        if (novaSenha.length() < 4 || novaSenha.length() > 12) {
            mensagemFeedback.setText("Erro: A senha deve ter entre 4 e 12 caracteres.");
            return;
        }
        if (!novaSenha.matches(".*[a-zA-Z].*")) {
            mensagemFeedback.setText("Erro: A senha deve conter ao menos uma letra.");
            return;
        }
        if (!novaSenha.matches(".*[0-9].*")) {
            mensagemFeedback.setText("Erro: A senha deve conter ao menos um número.");
            return;
        }
        if (!novaSenha.matches(".*[!@#$%^&()+=\\-].*")) {
            mensagemFeedback.setText("Erro: A senha deve conter ao menos um caractere especial.");
            return;
        }

        if (!EmailValidator.isValidEmail(email)) {
            mensagemFeedback.setText("Erro: Email inválido.");
            return;
        }
        
        String permissao = fazerLoginService.fazerLoginWithCredentials(email, senhaAtual);

        if (permissao == null) {
            
            mensagemFeedback.setText("Email ou senha atual incorretos.");
            return;
        }

        boolean sucessoAtualizacao = redefinirSenhaService.atualizarSenha(email, novaSenha);

        if (sucessoAtualizacao) {
            mensagemFeedback.setText("Senha redefinida com sucesso!");
            mensagemFeedback.setStyle("-fx-text-fill: green;");
            
            campoEmail.clear();
            campoSenhaAtual.clear();
            campoNovaSenha.clear();
            
            voltarParaLogin(event);
      
        } else {
            mensagemFeedback.setText("Erro ao redefinir a senha. Tente novamente.");
            
        }
    }

    @FXML
    private void voltarParaLogin(ActionEvent event) {
        try {
           
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/TelaLogin.fxml"));
            Parent root = loader.load();
      
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Login"); 
            stage.show(); 
            
        } catch (IOException e) {
            System.err.println("Erro ao carregar TelaLogin.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void imagemVoltarLoginClick(javafx.scene.input.MouseEvent event) {
        voltarParaLogin(new ActionEvent(event.getSource(), event.getTarget()));
    }

}