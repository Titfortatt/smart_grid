package fr.imta.smartgrid.server.handlers;

import java.util.List;

import fr.imta.smartgrid.model.Producer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

/**
 * Handler pour les routes liées aux producteurs d'énergie.
 * Gère la récupération de la liste complète des capteurs producteurs.
 */
public class ProducerHandler {

    /** EntityManager utilisé pour accéder à la base de données JPA. */
    private EntityManager db;

    public ProducerHandler(EntityManager db) {
        this.db = db;
    }

    //GET /producers — Retourne la liste complète des capteurs producteurs.
    public void getProducers(RoutingContext ctx) {
        List<Producer> producers = db.createQuery(
            "SELECT p FROM Producer p", Producer.class)
            .getResultList();

        JsonArray result = new JsonArray();
        for (Producer p : producers) {
            result.add(p.toJson());
        }

        ctx.json(result);
    }
}