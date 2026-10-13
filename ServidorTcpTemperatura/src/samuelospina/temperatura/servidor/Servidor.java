package samuelospina.temperatura.servidor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import net.sf.lipermi.exception.LipeRMIException;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.IServerListener;
import net.sf.lipermi.net.Server;
import samuelospina.temperatura.rmi.lib.IRemotaConversionTemperatura;

/**
 * Publica el objeto remoto con LipeRMI: el CallHandler registra la
 * implementacion bajo su interfaz y el Server abre el puerto TCP.
 *
 * @author Samuel David Ospina De Avila
 */
public class Servidor {

    private int puerto = 9007;
    private CallHandler invocador;
    private Server servidor;
    private final ConversionRmiTemperaturaImplem.ILogListener listener;
    private volatile boolean activo = false;

    /** Conexiones abiertas, para poder cortarlas al detener el servicio. */
    private final Set<Socket> clientes = Collections.synchronizedSet(new HashSet<Socket>());
    /** Distinto de null solo mientras se detiene: avisa que el hilo de escucha desperto. */
    private volatile CountDownLatch despertar;

    public Servidor() {
        this(9007, null);
    }

    public Servidor(int puerto, ConversionRmiTemperaturaImplem.ILogListener listener) {
        if (puerto > 0) {
            this.puerto = puerto;
        }
        this.listener = listener;
    }

    public void iniciar() throws Exception {
        // Se crean de nuevo en cada arranque: un Server cerrado no se puede reutilizar.
        invocador = new CallHandler();
        servidor = new Server();
        servidor.addServerListener(new IServerListener() {
            @Override
            public void clientConnected(Socket socket) {
                clientes.add(socket);
                CountDownLatch aviso = despertar;
                if (aviso != null) {
                    aviso.countDown();   // es la conexion de detener(), no un cliente
                    return;
                }
                log("[CONEXION] Cliente " + socket.getInetAddress().getHostAddress() + " conectado");
            }

            @Override
            public void clientDisconnected(Socket socket) {
                if (clientes.remove(socket) && activo) {
                    log("[CONEXION] Cliente " + socket.getInetAddress().getHostAddress()
                            + " desconectado");
                }
            }
        });
        try {
            invocador.registerGlobal(IRemotaConversionTemperatura.class,
                    new ConversionRmiTemperaturaImplem(listener));
            servidor.bind(puerto, invocador);
            activo = true;
        } catch (LipeRMIException ex) {
            throw new Exception("No es posible invocar metodos remotos: " + ex.getMessage());
        } catch (IOException ex) {
            throw new Exception("No se pudo abrir el puerto " + puerto
                    + " (puede estar ocupado por otro proceso)");
        }
    }

    /**
     * Server.close() de LipeRMI 1.0.1 solo baja una bandera: el hilo de escucha
     * sigue bloqueado en accept() y el ServerSocket nunca se cierra, asi que el
     * puerto quedaria ocupado y los clientes conectados seguirian siendo
     * atendidos. Aqui se completa la parada.
     */
    public void detener() {
        if (!activo) {
            return;
        }
        activo = false;
        servidor.close();

        // 1. Una conexion local saca al hilo de escucha de accept(); al ver la
        //    bandera abajo, termina su ciclo.
        despertar = new CountDownLatch(1);
        try (Socket llamada = new Socket("localhost", puerto)) {
            despertar.await(1, TimeUnit.SECONDS);
        } catch (IOException ex) {
            // Nadie escuchaba ya: no hay hilo que despertar.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }

        // 2. Se cierra el ServerSocket (campo privado de la libreria) para liberar el puerto.
        try {
            Field campo = Server.class.getDeclaredField("serverSocket");
            campo.setAccessible(true);
            ServerSocket escucha = (ServerSocket) campo.get(servidor);
            if (escucha != null) {
                escucha.close();
            }
        } catch (ReflectiveOperationException | IOException | RuntimeException ex) {
            log("[ERROR] No se pudo liberar el puerto " + puerto + ": " + ex.getMessage());
        }

        // 3. Se cortan las conexiones abiertas. Se recorre una copia y se vacia
        //    antes el conjunto para no registrar una "desconexion" por cada una.
        ArrayList<Socket> abiertas;
        synchronized (clientes) {
            abiertas = new ArrayList<>(clientes);
            clientes.clear();
        }
        for (Socket socket : abiertas) {
            try {
                socket.close();
            } catch (IOException ex) {
                // Ya estaba cerrado.
            }
        }
        despertar = null;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getPuerto() {
        return puerto;
    }

    private void log(String mensaje) {
        if (listener != null) {
            listener.log(mensaje);
        }
    }
}
