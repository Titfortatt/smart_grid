package fr.imta.smartgrid.server;

import java.util.Map;

import org.eclipse.persistence.logging.SessionLog;

import fr.imta.smartgrid.server.handlers.ConsumerHandler;
import fr.imta.smartgrid.server.handlers.GridHandler;
import fr.imta.smartgrid.server.handlers.IngressHandler;
import fr.imta.smartgrid.server.handlers.MeasurementHandler;
import fr.imta.smartgrid.server.handlers.PersonHandler;
import fr.imta.smartgrid.server.handlers.ProducerHandler;
import fr.imta.smartgrid.server.handlers.SensorHandler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

public class VertxServer {
    private Vertx vertx;
    private EntityManager db; // database object

    public VertxServer() {
        this.vertx = Vertx.vertx();

        // setup database connexion
        Map<String, String> properties = Map.of(
            LOGGING_LEVEL, SessionLog.WARNING_LABEL // change to FINE_LABEL to get details on SQL query to database
        );

        var emf = Persistence.createEntityManagerFactory("smart-grid", properties);
        db = emf.createEntityManager();
    }

    public void start() {
        Router router = Router.router(vertx);

        // add handlers for payload parsing and to allow swagger to send requests
        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create().addOrigin("*").allowedMethod(HttpMethod.DELETE).allowedMethod(HttpMethod.PUT));

        // create handlers and registers routes
        GridHandler gh = new GridHandler(db);
        router.get("/grids").handler(gh::getGrids);
        router.get("/grid/:id").handler(gh::getById);
        router.get("/grid/:id/production").handler(gh::getProduction);
        router.get("/grid/:id/consumption").handler(gh::getConsumption);

        PersonHandler ph = new PersonHandler(db);
        router.get("/persons").handler(ph::getPersons);
        router.get("/person/:id").handler(ph::getById);
        router.put("/person").handler(ph::create); 
        router.post("/person/:id").handler(ph::update); 
        router.delete("/person/:id").handler(ph::delete); 

        MeasurementHandler mh = new MeasurementHandler(db);
        router.get("/measurement/:id").handler(mh::getById);
        router.get("/measurement/:id/values").handler(mh::getValues);

        SensorHandler sh = new SensorHandler(db);
        router.get("/sensor/:id").handler(sh::getById);
        router.get("/sensors/:kind").handler(sh::getByKind);
        router.post("/sensor/:id").handler(sh::update);

        ProducerHandler prh = new ProducerHandler(db);
        router.get("/producers").handler(prh::getProducers);

        ConsumerHandler csh = new ConsumerHandler(db);
        router.get("/consumers").handler(csh::getConsumers);

        IngressHandler ih = new IngressHandler(db);
        router.post("/ingress/windturbine").handler(ih::windTurbine);
        router.post("/ingress/solarpanel").handler(ih::solarPanel);
        
        // start the server
        vertx.createHttpServer().requestHandler(router).listen(8080)
            .onSuccess(e -> 
                System.out.println("Server is listening on localhost:" + e.actualPort())
            ).onFailure(e -> {
                System.out.println("Cannot start server, got error: " + e.getLocalizedMessage());
                System.exit(1);
            });
    }

    public static void main(String[] args) {
        new VertxServer().start();
    }
}