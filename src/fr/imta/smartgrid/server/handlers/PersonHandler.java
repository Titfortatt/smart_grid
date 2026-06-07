package fr.imta.smartgrid.server.handlers;

import java.util.List;

import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;
import fr.imta.smartgrid.model.Sensor;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

/**
 * Handler pour les routes liées aux personnes.
 * Gère la lecture, création, modification et suppression via JPA.
 */
public class PersonHandler {

    // EntityManager utilisé pour accéder à la base de données
    private EntityManager db;

    public PersonHandler(EntityManager db) {
        this.db = db;
    }

    // GET /persons — retourne la liste de tous les IDs
    public void getPersons(RoutingContext ctx) {
        List<Integer> personIds = db.createQuery("SELECT p.id FROM Person p", Integer.class).getResultList();
        ctx.json(personIds);
    }

    // GET /person/:id — retourne les détails d'une personne
    public void getById(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid person id"));
            return;
        }

        Person person = db.find(Person.class, id);
        if (person == null) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Person not found"));
            return;
    }

    ctx.json(person.toJson());
    }

    // PUT /person — crée une nouvelle personne et retourne son ID
    public void create(RoutingContext ctx) {
        JsonObject input = ctx.body().asJsonObject();
        if (input == null 
            || !input.containsKey("first_name") 
            || !input.containsKey("last_name")
            || !input.containsKey("grid")) {
            ctx.response().setStatusCode(400)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("first_name, last_name and grid are required"));
            return;
        }

        Person person = new Person();
        person.setFirstName(input.getString("first_name"));
        person.setLastName(input.getString("last_name"));

        Grid grid = db.find(Grid.class, input.getInteger("grid"));
        if (grid == null) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Grid not found"));
            return;
        }
        person.setGrid(grid);

        db.getTransaction().begin();
        db.persist(person);
        db.getTransaction().commit();

        ctx.json(person.toJson());
    }

    // POST /person/:id — Met à jour partiellement une personne.
    public void update(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid person id"));
            return;
        }

        Person person = db.find(Person.class, id);
        if (person == null) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Person not found"));
            return;
        }

        JsonObject input = ctx.body().asJsonObject();
        if (input == null) {
            ctx.response().setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid JSON body"));
            return;
        }

        try {
            db.getTransaction().begin();

            if (input.containsKey("first_name")) {
                person.setFirstName(input.getString("first_name"));
            }
            if (input.containsKey("last_name")) {
                person.setLastName(input.getString("last_name"));
            }
            if (input.containsKey("grid")) {
                Grid grid = db.find(Grid.class, input.getInteger("grid"));
                if (grid == null) {
                    db.getTransaction().rollback();
                    ctx.response().setStatusCode(404).putHeader("Content-Type", "application/json").end(Json.encode("Grid not found"));
                    return;
                }
                person.setGrid(grid);
            }
            if (input.containsKey("owned_sensors")) {
                List<Sensor> sensors = new java.util.ArrayList<>();
                for (Object sensorId : input.getJsonArray("owned_sensors").getList()) {
                    Sensor s = db.find(Sensor.class, (Integer) sensorId);
                    if (s != null) sensors.add(s);
                }
                person.setSensors(sensors);
            }

            db.getTransaction().commit();
        } catch (Exception e) {
            db.getTransaction().rollback();
            ctx.response().setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Error during update"));
            return;
        }

        ctx.json(person.toJson());
    }

    // DELETE /person/:id — Supprime une personne de la base de données.
    public void delete(RoutingContext ctx) {
    int id;
    try {
        id = Integer.parseInt(ctx.pathParam("id"));
    } catch (NumberFormatException e) {
        ctx.response().setStatusCode(400)
           .putHeader("Content-Type", "application/json")
           .end(Json.encode("Invalid person id"));
        return;
    }

    Person person = db.find(Person.class, id);
    if (person == null) {
        ctx.response().setStatusCode(404)
           .putHeader("Content-Type", "application/json")
           .end(Json.encode("Person not found"));
        return;
    }

    try {
        db.getTransaction().begin();
        db.remove(person);
        db.getTransaction().commit();
    } catch (Exception e) {
        db.getTransaction().rollback();
        ctx.response().setStatusCode(500)
           .putHeader("Content-Type", "application/json")
           .end(Json.encode("Error during deletion"));
        return;
    }

    ctx.response().setStatusCode(200).end();
}
}
