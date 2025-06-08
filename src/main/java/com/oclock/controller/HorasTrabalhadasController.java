package com.oclock.controller;

import javafx.util.Duration;
import java.util.Comparator;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import com.oclock.model.HorasTrabalhadas;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class HorasTrabalhadasController implements Initializable {

	private final Locale BRAZIL_LOCALE = new Locale("pt", "BR");

	@FXML
    private VBox vboxMarcacoesPorDia;

    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private ImageView botaoMenu;
    @FXML
    private ImageView arrowIcon = new ImageView();
    
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

        if (vboxMarcacoesPorDia != null) {
            vboxMarcacoesPorDia.getChildren().clear();
        }

        Map<LocalDate, List<LocalDateTime>> marcacoes = horasTrabalhadasService.getMarcacoesPorUsuario(emailUsuarioLogado);

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

        // Ordenar as datas para exibir em ordem cronológica inversa (mais recentes primeiro)
        marcacoes.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
            .forEach(entry -> {
                LocalDate data = entry.getKey();
                List<LocalDateTime> pontosDoDia = entry.getValue();
                pontosDoDia.sort(LocalDateTime::compareTo); // Garante que os pontos estão em ordem cronológica

                // --- CONTAINER PRINCIPAL DO DIA (O QUE TERÁ A BORDA E O COMPORTAMENTO DE EXPANDIR) ---
                VBox dailyContainer = new VBox(5);
                dailyContainer.setPadding(new Insets(12));
                dailyContainer.setStyle(
                    "-fx-background-color: #26292a;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 6);" +
                    "-fx-cursor: hand;" // Adiciona um cursor de mão para indicar que é clicável
                );
                VBox.setMargin(dailyContainer, new Insets(0, 0, 10, 0)); // Margem inferior entre os dias

                // --- CABEÇALHO CLICÁVEL (DATA + TOTAL DO DIA NO MESMO HBOX) ---
                HBox header = new HBox(10); // Espaçamento entre a data e o total
                header.setAlignment(Pos.CENTER_LEFT); // Alinha o conteúdo à esquerda
                HBox.setHgrow(header, Priority.ALWAYS); // Permite que o cabeçalho preencha a largura

                // Label da Data com Dia da Semana
                Label dateLabel = new Label(data.format(dateFormatter));
                dateLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 16px;" +
                    "-fx-text-fill: #AAD7FF;"
                );
                HBox.setHgrow(dateLabel, Priority.ALWAYS); // Faz a data ocupar o espaço e empurrar o total para a direita

                // Calcular o total de horas do dia para o cabeçalho
                long totalSecondsToday = 0;
                if (pontosDoDia.size() >= 2) {
                    for (int i = 0; i < pontosDoDia.size() - 1; i += 2) {
                        LocalDateTime entryTime = pontosDoDia.get(i);
                        LocalDateTime exitTime = pontosDoDia.get(i + 1);
                        if (exitTime.isAfter(entryTime)) {
                           totalSecondsToday += java.time.Duration.between(entryTime, exitTime).getSeconds(); // CORRIGIDO AQUI
                        }
                    }
                }
                java.time.Duration totalDurationToday = java.time.Duration.ofSeconds(totalSecondsToday); // CORRIGIDO AQUI
                Label totalHoursSummaryLabel = new Label(
                    String.format("Total: %02d:%02d:%02d",
                        totalDurationToday.toHours(),
                        totalDurationToday.toMinutesPart(),
                        totalDurationToday.toSecondsPart())
                );
                totalHoursSummaryLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #92B4F4;" // Cor para o total
                );
                totalHoursSummaryLabel.setAlignment(Pos.CENTER_RIGHT); // Alinha o texto da label à direita

                header.getChildren().addAll(dateLabel, totalHoursSummaryLabel);
                dailyContainer.getChildren().add(header);


                // --- SEPARADOR ---
                AnchorPane separator = new AnchorPane();
                separator.setPrefHeight(0.8);
                separator.setStyle("-fx-background-color: #3F5E82; -fx-opacity: 0.8;");
                VBox.setMargin(separator, new Insets(0, 0, 10, 0));
                dailyContainer.getChildren().add(separator);


                // --- CONTEÚDO EXPANSÍVEL (PARES DE HORAS E DURAÇÃO) ---
                VBox expandableContent = new VBox(5);
                expandableContent.setManaged(false); // Não ocupa espaço quando está colapsado (para animação)
                expandableContent.setVisible(false); // Invisível inicialmente
                expandableContent.setOpacity(0.0); // Começa com opacidade 0 para o fade

                // Adicionar os pares de entrada/saída e suas durações
                for (int i = 0; i < pontosDoDia.size(); i++) {
                    LocalDateTime ponto = pontosDoDia.get(i);
                    Label pointLabel;

                    if (i % 2 == 0) { // Entrada
                        pointLabel = new Label("Entrada: " + ponto.format(timeFormatter));
                        pointLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7CFC00;"); // Verde para entrada
                        expandableContent.getChildren().add(pointLabel); // Adiciona a entrada imediatamente
                    } else { // Saída
                        LocalDateTime entryTime = pontosDoDia.get(i - 1);
                        pointLabel = new Label("Saída: " + ponto.format(timeFormatter));
                        pointLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF6347;"); // Vermelho para saída

                        // Calcula e exibe a duração do bloco de trabalho
                        if (ponto.isAfter(entryTime)) {
                            java.time.Duration blockDuration = java.time.Duration.between(entryTime, ponto); // CORRIGIDO AQUI
                            Label durationLabel = new Label(
                                String.format("(%02d:%02d:%02d)",
                                    blockDuration.toHours(),
                                    blockDuration.toMinutesPart(),
                                    blockDuration.toSecondsPart())
                            );
                            durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ADD8E6; -fx-padding: 0 0 0 15;"); // Azul claro
                            HBox blockInfo = new HBox(5, pointLabel, durationLabel); // Agrupa ponto e duração
                            blockInfo.setAlignment(Pos.CENTER_LEFT);
                            expandableContent.getChildren().add(blockInfo);
                        } else {
                            expandableContent.getChildren().add(pointLabel); // Adiciona só o ponto se a saída não é válida
                        }
                    }
                }
                
                // Lidar com ponto de entrada sem saída correspondente
                if (pontosDoDia.size() % 2 != 0) {
                    Label pendingExitLabel = new Label("• Saída pendente");
                    pendingExitLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700; -fx-font-weight: bold;"); // Amarelo para pendente
                    expandableContent.getChildren().add(pendingExitLabel);
                }


                dailyContainer.getChildren().add(expandableContent);


                // --- LÓGICA DE ANIMAÇÃO DE EXPANSÃO/COLAPSO ---
                dailyContainer.setOnMouseClicked(event -> {
                    boolean isVisible = expandableContent.isManaged();

                    Timeline timeline = new Timeline();
                    // FadeTransition ft = new FadeTransition(Duration.millis(200), expandableContent); // Removido, opacidade controlada pela Timeline

                    if (isVisible) { // Se está visível (expandido), vai colapsar
                        timeline.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO, // MANTENHA javafx.util.Duration AQUI
                                new KeyValue(expandableContent.prefHeightProperty(), expandableContent.getHeight()),
                                new KeyValue(expandableContent.opacityProperty(), 1.0)
                            ),
                            new KeyFrame(javafx.util.Duration.millis(200), // MANTENHA javafx.util.Duration AQUI
                                new KeyValue(expandableContent.prefHeightProperty(), 0, Interpolator.EASE_BOTH), // CORRIGIDO AQUI
                                new KeyValue(expandableContent.opacityProperty(), 0.0, Interpolator.EASE_BOTH) // CORRIGIDO AQUI
                            )
                        );
                        timeline.setOnFinished(e -> {
                            expandableContent.setVisible(false);
                            expandableContent.setManaged(false);
                        });
                    } else { // Se está invisível (colapsado), vai expandir
                        expandableContent.setVisible(true);
                        expandableContent.setManaged(true); // Começa a ocupar espaço (mas altura ainda 0)
                        
                        // Garante que o layout seja calculado antes de pegar a altura.
                        expandableContent.applyCss();
                        expandableContent.layout();
                        double targetHeight = expandableContent.prefHeight(-1); // Calcula a altura necessária
                        
                        timeline.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO, // MANTENHA javafx.util.Duration AQUI
                                new KeyValue(expandableContent.prefHeightProperty(), 0),
                                new KeyValue(expandableContent.opacityProperty(), 0.0)
                            ),
                            new KeyFrame(javafx.util.Duration.millis(200), // MANTENHA javafx.util.Duration AQUI
                                new KeyValue(expandableContent.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH), // CORRIGIDO AQUI
                                new KeyValue(expandableContent.opacityProperty(), 1.0, Interpolator.EASE_BOTH) // CORRIGIDO AQUI
                            )
                        );
                    }
                    // timeline.setInterpolator(Interpolator.EASE_BOTH); // REMOVIDO AQUI

                    timeline.play();
                });

                // --- Efeito de Hover (Opcional, mas melhora a UX) ---
                final String defaultStyle = dailyContainer.getStyle(); // Guarda o estilo original
                final String hoverStyle = "-fx-background-color: #313536; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 8); -fx-cursor: hand;"; // Estilo ao passar o mouse

                dailyContainer.setOnMouseEntered(e -> dailyContainer.setStyle(hoverStyle));
                dailyContainer.setOnMouseExited(e -> dailyContainer.setStyle(defaultStyle));


                // Adiciona o dailyContainer completo ao vboxMarcacoesPorDia pai
                if (vboxMarcacoesPorDia != null) {
                    vboxMarcacoesPorDia.getChildren().add(dailyContainer);
                }
            });
    }
  
  
    private void handleMenuButtonClick(MouseEvent event) {
    	System.out.println("handleMenuButtonClick: Botão de menu clicado em HorasTrabalhadas!");
        if (overlayPane != null) {
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false); 
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane); 
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.4);
            fadeTransition.play();
        }

        if (sidebarPane != null) {
            sidebarPane.setMouseTransparent(false);
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), sidebarPane);
            translateTransition.setToX(0); 
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