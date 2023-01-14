package tech.xinhecuican.automation.listener;

import java.util.List;

import tech.xinhecuican.automation.manager.OperationManager;
import tech.xinhecuican.automation.model.Operation;

public abstract class OperationListenerAdapter implements OperationManager.OperationListener {

    @Override
    public void onOperationDelete(List<Integer> pos) {

    }

    @Override
    public void onOperationAdd(Operation operation) {

    }

    @Override
    public void onPackageChange(String[] packages){

    }
}
