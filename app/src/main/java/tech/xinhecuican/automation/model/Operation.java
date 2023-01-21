package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import tech.xinhecuican.automation.AccessService;
import tech.xinhecuican.automation.utils.Debug;
import tech.xinhecuican.automation.utils.DelayScheduler;

public class Operation implements Serializable {
    private static final long serialVersionUID = 8530017724969645813L;
    private String name;
    private ModelGroup model;
    private Long createDate;
    private String packageName;
    private String activityName;
    private boolean isAuto;
    private boolean isOnePage;

    public Operation(String name)
    {
        this.name = name;
        this.model = new ModelGroup();
        this.createDate = new Date().getTime();
        packageName = "";
        activityName = "";
        isAuto = false;
        isOnePage = false;
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

    public List<ScheduledFuture> startProcess(AccessService service, DelayScheduler scheduler)
    {
        return startProcessInner(model, service, scheduler);
    }

    public List<ScheduledFuture> startProcessInner(Model root, AccessService service, DelayScheduler scheduler){
        List<ScheduledFuture> futures = new ArrayList<>();
        root.runStateListener = null;
        root.setService(service);
        root.setTaskErrorListener(service);
        switch(root.getModelType()){
            case 0: // click model
            case 1:{ // scroll model
                for (int k = 0; k < root.getRepeatTimes(); k++) {
                    futures.add(scheduler.schedule(root, root.delay, TimeUnit.MILLISECONDS));
                }
                break;
            }
            case 2:{ // model group
                ModelGroup modelGroup = (ModelGroup) root;
                for(int i=0; i< root.repeatTimes; i++){
                    if(root.delay != 0){
                        futures.add(scheduler.schedule(() -> {}, root.delay, TimeUnit.MILLISECONDS));
                    }
                    for(Model model : modelGroup.getModels()){
                        futures.addAll(startProcessInner(model, service, scheduler));
                    }
                }
                break;
            }
            case 3:{ // delay model
                DelayModel delayModel = (DelayModel) root;
                if (delayModel.lock == null) {
                    delayModel.lock = new ReentrantLock();
                    delayModel.condition = delayModel.lock.newCondition();
                }
                if(delayModel.getMode() == DelayModel.DELAY_MODE_WINDOW_CHANGE)
                    service.addWindowStateChangeListener(delayModel);
                futures.add(scheduler.schedule(delayModel, delayModel.delay, TimeUnit.MILLISECONDS));
                break;
            }
            case 4:{ // condition model
                ConditionModel conditionModel = (ConditionModel) root;
                service.addWindowStateChangeListener(conditionModel);
                conditionModel.successFutures = null;
                conditionModel.failFutures = null;
                if(conditionModel.lock == null){
                    conditionModel.lock = new ReentrantLock();
                    conditionModel.lockCondition = conditionModel.lock.newCondition();
                }
                futures.add(scheduler.schedule(conditionModel, conditionModel.delay, TimeUnit.MILLISECONDS));
                if(conditionModel.getSuccessModel() != null){
                    conditionModel.successFutures = startProcessInner(conditionModel.getSuccessModel(), service, scheduler);
                    futures.addAll(conditionModel.successFutures);
                }
                if(conditionModel.getFailModel() != null){
                    conditionModel.failFutures = startProcessInner(conditionModel.getFailModel(), service, scheduler);
                    futures.addAll(conditionModel.failFutures);
                }
                break;
            }

        }
        return futures;
    }

    public void moveModel(ModelGroup parent, int from, int to){
        parent.moveModel(from, to);
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

    public boolean isOnePage() {
        return isOnePage;
    }

    public void setOnePage(boolean onePage) {
        isOnePage = onePage;
    }
}
