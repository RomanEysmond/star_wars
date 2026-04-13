package com.example.starwars.presentation.utils

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.TextView

class AnimatedProgressBarManager(
    private val loadingContainer: View,
    private val loadingPhraseText: TextView,
    private val phrases: List<String>
) {
    companion object {
        private const val PHRASE_CHANGE_DELAY = 3500L
        private const val FADE_DURATION = 500L
    }

    private var phraseIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var phraseRunnable: Runnable? = null
    private var isAnimating = false

    /**
     * Start the phrase animation
     */
    fun start() {
        if (isAnimating) return

        isAnimating = true
        loadingContainer.visibility = View.VISIBLE

        if (phrases.isEmpty()) return

        phraseIndex = 0
        loadingPhraseText.text = phrases[phraseIndex]

        phraseRunnable = object : Runnable {
            override fun run() {
                if (!isAnimating) return

                val fadeOut = AlphaAnimation(1f, 0f).apply {
                    duration = FADE_DURATION
                }

                val fadeIn = AlphaAnimation(0f, 1f).apply {
                    duration = FADE_DURATION
                }

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        if (!isAnimating) return
                        phraseIndex = (phraseIndex + 1) % phrases.size
                        loadingPhraseText.text = phrases[phraseIndex]
                        loadingPhraseText.startAnimation(fadeIn)
                    }
                })

                loadingPhraseText.startAnimation(fadeOut)
                handler.postDelayed(this, PHRASE_CHANGE_DELAY)
            }
        }

        handler.post(phraseRunnable!!)
    }

    /**
     * Stop the phrase animation and hide the container
     */
    fun stop() {
        isAnimating = false
        phraseRunnable?.let {
            handler.removeCallbacks(it)
        }
        phraseRunnable = null
        loadingContainer.visibility = View.GONE
    }

    /**
     * Pause the animation (keep container visible)
     */
    fun pause() {
        isAnimating = false
        phraseRunnable?.let {
            handler.removeCallbacks(it)
        }
    }

    /**
     * Resume the animation from current state
     */
    fun resume() {
        if (isAnimating) return
        start()
    }

    /**
     * Check if animation is currently running
     */
    fun isRunning(): Boolean = isAnimating

    /**
     * Clean up resources (call in onDestroyView)
     */
    fun cleanup() {
        stop()
        handler.removeCallbacksAndMessages(null)
    }
}