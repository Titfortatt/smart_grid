package fr.imta.smartgrid.server.handlers;

import java.util.List;

import fr.imta.smartgrid.model.Consumer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

/**
 * Handler pour les routes liées aux consommateurs d'énergie.
 * Gère la récupération de la liste complète des capteurs consommateurs.
 */
public class ConsumerHandler {

    /** EntityManager utilisé pour accéder à la base de données JPA. */
    private EntityManager db;

    public ConsumerHandler(EntityManager db) {
        this.db = db;
    }

    //GET /consumers — Retourne la liste complète des capteurs consommateurs.
    public void getConsumers(RoutingContext ctx) {
        List<Consumer> consumers = db.createQuery(
            "SELECT c FROM Consumer c", Consumer.class)
            .getResultList();

        JsonArray result = new JsonArray();
        for (Consumer c : consumers) {
            result.add(c.toJson());
        }

        ctx.json(result);
    }
}
