package samuelospina.temperatura.servidor;

import samuelospina.temperatura.rmi.lib.DatosTemperatura;
import samuelospina.temperatura.rmi.lib.IRemotaConversionTemperatura;

/**
 * Objeto remoto del Ejercicio 3: convierte grados Celsius a Fahrenheit.
 * La formula vive UNICAMENTE aqui, en el servidor; el cliente la invoca
 * como si fuera un metodo local.
 *
 * Con LipeRMI es una clase comun que implementa la interfaz remota.
 *
 * @author Samuel David Ospina De Avila
 */
public class ConversionRmiTemperaturaImplem implements IRemotaConversionTemperatura {

    /** Cero absoluto en grados Celsius. */
    public static final float CERO_ABSOLUTO = -273.15f;

    public interface ILogListener {

        void log(String mensaje);
    }

    private ILogListener logListener;

    public ConversionRmiTemperaturaImplem() {
    }

    public ConversionRmiTemperaturaImplem(ILogListener logListener) {
        this.logListener = logListener;
    }

    @Override
    public DatosTemperatura convertirTemperatura(DatosTemperatura datos) {
        if (datos == null) {
            return null;
        }
        float celsius = datos.getCelsius();
        log("[SOLICITUD] Temperatura recibida -> " + celsius + " °C");

        if (Float.isNaN(celsius) || celsius < CERO_ABSOLUTO) {
            datos.setFahrenheit(0);
            datos.setClasificacion(null);
            datos.setMensaje("ERROR: " + celsius + " °C esta por debajo del cero absoluto ("
                    + CERO_ABSOLUTO + " °C)");
            log("[ERROR] " + datos.getMensaje());
            return datos;
        }

        float fahrenheit = celsius * 9 / 5 + 32;
        String clasificacion = clasificar(celsius);
        datos.setFahrenheit(fahrenheit);
        datos.setClasificacion(clasificacion);
        datos.setMensaje(celsius + " °C equivalen a " + fahrenheit + " °F (" + clasificacion + ")");
        log("[CALCULO] " + datos.getMensaje());
        return datos;
    }

    private String clasificar(float c) {
        if (c <= 0) {
            return "temperatura de congelacion";
        } else if (c < 15) {
            return "frio";
        } else if (c < 25) {
            return "templado";
        } else if (c < 40) {
            return "calor";
        } else {
            return "muy caliente";
        }
    }

    private void log(String mensaje) {
        if (logListener != null) {
            logListener.log(mensaje);
        }
    }
}
