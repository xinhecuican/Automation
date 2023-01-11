package tech.xinhecuican.automation.model;

import java.io.Serializable;

public class ClickModel extends Model implements Serializable {
    private int x;
    private int y;
    private int repeatTimes;

    public ClickModel(){
        x = 0;
        y = 0;
        repeatTimes = 1;
    }

    public ClickModel(int x, int y, int repeatTimes)
    {
        this.x = x;
        this.y = y;
        this.repeatTimes = repeatTimes;
    }

    @Override
    public int getModelType() {
        return 0;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getRepeatTimes() {
        return repeatTimes;
    }

    public void setRepeatTimes(int repeatTimes) {
        this.repeatTimes = repeatTimes;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}
