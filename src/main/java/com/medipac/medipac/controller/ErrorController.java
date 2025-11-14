package com.medipac.medipac.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(Model model, HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String requestURI = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Exception exception = (Exception) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        model.addAttribute("status", statusCode);
        
        // Mensajes personalizados según el contexto y código de error
        String customMessage = getCustomErrorMessage(statusCode, requestURI, errorMessage, exception);
        model.addAttribute("customMessage", customMessage);
        
        return "error/error"; // busca templates/error/error.html
    }
    
    private String getCustomErrorMessage(Object statusCode, String requestURI, String errorMessage, Exception exception) {
        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;
        
        // Mensajes específicos por contexto
        if (requestURI != null) {
            if (requestURI.contains("/register") || requestURI.contains("/auth/register")) {
                return "<p>Tus datos se intentaron cargar en la base de datos pero se perdieron en el ciberespacio.</p><p>No te preocupes, es solo un fallo temporal en la matriz.</p><p>Vuelve a intentarlo cuando los servidores estén sincronizados.</p>";
            }
            if (requestURI.contains("/login") || requestURI.contains("/auth/login")) {
                return "Tu credencial de acceso se desvaneció en el éter digital. Intenta reconectarte a la realidad virtual.";
            }
            if (requestURI.contains("/doctor")) {
                return "El portal médico se desconectó de la red neural. Los pacientes están seguros, pero el sistema necesita un reinicio.";
            }
            if (requestURI.contains("/paciente")) {
                return "Tu expediente médico se dispersó entre las ondas electromagnéticas. Tranquilo, lo recuperaremos del archivo cuántico.";
            }
            if (requestURI.contains("/cita")) {
                return "La cita se agendó en una dimensión paralela por error. El tiempo-espacio médico necesita recalibración.";
            }
        }
        
        // Mensajes por código de estado
        switch (status) {
            case 400:
                return "Los datos enviados se corrompieron durante la transmisión intergaláctica. Revisa tu información y reintenta.";
            case 401:
                return "Tu pase de acceso al ciberespacio ha expirado. Necesitas renovar tu licencia digital.";
            case 403:
                return "Esta zona del ciberespacio está protegida por un firewall cuántico. Acceso denegado.";
            case 404:
                return "Parece que has desaparecido en el ciberespacio. Pero no te preocupes, podemos ayudarte a encontrar el camino de vuelta.";
            case 500:
                return "El servidor médico sufrió un cortocircuito temporal. Los androides están trabajando en una solución.";
            case 503:
                return "El sistema está hibernando en modo de bajo consumo. Reactivando conexiones neuronales...";
            default:
                return "Algo extraño ocurrió en la matriz digital. Los datos se dispersaron por el multiverso, pero los recuperaremos.";
        }
    }
}
