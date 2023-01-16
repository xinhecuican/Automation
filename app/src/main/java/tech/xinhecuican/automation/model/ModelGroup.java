package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import tech.xinhecuican.automation.AccessService;

public class ModelGroup extends Model implements Serializable {
    List<Model> models;

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
    public void run() {

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

    public List<ScheduledFuture> startProcess(AccessService service, ScheduledExecutorService scheduler, int beginDelay){
        List<ScheduledFuture> futures = new ArrayList<>();
        for(int i=0; i<repeatTimes; i++) {
            if(delay != 0) {
                futures.add(scheduler.schedule(() -> {}, delay, TimeUnit.MILLISECONDS));
            }
            beginDelay += delay;
            for (Model model : models) {
                model.setService(service);
                if (model.getModelType() == 2) {
                    ModelGroup modelGroup = (ModelGroup) model;
                    futures.addAll(modelGroup.startProcess(service, scheduler, beginDelay));
                    beginDelay += modelGroup.getTotalDelay();
                }
                else if(model.getModelType() == 3){
                    DelayModel delayModel = (DelayModel) model;
                    service.addWindowStateChangeListener(delayModel);
                    futures.add(scheduler.schedule(model, model.delay, TimeUnit.MILLISECONDS));
                    beginDelay += model.delay;
                }
                else {
                    for (int k = 0; k < model.getRepeatTimes(); k++)
                        futures.add(scheduler.schedule(model, model.delay, TimeUnit.MILLISECONDS));
                    beginDelay += model.delay;
                }
            }
        }
        return futures;
    }
}
