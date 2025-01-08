package util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;


public class PdfUtil {


	public void generateInvoicePDF(
		    String dest,
		    JSONObject jsonResponse // Recebe o objeto JSON inteiro
		) throws Exception {

		    // Obtém os dados principais do JSON fora de "units"
		    String condoName = jsonResponse.optString("condo_name", "N/A");
		    String period = jsonResponse.optString("period", "N/A");
		    double totalEnergySpot = jsonResponse.optDouble("total_energy_spot", 0);
		    double totalCondo = jsonResponse.optDouble("total_condo", 0);
		    double totalConsumo = jsonResponse.optDouble("total_consumo", 0);
		    double totalUnity = jsonResponse.optDouble("total_unity", 0);

		    // Configuração inicial do documento
		    Document document = new Document(PageSize.A4, 36, 36, 36, 36);
		    PdfWriter.getInstance(document, new FileOutputStream(dest));
		    document.open();

		    Font headerFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
		    Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

		    // Adiciona título
		    Paragraph title = new Paragraph("Relatório de Rateio de Consumo", headerFont);
		    title.setAlignment(Element.ALIGN_CENTER);
		    document.add(title);
		    document.add(new Paragraph(" ")); // Espaçamento

		    // Informações principais
		    document.add(new Paragraph("Condomínio:    " + condoName, normalFont));
		    document.add(new Paragraph("Período:       " + period, normalFont));
		    document.add(new Paragraph(String.format("Total EnergySpot: R$ %.2f", totalEnergySpot), normalFont));
		    document.add(new Paragraph(String.format("Total Condomínio: R$ %.2f", totalCondo), normalFont));
		    document.add(new Paragraph(String.format("Total Consumo: %.2f kWh", totalConsumo), normalFont));
		    document.add(new Paragraph(String.format("Total Unidade:   R$ %.2f", totalUnity), normalFont));
		    document.add(new Paragraph(" ")); // Espaçamento

		    // Configuração da tabela
		    PdfPTable table = new PdfPTable(5); // 5 colunas
		    table.setWidthPercentage(100);
		    table.setWidths(new float[]{2, 1.5f, 1.5f, 1.5f, 1}); // Largura de cada coluna

		    Font tableHeaderFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
		    addTableHeader(table, tableHeaderFont, "Usuário", "Eqpto", "Vaga", "Consumo", "Total EnergySpot");

		    Font tableFont = new Font(Font.FontFamily.HELVETICA, 12);

		    // Itera sobre as unidades
		    JSONArray condoUnities = jsonResponse.optJSONArray("units");
		    if (condoUnities != null) {
		        for (int i = 0; i < condoUnities.length(); i++) {
		            JSONObject unit = condoUnities.optJSONObject(i);
		            if (unit != null) {
		                table.addCell(new PdfPCell(new Phrase(unit.optString("tx_unity", "N/A"), tableFont)));
		                table.addCell(new PdfPCell(new Phrase(String.format("%.2f kWh", unit.optDouble("total_consumo", 0)), tableFont)));
		                table.addCell(new PdfPCell(new Phrase(String.format("R$ %.2f", unit.optDouble("total_condo", 0)), tableFont)));
		                table.addCell(new PdfPCell(new Phrase(String.format("R$ %.2f", unit.optDouble("total_unity", 0)), tableFont)));
		                table.addCell(new PdfPCell(new Phrase(String.format("R$ %.2f", unit.optDouble("total_es", 0)), tableFont)));
		            }
		        }
		    } else {
		        document.add(new Paragraph("Nenhuma unidade encontrada.", normalFont));
		    }

		    document.add(table);

		    // Finaliza o documento
		    document.close();
		}

    private void addTableHeader(PdfPTable table, Font font, String... headers) {
        for (String header : headers) {
            PdfPCell headerCell = new PdfPCell(new Phrase(header, font));
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            table.addCell(headerCell);
        }
    }
}
