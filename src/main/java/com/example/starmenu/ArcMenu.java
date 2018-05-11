package com.example.starmenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TimeUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

/**
 * Created by tujianhua on 2017/9/17.
 */

public class ArcMenu extends ViewGroup implements View.OnClickListener{

    private static final int POS_LEFT_TOP=0;
    private static final int POS_LEFT_BOTTOM=1;
    private static final int POS_RIGHT_TOP=2;
    private static final int POS_RIGHT_BOTTOM=3;
    private position mposition;
    private int mradius;
    private states currentState=states.OPEN;



    private enum position{
        LEFT_BOTTOM,LEFT_TOP,RIGHT_BOTTOM,RIGHT_TOP
    }
    private enum states{
        CLOSE,OPEN
    }

    public interface onMenuItemClickListener{
        void onClick(View view, int pos);
    }

    public ArcMenu(Context context) {
        this(context,null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcMenu);
        int pos=a.getInt(R.styleable.ArcMenu_position,POS_RIGHT_BOTTOM);
        switch (pos){
            case POS_LEFT_TOP:{
                mposition=position.LEFT_TOP;
            }
            case POS_LEFT_BOTTOM:{
                mposition=position.LEFT_BOTTOM;
            }
            case POS_RIGHT_TOP:{
                mposition=position.RIGHT_TOP;
            }
            case POS_RIGHT_BOTTOM:{
                mposition = position.RIGHT_BOTTOM;
            }
        }
       mradius = (int) a.getDimension(R.styleable.ArcMenu_radius,TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count=getChildCount();
        for (int i=0;i<count;i++){
            measureChild(getChildAt(i),widthMeasureSpec,heightMeasureSpec);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
/*
* 布局子view
*
* */
    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if (b) {
            layoutCButton();

            int count = getChildCount();


            for (int j = 0; j < count-1; j++) {
                int ct = (int) (mradius * Math.sin(Math.PI / 2 / (count - 2)*j));
                int cl = (int) (mradius * Math.cos(Math.PI / 2 / (count - 2)*j));

                int sWidth = getChildAt(j + 1).getMeasuredWidth();
                int sHeight = getChildAt(j + 1).getMeasuredHeight();

                if (mposition==position.RIGHT_TOP||mposition==position.RIGHT_BOTTOM){
                    cl=getMeasuredWidth()-cl-sWidth;
                }
                if (mposition == position.LEFT_BOTTOM || mposition == position.RIGHT_BOTTOM) {
                    ct=getMeasuredHeight()-ct-sHeight;
                }

                getChildAt(j+1).layout(cl,ct,cl+sWidth,ct+sHeight);
            }
        }
    }

    private void layoutCButton() {
        View mCButton = getChildAt(0);
        mCButton.setOnClickListener(this);

        int l=0;
        int t=0;

        int cWidth=mCButton.getMeasuredWidth();
        int cHeight=mCButton.getMeasuredHeight();

        switch (mposition){
            case LEFT_TOP:{
                l=0;
                t=0;
                break;
            }
            case LEFT_BOTTOM:{
                l=0;
                t=getMeasuredHeight()-cHeight;
                break;
            }
            case RIGHT_TOP:{
                l=getMeasuredWidth()-cWidth;
                break;
            }
            case RIGHT_BOTTOM:{
                t=getMeasuredHeight()-cHeight;
                l=getMeasuredWidth()-cWidth;
                break;
            }
        }
        mCButton.layout(l,t,l+cWidth,t+cHeight);
    }
    @Override
    public void onClick(View view) {
        rotateCButton(view);
        toggleMenu(300);
    }

    private void toggleMenu(int duration) {
        int counts=getChildCount();

        for (int i=0;i<counts-1;i++){
            final View childView = getChildAt(i+1);
            childView.setVisibility(VISIBLE);
            int t = (int) (mradius * Math.sin(Math.PI / 2 / (counts - 2)*i));
            int l = (int) (mradius * Math.cos(Math.PI / 2 / (counts - 2)*i));
            int xflag=1;
            int yflag=1;
            if (mposition == position.LEFT_TOP || mposition == position.RIGHT_TOP) {
                yflag = -1;
            }
            if (mposition == position.LEFT_TOP || mposition == position.LEFT_BOTTOM) {
                xflag = -1;
            }

            TranslateAnimation translateAnimation=null;
            if (currentState == states.CLOSE) {
                translateAnimation = new TranslateAnimation(xflag * l, 0,yflag * t,0);
                childView.setClickable(true);
                childView.setFocusable(true);}
            if (currentState == states.OPEN) {
                translateAnimation = new TranslateAnimation(0, xflag * l, 0, yflag * t);
                childView.setClickable(false);
                childView.setFocusable(false);
            }
            translateAnimation.setDuration(duration);
            translateAnimation.setStartOffset(150/(counts-i));
            //translateAnimation.setFillEnabled(true);
            translateAnimation.setFillAfter(true);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (currentState == states.CLOSE) {
                        childView.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            RotateAnimation animMenu = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animMenu.setDuration(duration);
            //animMenu.setFillEnabled(true);
            animMenu.setFillAfter(true);
            AnimationSet animset = new AnimationSet(true);

            animset.addAnimation(animMenu);
            animset.addAnimation(translateAnimation);

            childView.startAnimation(animset);


        }
        changeStatus();
    }

    private void changeStatus() {
        currentState = (currentState == states.CLOSE ? states.OPEN : states.CLOSE);
    }

    private void rotateCButton(View CButton) {
        RotateAnimation animCButton = new RotateAnimation(0, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        animCButton.setDuration(300);
        animCButton.setFillAfter(true);
    }
}
