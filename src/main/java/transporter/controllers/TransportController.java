package transporter.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import transporter.authorizations.AuthService;
import transporter.entities.Transport;
import transporter.services.TransportService;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@RequestMapping(value = "/transport")
public class TransportController {

    @Autowired
    private TransportService transportService;
    @Autowired
    private AuthService authService;
    @Autowired
    private Environment environment;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> saveTransport(@RequestBody Transport body, HttpServletRequest request) {
        if (authService.validateToken(request) &&
                authService.resolveToken(request).getIssuer().equals(environment.getProperty("adminEmail"))) {
            Transport t = new Transport(body.getRoute(), LocalDateTime.parse(body.getDepartureTimeString()), null);
            transportService.saveTransport(t);
            return ResponseEntity.status(200).body("Sikeresen meghirdetted az új fuvart!");
        }
        return ResponseEntity.status(400).body("Az új fuvar meghirdetése sikertelen!");
    }

    @GetMapping(value = "/{transportId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> listTransport(@PathVariable Long transportId, HttpServletRequest request) {
        if (authService.validateToken(request) && transportService.listTransport(transportId) != null)
            return ResponseEntity.status(200).body(transportService.listTransport(transportId));
        else
            return ResponseEntity.status(400).body("Nem kérhető le a megadott fuvar!");
    }

    @GetMapping(value = "/transports/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity listAllTransports(HttpServletRequest request) {
        if (authService.validateToken(request))
            return ResponseEntity.status(200).body(transportService.listAllTransport());
        else
            return ResponseEntity.status(403).body("Nem lehet lekérdezni az összes fuvart!");
    }

    @PutMapping(value = "/{transportId}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> modifyTransport(@RequestBody Transport body, @PathVariable Long transportId, HttpServletRequest request) {
        if (authService.validateToken(request) &&
                authService.resolveToken(request).getIssuer().equals(environment.getProperty("adminEmail"))) {
            transportService.modifyTransport(body.getFreeSeats(), transportId);
            if (transportService.listTransport(transportId).getFreeSeats() == body.getFreeSeats())
                return ResponseEntity.status(200).body(transportService.listTransport(transportId));
        }
        return ResponseEntity.status(400).body("Nem lehet módosítani a megadott fuvart!");
    }

    @DeleteMapping(value = "/{transportId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> removeTransport(@PathVariable Long transportId, HttpServletRequest request) {
        if (authService.validateToken(request) &&
                authService.resolveToken(request).getIssuer().equals(environment.getProperty("adminEmail")) &&
                transportService.listTransport(transportId) != null) {
            transportService.removeTransport(transportId);
            if (transportService.listTransport(transportId) == null)
                return ResponseEntity.status(200).body("Sikeresen törölted a megadott fuvart!");
        }
        return ResponseEntity.status(400).body("Nem lehet törölni a megadott fuvart!");
    }
}