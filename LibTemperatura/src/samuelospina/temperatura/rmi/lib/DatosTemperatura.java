package samuelospina.temperatura.rmi.lib;

import java.io.Serializable;

/**
 * Objeto que viaja entre el cliente y el servidor en la invocacion remota.
 * El cliente solo llena {@code celsius}; el servidor devuelve el mismo objeto
 * con {@code fahrenheit}, {@code clasificacion} y {@code mensaje} calculados.
 *
 * Debe ser Serializable porque se transmite por el socket TCP.
 *
 * @author Samuel David Ospina De Avila
 */
public class DatosTemperatura implements Serializable {

    private static final long serialVersionUID = 1L;

    private float celsius;
    private float fahrenheit;
    private String clasificacion;
    private String mensaje;

    public DatosTemperatura() {
    }

    public DatosTemperatura(float celsius) {
        this.celsius = celsius;
    }

    public float getCelsius() {
        return celsius;
    }

    public void setCelsius(float celsius) {
        this.celsius = celsius;
    }

    public float getFahrenheit() {
        return fahrenheit;
    }

    public void setFahrenheit(float fahrenheit) {
        this.fahrenheit = fahrenheit;
    }

    public String getClasificacion() {
        return clasificacion;
    }

    public void setClasificacion(String clasificacion) {
        this.clasificacion = clasificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
