package fr.imta.smartgrid.server.handlers;

import fr.imta.smartgrid.model.Measurement;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

/**
 * Handler pour les routes liées aux mesures.
 * Gère la lecture des définitions de mesures et de leurs valeurs dans le temps.
 */
public class MeasurementHandler {

    // EntityManager utilisé pour accéder à la base de données
    private EntityManager db;

    public MeasurementHandler(EntityManager db) {
        this.db = db;
    }

    // GET /measurement/:id — retourne la définition d'une mesure avec tous ses datapoints
    public void getById(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Invalid measurement id"));
            return;
        }

        Measurement measurement = db.find(Measurement.class, id);
        if (measurement == null) {
            ctx.response().setStatusCode(404)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Measurement not found"));
            return;
        }

        ctx.json(measurement.toJson());
    }

    // GET /measurement/:id/values — etourne la série temporelle d'une mesure.
    public void getValues(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Invalid measurement id"));
            return;
        }

        Measurement measurement = db.find(Measurement.class, id);
        if (measurement == null) {
            ctx.response().setStatusCode(404)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Measurement not found"));
            return;
        }

        // Paramètres de filtrage temporel optionnels
        long from = 0;
        long to = 2147483646L;
        try {
            if (ctx.queryParam("from") != null && !ctx.queryParam("from").isEmpty()) {
                from = Long.parseLong(ctx.queryParam("from").get(0));
            }
            if (ctx.queryParam("to") != null && !ctx.queryParam("to").isEmpty()) {
                to = Long.parseLong(ctx.queryParam("to").get(0));
            }
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
               .putHeader("Content-Type", "application/json")
               .end(Json.encode("Invalid from/to parameter"));
            return;
        }

        ctx.json(measurement.toJsonWithValues(from, to));
    }
}
