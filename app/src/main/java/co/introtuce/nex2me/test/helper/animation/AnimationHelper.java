package co.introtuce.nex2me.test.helper.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import co.introtuce.nex2me.test.R;


public class AnimationHelper {



    public void fadeIn(View view){

        view.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                            view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }



    public void fadeOut(View view){

        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(null);
    }


    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }


    public void hideSlidUp(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0, // fromXDelta
                0,// toXDelta
                0,// fromYDelta
                -(view.getHeight())// toYDelta
        );
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }
    public void showSlidDown(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                -view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }


    public void applyZoomAnimation(View view, Context context){
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
        view.startAnimation(animation);
    }

    public void translateView(View view, int fromXDelta, int toXDelta, int fromYDelfta, int toYDelta){
        TranslateAnimation anim = new TranslateAnimation(fromXDelta,toXDelta,fromYDelfta,toXDelta);
        anim.setDuration(500);
        anim.setFillAfter(true);
        view.startAnimation(anim);
    }

    public void scaleView(View v, float startScale, float endScale) {
        Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF, 0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 1f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setDuration(1000);
        v.startAnimation(anim);
    }

    public void translateToTopLeftCorner(View view, View parent){
       /* int deltaX = (parent.getWidth() / 2) - (view.getWidth() / 2);
        int deltaY = (parent.getHeight() / 2) - (view.getHeight() / 2);

        TranslateAnimation anim = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0.0f,
                TranslateAnimation.ABSOLUTE, 0.0f,
                TranslateAnimation.ABSOLUTE, 0.0f,
                TranslateAnimation.ABSOLUTE, 0.0f
        );
        anim.setFillAfter(true);
        anim.setDuration(1000);

        view.startAnimation(anim);*/

        ObjectAnimator animator = ObjectAnimator.ofFloat(view,"translationX",0f);
        animator.setDuration(2000);
        animator.start();


    }

}
