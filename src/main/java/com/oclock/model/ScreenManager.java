package com.oclock.model;

import com.oclock.controller.HorasTrabalhadasController;
import com.oclock.controller.MenuUserController;
import com.oclock.controller.RelatoriosController;
import com.oclock.controller.ConfiguracoesController;
import com.oclock.controller.GestaoUsuariosController;
import com.oclock.controller.CadastroController;
import com.oclock.controller.EditarController; // Importe o EditarController

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.Node;

public class ScreenManager {

    // Método existente para carregar telas sem um objeto User específico para edição
    public static void loadScreen(Node sourceNode, String fxmlPath, String email, String permission) {
        try {
            FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource("/com/oclock/view/" + fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof RelatoriosController) {
                ((RelatoriosController) controller).initData(email, permission);

            } else if (controller instanceof GestaoUsuariosController) {
                ((GestaoUsuariosController) controller).initData(email, permission);
            }
            else if (controller instanceof HorasTrabalhadasController) {
                ((HorasTrabalhadasController) controller).initData(email, permission);

            } else if (controller instanceof MenuUserController) {
                ((MenuUserController) controller).initData(email, permission);

            } else if (controller instanceof ConfiguracoesController) {
                ((ConfiguracoesController) controller).initData(email, permission);

            } else if (controller instanceof CadastroController) {
                ((CadastroController) controller).initData(email, permission);
            }
            // Não adicione EditarController aqui, pois usaremos a sobrecarga abaixo para ele

            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Erro ao carregar tela: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadScreen(Stage stage, String fxmlPath, User userToEdit, String currentUserEmail, String currentUserPermission) {
        try {
            FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource("/com/oclock/view/" + fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof EditarController) {
               
                ((EditarController) controller).initData(userToEdit, currentUserEmail, currentUserPermission);
            } else {
                
                System.err.println("ScreenManager.loadScreen(Stage, String, User, String, String) chamado para um controlador não compatível com 'User': " + controller.getClass().getName());
            }

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
            stage.centerOnScreen();
        } catch (IOException e) {
            System.err.println("Erro ao carregar tela de edição: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void loadLoginScreen(Node sourceNode) {
        try {
            FXMLLoader loader = new FXMLLoader(ScreenManager.class.getResource("/com/oclock/view/TelaLogin.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) sourceNode.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Login");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar TelaLogin.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}