package tech.xinhecuican.automation.manager;

import java.util.ArrayList;
import java.util.List;

import tech.xinhecuican.automation.model.Storage;

public class OperationManager {
    private List<Integer> readyDelete;
    private List<OperationDeleteListener> listeners;

    public interface OperationDeleteListener
    {
        void onOperationDelete(List<Integer> pos);
    }

    public OperationManager()
    {
        readyDelete = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    public void addDeleteListener(OperationDeleteListener listener)
    {
        listeners.add(listener);
    }

    public void cancelDelete()
    {
        readyDelete.clear();
    }

    public void delete()
    {
        Storage.instance().deleteOperation(readyDelete);
        for(OperationDeleteListener listener : listeners)
            listener.onOperationDelete(readyDelete);
        readyDelete.clear();
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
}
