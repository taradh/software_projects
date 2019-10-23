package com.zybooks.dotty;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DotsGame mGame;
    private DotsGrid mDotsGrid;
    private TextView mMovesRemaining;
    private TextView mScore;

    private SoundEffects mSoundEffects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovesRemaining = findViewById(R.id.movesRemaining);
        mScore = findViewById(R.id.score);

        mSoundEffects = SoundEffects.getInstance(getApplicationContext());

        mDotsGrid = findViewById(R.id.gameGrid);
        mDotsGrid.setGridListener(mGridListener);

        mGame = DotsGame.getInstance();
        newGame();
    }

    private DotsGrid.DotsGridListener mGridListener = new DotsGrid.DotsGridListener() {

        @Override
        public void onDotSelected(Dot dot, DotsGrid.DotSelectionStatus status) {

            // Ignore selections when game is over
            if (mGame.isGameOver()) return;

            // First dot selected
            if (status == DotsGrid.DotSelectionStatus.First) {
                mSoundEffects.resetTones();
            }

            // Add point to the path
            DotsGame.AddDotStatus addStatus = mGame.addSelectedDot(dot);
            if (addStatus == DotsGame.AddDotStatus.Added) {
                mSoundEffects.playTone(true);
            }
            else if (addStatus == DotsGame.AddDotStatus.Removed) {
                mSoundEffects.playTone(false);
            }

            // Done selecting dots
            if (status == DotsGrid.DotSelectionStatus.Last) {
                if (mGame.getSelectedDots().size() > 1) {
                    mDotsGrid.animateDots();
                    updateMovesAndScore();
                } else {
                    mGame.clearSelectedDots();
                }
            }

            mDotsGrid.invalidate();
        }

        @Override
        public void onAnimationFinished() {
            mGame.finishMove();
            mDotsGrid.invalidate();
            updateMovesAndScore();

            if (mGame.isGameOver()) {
                mSoundEffects.playGameOver();
            }
        }

    };

    public void newGameClick(View view) {

        // Animate the board moving off the screen
        ObjectAnimator moveBoardOff = ObjectAnimator.ofFloat(mDotsGrid, "translationY",
                mDotsGrid.getHeight() * 1.5f);
        moveBoardOff.setDuration(700);
        moveBoardOff.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                // Reset board when off screen
                newGame();
            }
        });

        // Animate the board moving back onto the screen from above
        ObjectAnimator moveBoardOn = ObjectAnimator.ofFloat(mDotsGrid, "translationY",
                mDotsGrid.getHeight() * -1.5f, 0);
        moveBoardOn.setDuration(700);

        AnimatorSet animSet = new AnimatorSet();
        animSet.play(moveBoardOn).after(moveBoardOff);
        animSet.start();
    }

    private void newGame() {
        mGame.newGame();
        mDotsGrid.invalidate();
        updateMovesAndScore();
    }

    private void updateMovesAndScore() {
        mMovesRemaining.setText(Integer.toString(mGame.getMovesLeft()));
        mScore.setText(Integer.toString(mGame.getScore()));
    }
}
