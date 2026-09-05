    // ১. থিংকিং মোড চালু (Fast Visual Feedback)
    private fun setWifeThinkingState() {
        Handler(Looper.getMainLooper()).post {
            tvStatus.visibility = View.VISIBLE
            tvStatus.text = "Wife is thinking..."

            // দ্রুত পালসিং অ্যানিমেশন (দ্রুত বোঝাতে duration ৩০০ms)
            thinkingAnimator = ObjectAnimator.ofPropertyValuesHolder(
                floatingView,
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.15f),
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.15f),
                android.animation.PropertyValuesHolder.ofFloat(View.ALPHA, 1.0f, 0.7f)
            ).apply {
                duration = 300
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
                start()
            }
        }
    }

    // ২. উত্তর আসা মাত্র থিংকিং বন্ধ
    private fun stopWifeThinkingState() {
        Handler(Looper.getMainLooper()).post {
            thinkingAnimator?.cancel()
            floatingView?.scaleX = 1.0f
            floatingView?.scaleY = 1.0f
            floatingView?.alpha = 1.0f
            tvStatus.visibility = View.GONE
        }
    }
