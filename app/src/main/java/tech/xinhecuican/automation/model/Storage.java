package tech.xinhecuican.automation.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Storage implements Serializable {
    private int currentIndex;
    private List<Operation> operations;
    private static Storage _instance = null;
    private transient boolean isLoad;

    public static Storage instance(){
        if(_instance == null)
            _instance = new Storage();
        return _instance;
    }

    private Storage(){
        currentIndex = 0;
        operations = new ArrayList<>();
        isLoad = false;
    }

    public void setCurrentIndex(int index) {this.currentIndex = index;}
    public int getCurrentIndex() {return currentIndex;}
    public List<Operation> getOperations(){return operations;}
    public void setOperations(List<Operation> operations){this.operations = operations;}
    public void addOperation(Operation operation){this.operations.add(operation);}
    public void removeOperation(Operation operation){this.operations.remove(operation);}
    public void removeOperation(int index){this.operations.remove(index);}

    public void save()
    {

    }

    public void load()
    {
        if(isLoad)
            return;
        Operation operation = new Operation("test");
        operation.addModel(new ClickModel(1, 2, 3));
        operations.add(operation);
        isLoad = true;
    }

    public void deleteOperation(List<Integer> indexs)
    {
        indexs.sort(Comparator.naturalOrder());
        for(int i=0; i<indexs.size(); i++)
        {
            operations.remove(indexs.get(i) - i);
        }
    }
}
