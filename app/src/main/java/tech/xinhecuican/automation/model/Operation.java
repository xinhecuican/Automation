package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.utils.Debug;

public class Operation implements Serializable {
    private String name;
    private ModelGroup model;
    private Long createDate;
    private String packageName;
    private String activityName;
    private boolean isAuto;

    public Operation(String name)
    {
        this.name = name;
        this.model = new ModelGroup();
        this.createDate = new Date().getTime();
        packageName = "";
        activityName = "";
        isAuto = false;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


    public List<Model> getModels() {
        return model.getModels();
    }

    public int getModelCount(){
        return model.getModels().size();
    }

    public int getDateEllipse() {
        Long currentTime = new Date().getTime();
        return (int) ((currentTime - createDate) / 24 / 60 / 60 / 1000);
    }

    public void addModel(Model model){
        this.model.addModel(model);
    }

    public String getActivityName() {
        return activityName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean auto) {
        isAuto = auto;
    }

    public List<ScheduledFuture> startProcess(AccessService service, ScheduledExecutorService scheduler)
    {
        return model.startProcess(service, scheduler, 0);
    }

    public ModelGroup getRootModel(){
        return this.model;
    }

    public CoordinateDescription generateCoordDescription(int index){
        CoordinateDescription description =
                new CoordinateDescription();
        description.packageName = packageName;
        description.activityName = activityName;
        description.x = 0;
        description.y = 0;
        if(index != -1) {
            Model model = this.model.getModels().get(index);
            try {
                description.x = (int) model.getClass().getMethod("getX").invoke(model);
                description.y = (int) model.getClass().getMethod("getY").invoke(model);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                Debug.error(e.getMessage(), 0);
            } finally {
                return description;
            }
        }
        else
            return description;
    }

    public WidgetDescription generateWidgetDescription(int index){
        if(index != -1) {
            Model model = this.model.getModels().get(index);
            if (model.needWidgetDescription()) {
                try {
                    return (WidgetDescription) model.getClass().getMethod("getWidgetDescription").invoke(model);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        }
        WidgetDescription widgetDescription = new WidgetDescription();
        widgetDescription.packageName = packageName;
        widgetDescription.className = activityName;
        return widgetDescription;
    }
}
