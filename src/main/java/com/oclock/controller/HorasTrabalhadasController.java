package com.oclock.controller;

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
import javafx.util.Duration;
import javafx.event.ActionEvent;
import com.oclock.model.HorasTrabalhadas;
import com.oclock.model.HorasTrabalhadas.RegistroDiario;
import com.oclock.model.ScreenManager;

import java.io.IOException; 
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class HorasTrabalhadasController implements Initializable {

    private final Locale BRAZIL_LOCALE = new Locale("pt", "BR");

    // --- Elementos FXML injetados ---
    @FXML
    private VBox vboxMarcacoesPorDia;

    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane sidebarPane;
    @FXML
    private AnchorPane sidebarAdminPane;
    @FXML
    private ImageView botaoMenu;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button filterButton;
    @FXML
    private Button clearFilterButton;

    // --- Variáveis de estado do controlador ---
    private boolean sidebarVisible = false;
    private String userEmail;
    private String userPermission;
    private HorasTrabalhadas horasTrabalhadasService = new HorasTrabalhadas(0, null, null); // Instância do serviço

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * Configura o estado inicial dos painéis do sidebar e overlay, e as transições de animação.
     * Também configura os listeners de eventos para os botões de filtro e o botão de menu.
     * @param url A localização usada para resolver caminhos relativos para o objeto raiz, ou null se a localização não for conhecida.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuração inicial do overlayPane
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        } else {
            System.err.println("ERRO: overlayPane é NULO. Verifique o fx:id no FXML.");
        }

        // Inicializa sidebarPane (usuário) e sua posição inicial (fora da tela)
        if (sidebarPane != null) {
            sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());
        } else {
            System.err.println("ERRO: sidebarPane é NULO. Verifique o fx:id no FXML.");
        }

        // Inicializa sidebarAdminPane (administrador) e sua posição inicial (fora da tela)
        if (sidebarAdminPane != null) {
            sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
        } else {
            System.err.println("ERRO: sidebarAdminPane é NULO. Verifique o fx:id no FXML.");
        }

        // Define o handler para o botão de menu
        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        } else {
            System.err.println("ERRO: botaoMenu é NULO. Verifique o fx:id no FXML.");
        }

        // Configura os listeners de eventos para os botões de filtro
        if (filterButton != null) {
            filterButton.setOnAction(this::handleFilterButtonAction);
        }
        if (clearFilterButton != null) {
            clearFilterButton.setOnAction(this::handleClearFilterButtonAction);
        }
    }

    /**
     * Recebe e inicializa os dados do usuário (e-mail e permissão) passados de outra tela.
     * Também chama o método para atualizar a visibilidade do sidebar e carregar as marcações.
     * @param email O e-mail do usuário logado.
     * @param role A permissão (função) do usuário logado (ex: "admin", "usuario").
     */
    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        System.out.println("E-mail: " + userEmail + ", Permissão: " + userPermission);

        updateSidebarVisibility();
        carregarMarcacoesFiltradas();
    }

    // --- Métodos de Controle do Sidebar ---

    /**
     * Atualiza a visibilidade e o gerenciamento de espaço dos sidebars com base na permissão do usuário.
     * O sidebar de admin é mostrado se o usuário for admin, caso contrário, o sidebar padrão é mostrado.
     */
    private void updateSidebarVisibility() {
        if (sidebarPane == null || sidebarAdminPane == null) {
            System.err.println("Erro: Painéis do sidebar FXML não injetados em HorasTrabalhadasController.");
            return;
        }

        // Garante que ambos os sidebars estejam inicialmente fora da tela e não gerenciados
        sidebarAdminPane.setTranslateX(-sidebarAdminPane.getPrefWidth());
        sidebarPane.setTranslateX(-sidebarPane.getPrefWidth());

        sidebarAdminPane.setVisible(false);
        sidebarAdminPane.setManaged(false);
        sidebarPane.setVisible(false);
        sidebarPane.setManaged(false);

        if (userPermission == null || userPermission.isEmpty()) {
            System.err.println("Erro: Permissão do usuário não definida em HorasTrabalhadasController. Defaulting to usuario.");
            userPermission = "usuario"; // Define um padrão seguro
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userPermission);

        if (isAdmin) {
            sidebarAdminPane.setVisible(true);
            sidebarAdminPane.setManaged(true);
        } else {
            sidebarPane.setVisible(true);
            sidebarPane.setManaged(true);
        }
    }

    /**
     * Retorna o AnchorPane do sidebar ativo com base na permissão do usuário.
     * @return O AnchorPane do sidebar de admin se o usuário for admin, caso contrário, o sidebar padrão.
     */
    private AnchorPane getActiveSidebarPane() {
        return "admin".equalsIgnoreCase(userPermission) ? sidebarAdminPane : sidebarPane;
    }

    /**
     * Lida com o evento de clique no botão de menu (geralmente um ícone).
     * Abre o sidebar com uma animação de deslizamento e escurece o overlay.
     * @param event O evento de clique do mouse.
     */
    private void handleMenuButtonClick(MouseEvent event) {
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
            activeSidebar.setMouseTransparent(false);
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(0);
            translateTransition.play();
        }
        sidebarVisible = true;
    }

    /**
     * Lida com o evento de clique no overlayPane (a área escurecida da tela).
     * Fecha o sidebar se ele estiver aberto.
     * @param event O evento de clique do mouse.
     */
    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

    /**
     * Fecha o sidebar ativo com uma animação de deslizamento e clareia o overlay.
     */
    private void closeSidebar() {
        AnchorPane activeSidebar = getActiveSidebarPane();

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

        if (activeSidebar != null) {
            TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.3), activeSidebar);
            translateTransition.setToX(-activeSidebar.getPrefWidth());
            translateTransition.play();
            translateTransition.setOnFinished(event -> {
                activeSidebar.setVisible(false);
                activeSidebar.setManaged(false);
                activeSidebar.setMouseTransparent(true);
            });
        }
        sidebarVisible = false;
    }

    // --- Métodos de Ação da Tela de Horas Trabalhadas ---

    /**
     * Lida com o evento de clique no botão "Filtrar".
     * Chama o método para carregar as marcações filtradas com base nas datas selecionadas.
     * @param event O evento de ação.
     */
    @FXML
    private void handleFilterButtonAction(ActionEvent event) {
        carregarMarcacoesFiltradas();
    }

    /**
     * Lida com o evento de clique no botão "Limpar Filtro".
     * Limpa os DatePickers e recarrega todas as marcações.
     * @param event O evento de ação.
     */
    @FXML
    private void handleClearFilterButtonAction(ActionEvent event) {
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);
        carregarMarcacoesFiltradas();
    }

    /**
     * Carrega as marcações de ponto do usuário com base nas datas selecionadas nos DatePickers.
     * Exibe uma mensagem de erro se o usuário não estiver identificado ou se as datas forem inválidas.
     */
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

        LocalDate startDate = (startDatePicker != null) ? startDatePicker.getValue() : null;
        LocalDate endDate = (endDatePicker != null) ? endDatePicker.getValue() : null;

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

        List<RegistroDiario> registrosDiarios = horasTrabalhadasService.buscarRegistrosDiariosPorEmail(userEmail);

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

    /**
     * Renderiza as marcações de ponto diárias na interface do usuário (VBox).
     * Cria cards expansíveis para cada dia, exibindo a entrada, saída e total de horas.
     * @param registrosDiarios A lista de registros diários a serem exibidos.
     */
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
                LocalTime entrada = registroDiario.getEntrada();
                LocalTime saida = registroDiario.getSaida();
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

    // --- Métodos de Navegação (Menu Lateral/Sidebar) ---

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        closeSidebar();
        carregarMarcacoesFiltradas();
    }

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        closeSidebar();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/oclock/view/Configuracoes.fxml"));
            Parent root = loader.load();

            ConfiguracoesController controller = loader.getController();
            if (controller != null) {
                controller.initData(userEmail, userPermission);
            } else {
                System.err.println("Erro: Controlador de Configurações é nulo ao carregar.");
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("OnClock - Configurações");
            stage.show();
        } catch (IOException e) {
            System.err.println("Erro ao carregar Configuracoes.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSair(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "TelaLogin.fxml", null, null);
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "GestaoUsuarios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "TelaCadastro.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleRelatorio(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", userEmail, userPermission);
    }
}