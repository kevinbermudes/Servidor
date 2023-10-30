package com.example.demo.server;

import com.example.demo.common.models.Login;
import com.example.demo.common.models.Request;
import com.example.demo.common.models.Response;
import com.example.demo.common.models.User;
import com.example.demo.common.utils.LocalDateAdapter;
import com.example.demo.common.utils.LocalDateTimeAdapter;
import com.example.demo.common.utils.UuidAdapter;
import com.example.demo.server.repositories.users.UserRepository;
import com.example.demo.server.services.funko.FunkoServiceImpl;
import com.example.demo.server.services.token.TokenService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.ServerException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class ClientHandler extends Thread{
    private final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(UUID.class, new UuidAdapter()).create();
    private final long clientNumber;
    private final FunkoServiceImpl funkoService;
    BufferedReader in;
    PrintWriter out;

    public ClientHandler(Socket socket, long clientNumber,  FunkoServiceImpl funkoService) {
        this.clientSocket = socket;
        this.clientNumber = clientNumber;
        this.funkoService = funkoService;
    }
    public void run() {

    }
    @SuppressWarnings("unchecked")
    private void handleRequest(Request request) throws IOException, ServerException {
        logger.debug("Petición para procesar: " + request);
        // Procesamos la petición y devolvemos la respuesta, esto puede ser un método
        switch (request.type()) {
            case LOGIN -> processLogin(request);
            case SALIR -> processSalir();
            case GETALL -> procesasGetAll(request);
            case GETBYID -> procesasGetById(request);
            case GETBYUUID -> procesasGetByUuid(request);
            case POST -> procesarPost(request);
            case UPDATE -> procesasUpdate(request);
            case DELETE -> procesasDelete(request);
            default -> new Response(Response.Status.ERROR, "No tengo ni idea", LocalDateTime.now().toString());
        }
    }
    private void processSalir() throws IOException {
        out.println(gson.toJson(new Response(Response.Status.BYE, "Adios", LocalDateTime.now().toString())));
        closeConnection();
    }
    private void processLogin(Request request) throws ServerException {
        logger.debug("Petición de login recibida: " + request);
        // Aquí procesamos el login es un dato anidado!!! Descomponemos la petición
        Login login = gson.fromJson(String.valueOf(request.content()), new TypeToken<Login>() {
        }.getType());
        // existe el usuario??
        // System.out.println(login);
        var user = UsersRepository.getInstance().findByByUsername(login.username());
        if (user.isEmpty() || !BCrypt.checkpw(login.password(), user.get().password())) {
            logger.warn("Usuario no encontrado o falla la contraseña");
            throw new ServerException("Usuario o contraseña incorrectos");
        }
        // Creamos el token
        var token = TokenService.getInstance().createToken(user.get(), Server.TOKEN_SECRET, Server.TOKEN_EXPIRATION);
        // Enviamos la respuesta
        logger.debug("Respuesta enviada: " + token);
        out.println(gson.toJson(new Response(Response.Status.TOKEN, token, LocalDateTime.now().toString())));

    }
    private Optional<User> procesarToken(String token) throws ServerException {
        if (TokenService.getInstance().verifyToken(token, Server.TOKEN_SECRET
            logger.debug("Token válido");
            var claims = TokenService.getInstance().getClaims(token, Server.TOKEN_SECRET);
            var id = claims.get("userid").asInt(); // es verdad que podríamos obtener otro tipo de datos
            var user = UserRepository.getInstance().findByById(id);
            if (user.isEmpty()) {
                logger.error("Usuario no autenticado correctamente");
                throw new ServerException("Usuario no autenticado correctamente");
            }
            return user;
        } else {
            logger.error("Token no válido");
            throw new ServerException("Token no válido");
        }
        private void procesasGetAll(Request request) throws ServerException {
            procesarToken(request.token());
            // Todos pueden obtener todos los alumnos
            funkoService.findAll()
                    .collectList()
                    .subscribe(alumnos -> {
                        logger.debug("Respuesta enviada: " + alumnos);
                        var resJson = gson.toJson(alumnos); // contenido
                        out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString()))); // Respuesta
                    });
        }
    }
    private void procesasGetAll(Request request) throws ServerException {
        procesarToken(request.token());
        // Todos pueden obtener todos los Funkos
        funkoService.findAll()
                .collectList()
                .subscribe(funkos -> {
                    logger.debug("Respuesta enviada: " + funkos);
                    var resJson = gson.toJson(funkos);
                    out.println(gson.toJson(new Response(Response.Status.OK, resJson, LocalDateTime.now().toString())));
                });
    }

}
