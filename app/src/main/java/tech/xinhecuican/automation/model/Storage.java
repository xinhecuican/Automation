package tech.xinhecuican.automation.model;

import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import tech.xinhecuican.automation.utils.Utils;

public class Storage implements Serializable {
    private static final long serialVersionUID = 8013166858589769929L;

    private List<Operation> operations;
    private static Storage _instance = null;
    private transient boolean isLoad = false;
    private transient boolean isShowBall = false;
    private boolean isOpen;
    private boolean isHideTray;
    // 根据活动名查找活动
    private Map<String, List<Operation>> activityChooser;
    private Set<String> operationPackageNames;
    private transient boolean isChooseActivity = false;
    private transient Operation choosedOperation = null;


    public static Storage instance(){
        if(_instance == null)
            _instance = new Storage();
        return _instance;
    }

    private Storage(){
        operations = new ArrayList<>();
        isOpen = false;
        activityChooser = new HashMap<>();
        operationPackageNames = new HashSet<>();
        load();
    }

    public List<Operation> getOperations(){return operations;}
    public void setOperations(List<Operation> operations){this.operations = operations;}
    public void addOperation(Operation operation){this.operations.add(operation);}
    public void removeOperation(Operation operation){this.operations.remove(operation);}
    public void setOperation(int index, Operation operation){
        operations.set(index, operation);
    }
    public Operation getOperation(int index){
        if(index >= operationCount() || index < 0){
            return null;
        }
        return operations.get(index);
    }
    public void removeOperation(int index){this.operations.remove(index);}
    public int operationCount(){return this.operations.size();}
    public boolean isOpen() {
        return isOpen;
    }
    public void setOpen(boolean open) {
        isOpen = open;
        save();
    }

    public void reset(){
        operations = new ArrayList<>();
        isOpen = false;
        activityChooser = new HashMap<>();
        operationPackageNames = new HashSet<>();
        isHideTray = false;
        isChooseActivity = false;
    }

    public boolean isShowBall() {
        return isShowBall;
    }

    public void setShowBall(boolean showBall) {
        isShowBall = showBall;
        save();
    }

    public boolean isHideTray() {
        return isHideTray;
    }

    public void setHideTray(boolean hideTray) {
        isHideTray = hideTray;
        save();
    }

    public void checkOpen(boolean open){
        if(!open)
            isOpen = false;
    }

    public void removeActivity(String activityName, Operation operation){
        if(activityChooser.containsKey(activityName)){
            activityChooser.get(activityName).remove(operation);
        }
    }

    public void removeActivity(int index){
        String activityName = operations.get(index).getActivityName();
        Operation operation = operations.get(index);
        removeActivity(activityName, operation);
    }

    public void removeActivity(List<Integer> list){
        for(Integer index : list){
            removeActivity(operations.get(index).getActivityName(), operations.get(index));
        }
    }

    public void addActivity(Operation operation){
        if(!activityChooser.containsKey(operation.getActivityName()))
            activityChooser.put(operation.getActivityName(), new ArrayList<>());
        List<Operation> chooserActivities = activityChooser.get(operation.getActivityName());
        boolean find = false;
        for(int i=0; i<chooserActivities.size(); i++){
            if(chooserActivities.get(i) == operation){
                find = true;
                chooserActivities.set(i, operation);
                break;
            }
        }
        if(!find)
            activityChooser.get(operation.getActivityName()).add(operation);
    }

    public boolean addPackageName(Operation operation){
        if(!operation.getPackageName().equals("")){
            return operationPackageNames.add(operation.getPackageName());
        }
        return false;
    }

    public String[] getOperationPackageNames() {
        String [] translate = operationPackageNames.toArray(new String[0]);
        if(translate == null)
            return new String[0];
        else
            return translate;
    }

    public boolean removePackageName(int oldLength){
        Set<String> newSet = new HashSet<>();
        for(Operation operation : operations){
            if(!operation.getPackageName().equals(""))
                newSet.add(operation.getPackageName());
        }
        operationPackageNames = newSet;
        return oldLength != operationPackageNames.size();
    }

    public Operation findOperationByActivity(String activityName){
        if(activityChooser.containsKey(activityName)) {
            Operation result = null;
            for(Operation operation : Objects.requireNonNull(activityChooser.get(activityName))){
                if(operation.isAuto())
                    return operation;
                result = operation;
            }
            return result;
        }
        return null;
    }



    public void save()
    {
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(
                    new FileOutputStream("/data/data/" + Utils.packageName + "/" + "save"));
            outputStream.writeObject(this);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void save(ParcelFileDescriptor path){
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(
                    new FileOutputStream(path.getFileDescriptor()));
            outputStream.writeObject(this);
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void load()
    {
        if(isLoad)
            return;
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(
                    new FileInputStream("/data/data/" + Utils.packageName + "/" + "save")
            );
            Storage storage = (Storage)inputStream.readObject();
            copy(storage);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isLoad = true;
        }
    }

    public void load(ParcelFileDescriptor path){
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(
                    new FileInputStream(path.getFileDescriptor())
            );
            Storage storage = (Storage)inputStream.readObject();
            combine(storage);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isLoad = true;
        }
    }

    public void deleteOperation(List<Integer> indexs)
    {
        indexs.sort(Comparator.naturalOrder());
        for(int i=0; i<indexs.size(); i++)
        {
            operations.remove(indexs.get(i) - i);
        }
    }

    private void copy(Storage storage){
        if(storage.operations != null)
            this.operations = storage.operations;
        if(storage.activityChooser != null)
            this.activityChooser = storage.activityChooser;
        if(storage.operationPackageNames != null)
            this.operationPackageNames = storage.operationPackageNames;
        this.isOpen = storage.isOpen;
        this.isHideTray = storage.isHideTray;
    }

    private void combine(Storage storage){
        if(storage.operations != null){
            this.operations.addAll(storage.operations);
            for(Operation operation : storage.operations){
                addActivity(operation);
                addPackageName(operation);
            }
        }
    }

    public void setChooseActivity(boolean chooseActivity){
        this.isChooseActivity = chooseActivity;
    }

    public void setChoosedOperation(Operation choosedOperation) {
        this.choosedOperation = choosedOperation;
    }

    public boolean isChooseActivity() {
        return isChooseActivity;
    }

    public Operation getChoosedOperation() {
        return choosedOperation;
    }
}
