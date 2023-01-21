package tech.xinhecuican.automation.manager;

import java.util.ArrayList;
import java.util.List;

import tech.xinhecuican.automation.model.Operation;
import tech.xinhecuican.automation.model.Storage;

public class OperationManager {
    private List<Integer> readyDelete;
    private List<OperationListener> listeners;
    private static OperationManager _instance = null;

    public static OperationManager instance(){
        if(_instance == null)
            _instance = new OperationManager();
        return _instance;
    }

    public static interface OperationListener
    {
        default void onOperationDelete(List<Integer> pos){}
        default void onOperationAdd(Operation operation){}
        default void onPackageChange(String[] packages){}
    }

    public OperationManager()
    {
        readyDelete = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public void addListener(OperationListener listener)
    {
        listeners.add(listener);
    }
    public void removeListener(OperationListener listener){listeners.remove(listener);}

    public void cancelDelete()
    {
        readyDelete.clear();
    }

    public void delete()
    {
        int oldLength = Storage.instance().getOperationPackageNames().length;
        Storage.instance().removeActivity(readyDelete);
        for(OperationListener listener : listeners)
            listener.onOperationDelete(readyDelete);
        Storage.instance().deleteOperation(readyDelete);
        if(Storage.instance().removePackageName(oldLength)){
            for(OperationListener listener : listeners)
                listener.onPackageChange(Storage.instance().getOperationPackageNames());
        }
        readyDelete.clear();
        Storage.instance().save();
    }

    public void append(Operation operation){
        Storage.instance().addOperation(operation);
        Storage.instance().addActivity(operation);
        if(Storage.instance().addPackageName(operation)){
            for(OperationListener listener : listeners)
                listener.onPackageChange(Storage.instance().getOperationPackageNames());
        }
        for(OperationListener listener : listeners)
            listener.onOperationAdd(operation);
        Storage.instance().save();
    }

    public void change(Operation operation, int index){
        boolean activityChange = !operation.getActivityName().equals(
                Storage.instance().getOperation(index).getActivityName());
        boolean packageChange = !operation.getPackageName().equals(
                Storage.instance().getOperation(index).getPackageName());
        if(activityChange)
            Storage.instance().removeActivity(index);
        int packageLength = Storage.instance().getOperationPackageNames().length;
        Storage.instance().setOperation(index, operation);
        Storage.instance().addActivity(operation);
        if(packageChange){
            Storage.instance().removePackageName(packageLength);
            Storage.instance().addPackageName(operation);
            for(OperationListener listener : listeners){
                listener.onPackageChange(Storage.instance().getOperationPackageNames());
            }
        }
        Storage.instance().save();
    }

    public void addDelete(int pos)
    {
        readyDelete.add(pos);
    }

    public void removeDelete(int pos)
    {
        for(int i=0; i<readyDelete.size(); i++)
        {
            if(readyDelete.get(i) == pos)
            {
                readyDelete.remove(i);
                break;
            }
        }
    }

    public int getSearchPostion(String text){
        int index = 0;
        for(Operation operation : Storage.instance().getOperations()){
            if(operation.getName().contains(text))
                return index;
            else if(operation.getActivityName().contains(text))
                return index;
            index++;
        }
        return -1;
    }
}
