package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ModelGroup extends Model implements Serializable {
    private static final long serialVersionUID = 1626838888606104993L;
    private List<Model> models;

    public ModelGroup(){
        super();
        models = new ArrayList<>();
    }

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    public void addModel(Model model){
        this.models.add(model);
    }

    public void removeModel(Model model){
        this.models.remove(model);
    }

    @Override
    public int getModelType() {
        return 2;
    }

    @Override
    public void onRun() {

    }

    public void moveModel(int from, int to){
        if(to == models.size()){
            models.add(models.remove(from));
        }
        else{
            if(from < to)
                to--;
            models.add(to, models.remove(from));
        }
    }

    public int getTotalDelay(){
        int currentDelay = delay;
        for(Model model : models){
            if(model.getModelType() == 2){
                ModelGroup group = (ModelGroup) model;
                currentDelay += group.getTotalDelay();
            }
            else{
                currentDelay += model.delay;
            }
        }
        return currentDelay * repeatTimes;
    }
}
