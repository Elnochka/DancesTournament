package com.dances.controller;

import com.dances.entity.dto.PdfRequest;
import com.dances.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final ReportService reportService;

    public DocumentController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/report")
    public void generatePdf(@RequestBody PdfRequest request,
                            HttpServletResponse response) throws Exception {

        byte[] pdf = reportService.generatePdf(
                "Report",
//                request.getHeaders(),
                request.getRows()
        );

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=report.pdf");
        response.getOutputStream().write(pdf);
    }

}
