package com.foreverrafs.radiocore.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Rafsanjani on 7/1/2019
 */
public abstract class AnimationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int BOTTOM_UP = 1;
    public static final int FADE_IN = 2;
    public static final int LEFT_RIGHT = 3;
    public static final int RIGHT_LEFT = 4;
    public static final int NONE = 0;
    /* animation duration */
    private static final long DURATION_IN_BOTTOM_UP = 250;
    private static final long DURATION_IN_FADE_ID = 250;
    private static final long DURATION_IN_LEFT_RIGHT = 150;
    private static final long DURATION_IN_RIGHT_LEFT = 150;
    private final AnimationType mAnimationType;
    private final int mAnimationDuration;
    private int mLastPosition = -1;
    private boolean mOn_Attach = true;

    public AnimationAdapter(AnimationType mAnimationType, int mAnimationDuration) {
        this.mAnimationDuration = mAnimationDuration;
        this.mAnimationType = mAnimationType;
    }

    private void animate(View view, int position, AnimationType type, int duration) {
        switch (type) {
            case BOTTOM_UP:
                animateBottomUp(view, position, duration);
                break;

            case FADE_IN:
                animateFadeIn(view, position, duration);
                break;

            case LEFT_RIGHT:
                animateLeftRight(view, position, duration);
                break;

            case RIGHT_LEFT:
                animateRightLeft(view, position, duration);
                break;
        }
    }

    private void animateBottomUp(View view, int position, int duration) {
        boolean not_first_item = position == -1;
        position = position + 1;
        view.setTranslationY(not_first_item ? 800 : 500);
        view.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(view, "translationY", not_first_item ? 800 : 500, 0);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 1.f);
        animatorTranslateY.setStartDelay(not_first_item ? 0 : (position * duration));
        animatorTranslateY.setDuration((not_first_item ? 3 : 1) * duration);
        animatorSet.playTogether(animatorTranslateY, animatorAlpha);
        animatorSet.start();
    }

    private void animateFadeIn(View view, int position, int duration) {
        boolean not_first_item = position == -1;
        position = position + 1;
        view.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 0.f, 0.5f, 1.f);
        ObjectAnimator.ofFloat(view, "alpha", 0.f).start();
        animatorAlpha.setStartDelay(not_first_item ? duration / 2 : (position * duration / 3));
        animatorAlpha.setDuration(duration);
        animatorSet.play(animatorAlpha);
        animatorSet.start();
    }

    private void animateLeftRight(View view, int position, int duration) {
        boolean not_first_item = position == -1;
        position = position + 1;
        view.setTranslationX(-800f);
        view.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(view, "translationX", -800f, 0);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 1.f);
        ObjectAnimator.ofFloat(view, "alpha", 0.f).start();
        animatorTranslateY.setStartDelay(not_first_item ? duration : (position * duration));
        animatorTranslateY.setDuration((not_first_item ? 2 : 1) * duration);
        animatorSet.playTogether(animatorTranslateY, animatorAlpha);
        animatorSet.start();
    }

    private void animateRightLeft(View view, int position, int duration) {
        boolean not_first_item = position == -1;
        position = position + 1;
        view.setTranslationX(view.getX() + 400);
        view.setAlpha(0.f);
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorTranslateY = ObjectAnimator.ofFloat(view, "translationX", view.getX() + 400, 0);
        ObjectAnimator animatorAlpha = ObjectAnimator.ofFloat(view, "alpha", 1.f);
        ObjectAnimator.ofFloat(view, "alpha", 0.f).start();
        animatorTranslateY.setStartDelay(not_first_item ? duration : (position * duration));
        animatorTranslateY.setDuration((not_first_item ? 2 : 1) * duration);
        animatorSet.playTogether(animatorTranslateY, animatorAlpha);
        animatorSet.start();
    }

    /**
     * Set's an animation to be used with the adapter during view binding. Call this function with the itemView and adapter positions
     * as arguments for item animations to take effect.
     *
     * @param view            The itemView of the recyclerView to be animated
     * @param adapterPosition The position of the itemView
     */
    protected void setAnimation(View view, int adapterPosition) {
        if (adapterPosition > mLastPosition) {
            animate(view, mOn_Attach ? adapterPosition : -1, mAnimationType, mAnimationDuration);
            mLastPosition = adapterPosition;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                mOn_Attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    public enum AnimationType {
        BOTTOM_UP,
        FADE_IN,
        LEFT_RIGHT,
        RIGHT_LEFT
    }
}
