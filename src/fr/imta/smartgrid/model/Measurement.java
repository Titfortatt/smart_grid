package fr.imta.smartgrid.model;

import java.util.ArrayList;
import java.util.List;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "measurement")
public class Measurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String unit;

    private String name;

    @ManyToOne
    @JoinColumn(name = "sensor")
    private Sensor sensor;

    @OneToMany(mappedBy = "measurement")
    private List<DataPoint> datapoints = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public List<DataPoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<DataPoint> datapoints) {
        this.datapoints = datapoints;
    }


    //Retourne la définition de la mesure en JSON (sans datapoints).
     
    public JsonObject toJson() {
        JsonObject result = new JsonObject()
            .put("id", this.getId())
            .put("name", this.getName())
            .put("unit", this.getUnit());

        if (this.getSensor() != null) {
            result.put("sensor", this.getSensor().getId());
        } else {
            result.putNull("sensor");
        }

        return result;
    }

    //Retourne la mesure avec sa série temporelle filtrée entre from et to.
    public JsonObject toJsonWithValues(long from, long to) {
        JsonArray values = new JsonArray();

        for (DataPoint dp : this.getDatapoints()) {
            if (dp.getTimestamp() >= from && dp.getTimestamp() <= to) {
                values.add(new JsonObject()
                    .put("timestamp", dp.getTimestamp())
                    .put("value", dp.getValue()));
            }
        }

        return new JsonObject()
            .put("sensor_id", this.getSensor() != null ? this.getSensor().getId() : null)
            .put("measurement_id", this.getId())
            .put("values", values);
    }    
}
