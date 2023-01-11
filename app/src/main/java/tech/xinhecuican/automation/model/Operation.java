package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Operation implements Serializable {
    private String name;
    private List<Model> models;
    private Long createDate;

    Operation(String name)
    {
        this.name = name;
        this.models = new ArrayList<>();
        this.createDate = new Date().getTime();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    public List<Model> getModels() {
        return models;
    }

    public int getModelCount(){
        return models.size();
    }

    public int getDateEllipse() {
        Long currentTime = new Date().getTime();
        return (int) ((currentTime - createDate) / 24 / 60 / 60 / 1000);
    }

    public void addModel(Model model){
        this.models.add(model);
    }
}
