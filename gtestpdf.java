    private JSONObject g_test_pdf(HttpServletRequest req, HttpServletResponse res) throws SQLException {
        int cod_condo = getParmInt(PARM.COD_CONDO, req);
        String dt_ini = getParm(PARM.DT_INI, req);
        String dt_end = getParm(PARM.DT_END, req);

        // Chamada
        JSONObject jsonObject = Command.execCallRSJsonMany(SQL.SP_CONSUMER_BILL_PER_CONDO,FIELD.PACK_CONSUMER_BILL_PER_CONDO,cod_condo, dt_ini, dt_end);

        //dados
        JSONObject condo = jsonObject.getJSONArray("condo").getJSONObject(0); // Primeiro item do array "condo"
        JSONArray condoUnities = jsonObject.getJSONArray("condo_unities");

        // Criação de um novo JSONObject para os outros parâmetros
        JSONObject invoiceData = new JSONObject();
        invoiceData.put("condo_name", condo.getString("nm_condo"));
        invoiceData.put("period", condo.getString("period_ini") + " a " + condo.getString("period_end"));
        invoiceData.put("total_consumo", condo.getDouble("total_consumer_kwa"));
        invoiceData.put("total_condo", condo.getDouble("total_condo"));
        invoiceData.put("total_unity", condo.getDouble("total_unity"));
        invoiceData.put("total_energy_spot", condo.getDouble("total_empresa"));

        // Criação do array de unidades (condo_unities)
        JSONArray unitsData = new JSONArray();
        for (int i = 0; i < condoUnities.length(); i++) {
            JSONObject unit = condoUnities.getJSONObject(i);
            
            JSONObject unitData = new JSONObject();
            unitData.put("tx_unity", unit.getString("tx_unity"));
            unitData.put("total_consumo", unit.getDouble("total_consumer_kwa"));
            unitData.put("total_condo", unit.getDouble("total_condo"));
            unitData.put("total_unity", unit.getDouble("total_unity"));
            unitData.put("total_es", unit.getDouble("total_empresa"));
            
            unitsData.put(unitData);
        }

        // Adiciona o array de unidades ao invoiceData
        invoiceData.put("units", unitsData);

        try {
            // Geração do PDF no formato de saída desejado
            PdfUtil generator = new PdfUtil();
            
            // Geração do PDF e armazenamento em um arquivo temporário
            String pdfPath = "invoice.pdf";
            generator.generateInvoicePDF(pdfPath, invoiceData);

            // Enviar o PDF gerado como resposta para download
            File pdfFile = new File(pdfPath);
            if (pdfFile.exists()) {
                // Define o tipo de conteúdo para o navegador
                res.setContentType("application/pdf");
                res.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");
                res.setContentLength((int) pdfFile.length());

                // Cria um fluxo de entrada para o arquivo PDF
                FileInputStream fileInputStream = new FileInputStream(pdfFile);
                OutputStream outputStream = res.getOutputStream();

                // Envia o arquivo para o cliente
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileInputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                fileInputStream.close();
                outputStream.close();

                // Apaga o arquivo após o envio
                pdfFile.delete();
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao gerar PDF");
        }

        // Retorno do sucesso após gerar o PDF
        JSONObject result = new JSONObject();
        result.put(PARM.SUCCESS, S.TRUE_STR);
        return result;
    }
