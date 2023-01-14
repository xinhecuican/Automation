package tech.xinhecuican.automation.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {
    public AlphaAnimation alphaAniShow, alphaAniHide;
    public TranslateAnimation translateAniUp, translateAniDown;
    public AnimationSet defaultShow, defaultHide;

    private static AnimationUtil _instance;

    public static AnimationUtil instance(){
        if(_instance == null)
            _instance = new AnimationUtil();
        return _instance;
    }

    private AnimationUtil()
    {
        translateAnimation();
        alphaAnimation();
        defaultShow = new AnimationSet(true);
        defaultShow.setDuration(200);
        defaultShow.setInterpolator(new DecelerateInterpolator());
        defaultShow.addAnimation(translateAniUp);
        defaultShow.addAnimation(alphaAniShow);

        defaultHide = new AnimationSet(true);
        defaultHide.setDuration(200);
        defaultHide.setInterpolator(new DecelerateInterpolator());
        defaultHide.addAnimation(translateAniDown);
        defaultHide.addAnimation(alphaAniHide);
    }


    //位移动画
    private void translateAnimation() {


        //向上位移显示动画  从自身位置的最下端向上滑动了自身的高度
        translateAniUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,//RELATIVE_TO_SELF表示操作自身
                0,//fromXValue表示开始的X轴位置
                Animation.RELATIVE_TO_SELF,
                0,//fromXValue表示结束的X轴位置
                Animation.RELATIVE_TO_SELF,
                1,//fromXValue表示开始的Y轴位置
                Animation.RELATIVE_TO_SELF,
                0);//fromXValue表示结束的Y轴位置
        translateAniUp.setRepeatMode(Animation.REVERSE);
        translateAniUp.setDuration(200);

        //向下位移隐藏动画  从自身位置的最上端向下滑动了自身的高度
        translateAniDown = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF,//RELATIVE_TO_SELF表示操作自身
                0,//fromXValue表示开始的X轴位置
                Animation.RELATIVE_TO_SELF,
                0,//fromXValue表示结束的X轴位置
                Animation.RELATIVE_TO_SELF,
                0,//fromXValue表示开始的Y轴位置
                Animation.RELATIVE_TO_SELF,
                1);//fromXValue表示结束的Y轴位置
        translateAniDown.setRepeatMode(Animation.REVERSE);
        translateAniDown.setDuration(200);
    }


    //透明度动画
    private void alphaAnimation() {
        //显示
        alphaAniShow = new AlphaAnimation(0, 1);//百分比透明度，从0%到100%显示
        alphaAniShow.setDuration(1000);//一秒

        //隐藏
        alphaAniHide = new AlphaAnimation(1, 0);
        alphaAniHide.setDuration(1000);
    }

}
