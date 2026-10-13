package samuelospina.temperatura.cliente.vistas;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import net.sf.lipermi.handler.CallHandler;
import net.sf.lipermi.net.Client;
import samuelospina.temperatura.rmi.lib.DatosTemperatura;
import samuelospina.temperatura.rmi.lib.IRemotaConversionTemperatura;

/**
 * Cliente Swing del Ejercicio 3: pide la temperatura en grados Celsius e
 * invoca de forma remota (LipeRMI) el metodo del servidor que la convierte
 * a Fahrenheit. Aqui NO se hace ninguna conversion.
 *
 * La interfaz esta pintada con el kit {@link Glass} (material translucido,
 * fondo animado, controles flotantes).
 *
 * @author Samuel David Ospina De Avila
 */
public class VentanaPrincipal extends javax.swing.JFrame {

    /** Clasificaciones que devuelve el servidor, en el orden de los tramos del medidor. */
    private static final String[] CLASES = {"temperatura de congelacion", "frio", "templado",
        "calor", "muy caliente"};
    private static final int MAX_HISTORIAL = 3;

    CallHandler invocadorRemoto;
    Client cliente;
    IRemotaConversionTemperatura conversionRemota;

    private CardLayout tarjetas;
    private JPanel contenedor;
    private float ultimoValor = 0f;
    private Timer contador;

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Conversor de Temperatura · Cliente LipeRMI");

        Glass.Fondo fondo = new Glass.Fondo();
        fondo.setLayout(new BorderLayout(0, 18));
        fondo.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));
        setContentPane(fondo);

        fondo.add(construirCabecera(), BorderLayout.NORTH);

        tarjetas = new CardLayout();
        contenedor = new JPanel(tarjetas);
        contenedor.setOpaque(false);
        contenedor.add(construirPanelConexion(), "conexion");
        contenedor.add(construirPanelConversion(), "convertir");

        segmentado = new Glass.Segmentado(new String[]{"CONEXION", "CONVERTIR"},
                i -> tarjetas.show(contenedor, i == 0 ? "conexion" : "convertir"));

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(segmentado, BorderLayout.CENTER);
        centro.add(fila, BorderLayout.NORTH);
        centro.add(contenedor, BorderLayout.CENTER);
        fondo.add(centro, BorderLayout.CENTER);

        setMinimumSize(new Dimension(740, 640));
        setSize(getMinimumSize());
        setLocationRelativeTo(null);
    }

    private JPanel construirCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Conversor de Temperatura");
        titulo.setFont(Glass.fuente(Font.BOLD, 27f));
        titulo.setForeground(Glass.TINTA);

        JLabel subtitulo = new JLabel("Cliente RMI  ·  Ejercicio 3  ·  Samuel Ospina");
        subtitulo.setFont(Glass.fuente(Font.PLAIN, 12f));
        subtitulo.setForeground(Glass.TINTA_SUAVE);

        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(5));
        titulos.add(subtitulo);
        cabecera.add(titulos, BorderLayout.WEST);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);
        derecha.add(new Glass.Chip("LipeRMI", Glass.CALOR));
        derecha.add(new Glass.Chip("°C  →  °F", Glass.FRIO));
        cabecera.add(derecha, BorderLayout.EAST);

        return cabecera;
    }

    private JPanel construirPanelConexion() {
        Glass.Tarjeta panel = new Glass.Tarjeta();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 26, 28));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(9, 0, 9, 12);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        campoIPServidor = new Glass.Campo("localhost", Glass.FRIO);
        campoPuertoServidor = new Glass.Campo("9007", Glass.FRIO);

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(Glass.rotulo("DIRECCION IP"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campoIPServidor, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(Glass.rotulo("PUERTO DE RED"), c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(campoPuertoServidor, c);

        btnIniciar = new Glass.Pildora("Conectar", Glass.VERDE);
        btnIniciar.addActionListener(evt -> btnIniciarActionPerformed(evt));
        c.gridx = 1;
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(16, 0, 9, 0);
        panel.add(btnIniciar, c);

        txtEstado = new JLabel("Desconectado");
        txtEstado.setFont(Glass.fuente(Font.BOLD, 13f));
        txtEstado.setForeground(Glass.ROJO);

        JPanel estado = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        estado.setOpaque(false);
        estado.add(new Glass.Punto(txtEstado));
        estado.add(txtEstado);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 0, 0, 0);
        panel.add(estado, c);

        JLabel ayuda = new JLabel("<html><div style='width:460px'>Al conectar, LipeRMI entrega un "
                + "proxy de la interfaz remota: el cliente llama a convertirTemperatura() como si "
                + "fuera un metodo local y el calculo ocurre en el servidor.</div></html>");
        ayuda.setFont(Glass.fuente(Font.PLAIN, 11f));
        ayuda.setForeground(Glass.TINTA_SUAVE);
        c.gridy = 4;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        panel.add(ayuda, c);

        return panel;
    }

    private JPanel construirPanelConversion() {
        Glass.Tarjeta panel = new Glass.Tarjeta();
        panel.setLayout(new BorderLayout(0, 22));
        panel.setBorder(BorderFactory.createEmptyBorder(26, 28, 22, 28));

        JPanel entrada = new JPanel(new GridBagLayout());
        entrada.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 12);
        c.anchor = GridBagConstraints.WEST;

        entrada.add(Glass.rotulo("TEMPERATURA (°C)"), c);

        campoCelsius = new Glass.Campo("", Glass.FRIO);
        campoCelsius.setFont(Glass.fuente(Font.BOLD, 22f));
        campoCelsius.setHorizontalAlignment(SwingConstants.CENTER);
        campoCelsius.setPreferredSize(new Dimension(130, 50));
        campoCelsius.addActionListener(evt -> btnCalcularActionPerformed(evt));
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        entrada.add(campoCelsius, c);

        btnCalcular = new Glass.Pildora("CONVERTIR", Glass.CALOR);
        btnCalcular.addActionListener(evt -> btnCalcularActionPerformed(evt));
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.insets = new Insets(0, 0, 0, 0);
        entrada.add(btnCalcular, c);

        panel.add(entrada, BorderLayout.NORTH);

        // Bloque de resultado
        JPanel resultado = new JPanel();
        resultado.setOpaque(false);
        resultado.setLayout(new BoxLayout(resultado, BoxLayout.Y_AXIS));

        JLabel rotulo = new JLabel("RESULTADO EN GRADOS FAHRENHEIT");
        rotulo.setFont(Glass.fuente(Font.BOLD, 11f));
        rotulo.setForeground(Glass.TINTA_SUAVE);
        rotulo.setAlignmentX(LEFT_ALIGNMENT);

        txtResultado = new JLabel("0.0 °F");
        txtResultado.setFont(Glass.fuente(Font.BOLD, 52f));
        txtResultado.setForeground(Glass.CALOR);
        txtResultado.setAlignmentX(LEFT_ALIGNMENT);

        txtMensaje = new JLabel("Conecte con el servidor y escriba una temperatura.");
        txtMensaje.setFont(Glass.fuente(Font.PLAIN, 13f));
        txtMensaje.setForeground(Glass.TINTA);
        txtMensaje.setAlignmentX(LEFT_ALIGNMENT);

        // Escala visual: solo ubica el dato que devolvio el servidor
        medidor = new Glass.Medidor(new float[]{-40, 0, 15, 25, 40, 110},
                new Color[]{new Color(0x8FB4D9), Glass.FRIO, Glass.VERDE, Glass.CALOR, Glass.ROJO},
                new String[]{"congelacion", "frio", "templado", "calor", "muy caliente"});
        medidor.setAlignmentX(LEFT_ALIGNMENT);

        resultado.add(rotulo);
        resultado.add(Box.createVerticalStrut(8));
        resultado.add(txtResultado);
        resultado.add(Box.createVerticalStrut(10));
        resultado.add(txtMensaje);
        resultado.add(Box.createVerticalStrut(18));
        resultado.add(medidor);
        resultado.add(Box.createVerticalStrut(18));

        // Accesos rapidos: solo rellenan el campo, el calculo sigue siendo del servidor
        JPanel rapidos = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rapidos.setOpaque(false);
        rapidos.setAlignmentX(LEFT_ALIGNMENT);
        rapidos.add(etiquetaMenor("PRUEBE CON  "));
        for (String v : new String[]{"0", "25", "37", "100", "-40"}) {
            Glass.Chip chip = new Glass.Chip(v + " °C", Glass.FRIO);
            chip.setFont(Glass.fuente(Font.BOLD, 11f));
            chip.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            chip.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    campoCelsius.setText(v);
                    campoCelsius.requestFocusInWindow();
                }
            });
            rapidos.add(chip);
        }
        resultado.add(rapidos);
        panel.add(resultado, BorderLayout.CENTER);

        // Pie: historial de invocaciones remotas y latencia de la ultima
        historial = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        historial.setOpaque(false);
        historial.add(etiquetaMenor("HISTORIAL  "));

        txtLatencia = etiquetaMenor("");
        txtLatencia.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        pie.add(historial, BorderLayout.CENTER);
        pie.add(txtLatencia, BorderLayout.EAST);
        panel.add(pie, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel etiquetaMenor(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Glass.fuente(Font.BOLD, 11f));
        l.setForeground(Glass.TINTA_SUAVE);
        return l;
    }

    /** Animacion del contador: interpola del valor anterior al nuevo. */
    private void animarResultado(float desde, float hasta) {
        if (contador != null && contador.isRunning()) {
            contador.stop();
        }
        final long inicio = System.currentTimeMillis();
        final int duracion = 420;
        contador = new Timer(16, null);
        contador.addActionListener(e -> {
            float p = Math.min(1f, (System.currentTimeMillis() - inicio) / (float) duracion);
            float suave = 1 - (1 - p) * (1 - p) * (1 - p);   // ease-out cubico
            float valor = desde + (hasta - desde) * suave;
            txtResultado.setText(String.format(java.util.Locale.US, "%.1f °F", valor));
            if (p >= 1f) {
                txtResultado.setText(hasta + " °F");
                contador.stop();
            }
        });
        contador.start();
    }

    /** Agrega la invocacion al historial, conservando solo las mas recientes. */
    private void agregarAlHistorial(String texto) {
        Glass.Chip chip = new Glass.Chip(texto, Glass.CALOR);
        chip.setFont(Glass.fuente(Font.BOLD, 11f));
        historial.add(chip, 1);   // despues del rotulo: la mas reciente primero
        while (historial.getComponentCount() > MAX_HISTORIAL + 1) {
            historial.remove(historial.getComponentCount() - 1);
        }
        historial.revalidate();
        historial.repaint();
    }

    private int tramoDe(String clasificacion) {
        for (int i = 0; i < CLASES.length; i++) {
            if (CLASES[i].equalsIgnoreCase(clasificacion)) {
                return i;
            }
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // Invocacion remota
    // ------------------------------------------------------------------
    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        if (btnIniciar.getText().equalsIgnoreCase("Desconectar")) {
            desconectar();
            return;
        }
        int puerto;
        try {
            puerto = Integer.parseInt(campoPuertoServidor.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El PUERTO DE RED debe ser un numero entero",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String ip = campoIPServidor.getText().trim();
        if (ip.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba la DIRECCION IP del servidor",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            invocadorRemoto = new CallHandler();
            final Client conexion = new Client(ip, puerto, invocadorRemoto);
            cliente = conexion;
            // Si el servidor se detiene, la ventana pasa sola a "Desconectado".
            conexion.addClientListener(() -> SwingUtilities.invokeLater(() -> {
                if (cliente == conexion) {
                    desconectar();
                }
            }));
            conversionRemota = (IRemotaConversionTemperatura) cliente.getGlobal(
                    IRemotaConversionTemperatura.class);
            btnIniciar.setText("Desconectar");
            btnIniciar.setBackground(Glass.ROJO);
            txtEstado.setText("Conectado a " + ip + ":" + puerto);
            txtEstado.setForeground(Glass.VERDE);
            segmentado.setSeleccion(1);
            repaint();
        } catch (Exception ex) {
            System.out.println("ERROR AL CONECTAR");
            JOptionPane.showMessageDialog(this, "No se pudo conectar al servidor: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void desconectar() {
        try {
            if (cliente != null) {
                cliente.close();
            }
        } catch (Exception ex) {
            // La conexion ya estaba caida: igual se deja el cliente desconectado.
        }
        cliente = null;
        conversionRemota = null;
        btnIniciar.setText("Conectar");
        btnIniciar.setBackground(Glass.VERDE);
        txtEstado.setText("Desconectado");
        txtEstado.setForeground(Glass.ROJO);
        repaint();
    }

    private void btnCalcularActionPerformed(java.awt.event.ActionEvent evt) {
        final IRemotaConversionTemperatura remoto = conversionRemota;
        if (remoto == null) {
            JOptionPane.showMessageDialog(this, "Cliente OffLine, Conecte con el Servidor",
                    "Sin Conexión", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Validacion de los datos ANTES de invocar al servidor.
        // Aqui no se convierte nada: el calculo es responsabilidad del servidor.
        String texto = campoCelsius.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Escriba la temperatura en grados Celsius",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        float celsius;
        try {
            celsius = Float.parseFloat(texto);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "\"" + texto + "\" no es un numero valido",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (Float.isNaN(celsius) || Float.isInfinite(celsius)) {
            JOptionPane.showMessageDialog(this, "\"" + texto + "\" no es un numero valido",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (celsius < -273.15f) {
            JOptionPane.showMessageDialog(this,
                    "No existen temperaturas por debajo del cero absoluto (-273.15 °C)",
                    "Dato invalido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        final float valor = celsius;
        Thread hilo = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("Celsius: " + valor);
                    long inicio = System.nanoTime();
                    DatosTemperatura datos = remoto.convertirTemperatura(new DatosTemperatura(valor));
                    long ms = (System.nanoTime() - inicio) / 1000000;
                    System.out.println("Fahrenheit: " + datos.getFahrenheit()
                            + "\nMensaje: " + datos.getMensaje());
                    // La interfaz se toca desde el hilo de Swing (EDT)
                    SwingUtilities.invokeLater(() -> mostrarResultado(datos, ms));
                } catch (Exception ex) {
                    System.out.println("ERROR con el cliente " + ex.getMessage());
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        desconectar();
                        JOptionPane.showMessageDialog(VentanaPrincipal.this,
                                "Se perdio la comunicacion con el servidor", "Error",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            }
        };
        hilo.start();
    }

    private void mostrarResultado(DatosTemperatura datos, long ms) {
        String msj = datos.getMensaje();
        boolean error = msj == null || msj.startsWith("ERROR");
        txtResultado.setForeground(error ? Glass.ROJO : Glass.CALOR);
        if (error) {
            txtResultado.setText("—");
            medidor.limpiar();
        } else {
            animarResultado(ultimoValor, datos.getFahrenheit());
            ultimoValor = datos.getFahrenheit();
            medidor.setValor(datos.getCelsius(), tramoDe(datos.getClasificacion()));
            agregarAlHistorial(datos.getCelsius() + " °C → " + datos.getFahrenheit() + " °F");
        }
        txtMensaje.setText(msj);
        txtLatencia.setText("LATENCIA  " + ms + " ms");
    }

    public JLabel getTxtEstado() {
        return txtEstado;
    }

    public JButton getBtnIniciar() {
        return btnIniciar;
    }

    private JButton btnIniciar;
    private JButton btnCalcular;
    private Glass.Campo campoCelsius;
    private Glass.Campo campoIPServidor;
    private Glass.Campo campoPuertoServidor;
    private Glass.Medidor medidor;
    private Glass.Segmentado segmentado;
    private JPanel historial;
    private JLabel txtEstado;
    private JLabel txtLatencia;
    private JLabel txtMensaje;
    private JLabel txtResultado;
}
