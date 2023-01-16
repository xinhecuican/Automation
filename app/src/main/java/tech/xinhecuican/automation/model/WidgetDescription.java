package tech.xinhecuican.automation.model;

import android.graphics.Rect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class WidgetDescription implements Externalizable {
    public String packageName;
    public String activityName;
    public String className;
    public String id;
    public String description;
    public String text;
    public Rect rect;
    public boolean isClickable;
    public boolean isScrollable;

    public WidgetDescription() {
        packageName = "";
        activityName = "";
        className = "";
        id = "";
        description = "";
        text = "";
        rect = new Rect();
        isClickable = false;
        isScrollable = false;
    }

    public WidgetDescription(WidgetDescription other) {
        this.packageName = other.packageName;
        activityName = other.activityName;
        this.className = other.className;
        this.id = other.id;
        this.description = other.description;
        this.text = other.text;
        this.rect = other.rect;
        this.isClickable = other.isClickable;
        this.isScrollable = other.isScrollable;
    }

    public String toString(){
        return "click:".concat((this.isClickable ? "true scroll " : "false scroll "))
                .concat(this.isScrollable ? "true" : "false")
                .concat(" id:").concat(this.id).concat(" desc:").concat(this.description)
                .concat(" text:").concat(this.text);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(packageName);
        out.writeObject(className);
        out.writeObject(id);
        out.writeObject(description);
        out.writeObject(text);
        out.writeInt(rect.top);
        out.writeInt(rect.left);
        out.writeInt(rect.bottom);
        out.writeInt(rect.right);
        out.writeBoolean(isClickable);
        out.writeBoolean(isScrollable);
    }

    @Override
    public void readExternal(ObjectInput in) throws ClassNotFoundException, IOException {
        packageName = (String) in.readObject();
        className = (String) in.readObject();
        id = (String) in.readObject();
        description = (String) in.readObject();
        text = (String) in.readObject();
        int top = in.readInt();
        int left = in.readInt();
        int bottom = in.readInt();
        int right = in.readInt();
        isClickable = in.readBoolean();
        isScrollable = in.readBoolean();
        rect = new Rect(left, top, right, bottom);
    }
}
