package com.oclock.controller;

import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Comparator;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import com.oclock.model.HorasTrabalhadas;
import com.oclock.model.HorasTrabalhadas.RegistroDiario;
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
    private AnchorPane sidebarPane, sidebarAdminPane;
    @FXML
    private ImageView botaoMenu;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;  
    @FXML private Button filterButton;     
    @FXML private Button clearFilterButton; 
    
    private boolean sidebarVisible = false;
    private String userEmail, userPermission;
    private HorasTrabalhadas horasTrabalhadasService = new HorasTrabalhadas(0, null, null); // Inicializar corretamente

    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("E-mail: " + userEmail + ", Permissão: " + userPermission);
        
        updateSidebarVisibility(); 
        carregarMarcacoesFiltradas(); // Chamar aqui para carregar os dados ao iniciar a tela
    }
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("HorasTrabalhadasController: Inicializado.");
        
        botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        overlayPane.setOnMouseClicked(this::handleOverlayClick);

        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
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
        }

        if (filterButton != null) {
            filterButton.setOnAction(this::handleFilterButtonAction);
        }
        if (clearFilterButton != null) {
            clearFilterButton.setOnAction(this::handleClearFilterButtonAction);
        }
    }

    private void updateSidebarVisibility() {
        boolean isAdmin = "admin".equalsIgnoreCase(userPermission);

        if (isAdmin) {
            if (sidebarAdminPane != null) {
                sidebarAdminPane.setVisible(true);
                sidebarAdminPane.setManaged(true);
                sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
            }
            if (sidebarPane != null) {
                sidebarPane.setVisible(false);
                sidebarPane.setManaged(false);
            }
        } else {
            if (sidebarAdminPane != null) {
                sidebarAdminPane.setVisible(false);
                sidebarAdminPane.setManaged(false);
            }
            if (sidebarPane != null) {
                sidebarPane.setVisible(true);
                sidebarPane.setManaged(true);
                sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
            }
        }
    }

    private AnchorPane getActiveSidebarPane() {
        return "admin".equalsIgnoreCase(userPermission) ? sidebarAdminPane : sidebarPane;
    }
        
    private void handleMenuButtonClick(MouseEvent event) {
        System.out.println("handleMenuButtonClick: Botão de menu clicado em HorasTrabalhadas!");
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
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(0);
            translateTransition.play();
        }
        sidebarVisible = true;
    }

    @FXML
    private void handleFilterButtonAction(ActionEvent event) {
        carregarMarcacoesFiltradas();
    }

    @FXML
    private void handleClearFilterButtonAction(ActionEvent event) {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        carregarMarcacoesFiltradas();
    }

    private void carregarMarcacoesFiltradas() {
        if (userEmail == null || userEmail.isEmpty()) {
            if (vboxMarcacoesPorDia != null) {
                vboxMarcacoesPorDia.getChildren().clear();
                Label errorLabel = new Label("Não foi possível carregar as marcações. Usuário não identificado.");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                vboxMarcacoesPorDia.getChildren().add(errorLabel);
            }
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            if (vboxMarcacoesPorDia != null) {
                vboxMarcacoesPorDia.getChildren().clear();
                Label errorLabel = new Label("Data de início não pode ser depois da data de fim.");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                vboxMarcacoesPorDia.getChildren().add(errorLabel);
            }
            return;
        }

        if (vboxMarcacoesPorDia != null) {
            vboxMarcacoesPorDia.getChildren().clear();
        }

        // Agora chamamos o método que retorna a lista de RegistroDiario
        List<RegistroDiario> registrosDiarios = 
            horasTrabalhadasService.buscarRegistrosDiariosPorEmail(userEmail);
        
        // Filtrar a lista no lado do cliente, se necessário, com base nas datas selecionadas
        List<RegistroDiario> registrosFiltrados = new ArrayList<>();
        for (RegistroDiario registro : registrosDiarios) {
            LocalDate dataRegistro = registro.getData();
            boolean passFilter = true;
            if (startDate != null && dataRegistro.isBefore(startDate)) {
                passFilter = false;
            }
            if (endDate != null && dataRegistro.isAfter(endDate)) {
                passFilter = false;
            }
            if (passFilter) {
                registrosFiltrados.add(registro);
            }
        }

        renderMarcacoes(registrosFiltrados);
    }

    private void renderMarcacoes(List<RegistroDiario> registrosDiarios) {
        if (registrosDiarios.isEmpty()) {
            Label noDataLabel = new Label("Nenhum registro de ponto encontrado para o período selecionado.");
            noDataLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
            if (vboxMarcacoesPorDia != null) {
                 vboxMarcacoesPorDia.getChildren().add(noDataLabel);
            }
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        registrosDiarios.stream()
            .sorted(Comparator.comparing(RegistroDiario::getData, Comparator.reverseOrder()))
            .forEach(registroDiario -> {
                LocalDate data = registroDiario.getData();
                LocalDateTime entrada = registroDiario.getEntrada();
                LocalDateTime saida = registroDiario.getSaida();
                String horasTrabalhadasFormatadas = registroDiario.getHorasTrabalhadas();

                VBox dailyContainer = new VBox(5);
                dailyContainer.setPadding(new Insets(12));
                dailyContainer.setStyle(
                    "-fx-background-color: #26292a;" +
                    "-fx-border-radius: 8;" +
                    "-fx-background-radius: 8;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0, 0, 6);" +
                    "-fx-cursor: hand;"
                );
                VBox.setMargin(dailyContainer, new Insets(0, 0, 10, 0));

                HBox header = new HBox(10);
                header.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(header, Priority.ALWAYS);

                Label dateLabel = new Label(data.format(dateFormatter)); 
                dateLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 16px;" +
                    "-fx-text-fill: #AAD7FF;"
                );
                HBox.setHgrow(dateLabel, Priority.ALWAYS);

                Label totalHoursSummaryLabel = new Label("Total: " + horasTrabalhadasFormatadas);
                totalHoursSummaryLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 14px;" +
                    "-fx-text-fill: #92B4F4;"
                );
                totalHoursSummaryLabel.setAlignment(Pos.CENTER_RIGHT);

                header.getChildren().addAll(dateLabel, totalHoursSummaryLabel);
                dailyContainer.getChildren().add(header);

                AnchorPane separator = new AnchorPane();
                separator.setPrefHeight(0.8);
                separator.setStyle("-fx-background-color: #3F5E82; -fx-opacity: 0.8;");
                VBox.setMargin(separator, new Insets(0, 0, 10, 0));
                dailyContainer.getChildren().add(separator);

                VBox expandableContent = new VBox(5);
                expandableContent.setManaged(false);
                expandableContent.setVisible(false);
                expandableContent.setOpacity(0.0);

                // Exibir Entrada e Saída
                if (entrada != null) {
                    Label entryLabel = new Label("Entrada: " + entrada.format(timeFormatter));
                    entryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7CFC00;");
                    expandableContent.getChildren().add(entryLabel);
                } else {
                    Label noEntryLabel = new Label("Entrada: -");
                    noEntryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700;");
                    expandableContent.getChildren().add(noEntryLabel);
                }
                
                if (saida != null) {
                    Label exitLabel = new Label("Saída: " + saida.format(timeFormatter));
                    exitLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF6347;");
                    expandableContent.getChildren().add(exitLabel);
                } else {
                    Label pendingExitLabel = new Label("Saída: Pendente");
                    pendingExitLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FFD700; -fx-font-weight: bold;");
                    expandableContent.getChildren().add(pendingExitLabel);
                }
                
                dailyContainer.getChildren().add(expandableContent);

                dailyContainer.setOnMouseClicked(event -> {
                    boolean isVisible = expandableContent.isManaged();
                    Timeline timeline = new Timeline();

                    if (isVisible) {
                        timeline.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO,
                                new KeyValue(expandableContent.prefHeightProperty(), expandableContent.getHeight()),
                                new KeyValue(expandableContent.opacityProperty(), 1.0)
                            ),
                            new KeyFrame(Duration.millis(200),
                                new KeyValue(expandableContent.prefHeightProperty(), 0, Interpolator.EASE_BOTH),
                                new KeyValue(expandableContent.opacityProperty(), 0.0, Interpolator.EASE_BOTH)
                            )
                        );
                        timeline.setOnFinished(e -> {
                            expandableContent.setVisible(false);
                            expandableContent.setManaged(false);
                        });
                    } else {
                        expandableContent.setVisible(true);
                        expandableContent.setManaged(true);
                        
                        expandableContent.applyCss();
                        expandableContent.layout();
                        double targetHeight = expandableContent.prefHeight(-1);
                        
                        timeline.getKeyFrames().addAll(
                            new KeyFrame(Duration.ZERO,
                                new KeyValue(expandableContent.prefHeightProperty(), 0),
                                new KeyValue(expandableContent.opacityProperty(), 0.0)
                            ),
                            new KeyFrame(Duration.millis(200),
                                new KeyValue(expandableContent.prefHeightProperty(), targetHeight, Interpolator.EASE_BOTH),
                                new KeyValue(expandableContent.opacityProperty(), 1.0, Interpolator.EASE_BOTH)
                            )
                        );
                    }
                    timeline.play();
                });

                final String defaultStyle = dailyContainer.getStyle();
                final String hoverStyle = "-fx-background-color: #313536; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 15, 0, 0, 8); -fx-cursor: hand;";

                dailyContainer.setOnMouseEntered(e -> dailyContainer.setStyle(hoverStyle));
                dailyContainer.setOnMouseExited(e -> dailyContainer.setStyle(defaultStyle));

                if (vboxMarcacoesPorDia != null) {
                    vboxMarcacoesPorDia.getChildren().add(dailyContainer);
                }
            });
    }

    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

    private void closeSidebar() {
        AnchorPane activeSidebar = getActiveSidebarPane(); // Use activeSidebar para fechar corretamente

        if (overlayPane != null) {
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.3), overlayPane);
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0);
            fadeTransition.play();
            fadeTransition.setOnFinished(event -> {
                overlayPane.setVisible(false);
                overlayPane.setMouseTransparent(true);
            });
        }

        if (activeSidebar != null) { // Fechar a sidebar ativa
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(-activeSidebar.getPrefWidth());
            translateTransition.play();
            translateTransition.setOnFinished(event -> {
                activeSidebar.setVisible(false); // Esconder completamente após animação
                activeSidebar.setManaged(false); // Parar de gerenciar espaço
                activeSidebar.setMouseTransparent(true);
            });
        }
        sidebarVisible = false;
    }

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        System.out.println("Clicou em Consultar Horas Trabalhadas (já nesta tela).");
        closeSidebar();
        carregarMarcacoesFiltradas(); 
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
                controller.initData(userEmail, userPermission);
            } else {
                System.err.println("Erro: Controlador de Configurações é nulo ao voltar.");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Configurações");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar Configurações.fxml: " + e.getMessage());
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

    @FXML
    private void handleGestao(ActionEvent event) {
        System.out.println("Teste");
        closeSidebar();
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
    	System.out.println("Clicou em Cadastro");
        closeSidebar();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/TelaCadastro.fxml"));
            Parent root = loader.load();

            CadastroController controller = loader.getController();
            if (controller != null) {
                controller.initData(userEmail, userPermission);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Cadastro de Usuários");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar TelaCadastro.fxml para registrar marcação: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        System.out.println("Teste");
        closeSidebar();
    }
}