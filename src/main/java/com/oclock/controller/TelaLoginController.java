package com.oclock.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;

import com.oclock.model.FazerLogin;
import com.oclock.model.ScreenManager;

public class TelaLoginController {

    @FXML
    private TextField campoEmail;
    @FXML
    private PasswordField campoSenha;
    @FXML
    private Label mensagemErro;
    @FXML
    private Hyperlink redefinirSenha; 

    private FazerLogin fazerLogin = new FazerLogin();

    /**
     * Lida com a ação do botão de login. Tenta autenticar o usuário e, se bem-sucedido,
     * navega para a tela apropriada (MenuUser para ambos, admin e usuário), passando
     * os dados do usuário.
     * @param event O evento de ação que acionou este método.
     */
    @FXML
    private void LoginButtonAction(ActionEvent event) {
        if (mensagemErro != null) {
            mensagemErro.setVisible(false);
            mensagemErro.setManaged(false);
            mensagemErro.setText("");
        }

        String email = campoEmail.getText();
        String senha = campoSenha.getText();

        String permission = fazerLogin.fazerLoginWithCredentials(email, senha);

        if (permission != null && !permission.isEmpty()) {
            if (mensagemErro != null) {
                mensagemErro.setText("Login realizado com sucesso!");
                mensagemErro.setStyle("-fx-text-fill: green;");
                mensagemErro.setVisible(true);
                mensagemErro.setManaged(true);
            }

            // Usando ScreenManager para carregar a tela após o login
            // Ambos os perfis (admin e usuario) vão para MenuUser,
            // e o MenuUserController lida com a exibição correta dos elementos.
            ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", email, permission);

            // Limpa os campos após o login bem-sucedido para segurança e usabilidade
            campoEmail.clear();
            campoSenha.clear();

        } else {
            if (mensagemErro != null) {
                mensagemErro.setText("E-mail ou senha inválidos.");
                mensagemErro.setStyle("-fx-text-fill: red;");
                mensagemErro.setVisible(true);
                mensagemErro.setManaged(true);
            }
        }
    }

    /**
     * Lida com a ação do hyperlink "Redefinir Senha". Navega para a tela de redefinição de senha.
     * @param event O evento de ação que acionou este método.
     */
    @FXML
    private void onRedefinirSenha(ActionEvent event) {
        // Usando ScreenManager para navegar para a tela de redefinição de senha
        ScreenManager.loadScreen((Node) event.getSource(), "RedefinirSenha.fxml", null, null);
    }
}