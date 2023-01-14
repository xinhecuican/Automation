package tech.xinhecuican.automation.model;

import java.io.Serializable;

public class CoordinateDescription implements Serializable {
    public String packageName;
    public String activityName;
    public int x;
    public int y;

    public CoordinateDescription() {
        packageName = "";
        activityName = "";
        x = 0;
        y = 0;
    }

    public CoordinateDescription(CoordinateDescription other) {
        packageName = other.packageName;
        activityName = other.activityName;
        x = other.x;
        y = other.y;
    }
}
