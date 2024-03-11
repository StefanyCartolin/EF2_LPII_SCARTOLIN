package cibertec.com.pe.controller;

import cibertec.com.pe.model.Boleto;
import cibertec.com.pe.model.Ciudad;
import cibertec.com.pe.model.Venta;
import cibertec.com.pe.model.VentaDetalle;
import cibertec.com.pe.repository.ICiudadRepository;
import cibertec.com.pe.repository.IVentaDetalleRepository;
import cibertec.com.pe.repository.IVentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Controller
@SessionAttributes({"boletosGuardados", "ciudades"})
public class VuelosController {

    @Autowired
    private ICiudadRepository ciudadRepository;

    @Autowired
    private IVentaRepository ventaRepository;

    @Autowired
    private IVentaDetalleRepository ventaDetalleRepository;

    @GetMapping("/")
    public String index(Model model) {
        List<Ciudad> ciudades = ciudadRepository.findAll();

        model.addAttribute("boleto", new Boleto());
        model.addAttribute("ciudades", ciudades);

        return "index";
    }

    @PostMapping("/guardarBoleto")
    public String guardarBoleto(Model model, @ModelAttribute Boleto boleto) {
        List<Boleto> boletos = (List<Boleto>) model.getAttribute("boletosGuardados");
        Double precioBoleto = 50.00;

        boleto.setSubTotal(boleto.getCantidad() * precioBoleto);

        boletos.add(boleto);

        model.addAttribute("boletosGuardados", boletos);
        model.addAttribute("boleto", new Boleto());

        return "index";
    }

    @GetMapping("/comprarBoletosGuardados")
    public String comprarBoletosGuardados(Model model) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        List<Boleto> boletos = (List<Boleto>) model.getAttribute("boletosGuardados");
        Double montoTotal = 0.0;

        for (Boleto boleto : boletos) {
            montoTotal += boleto.getSubTotal();
        }

        Venta nuevaVenta = new Venta();
        nuevaVenta.setFechaVenta(new Date());
        nuevaVenta.setMontoTotal(montoTotal);
        nuevaVenta.setNombreComprador(boletos.get(0).getNombreComprador());

        Venta ventaGuardada = ventaRepository.save(nuevaVenta);

        for (Boleto boleto : boletos) {
            VentaDetalle ventaDetalle = new VentaDetalle();

            Ciudad ciudadDestino = ciudadRepository.findById(boleto.getCiudadDestino()).get();
            ventaDetalle.setCiudadDestino(ciudadDestino);
            Ciudad ciudadOrigen = ciudadRepository.findById(boleto.getCiudadOrigen()).get();
            ventaDetalle.setCiudadOrigen(ciudadOrigen);

            ventaDetalle.setCantidad(boleto.getCantidad());
            ventaDetalle.setSubTotal(boleto.getSubTotal());

            Date fechaRetorno = formatter.parse(boleto.getFechaRetorno());
            ventaDetalle.setFechaRetorno(fechaRetorno);

            Date fechaSalida = formatter.parse(boleto.getFechaSalida());
            ventaDetalle.setFechaViaje(fechaSalida);

            ventaDetalle.setVenta(ventaGuardada);

            ventaDetalleRepository.save(ventaDetalle);
        }

        model.addAttribute("boleto", new Boleto());

        return "index";
    }

    @ModelAttribute("boletosGuardados")
    public List<Boleto> boletosGuardados() {
        return new ArrayList<>();
    }

    @ModelAttribute("ciudades")
    public List<Ciudad> ciudades() {return new ArrayList<>();}
}
