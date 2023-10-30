package com.example.demo.Client;

import com.example.demo.Client.Excepciones.ClienteExcepciones;
import com.example.demo.common.models.Funko;
import com.example.demo.common.models.Login;
import com.example.demo.common.models.Request;
import com.example.demo.common.models.Response;
import com.example.demo.common.utils.PropertiesReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import javax.net.ssl.SSLSocket;

import static com.example.demo.common.models.Request.Type.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 3000;
    private static Logger logger = LoggerFactory.getLogger(Client.class);

    private final Gson gson;
    private SSLSocket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String token;

    public Client(Gson gson) {
        this.gson = gson;
    }

    public static void main(String[] args) {
        Client client = new Client(new Gson());
        try {
            client.start();
        } catch (Exception e) {
            logger.error("Error al iniciar el cliente", e);
        }
    }

    private void start() {
        System.out.println("🔵 Iniciando Cliente");
        try {
            // Nos conectamos al servidor
            openConnection();

            // Probamos las peticiones
            token = sendRequestLogin();

            Thread.sleep(150); // Pausa para asegurarse de que la respuesta llegue

            // Obtener todos los Funkos
            sendRequestGetAllFunkos(token);

            // Crear un Funko
            Funko funko = Funko.builder().id(2L).nombre("Funko").precio(7.5).modelo(Funko.Modelo.valueOf("DISNEY")).fechaLanzamiento(LocalDate.parse("2024-04-09")).build();
            sendRequestPostFunko(token, funko);

            // Obtener un Funko por UUID
            sendRequestGetFunkoByUuid(token, String.valueOf(funko.getCod()));

            // Actualizar un Funko
            funko.setNombre("MARVEL Updated");
            sendRequestUpdateFunko(token, funko);

            // Eliminar un Funko
            sendRequestDeleteFunkoByUuid(token, String.valueOf(funko.getCod()));

            // Verificar que se eliminó
            sendRequestGetAllFunkos(token);

            sendRequestSalir(); // Salimos

            closeConnection(); // Cerramos la conexión al final

        } catch (ClienteExcepciones ex) {
            logger.error("Error en el cliente: " + ex.getMessage());
            System.err.println("🔴 Error: " + ex.getMessage());
        } catch (InterruptedException e) {
            logger.error("Interrupción: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Error de I/O: " + e.getMessage());
        } finally {
            // Asegurarnos de que la conexión se cierra incluso si ocurre una excepción
            try {
                closeConnection();
            } catch (IOException e) {
                logger.error("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }

    private void closeConnection() throws IOException {
        logger.debug("Cerrando la conexión con el servidor: " + HOST + ":" + PORT);
        System.out.println("🔵 Cerrando Cliente");
        if (in != null)
            in.close();
        if (out != null)
            out.close();
        if (socket != null)
            socket.close();
    }

    private void openConnection() throws IOException {
        System.out.println("🔵 Iniciando Cliente");
        Map<String, String> myConfig = readConfigFile();

        logger.debug("Cargando fichero de propiedades");
        // System.setProperty("javax.net.debug", "ssl, keymanager, handshake"); // Debug
        System.setProperty("javax.net.ssl.trustStore", myConfig.get("keyFile")); // llavero cliente
        System.setProperty("javax.net.ssl.trustStorePassword", myConfig.get("keyPassword")); // clave

        SSLSocketFactory clientFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) clientFactory.createSocket(HOST, PORT);

        // Opcionalmente podemos forzar el tipo de protocolo -> Poner el mismo que el cliente
        logger.debug("Protocolos soportados: " + Arrays.toString(socket.getSupportedProtocols()));
        socket.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        socket.setEnabledProtocols(new String[]{"TLSv1.3"});

        logger.debug("Conectando al servidor: " + HOST + ":" + PORT);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("✅ Cliente conectado a " + HOST + ":" + PORT);

        infoSession(socket);

    }

    public Map<String, String> readConfigFile() {
        try {
            logger.debug("Leyendo el fichero de configuracion");
            PropertiesReader properties = new PropertiesReader("client.properties");

            String keyFile = properties.getProperty("keyFile");
            String keyPassword = properties.getProperty("keyPassword");

            // Comprobamos que no estén vacías
            if (keyFile.isEmpty() || keyPassword.isEmpty()) {
                throw new IllegalStateException("Hay errores al procesar el fichero de propiedades o una de ellas está vacía");
            }

            // Comprobamos el fichero de la clave
            if (!Files.exists(Path.of(keyFile))) {
                throw new FileNotFoundException("No se encuentra el fichero de la clave");
            }

            Map<String, String> configMap = new HashMap<>();
            configMap.put("keyFile", keyFile);
            configMap.put("keyPassword", keyPassword);

            return configMap;
        } catch (FileNotFoundException e) {
            logger.error("Error en clave: " + e.getLocalizedMessage());
            System.exit(1);
            return null; // Este retorno nunca se ejecutará debido a System.exit(1)
        } catch (IOException e) {
            logger.error("Error al leer el fichero de configuracion: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void sendRequestGetFunkoByUuid(String token, String uuid) throws IOException, ClienteExcepciones, ClienteExcepciones {
        Request request = new Request(GETBYUUID, uuid, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + GETBYUUID);
        logger.debug("Petición enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> {
                Funko responseContent = gson.fromJson(response.content(), new TypeToken<Funko>() {
                }.getType());
                System.out.println("🟢 El Funko solicitado es: " + responseContent);
            }
            case ERROR ->
                    System.err.println("🔴 Error: Funko no encontrado con uuid: " + uuid + ". " + response.content());
            default -> throw new ClienteExcepciones("Error no esperado al obtener el Funko");
        }
    }

    private void sendRequestPostFunko(String token, Funko funko) throws IOException, ClienteExcepciones, ClienteExcepciones {

        var funkoJson = gson.toJson(funko);
        Request request = new Request(POST, funkoJson, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + POST);
        logger.debug("Petición enviada: " + request);

        out.println(gson.toJson(request));

        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                Funko responseContent = gson.fromJson(response.content(), new TypeToken<Funko>() {
                }.getType());
                System.out.println("🟢 El Funko insertado es: " + responseContent);
            }
            case ERROR -> System.err.println("🔴 Error: No se ha podido insertar el Funko: " + response.content());
            default -> throw new ClienteExcepciones("Error no esperado al insertar el Funko");
        }
    }

    private void sendRequestDeleteFunkoByUuid(String token, String uuid) throws IOException, ClienteExcepciones, ClienteExcepciones {
        Request request = new Request(GETBYUUID, uuid, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + GETBYUUID);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        logger.debug("Respuesta recibida: " + response.toString());

        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> System.out.println("🟢 El Funko con UUID: " + uuid + " ha sido eliminado.");
            case ERROR -> System.err.println("🔴 Error: No se ha podido eliminar el Funko con UUID: " + uuid + ". " + response.content());
            default -> throw new ClienteExcepciones("Error no esperado al eliminar el Funko");
        }
    }

    private void sendRequestUpdateFunko(String token, Funko funko) throws IOException, ClienteExcepciones, ClienteExcepciones {
        var funkoJson = gson.toJson(funko);
        Request request = new Request(UPDATE, funkoJson, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + UPDATE);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        logger.debug("Respuesta recibida: " + response.toString());

        System.out.println("Respuesta recibida de tipo: " + response.status());
        switch (response.status()) {
            case OK -> {
                Funko updatedFunko = gson.fromJson(response.content(), new TypeToken<Funko>() {
                }.getType());
                System.out.println("🟢 El Funko actualizado es: " + updatedFunko);
            }
            case ERROR -> System.err.println("🔴 Error: No se ha podido actualizar el Funko. " + response.content());
            default -> throw new ClienteExcepciones("Error no esperado al actualizar el Funko");
        }
    }

    private void infoSession(SSLSocket socket) {
        logger.debug("Información de la sesión");
        System.out.println("Información de la sesión");
        try {
            SSLSession session = socket.getSession();
            System.out.println("Servidor: " + session.getPeerHost());
            System.out.println("Cifrado: " + session.getCipherSuite());
            System.out.println("Protocolo: " + session.getProtocol());
            System.out.println("Identificador:" + new BigInteger(session.getId()));
            System.out.println("Creación de la sesión: " + session.getCreationTime());
            X509Certificate certificado = (X509Certificate) session.getPeerCertificates()[0];
            System.out.println("Propietario : " + certificado.getSubjectX500Principal());
            System.out.println("Algoritmo: " + certificado.getSigAlgName());
            System.out.println("Tipo: " + certificado.getType());
            System.out.println("Número Serie: " + certificado.getSerialNumber());
            // expiración del certificado
            System.out.println("Válido hasta: " + certificado.getNotAfter());
        } catch (SSLPeerUnverifiedException ex) {
            logger.error("Error en la sesión: " + ex.getLocalizedMessage());
        }
    }
    private void sendRequestGetAllFunkos(String token) throws ClienteExcepciones, IOException {
        // Al usar el toString me ahorro el problema de las fechas con Gson
        Request request = new Request(GETALL, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + GETALL);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        logger.debug("Respuesta recibida: " + response.toString());
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case OK -> {
                List<Funko> responseContent = gson.fromJson(response.content(), new TypeToken<List<Funko>>() {
                }.getType());
                System.out.println("🟢 Los Funkos son: " + responseContent);
            }
            case ERROR -> System.err.println("🔴 Error: " + response.content()); // No se ha encontrado
        }
    }


    private String sendRequestLogin() throws ClienteExcepciones, IOException {
        String myToken = null;
        var loginJson = gson.toJson(new Login("pepe", "pepe1234"));
        Request request = new Request(LOGIN, loginJson, null, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + LOGIN);
        logger.debug("Petición enviada: " + request);
        // Enviamos la petición
        out.println(gson.toJson(request));


        // Recibimos la respuesta
        try {
            // Es estring porque el content es String no datos
            Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
            }.getType());

            logger.debug("Respuesta recibida: " + response.toString());
            // Ahora podríamos implementar un switch para cada tipo de respuesta
            // y hacer lo que queramos con ella...
            System.out.println("Respuesta recibida de tipo: " + response.status());

            switch (response.status()) {
                case TOKEN -> {
                    System.out.println("🟢 Mi token es: " + response.content());
                    myToken = response.content();
                }
                default -> throw new ClienteExcepciones("Tipo de respuesta no esperado: " + response.content());

            }
        } catch (IOException e) {
            logger.error("Error: " + e.getMessage());
        } catch (ClienteExcepciones e) {
            throw new RuntimeException(e);
        }
        return myToken;
    }
    private void sendRequestSalir() throws IOException, ClienteExcepciones, ClienteExcepciones {
        // Al usar el toString me ahorro el problem ade las fechas con Gson
        Request request = new Request(SALIR, null, token, LocalDateTime.now().toString());
        System.out.println("Petición enviada de tipo: " + SALIR);
        logger.debug("Petición enviada: " + request);

        // Enviamos la petición
        out.println(gson.toJson(request));

        // Recibimos la respuesta
        Response response = gson.fromJson(in.readLine(), new TypeToken<Response>() {
        }.getType());
        logger.debug("Respuesta recibida: " + response.toString());
        // Ahora podríamos implementar un switch para cada tipo de respuesta
        // y hacer lo que queramos con ella...
        System.out.println("Respuesta recibida de tipo: " + response.status());

        switch (response.status()) {
            case ERROR -> System.err.println("🔴 Error: " + response.content());
            case BYE -> {
                System.out.println("Vamos a cerrar la conexión " + response.content());
                closeConnection();
            }
            default -> throw new ClienteExcepciones(response.content());
        }
    }
}

