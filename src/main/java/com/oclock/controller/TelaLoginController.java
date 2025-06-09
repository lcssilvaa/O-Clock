package com.oclock.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.io.IOException;

import com.oclock.model.FazerLogin;

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

            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                FXMLLoader loader = null;
                Object controllerInstance = null;

                if ("admin".equalsIgnoreCase(permission)) {
                    loader = new FXMLLoader(getClass().getResource("/com/oclock/view/MenuUser.fxml"));
                    stage.setTitle("OnClock - Administrador");
                } else if ("usuario".equalsIgnoreCase(permission)) {
                    loader = new FXMLLoader(getClass().getResource("/com/oclock/view/MenuUser.fxml"));
                    stage.setTitle("OnClock - Usuário");
                } else {
                    if (mensagemErro != null) {
                        mensagemErro.setText("Permissão de usuário desconhecida.");
                        mensagemErro.setStyle("-fx-text-fill: orange;");
                        mensagemErro.setVisible(true);
                        mensagemErro.setManaged(true);
                    }
                    return;
                }

                if (loader != null) {
                    Parent root = loader.load();
                    controllerInstance = loader.getController();

                    if (controllerInstance != null) {
                        if (controllerInstance instanceof MenuUserController) {
                            MenuUserController menuUserController = (MenuUserController) controllerInstance;
                            menuUserController.initData(email, permission);
                        } else if (controllerInstance instanceof CadastroController) {
                            CadastroController cadastroController = (CadastroController) controllerInstance;
                            cadastroController.initData(email, permission);
                        }
                    }
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                }

            } catch (IOException e) {
                e.printStackTrace();
                if (mensagemErro != null) {
                    mensagemErro.setText("Erro ao carregar menu.");
                    mensagemErro.setStyle("-fx-text-fill: red;");
                    mensagemErro.setVisible(true);
                    mensagemErro.setManaged(true);
                }
            }
        } else {
            if (mensagemErro != null) {
                mensagemErro.setText("E-mail ou senha inválidos.");
                mensagemErro.setStyle("-fx-text-fill: red;");
                mensagemErro.setVisible(true);
                mensagemErro.setManaged(true);
            }
        }
    }

    @FXML
    private void onRedefinirSenha(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/RedefinirSenha.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Redefinir Senha");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}