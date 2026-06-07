package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "grid")
public class Grid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "grid")
    private List<Person> persons = new ArrayList<>();

    @OneToMany(mappedBy = "grid")
    private List<Sensor> sensors = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    //Convertit la grid en objet JSON 
    //Contient l'id, le nom, la description, les IDs des utilisateurs et des capteurs.  
    public JsonObject toJson() {
        JsonArray personIds = new JsonArray();
        for (var person : this.getPersons()) {
            personIds.add(person.getId());
        }
        JsonArray sensorIds = new JsonArray();
        for (var sensor : this.getSensors()) {
            sensorIds.add(sensor.getId());
        }
        return new JsonObject()
            .put("id", this.getId())
            .put("name", this.getName())
            .put("description", this.getDescription())
            .put("users", personIds)
            .put("sensors", sensorIds);
    }
    
    //Retourne la somme des dernières valeurs mesurées de tous les producteurs de la grid.
    public double getTotalProduction() {
        return getTotalForSensorType(Producer.class);
    }

    //Retourne la somme des dernières valeurs mesurées de tous les consommateurs de la grid.
    public double getTotalConsumption() {
        return getTotalForSensorType(Consumer.class);
    }

    //Calcule la somme du dernier datapoint de chaque mesure, pour les capteurs du type donné (Producer ou Consumer).
    private double getTotalForSensorType(Class<? extends Sensor> type) {
    double total = 0.0;

    for (Sensor sensor : this.getSensors()) {
        if (!type.isInstance(sensor)) continue;

        for (Measurement m : sensor.getMeasurements()) {
            DataPoint last = null;

            for (DataPoint dp : m.getDatapoints()) {
                if (last == null || dp.getTimestamp() > last.getTimestamp()) {
                    last = dp;
                }
            }

            if (last != null) {
                total += last.getValue();
            }
        }
    }

    return total;
    }
}
