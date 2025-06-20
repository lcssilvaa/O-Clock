package com.oclock.controller;

import com.oclock.model.HorasTrabalhadas;
import com.oclock.model.HorasTrabalhadas.RegistroDiario;
import com.oclock.model.ScreenManager;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

// Importações para PDF (iText)
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

// Importações para Excel (Apache POI)
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class RelatoriosController implements Initializable {

    @FXML private AnchorPane overlayPane;
    @FXML private AnchorPane sidebarPane, sidebarAdminPane;
    @FXML private ImageView botaoMenu;

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField userEmailFilterField;
    @FXML private Label statusLabel;

    @FXML private VBox userEmailFilterVBox;

    private String userEmail;
    private String userPermission;
    private boolean sidebarVisible = false;

    private HorasTrabalhadas horasTrabalhadasService;

    private List<RegistroDiario> currentFilteredRecords = new ArrayList<>();

    /**
     * Inicializa os dados do usuário (email e permissão) passados de outra tela.
     * Atualiza a visibilidade do sidebar com base na permissão do usuário.
     * Chama handleFilterData para carregar os dados iniciais.
     * @param email O email do usuário logado.
     * @param role A permissão (função) do usuário logado (ex: "admin", "usuario").
     */
    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role;
        if (userEmailFilterField != null) {
            if ("admin".equalsIgnoreCase(userPermission)) {
                userEmailFilterField.setEditable(true);
                userEmailFilterField.setText(""); 
            } else {
                userEmailFilterField.setText(this.userEmail);
                userEmailFilterField.setEditable(false);
            }
        }
        updateSidebarVisibility();
        handleFilterData(null); // Carrega os dados iniciais ao iniciar a tela
    }

    /**
     * Método de inicialização do controlador, chamado automaticamente após o carregamento do FXML.
     * Configura o estado inicial dos painéis do sidebar e overlay, e os seletores de data.
     * @param url A localização usada para resolver caminhos relativos para o objeto raiz, ou null se a localização não for conhecida.
     * @param resourceBundle Os recursos usados para localizar o objeto raiz, ou null se o objeto raiz não foi localizado.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (overlayPane != null) {
            overlayPane.setVisible(false);
            overlayPane.setOpacity(0.0);
            overlayPane.setMouseTransparent(true);
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
        if (botaoMenu != null) {
            botaoMenu.setOnMouseClicked(this::handleMenuButtonClick);
        }
        if (overlayPane != null) {
            overlayPane.setOnMouseClicked(this::handleOverlayClick);
        }

        horasTrabalhadasService = new HorasTrabalhadas(0, null, null);

        // Define as datas iniciais padrão (primeiro dia do mês atual até hoje)
        LocalDate today = LocalDate.now();
        if (startDatePicker != null) {
            startDatePicker.setValue(today.withDayOfMonth(1));
        }
        if (endDatePicker != null) {
            endDatePicker.setValue(today);
        }
    }

    /**
     * Lida com o evento de clique no botão de filtro de dados.
     * Busca os registros de ponto com base nas datas e e-mail filtrados.
     * @param event O evento de clique (pode ser nulo se chamado programaticamente).
     */
    @FXML
    private void handleFilterData(ActionEvent event) {
        if (statusLabel != null) statusLabel.setText("Buscando dados...");

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        String filterEmail = userEmailFilterField != null ? userEmailFilterField.getText().trim() : "";

        List<RegistroDiario> allRegistros = horasTrabalhadasService.buscarTodosRegistrosDiarios();

        if (allRegistros == null) {
            allRegistros = new ArrayList<>(); // Garante que a lista não é null
            System.err.println("AVISO: horasTrabalhadasService.buscarTodosRegistrosDiarios() retornou null. Inicializando como lista vazia.");
        }

        // Aplica o filtro de e-mail se o campo não estiver vazio
        if (!filterEmail.isEmpty()) {
            allRegistros = allRegistros.stream()
                                     .filter(r -> r.getEmailUsuario() != null && r.getEmailUsuario().equalsIgnoreCase(filterEmail))
                                     .collect(Collectors.toList());
        }

        // Aplica o filtro de data
        List<RegistroDiario> filteredAndSortedRecords = allRegistros.stream()
            .filter(registro -> {
                LocalDate dataRegistro = registro.getData();
                boolean passFilter = true;

                if (startDate != null && dataRegistro.isBefore(startDate)) {
                    passFilter = false;
                }
                if (endDate != null && dataRegistro.isAfter(endDate)) {
                    passFilter = false;
                }
                return passFilter;
            })
            .sorted((r1, r2) -> r2.getData().compareTo(r1.getData())) // Ordena por data decrescente
            .collect(Collectors.toList());

        currentFilteredRecords = filteredAndSortedRecords;

        if (statusLabel != null) statusLabel.setText("Dados prontos para exportação: " + currentFilteredRecords.size() + " registros.");
    }

    /**
     * Lida com a exportação dos dados filtrados para um arquivo CSV.
     * @param event O evento de clique do botão.
     */
    @FXML
    private void handleExportCsv(ActionEvent event) {
        if (currentFilteredRecords.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Nenhum dado para exportar", "Não há registros de ponto para o período selecionado ou filtrado.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório de Horas Trabalhadas (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos CSV (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("relatorio_horas_trabalhadas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write("Data,Entrada,Saida,Horas Trabalhadas,Email Usuario\n");

                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                for (RegistroDiario registro : currentFilteredRecords) {
                    String entradaStr = registro.getEntrada() != null ? registro.getEntrada().format(timeFormatter) : "";
                    String saidaStr = registro.getSaida() != null ? registro.getSaida().format(timeFormatter) : "Pendente";
                    writer.write(
                        registro.getData().format(DateTimeFormatter.ISO_LOCAL_DATE) + "," +
                        entradaStr + "," +
                        saidaStr + "," +
                        registro.getHorasTrabalhadas() + "," +
                        registro.getEmailUsuario() + "\n"
                    );
                }
                if (statusLabel != null) statusLabel.setText("Relatório CSV exportado com sucesso!");
                showAlert(AlertType.INFORMATION, "Sucesso!", "Relatório CSV exportado com sucesso para:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                if (statusLabel != null) statusLabel.setText("Erro ao exportar CSV.");
                System.err.println("ERRO: Erro ao exportar relatório CSV: " + e.getMessage());
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erro na Exportação", "Ocorreu um erro ao exportar o relatório CSV:\n" + e.getMessage());
            }
        } else {
            if (statusLabel != null) statusLabel.setText("Exportação CSV cancelada.");
        }
    }

    /**
     * Lida com a exportação dos dados filtrados para um arquivo PDF.
     * @param event O evento de clique do botão.
     */
    @FXML
    private void handleExportPdf(ActionEvent event) {
        if (currentFilteredRecords.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Nenhum dado para exportar", "Não há registros de ponto para o período selecionado ou filtrado.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório de Horas Trabalhadas (PDF)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF (*.pdf)", "*.pdf"));
        fileChooser.setInitialFileName("relatorio_horas_trabalhadas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");

        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            try {
                PdfWriter writer = new PdfWriter(file.getAbsolutePath());
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                document.add(new Paragraph("Relatório de Horas Trabalhadas")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(18));

                LocalDate startDate = startDatePicker.getValue();
                LocalDate endDate = endDatePicker.getValue();
                String periodoStr = "Período: " +
                        (startDate != null ? startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Início") +
                        " a " +
                        (endDate != null ? endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Fim");

                document.add(new Paragraph(periodoStr)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(12)
                        .setMarginBottom(20));

                float[] columnWidths = {1, 1, 1, 1, 2};
                Table table = new Table(UnitValue.createPercentArray(columnWidths));
                table.setWidth(UnitValue.createPercentValue(100));

                com.itextpdf.layout.Style headerStyle = new com.itextpdf.layout.Style()
                    .setFontSize(10)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY);

                table.addHeaderCell(new Paragraph("Data").addStyle(headerStyle));
                table.addHeaderCell(new Paragraph("Entrada").addStyle(headerStyle));
                table.addHeaderCell(new Paragraph("Saída").addStyle(headerStyle));
                table.addHeaderCell(new Paragraph("Horas Totais").addStyle(headerStyle));
                table.addHeaderCell(new Paragraph("E-mail Usuário").addStyle(headerStyle));

                com.itextpdf.layout.Style cellStyle = new com.itextpdf.layout.Style()
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER);

                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                for (RegistroDiario registro : currentFilteredRecords) {
                    String entradaStr = registro.getEntrada() != null ? registro.getEntrada().format(timeFormatter) : "";
                    String saidaStr = registro.getSaida() != null ? registro.getSaida().format(timeFormatter) : "Pendente";

                    table.addCell(new Paragraph(registro.getData().format(dateFormatter)).addStyle(cellStyle));
                    table.addCell(new Paragraph(entradaStr).addStyle(cellStyle));
                    table.addCell(new Paragraph(saidaStr).addStyle(cellStyle));
                    table.addCell(new Paragraph(registro.getHorasTrabalhadas()).addStyle(cellStyle));
                    table.addCell(new Paragraph(registro.getEmailUsuario()).addStyle(cellStyle));
                }

                document.add(table);
                document.close();

                if (statusLabel != null) statusLabel.setText("Relatório PDF exportado com sucesso!");
                showAlert(AlertType.INFORMATION, "Sucesso!", "Relatório PDF exportado com sucesso para:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                if (statusLabel != null) statusLabel.setText("Erro ao exportar PDF.");
                System.err.println("ERRO: Erro ao exportar relatório PDF: " + e.getMessage());
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erro na Exportação", "Ocorreu um erro ao exportar o relatório PDF:\n" + e.getMessage());
            }
        } else {
            if (statusLabel != null) statusLabel.setText("Exportação PDF cancelada.");
        }
    }

    /**
     * Lida com a exportação dos dados filtrados para um arquivo Excel (XLSX).
     * @param event O evento de clique do botão.
     */
    @FXML
    private void handleExportExcel(ActionEvent event) {
        if (currentFilteredRecords.isEmpty()) {
            showAlert(AlertType.INFORMATION, "Nenhum dado para exportar", "Não há registros de ponto para o período selecionado ou filtrado.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório de Horas Trabalhadas (Excel)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos Excel (*.xlsx)", "*.xlsx"));
        fileChooser.setInitialFileName("relatorio_horas_trabalhadas_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");

        File file = fileChooser.showSaveDialog(((Node) event.getSource()).getScene().getWindow());

        if (file != null) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Registros de Ponto");

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
            headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Data", "Entrada", "Saída", "Horas Trabalhadas", "Email Usuario"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            for (RegistroDiario registro : currentFilteredRecords) {
                Row row = sheet.createRow(rowNum++);

                String entradaStr = registro.getEntrada() != null ? registro.getEntrada().format(timeFormatter) : "";
                String saidaStr = registro.getSaida() != null ? registro.getSaida().format(timeFormatter) : "Pendente";

                row.createCell(0).setCellValue(registro.getData().format(dateFormatter));
                row.createCell(1).setCellValue(entradaStr);
                row.createCell(2).setCellValue(saidaStr);
                row.createCell(3).setCellValue(registro.getHorasTrabalhadas());
                row.createCell(4).setCellValue(registro.getEmailUsuario());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                if (statusLabel != null) statusLabel.setText("Relatório Excel exportado com sucesso!");
                showAlert(AlertType.INFORMATION, "Sucesso!", "Relatório Excel exportado com sucesso para:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                if (statusLabel != null) statusLabel.setText("Erro ao exportar Excel.");
                System.err.println("ERRO: Erro ao escrever arquivo Excel: " + e.getMessage());
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erro na Exportação", "Ocorreu um erro ao exportar o relatório Excel:\n" + e.getMessage());
            } finally {
                try {
                    workbook.close();
                } catch (IOException e) {
                    System.err.println("ERRO: Erro ao fechar workbook Excel: " + e.getMessage());
                }
            }
        } else {
            if (statusLabel != null) statusLabel.setText("Exportação Excel cancelada.");
        }
    }

    /**
     * Exibe uma caixa de diálogo de alerta para o usuário.
     * @param type O tipo de alerta (INFORMATION, WARNING, ERROR, CONFIRMATION).
     * @param title O título da janela de alerta.
     * @param message A mensagem a ser exibida no corpo do alerta.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Atualiza a visibilidade e o gerenciamento de espaço dos sidebars com base na permissão do usuário.
     * O sidebar de admin é mostrado se o usuário for admin, caso contrário, o sidebar padrão é mostrado.
     * Também ajusta a visibilidade e editabilidade do campo de filtro de e-mail.
     */
    private void updateSidebarVisibility() {
        AnchorPane userSidebar = sidebarPane;
        AnchorPane adminSidebar = sidebarAdminPane;

        if (userSidebar == null || adminSidebar == null) {
            System.err.println("Sidebars não inicializadas. Verifique fx:id e includes no FXML.");
            return;
        }

        // Primeiro, desabilita e esconde ambos
        userSidebar.setVisible(false);
        userSidebar.setManaged(false);
        userSidebar.setMouseTransparent(true);
        userSidebar.setTranslateX(-userSidebar.getPrefWidth()); // Garante que esteja escondido

        adminSidebar.setVisible(false);
        adminSidebar.setManaged(false);
        adminSidebar.setMouseTransparent(true);
        adminSidebar.setTranslateX(-adminSidebar.getPrefWidth()); // Garante que esteja escondido

        // Controla a visibilidade do campo de filtro de e-mail
        if (userEmailFilterVBox != null) {
            boolean isAdmin = "admin".equalsIgnoreCase(userPermission);
            userEmailFilterVBox.setVisible(isAdmin);
            userEmailFilterVBox.setManaged(isAdmin);

            if (userEmailFilterField != null) {
                if (isAdmin) {
                    userEmailFilterField.setText(""); // Admins podem filtrar por qualquer e-mail
                    userEmailFilterField.setEditable(true);
                } else {
                    userEmailFilterField.setText(this.userEmail); // Usuários comuns só veem seus próprios registros
                    userEmailFilterField.setEditable(false);
                }
            }
        }

        // Ativa o sidebar correto
        if ("admin".equalsIgnoreCase(userPermission)) {
            adminSidebar.setVisible(true);
            adminSidebar.setManaged(true);
            adminSidebar.setMouseTransparent(false);
        } else {
            userSidebar.setVisible(true);
            userSidebar.setManaged(true);
            userSidebar.setMouseTransparent(false);
        }
        // As transições de TranslateX para mostrar/esconder o sidebar são feitas no handleMenuButtonClick/closeSidebar
    }

    /**
     * Retorna o AnchorPane do sidebar ativo com base na permissão do usuário.
     * @return O AnchorPane do sidebar de admin se o usuário for admin, caso contrário, o sidebar padrão.
     */
    private AnchorPane getActiveSidebarPane() {
        if ("admin".equalsIgnoreCase(userPermission)) {
            return sidebarAdminPane;
        } else {
            return sidebarPane;
        }
    }

    /**
     * Lida com o evento de clique no botão de menu (geralmente um ícone).
     * Abre o sidebar com uma animação de deslizamento e escurece o overlay.
     * @param event O evento de clique do mouse.
     */
    @FXML
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
    @FXML
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

    // --- Métodos de Navegação (Menu Lateral/Sidebar) ---

    @FXML
    private void handleConsultarHoras(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "HorasTrabalhadas.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleRegistrarMarcacao(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "MenuUser.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleConfiguracoes(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Configuracoes.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleSair(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadLoginScreen((Node) event.getSource());
    }

    @FXML
    private void handleGestao(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "GestaoUsuarios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleRelatorios(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "TelaCadastro.fxml", userEmail, userPermission);
    }
}