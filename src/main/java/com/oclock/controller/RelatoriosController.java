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
import javafx.util.Duration;
import javafx.stage.FileChooser;

// Importações para PDF (iText)
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.colors.ColorConstants;

// Importações para Excel (Apache POI)
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
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

    // VBox que contém o filtro de e-mail - será sempre visível nesta tela
    @FXML private VBox userEmailFilterVBox; 

    private String userEmail; // Manter para compatibilidade com ScreenManager, mas não será usado para filtrar permissão
    private String userPermission; // Manter para compatibilidade com ScreenManager, mas assumimos que é ADMIN

    private boolean sidebarVisible = false;

    private HorasTrabalhadas horasTrabalhadasService;

    private List<RegistroDiario> currentFilteredRecords = new ArrayList<>();

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

        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.withDayOfMonth(1));
        endDatePicker.setValue(today);

        // Não há necessidade de controlar a visibilidade de userEmailFilterVBox aqui
        // pois a tela é para ADMINs e o campo já deve estar visível por padrão no FXML.
        // Se houver algum problema de layout ou visibilidade, pode-se forçar:
        // if (userEmailFilterVBox != null) {
        //     userEmailFilterVBox.setVisible(true);
        //     userEmailFilterVBox.setManaged(true);
        // }
    }

    public void initData(String email, String role) {
        this.userEmail = email;
        this.userPermission = role; // Assume-se que 'role' sempre será "ADMIN" para esta tela
        System.out.println("DEBUG (RelatoriosController): E-mail: " + userEmail + ", Permissão: " + userPermission);
        updateSidebarVisibility(); // Mantém a lógica da sidebar baseada na permissão, caso ela seja genérica

        // Não há necessidade de esconder ou mostrar o campo de email aqui,
        // pois a página é dedicada a ADMINs e o campo já é exibido por padrão.
        
        // Carrega e filtra os dados iniciais ao iniciar a tela
        handleFilterData(null); 
    }

    @FXML
    private void handleFilterData(ActionEvent event) {
        statusLabel.setText("Buscando dados...");
        
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        // O campo de email agora SEMPRE existirá e será visível, então podemos ler diretamente
        String filterEmail = userEmailFilterField != null ? userEmailFilterField.getText().trim() : "";

        // Sempre busca todos os registros, já que a tela é para ADMIN
        List<RegistroDiario> allRegistros = horasTrabalhadasService.buscarTodosRegistrosDiarios();

        // Verificação de null para allRegistros antes de operar (PONTO DE ERRO ANTERIOR)
        if (allRegistros == null) {
            allRegistros = new ArrayList<>(); // Garante que a lista não é null
            System.err.println("AVISO: horasTrabalhadasService.buscarTodosRegistrosDiarios() retornou null. Inicializando como lista vazia.");
        }
        
        System.out.println("DEBUG: Registros totais carregados (antes de filtros de data/email): " + allRegistros.size());

        // Aplica o filtro de e-mail se o campo não estiver vazio
        if (!filterEmail.isEmpty()) {
            System.out.println("DEBUG: Filtrando por email: '" + filterEmail + "'");
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
            .sorted((r1, r2) -> r2.getData().compareTo(r1.getData()))
            .collect(Collectors.toList());

        currentFilteredRecords = filteredAndSortedRecords;
        
        statusLabel.setText("Dados prontos para exportação: " + currentFilteredRecords.size() + " registros.");
        System.out.println("DEBUG: " + currentFilteredRecords.size() + " registros filtrados para exportação.");
    }

    @FXML
    private void handleExportCsv(ActionEvent event) {
        System.out.println("DEBUG: Clicou em Exportar CSV.");
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
                statusLabel.setText("Relatório CSV exportado com sucesso!");
                System.out.println("DEBUG: Relatório CSV exportado com sucesso para: " + file.getAbsolutePath());
                showAlert(AlertType.INFORMATION, "Sucesso!", "Relatório CSV exportado com sucesso para:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                statusLabel.setText("Erro ao exportar CSV.");
                System.err.println("ERRO: Erro ao exportar relatório CSV: " + e.getMessage());
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erro na Exportação", "Ocorreu um erro ao exportar o relatório CSV:\n" + e.getMessage());
            }
        } else {
            statusLabel.setText("Exportação CSV cancelada.");
            System.out.println("DEBUG: Exportação CSV cancelada pelo usuário.");
        }
    }

    @FXML
    private void handleExportPdf(ActionEvent event) {
        System.out.println("DEBUG: Clicou em Exportar PDF.");
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

                statusLabel.setText("Relatório PDF exportado com sucesso!");
                System.out.println("DEBUG: Relatório PDF exportado com sucesso para: " + file.getAbsolutePath());
                showAlert(AlertType.INFORMATION, "Sucesso!", "Relatório PDF exportado com sucesso para:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                statusLabel.setText("Erro ao exportar PDF.");
                System.err.println("ERRO: Erro ao exportar relatório PDF: " + e.getMessage());
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Erro na Exportação", "Ocorreu um erro ao exportar o relatório PDF:\n" + e.getMessage());
            }
        } else {
            statusLabel.setText("Exportação PDF cancelada.");
            System.out.println("DEBUG: Exportação PDF cancelada pelo usuário.");
        }
    }

    @FXML
    private void handleExportExcel(ActionEvent event) {
        System.out.println("DEBUG: Clicou em Exportar Excel.");
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
                statusLabel.setText("Relatório Excel exportado com sucesso!");
                System.out.println("DEBUG: Relatório Excel exportado com sucesso para: " + file.getAbsolutePath());
                showAlert(AlertType.INFORMATION, "Sucesso!", "Relatório Excel exportado com sucesso para:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                statusLabel.setText("Erro ao exportar Excel.");
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
            statusLabel.setText("Exportação Excel cancelada.");
            System.out.println("DEBUG: Exportação Excel cancelada pelo usuário.");
        }
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // --- Métodos de Controle da Sidebar ---
    private void updateSidebarVisibility() {
        AnchorPane userSidebar = sidebarPane;
        AnchorPane adminSidebar = sidebarAdminPane;

        if (userSidebar == null || adminSidebar == null) {
            System.err.println("Sidebars não inicializadas. Verifique fx:id e includes no FXML.");
            return;
        }

        // Esta lógica de sidebar continua baseada na permissão, pois outras telas
        // podem ter sidebars diferentes.
        userSidebar.setVisible(false);
        userSidebar.setManaged(false);
        userSidebar.setMouseTransparent(true);

        adminSidebar.setVisible(false);
        adminSidebar.setManaged(false);
        adminSidebar.setMouseTransparent(true);

        if ("ADMIN".equals(userPermission)) {
            adminSidebar.setVisible(true);
            adminSidebar.setManaged(true);
        } else {
            userSidebar.setVisible(true);
            userSidebar.setManaged(true);
        }
        if (!sidebarVisible) {
            userSidebar.setTranslateX(-userSidebar.getPrefWidth());
            adminSidebar.setTranslateX(-adminSidebar.getPrefWidth());
        }
    }

    private AnchorPane getActiveSidebarPane() {
        if ("ADMIN".equals(userPermission)) {
            return sidebarAdminPane;
        } else {
            return sidebarPane;
        }
    }

    @FXML
    private void handleMenuButtonClick(MouseEvent event) {
        System.out.println("handleMenuButtonClick: Botão de menu clicado em Relatorios!");
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

    @FXML
    private void handleOverlayClick(MouseEvent event) {
        closeSidebar();
    }

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
        ScreenManager.loadScreen((Node) event.getSource(), "GerenciarUsuarios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleRelatorios(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Relatorios.fxml", userEmail, userPermission);
    }

    @FXML
    private void handleCadastrar(ActionEvent event) {
        closeSidebar();
        ScreenManager.loadScreen((Node) event.getSource(), "Cadastro.fxml", userEmail, userPermission);
    }
}