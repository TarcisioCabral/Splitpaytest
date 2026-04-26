package com.splitpay.transaction;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/transactions/export")
@RequiredArgsConstructor
public class ExportController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/csv")
    public void exportToCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; file=transactions.csv");

        List<Transaction> transactions = transactionRepository.findAll();

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            String[] header = {"ID", "NFe Key", "Valor Bruto (BRL)", "IBS Retido", "CBS Retido", "Liquido", "Moeda Original", "Valor Original"};
            writer.writeNext(header);

            for (Transaction tx : transactions) {
                writer.writeNext(new String[]{
                        tx.getId().toString(),
                        tx.getNfeKey(),
                        tx.getValorBruto().toString(),
                        tx.getIbsRetido().toString(),
                        tx.getCbsRetido().toString(),
                        tx.getLiquido().toString(),
                        tx.getCurrency(),
                        tx.getOriginalAmount() != null ? tx.getOriginalAmount().toString() : tx.getValorBruto().toString()
                });
            }
        }
    }

    @GetMapping("/pdf")
    public void exportToPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; file=transactions.pdf");

        List<Transaction> transactions = transactionRepository.findAll();

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);

        Paragraph p = new Paragraph("Relatório de Transações - Splitpay", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100f);
        table.setSpacingBefore(10);

        writeTableHeader(table);
        writeTableData(table, transactions);

        document.add(table);
        document.close();
    }

    private void writeTableHeader(PdfPTable table) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(5);
        Font font = FontFactory.getFont(FontFactory.HELVETICA);

        cell.setPhrase(new Phrase("ID", font));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Chave NFe", font));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Bruto (BRL)", font));
        table.addCell(cell);
        cell.setPhrase(new Phrase("IBS", font));
        table.addCell(cell);
        cell.setPhrase(new Phrase("CBS", font));
        table.addCell(cell);
        cell.setPhrase(new Phrase("Líquido", font));
        table.addCell(cell);
    }

    private void writeTableData(PdfPTable table, List<Transaction> transactions) {
        for (Transaction tx : transactions) {
            table.addCell(tx.getId().toString());
            table.addCell(tx.getNfeKey());
            table.addCell(tx.getValorBruto().toString());
            table.addCell(tx.getIbsRetido().toString());
            table.addCell(tx.getCbsRetido().toString());
            table.addCell(tx.getLiquido().toString());
        }
    }
}
