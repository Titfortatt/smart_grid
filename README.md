# Smart Grid API — Rendu Final

## Routes implémentées

### Grids 
| Méthode | Route | Description |
|---------|-------|-------------|
| GET | `/grids` | Retourne la liste de tous les IDs de grids |
| GET | `/grid/{id}` | Retourne les détails d'une grid |
| GET | `/grid/{id}/production` | Retourne la production totale de la grid |
| GET | `/grid/{id}/consumption` | Retourne la consommation totale de la grid |

### Persons 
| Méthode | Route | Description |
|---------|-------|-------------|
| GET | `/persons` | Retourne la liste de tous les IDs de personnes |
| GET | `/person/{id}` | Retourne les détails d'une personne |
| PUT | `/person` | Crée une nouvelle personne |
| POST | `/person/{id}` | Met à jour partiellement une personne |
| DELETE | `/person/{id}` | Supprime une personne |

### Measurements 
| Méthode | Route | Description |
|---------|-------|-------------|
| GET | `/measurement/{id}` | Retourne la définition d'une mesure |
| GET | `/measurement/{id}/values` | Retourne la série temporelle d'une mesure (paramètres optionnels `from` et `to`) |

### Sensors 
| Méthode | Route | Description |
|---------|-------|-------------|
| GET | `/sensor/{id}` | Retourne les détails complets d'un capteur |
| GET | `/sensors/{kind}` | Retourne les IDs des capteurs d'un type donné (SolarPanel, WindTurbine, EVCharger) |
| POST | `/sensor/{id}` | Met à jour partiellement un capteur |

### Producers 
| Méthode | Route | Description |
|---------|-------|-------------|
| GET | `/producers` | Retourne la liste complète des producteurs avec leurs champs spécifiques |

### Consumers 
| Méthode | Route | Description |
|---------|-------|-------------|
| GET | `/consumers` | Retourne la liste complète des consommateurs avec leurs champs spécifiques |

### Ingress 
| Méthode | Route | Description |
|---------|-------|-------------|
| POST | `/ingress/windturbine` | Reçoit une mesure d'une éolienne (body JSON) |
| POST | `/ingress/solarpanel` | Reçoit une mesure d'un panneau solaire (body texte brut `id:temperature:power:timestamp`) |

### Routes non implémentées
Aucune — toutes les routes de la spécification sont implémentées.

---

## Tests effectués

### Tests automatiques
Les tests fournis par le correcteur lors de la première évaluation ont donné les résultats suivants (d'après le retour reçu) :

| Test | Résultat première version | Résultat version corrigée |
|------|--------------------------|--------------------------|
| `testGetGrids` | ✅ | ✅ |
| `testGetGrid` | ✅ | ✅ |
| `testGetProductionConsumption` | ✅ | ✅ |
| `testGetPersons` | ✅ | ✅ |
| `testGetPerson` | ❌ | ✅ (corrigé : first_name, last_name, owned_sensors) |
| `testAddUser` | ❌ | ✅ (corrigé : réponse JSON complète) |
| `testUpdateUser` | ❌ | ✅ (corrigé : first_name, last_name) |
| `testDeleteUser` | ❌ | ✅ (corrigé : gestion erreur JPA) |
| `testGetMeasurement` | ❌ | ✅ (corrigé : champ datapoints retiré) |
| `testGetValuesMeasurement` | ❌ | ✅ (corrigé : format sensor_id/measurement_id/values) |
| `testGetSensor` | ❌ | ✅ (corrigé : kind, available_measurements, blade_length) |
| `testGetSensorKind` | ❌ | ✅ (corrigé : valeurs SolarPanel/WindTurbine/EVCharger) |
| `testGetConsumers` | ❌ | ✅ (corrigé : objets complets au lieu d'IDs) |
| `testGetProducers` | ❌ | ✅ (corrigé : objets complets au lieu d'IDs) |
| `testIngressWindturbine` | ❌ | ✅ (corrigé : format réponse) |
| `testIngressSolarPanel` | ❌ | ✅ (corrigé : parsing texte brut) |

Les tests Person ont été vérifiés avec pytest en local (5/5 PASSED).
Les autres corrections ont été vérifiées manuellement via Swagger.
### Tests manuels
Les routes ont également été testées manuellement via l'interface Swagger disponible à `http://localhost:8080`.

---

## Choix d'implémentation

- La logique de calcul de production/consommation est implémentée en Java pur via la navigation des relations JPA (`getSensors()` → `getMeasurements()` → `getDatapoints()`), sans requête SQL native.
- La construction des objets JSON est déléguée aux classes modèles (`Grid.toJson()`, `Person.toJson()`, `Sensor.toJson()`, `Measurement.toJson()`) conformément au principe de séparation des responsabilités.
- Les sous-classes de `Sensor` (`SolarPanel`, `WindTurbine`, `EVCharger`) surchargent `toJson()` pour ajouter leurs champs spécifiques.
- Toutes les routes gèrent les cas d'erreur (400, 404, 500) avec des réponses JSON conformes à la spécification et le header `Content-Type: application/json`.
- Les suppressions et modifications en base sont encapsulées dans des blocs try-catch avec rollback en cas d'erreur JPA.

---

## Bugs résiduels

Aucun bug résiduel connu.