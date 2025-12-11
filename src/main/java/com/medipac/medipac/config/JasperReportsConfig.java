package com.medipac.medipac.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuración de JasperReports para el sistema MediPac
 * Gestiona la compilación y carga de plantillas de reportes
 */
@Configuration
public class JasperReportsConfig {

    /**
     * Directorio donde se almacenan las plantillas JRXML
     */
    public static final String REPORTS_PATH = "/reports/";

    /**
     * Cache de reportes compilados para mejorar el rendimiento
     */
    private final Map<String, JasperReport> compiledReports = new HashMap<>();

    /**
     * Compila y cachea una plantilla de reporte
     * 
     * @param reportName Nombre del archivo JRXML (sin extensión)
     * @return JasperReport compilado
     * @throws Exception Si hay error al compilar la plantilla
     */
    public JasperReport getCompiledReport(String reportName) throws Exception {
        if (compiledReports.containsKey(reportName)) {
            return compiledReports.get(reportName);
        }

        String reportPath = REPORTS_PATH + reportName + ".jrxml";
        InputStream reportStream = getClass().getResourceAsStream(reportPath);

        if (reportStream == null) {
            throw new IllegalArgumentException("No se encontró la plantilla: " + reportPath);
        }

        JasperReport compiledReport = JasperCompileManager.compileReport(reportStream);
        compiledReports.put(reportName, compiledReport);

        return compiledReport;
    }

    /**
     * Limpia el cache de reportes compilados
     */
    public void clearCache() {
        compiledReports.clear();
    }
}
