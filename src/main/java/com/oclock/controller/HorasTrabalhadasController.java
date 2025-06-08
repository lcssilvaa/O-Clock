package com.oclock.controller;

import javafx.util.Duration;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    // Removido: private ImageView arrowIcon = new ImageView(); // Não está no FXML e não é usado
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;  
    @FXML private Button filterButton;     
    @FXML private Button clearFilterButton; 
    
    private boolean sidebarVisible = false;
    private String emailUsuarioLogado;
    // CORREÇÃO: Usando a classe de serviço, não o modelo
    private HorasTrabalhadas horasTrabalhadasService = new HorasTrabalhadas(); 

    public void initData(String email) {
        this.emailUsuarioLogado = email;
        System.out.println("HorasTrabalhadasController: Email do usuário recebido: " + emailUsuarioLogado);
        
        // CORREÇÃO: Definir valores padrão dos DatePickers aqui, após o email ser definido
        if (startDatePicker != null) {
            startDatePicker.setValue(LocalDate.now().minusMonths(1)); // Padrão: último mês
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(LocalDate.now()); // Padrão: hoje
        }
        
        carregarMarcacoesFiltradas();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("HorasTrabalhadasController: Inicializado.");

        // Opcional: Apenas para garantir que os DatePickers não iniciem com valores nulos
        // caso initData() não seja chamado imediatamente após a inicialização.
        // No entanto, como initData() chamará carregarMarcacoesFiltradas,
        // a definição das datas lá é suficiente.
        // Se você não tiver um método initData() ou ele for opcional, descomente as linhas abaixo.
        // if (startDatePicker != null && startDatePicker.getValue() == null) {
        //     startDatePicker.setValue(LocalDate.now().minusMonths(1));
        // }
        // if (endDatePicker != null && endDatePicker.getValue() == null) {
        //     endDatePicker.setValue(LocalDate.now());
        // }


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

    @FXML
    private void handleFilterButtonAction(ActionEvent event) {
        carregarMarcacoesFiltradas();
    }

    @FXML
    private void handleClearFilterButtonAction(ActionEvent event) {
        startDatePicker.setValue(null); // Limpa a data inicial
        endDatePicker.setValue(null);   // Limpa a data final
        carregarMarcacoesFiltradas(); // Recarrega as marcações sem filtro
    }

    private void carregarMarcacoesFiltradas() {
        if (emailUsuarioLogado == null || emailUsuarioLogado.isEmpty()) {
            System.err.println("Erro: Email do usuário não definido para carregar marcações em HorasTrabalhadasController.");
            // Exibir mensagem de erro na UI
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

        // Validação das datas
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
            vboxMarcacoesPorDia.getChildren().clear(); // Limpa as marcações existentes antes de carregar novas
        }

        // Chamar o serviço com o filtro de datas
        Map<LocalDate, List<LocalDateTime>> marcacoes = 
            horasTrabalhadasService.getMarcacoesPorUsuarioComFiltro(emailUsuarioLogado, startDate, endDate);

        // Renderizar as marcações filtradas
        renderMarcacoes(marcacoes);
    }

    private void renderMarcacoes(Map<LocalDate, List<LocalDateTime>> marcacoes) {
        if (marcacoes.isEmpty()) {
            Label noDataLabel = new Label("Nenhuma marcação de ponto encontrada para o período selecionado.");
            noDataLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic;");
            if (vboxMarcacoesPorDia != null) {
                 vboxMarcacoesPorDia.getChildren().add(noDataLabel);
            }
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        marcacoes.entrySet().stream()
            .sorted(Map.Entry.comparingByKey(Comparator.reverseOrder()))
            .forEach(entry -> {
                LocalDate data = entry.getKey();
                List<LocalDateTime> pontosDoDia = entry.getValue();
                pontosDoDia.sort(LocalDateTime::compareTo);

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

                // Data sem o dia da semana
                Label dateLabel = new Label(data.format(dateFormatter)); 
                dateLabel.setStyle(
                    "-fx-font-weight: bold;" +
                    "-fx-font-size: 16px;" +
                    "-fx-text-fill: #AAD7FF;"
                );
                HBox.setHgrow(dateLabel, Priority.ALWAYS);

                long totalSecondsToday = 0;
                if (pontosDoDia.size() >= 2) {
                    for (int i = 0; i < pontosDoDia.size() - 1; i += 2) {
                        LocalDateTime entryTime = pontosDoDia.get(i);
                        LocalDateTime exitTime = pontosDoDia.get(i + 1);
                        if (exitTime.isAfter(entryTime)) {
                           totalSecondsToday += java.time.Duration.between(entryTime, exitTime).getSeconds();
                        }
                    }
                }
                java.time.Duration totalDurationToday = java.time.Duration.ofSeconds(totalSecondsToday);
                Label totalHoursSummaryLabel = new Label(
                    String.format("Total: %02d:%02d:%02d",
                        totalDurationToday.toHours(),
                        totalDurationToday.toMinutesPart(),
                        totalDurationToday.toSecondsPart())
                );
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

                for (int i = 0; i < pontosDoDia.size(); i++) {
                    LocalDateTime ponto = pontosDoDia.get(i);
                    Label pointLabel;

                    if (i % 2 == 0) { // Entrada
                        pointLabel = new Label("Entrada: " + ponto.format(timeFormatter));
                        pointLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7CFC00;");
                        expandableContent.getChildren().add(pointLabel);
                    } else { // Saída
                        LocalDateTime entryTime = pontosDoDia.get(i - 1);
                        pointLabel = new Label("Saída: " + ponto.format(timeFormatter));
                        pointLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #FF6347;");

                        if (ponto.isAfter(entryTime)) {
                            java.time.Duration blockDuration = java.time.Duration.between(entryTime, ponto);
                            Label durationLabel = new Label(
                                String.format("(%02d:%02d:%02d)",
                                    blockDuration.toHours(),
                                    blockDuration.toMinutesPart(),
                                    blockDuration.toSecondsPart())
                            );
                            durationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ADD8E6; -fx-padding: 0 0 0 15;");
                            HBox blockInfo = new HBox(5, pointLabel, durationLabel);
                            blockInfo.setAlignment(Pos.CENTER_LEFT);
                            expandableContent.getChildren().add(blockInfo);
                        } else {
                            expandableContent.getChildren().add(pointLabel);
                        }
                    }
                }
                
                if (pontosDoDia.size() % 2 != 0) {
                    Label pendingExitLabel = new Label("• Saída pendente");
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
  
    private void handleMenuButtonClick(MouseEvent event) {
    	System.out.println("handleMenuButtonClick: Botão de menu clicado em HorasTrabalhadas!");
        if (overlayPane != null) {
            overlayPane.setVisible(true);
            overlayPane.setMouseTransparent(false); 
            FadeTransition fadeTransition = new FadeTransition(javafx.util.Duration.seconds(0.3), overlayPane); 
            fadeTransition.setFromValue(overlayPane.getOpacity());
            fadeTransition.setToValue(0.4);
            fadeTransition.play();
        }

        if (sidebarPane != null) {
            sidebarPane.setMouseTransparent(false);
            TranslateTransition translateTransition = new TranslateTransition(javafx.util.Duration.seconds(0.3), sidebarPane);
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
        // Não é necessário chamar carregarMarcacoesFiltradas() novamente aqui,
        // pois a tela já está exibindo os dados filtrados.
        // Se a intenção era recarregar com o filtro atual, a linha abaixo é ok.
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
                System.err.println("Erro: Controlador de Configurações é nulo ao voltar."); // Correção de mensagem
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Configurações");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar Configurações.fxml: " + e.getMessage()); // Correção de mensagem
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
    private void GestaoButtonAction(ActionEvent event) {
    	System.out.println("Teste");
    	closeSidebar();
    	}

    @FXML
    private void CadastrarButtonAction(ActionEvent event) {
    	System.out.println("Teste");
    	closeSidebar();
    	}
    @FXML
    private void RelatorioButtonAction(ActionEvent event) {
    	System.out.println("Teste");
    	closeSidebar();
    	}
    }
