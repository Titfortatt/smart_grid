package fr.imta.smartgrid.server.handlers;

import java.util.List;

import fr.imta.smartgrid.model.DataPoint;
import fr.imta.smartgrid.model.Measurement;
import fr.imta.smartgrid.model.Sensor;
import fr.imta.smartgrid.model.SolarPanel;
import fr.imta.smartgrid.model.WindTurbine;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

/**
 * Handler pour la réception de données en provenance des capteurs.
 */
public class IngressHandler {

    // EntityManager utilisé pour accéder à la base de données
    private EntityManager db;

    public IngressHandler(EntityManager db) {
        this.db = db;
    }

    // POST /ingress/solarpanel — reçoit une mesure d'un panneau solaire
    public void solarPanel(RoutingContext ctx) {
        String body = ctx.body().asString();

        if (body == null || body.isBlank()) {
            ctx.response().setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid payload"));
            return;
        }

        String[] parts = body.split(":");
        if (parts.length != 4) {
            ctx.response().setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid payload"));
            return;
        }

        int sensorId;
        double temperature, power;
        long timestamp;
        try {
            sensorId    = Integer.parseInt(parts[0]);
            temperature = Double.parseDouble(parts[1]);
            power       = Double.parseDouble(parts[2]);
            timestamp   = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid payload"));
            return;
        }

        Sensor sensor = db.find(Sensor.class, sensorId);
        if (sensor == null || !(sensor instanceof SolarPanel)) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Solar panel not found"));
            return;
        }

        JsonObject data = new JsonObject()
            .put("temperature", temperature)
            .put("power", power);

        persistData(sensorId, data, timestamp);

        ctx.json(new JsonObject().put("status", "success"));
        }

    // POST /ingress/windturbine — reçoit une mesure d'une éolienne
    public void windTurbine(RoutingContext ctx) {
        JsonObject input = ctx.body().asJsonObject();
        if (input == null || !input.containsKey("windturbine") || !input.containsKey("data")) {
            ctx.response().setStatusCode(500)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid JSON payload"));
            return;
        }

        int sensorId = input.getInteger("windturbine");
        JsonObject data = input.getJsonObject("data");
        long timestamp = input.getLong("timestamp");

        Sensor sensor = db.find(Sensor.class, sensorId);
        if (sensor == null || !(sensor instanceof WindTurbine)) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Wind turbine not found"));
            return;
        }

        persistData(sensorId, data, timestamp);
        ctx.response().setStatusCode(200).end("success");
        }

    // Enregistreles datapoints pour chaque mesure du capteur
    private void persistData(int sensorId, JsonObject data, long timestamp) {
        db.getTransaction().begin();

        for (String measurementName : data.fieldNames()) {
            double value = data.getDouble(measurementName);

            List<Measurement> results = db.createQuery(
                "SELECT m FROM Measurement m WHERE m.name = :name AND m.sensor.id = :sensorId",
                Measurement.class)
                .setParameter("name", measurementName)
                .setParameter("sensorId", sensorId)
                .getResultList();

            if (results.isEmpty()) continue;

            DataPoint dp = new DataPoint();
            dp.setValue(value);
            dp.setTimestamp(timestamp);
            dp.setMeasurement(results.get(0));
            db.persist(dp);
        }

        db.getTransaction().commit();
        }
}

