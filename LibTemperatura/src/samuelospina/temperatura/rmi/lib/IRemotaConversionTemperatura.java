package samuelospina.temperatura.rmi.lib;

/**
 * Interfaz remota del Ejercicio 3 (Celsius a Fahrenheit) para LipeRMI.
 *
 * Con LipeRMI es una interfaz Java comun: no extiende java.rmi.Remote ni
 * sus metodos declaran RemoteException, como si exige el RMI estandar.
 *
 * @author Samuel David Ospina De Avila
 */
public interface IRemotaConversionTemperatura {

    public DatosTemperatura convertirTemperatura(DatosTemperatura datos);
}
