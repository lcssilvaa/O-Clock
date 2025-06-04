package com.oclock.controller;

import javafx.util.Duration;
import java.util.Comparator;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import com.oclock.model.HorasTrabalhadas;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class HorasTrabalhadasController implements Initializable {

    @FXML
    private VBox vboxMarcacoesPorDia;

    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private ImageView botaoMenu;

    private boolean sidebarVisible = false;

    private String emailUsuarioLogado;
    private HorasTrabalhadas horasTrabalhadasService = new HorasTrabalhadas();

    public void initData(String email) {
        this.emailUsuarioLogado = email;
        System.out.println("HorasTrabalhadasController: Email do usuário recebido: " + emailUsuarioLogado);
        carregarMarcacoes();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("HorasTrabalhadasController: Inicializado.");

        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
        }

        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
        }

        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        }
        if (overlayPane != null) {
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        }
        if (vboxMarcacoesPorDia != null) {
            vboxMarcacoesPorDia.setStyle("-fx-background-color: transparent;");
        }
    }

    private void carregarMarcacoes() {
        
        if (emailUsuarioLogado == null || emailUsuarioLogado.isEmpty()) {
            System.err.println("Erro: Email do usuário não definido para carregar marcações em HorasTrabalhadasController.");
            Label errorLabel = new Label("Não foi possível carregar as marcações. Usuário não identificado.");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            if (vboxMarcacoesPorDia != null) {
                 vboxMarcacoesPorDia.getChildren().add(errorLabel);
            }
            return;
        }

        // Limpa qualquer conteúdo anterior do VBox principal para exibir as novas marcações
        if (vboxMarcacoesPorDia != null) {
            vboxMarcacoesPorDia.getChildren().clear();
        }

        // Busca as marcações de ponto do usuário no serviço (model)
        Map<LocalDate, List<LocalDateTime>> marcacoes = horasTrabalhadasService.getMarcacoesPorUsuario(emailUsuarioLogado);

        // Verifica se não há marcações encontradas
        if (marcacoes.isEmpty()) {
            Label noDataLabel = new Label("Nenhuma marcação de ponto encontrada para este usuário.");
            noDataLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
            if (vboxMarcacoesPorDia != null) {
                 vboxMarcacoesPorDia.getChildren().add(noDataLabel);
            }
            System.out.println("DEBUG: Nenhuma marcação encontrada no banco de dados para " + emailUsuarioLogado);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        for (Map.Entry<LocalDate, List<LocalDateTime>> entry : marcacoes.entrySet()) {
            LocalDate data = entry.getKey();
            List<LocalDateTime> pontosDoDia = entry.getValue();

            // Cria um VBox para representar o 'card' de cada dia de marcação
            VBox dailyBox = new VBox(5);

            // Define a largura do card
            if (vboxMarcacoesPorDia != null) {
                dailyBox.setPrefWidth(vboxMarcacoesPorDia.getPrefWidth() - 20);
            } else {
                dailyBox.setPrefWidth(300); // Largura padrão de emergência
            }

            // --- ESTILO AINDA MAIS BONITO PARA O CARD ---
            dailyBox.setStyle(
                "-fx-background-color: #26292a;" + // Fundo do card cinza escuro (o mesmo do sidebar)
                "-fx-padding: 12;" +              // Preenchimento interno
                "-fx-border-radius: 8;" +         // Arredondamento das bordas
                "-fx-background-radius: 8;" +     // Arredondamento do fundo
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 6);" // Sombra um pouco mais pronunciada
            );

            // Estilo para a data (topo do card)
            Label dateLabel = new Label(data.format(dateFormatter));
            dateLabel.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-font-size: 16px;" +
                "-fx-text-fill: #AAD7FF;" + // Azul claro vibrante para a data
                "-fx-padding: 0 0 2 0;" // Pequeno padding inferior
            );
            dailyBox.getChildren().add(dateLabel);

            // Linha separadora: mais fina e um tom de azul que combine
            AnchorPane separator = new AnchorPane();
            separator.setPrefHeight(0.8); // Linha mais fina
            separator.setPrefWidth(dailyBox.getPrefWidth() - 30);
            separator.setStyle("-fx-background-color: #3F5E82; -fx-opacity: 0.8;"); // Azul um pouco mais escuro e sólido
            javafx.scene.layout.VBox.setMargin(separator, new javafx.geometry.Insets(0, 0, 10, 0)); // Margem inferior maior
            dailyBox.getChildren().add(separator);

            // Estilo para as horas marcadas
            for (LocalDateTime ponto : pontosDoDia) {
                Label timeLabel = new Label("• " + ponto.format(timeFormatter));
                timeLabel.setStyle(
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #FFFFFF;" + // Branco puro para as horas
                    "-fx-padding: 2 0 2 0;" // Pequeno padding vertical
                );
                dailyBox.getChildren().add(timeLabel);
            }
            if (vboxMarcacoesPorDia != null) {
                 vboxMarcacoesPorDia.getChildren().add(dailyBox);
            }
            System.out.println("DEBUG: Adicionada marcação para o dia: " + data);
            
            long totalSecondsToday = 0;
            if (pontosDoDia.size() >= 2) {
                
                pontosDoDia.sort(LocalDateTime::compareTo);

                // abaixo, calcula a hora trabalhada no dia
                
                for (int i = 0; i < pontosDoDia.size() - 1; i += 2) {
                    LocalDateTime entryTime = pontosDoDia.get(i);
                    LocalDateTime exitTime = pontosDoDia.get(i + 1);
                    if (exitTime.isAfter(entryTime)) {
                       
                        totalSecondsToday += java.time.Duration.between(entryTime, exitTime).getSeconds();
                    }
                }

                java.time.Duration totalDuration = java.time.Duration.ofSeconds(totalSecondsToday);
                long hours = totalDuration.toHours();
                long minutes = totalDuration.toMinutesPart();
                long seconds = totalDuration.toSecondsPart();

            Label totalHoursLabel = new Label(
                String.format("Total: %02d:%02d:%02d", hours, minutes, seconds)
            );
            totalHoursLabel.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-text-fill: #92B4F4;" + // Um tom de azul diferente ou verde para destaque
                "-fx-alignment: center-right;" // Alinha à direita para contraste
            );
            dailyBox.getChildren().add(totalHoursLabel);
            }
        }
    }
    private void handleMenuButtonClick(MouseEvent event) {
    	System.out.println("handleMenuButtonClick: Botão de menu clicado em HorasTrabalhadas!");
        if (overlayPane != null) {
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false); // Torna o overlay interativo para que possa ser clicado
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane); // Define a duração e o nó
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.4);
            fadeTransition.play();
        }

        if (sidebarPane != null) {
            sidebarPane.setMouseTransparent(false); // Torna o sidebar interativo
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), sidebarPane); // Define a duração e o nó
            translateTransition.setToX(0); // Move para a posição visível
            translateTransition.play();
        }
        sidebarVisible = true;
    }

    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

    private void closeSidebar() {
    	if (overlayPane != null) {
            
            FadeTransition fadeTransition = new FadeTransition(javafx.util.Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0);
            fadeTransition.play();
            fadeTransition.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
            });
        }

        if (sidebarPane != null) {
            
            TranslateTransition translateTransition = new TranslateTransition(javafx.util.Duration.seconds(0.3), sidebarPane);
            translateTransition.setToX(-sidebarPane.getPrefWidth());
            translateTransition.play();
            translateTransition.setOnFinished(event -> {
                sidebarPane.setMouseTransparent(true);
            });
        }
        sidebarVisible = false;
    }

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        System.out.println("Clicou em Consultar Horas Trabalhadas (já nesta tela).");
        closeSidebar();
        carregarMarcacoes();
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
                controller.setUserEmail(emailUsuarioLogado);
            } else {
                System.err.println("Erro: Controlador de MenuUser é nulo ao voltar.");
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
    private void handleConfiguracoes(ActionEvent event) {
    	System.out.println("Clicou em Configurações");
        closeSidebar();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/Configuracoes.fxml"));
            Parent root = loader.load();

            ConfiguracoesController controller = loader.getController();
            if (controller != null) {
                controller.initData(emailUsuarioLogado);
            } else {
                System.err.println("Erro: Controlador de HorasTrabalhadas é nulo ao voltar.");
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
    private void handleSair(ActionEvent event) {
        System.out.println("Clicou em Sair.");
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
}