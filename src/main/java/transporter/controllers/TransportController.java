package transporter.controllers;

import org.springframework.beans.factory.annotation.Autowired;
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

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> saveTransport(@RequestBody Transport body, HttpServletRequest request) {
        if (authService.validateAdmin(request)) {
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
    public ResponseEntity listAllTransports() {
        return ResponseEntity.status(200).body(transportService.listAllTransport());
    }

    @PutMapping(value = "/{transportId}", consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> modifyTransport(@RequestBody Transport body, @PathVariable Long transportId, HttpServletRequest request) {
        if (authService.validateAdmin(request)) {
            transportService.modifyTransport(body.getFreeSeats(), transportId);
            if (transportService.listTransport(transportId).getFreeSeats() == body.getFreeSeats())
                return ResponseEntity.status(200).body(transportService.listTransport(transportId));
        }
        return ResponseEntity.status(400).body("Nem lehet módosítani a megadott fuvart!");
    }

    @DeleteMapping(value = "/{transportId}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Object> removeTransport(@PathVariable Long transportId, HttpServletRequest request) {
        if (authService.validateToken(request) &&
                transportService.listTransport(transportId) != null) {
            transportService.removeTransport(transportId);
            if (transportService.listTransport(transportId) == null)
                return ResponseEntity.status(200).body("Sikeresen törölted a megadott fuvart!");
        }
        return ResponseEntity.status(400).body("Nem lehet törölni a megadott fuvart!");
    }
}