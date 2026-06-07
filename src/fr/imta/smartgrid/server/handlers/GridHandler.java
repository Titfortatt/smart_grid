package fr.imta.smartgrid.server.handlers;

import java.util.List;

import io.vertx.ext.web.RoutingContext;
import jakarta.persistence.EntityManager;

import fr.imta.smartgrid.model.Grid;
import io.vertx.core.json.Json;

/**
 * Handler pour les routes liées aux grids électriques.
 * Gère la lecture des grids et le calcul de leur production/consommation totale.
 */
public class GridHandler {

    // EntityManager utilisé pour accéder à la base de données
    private EntityManager db;

    public GridHandler(EntityManager db) {
        this.db = db;
    }

    // GET /grids — retourne la liste de tous les IDs de grids
    public void getGrids(RoutingContext ctx) {
        List<Integer> grids = db.createQuery("SELECT g.id FROM Grid g", Integer.class).getResultList();
        ctx.json(grids);
    }

    // GET /grid/{id} — retourne les détails d'une grid : nom, description, personnes et capteurs associés
    public void getById(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid grid id"));
            return;
        }

        Grid grid = db.find(Grid.class, id);
        if (grid == null) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Grid not found"));
            return;
        }

    ctx.json(grid.toJson());  // ← toute la logique JSON est déléguée au modèle
}

    // GET /grid/{id}/production — retourne la somme de la dernière valeur mesurée de chaque producteur de la grid
    public void getProduction(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid grid id"));
            return;
        }

        Grid grid = db.find(Grid.class, id);
        if (grid == null) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Grid not found"));
            return;
        }

        ctx.json(grid.getTotalProduction());
    }

     // GET /grid/{id}/consumption — retourne la somme de la dernière valeur mesurée de chaque consommateur de la grid
    public void getConsumption(RoutingContext ctx) {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            ctx.response().setStatusCode(400)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Invalid grid id"));
            return;
        }

        Grid grid = db.find(Grid.class, id);
        if (grid == null) {
            ctx.response().setStatusCode(404)
            .putHeader("Content-Type", "application/json")
            .end(Json.encode("Grid not found"));
            return;
        }

        ctx.json(grid.getTotalConsumption());
    }
}
