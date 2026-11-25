package com.dances.service;

import com.dances.entity.dto.TableRowDto;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.design.*;
import net.sf.jasperreports.engine.type.HorizontalTextAlignEnum;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class ReportService {

    public byte[] generatePdf(
            String title,
            List<TableRowDto> items
    ) throws Exception {

        InputStream template = getClass()
                .getResourceAsStream(
                        "/reports/universal_table.jrxml"
                );

        if (template == null) {
            throw new IllegalStateException(
                    "Report template not found: " +
                            "/reports/universal_table.jrxml"
            );
        }

        JasperDesign design =
                JRXmlLoader.load(template);

        int pageWidth =
                design.getPageWidth()
                        - design.getLeftMargin()
                        - design.getRightMargin();

        List<String> headers =
                items.isEmpty()
                        ? new ArrayList<>()
                        : new ArrayList<>(
                        items.getFirst()
                                .getColumns()
                                .keySet()
                );

        if (headers.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot generate PDF without columns"
            );
        }

        int columnWidth =
                pageWidth / headers.size();

        JRDesignBand headerBand =
                new JRDesignBand();

        headerBand.setHeight(25);

        JRDesignBand detailBand =
                new JRDesignBand();

        detailBand.setHeight(20);

        int x = 0;

        for (int index = 0;
             index < headers.size();
             index++) {

            String header = headers.get(index);

            String fieldName =
                    "field_" + index;

            JRDesignField field =
                    new JRDesignField();

            field.setName(fieldName);
            field.setValueClass(String.class);

            design.addField(field);

            JRDesignStaticText headerText =
                    new JRDesignStaticText();

            headerText.setX(x);
            headerText.setY(0);
            headerText.setWidth(columnWidth);
            headerText.setHeight(25);

            headerText.setText(header);

            headerText.setHorizontalTextAlign(
                    HorizontalTextAlignEnum.CENTER
            );

            headerBand.addElement(headerText);

            JRDesignTextField textField =
                    new JRDesignTextField();

            textField.setX(x);
            textField.setY(0);
            textField.setWidth(columnWidth);
            textField.setHeight(20);

            textField.setExpression(
                    new JRDesignExpression(
                            "$F{" + fieldName + "}"
                    )
            );

            detailBand.addElement(textField);

            x += columnWidth;
        }

        design.setColumnHeader(headerBand);

        ((JRDesignSection)
                design.getDetailSection())
                .addBand(detailBand);

        JasperReport report =
                JasperCompileManager
                        .compileReport(design);

        Map<String, Object> params =
                new HashMap<>();

        params.put(
                "reportTitle",
                title
        );

        List<Map<String, ?>> rows = new ArrayList<>();

        for (TableRowDto item : items) {

            Map<String, Object> row =
                    new LinkedHashMap<>();

            int index = 0;

            for (Object value : item.getColumns().values()) {

                row.put(
                        "field_" + index,
                        value
                );

                index++;
            }

            rows.add(row);
        }

        JRMapCollectionDataSource dataSource =
                new JRMapCollectionDataSource(rows);

        JasperPrint print =
                JasperFillManager.fillReport(
                        report,
                        params,
                        dataSource
                );

        return JasperExportManager
                .exportReportToPdf(print);
    }
}

