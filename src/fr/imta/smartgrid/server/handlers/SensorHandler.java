package fr.imta.smartgrid.server.handlers;

import java.util.List;

import fr.imta.smartgrid.model.EVCharger;
import fr.imta.smartgrid.model.Grid;
import fr.imta.smartgrid.model.Person;

import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

/**
 * Handler pour les routes liées aux capteurs.
 * Gère la lecture et la modification des sensors, avec support de l'héritage de types.
 */
public class SensorHandler {

    // EntityManager utilisé pour accéder à la base de données
    private EntityManager db;

    public SensorHandler(EntityManager db) {
        this.db = db;
    }

    // GET /sensors/:kind — retourne les IDs des capteurs d'un type donné 
    public void getByKind(RoutingContext ctx) {
        String kind = ctx.pathParam("kind");

        Class<? extends Sensor> sensorClass;
        switch (kind) {
            case "SolarPanel":  sensorClass = SolarPanel.class;  break;
            case "WindTurbine": sensorClass = WindTurbine.class; break;
            case "EVCharger":   sensorClass = EVCharger.class;   break;
            default:
                ctx.response().setStatusCode(400)
                   .putHeader("Content-Type", "application/json")
                   .end(Json.encode("Unknown kind: " + kind + ". Valid values: SolarPanel, WindTurbine, EVCharger"));
                return;
        }

        List<Integer> ids = db.createQuery(
            "SELECT s.id FROM Sensor s WHERE TYPE(s) = :type", Integer.class)
            .setParameter("type", sensorClass)
            .getResultList();

        ctx.json(ids);
    }

    // GET /sensor/:id — Retourne les détails complets d'un capteur.
    public void getById(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Invalid sensor id"));
            return;
        }

        Sensor sensor = db.find(Sensor.class, id);
        if (sensor == null) {
            ctx.response().setStatusCode(404)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Sensor not found"));
            return;
        }

        ctx.json(sensor.toJson());
    }

    // POST /sensor/:id — met à jour les champs fournis dans le body (name, description, grid)
    public void update(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Invalid sensor id"));
            return;
        }

        Sensor sensor = db.find(Sensor.class, id);
        if (sensor == null) {
            ctx.response().setStatusCode(404)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Sensor not found"));
            return;
        }

        JsonObject input = ctx.body().asJsonObject();
        if (input == null) {
            ctx.response().setStatusCode(400)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Invalid JSON body"));
            return;
        }

        db.getTransaction().begin();

        if (input.containsKey("name")) {
            sensor.setName(input.getString("name"));
        }
        if (input.containsKey("description")) {
            sensor.setDescription(input.getString("description"));
        }
        if (input.containsKey("grid")) {
            Grid grid = db.find(Grid.class, input.getInteger("grid"));
            if (grid == null) {
                db.getTransaction().rollback();
                ctx.response().setStatusCode(404)
                   .putHeader("Content-Type", "application/json")
                   .end(Json.encode("Grid not found"));
                return;
            }
            sensor.setGrid(grid);
        }
        if (input.containsKey("owners")) {
            List<Person> owners = new java.util.ArrayList<>();
            for (Object ownerId : input.getJsonArray("owners").getList()) {
                Person p = db.find(Person.class, (Integer) ownerId);
                if (p != null) owners.add(p);
            }
            sensor.setOwners(owners);
        }
        if (sensor instanceof WindTurbine wt) {
            if (input.containsKey("height")) wt.setHeight(input.getDouble("height"));
            if (input.containsKey("blade_length")) wt.setBladeLength(input.getDouble("blade_length"));
            if (input.containsKey("power_source")) wt.setPowerSource(input.getString("power_source"));
        }
        if (sensor instanceof SolarPanel sp) {
            if (input.containsKey("power_source")) sp.setPowerSource(input.getString("power_source"));
            if (input.containsKey("efficiency")) sp.setEfficiency(input.getFloat("efficiency"));
        }

        db.getTransaction().commit();

        ctx.json(sensor.toJson());
    }
}
