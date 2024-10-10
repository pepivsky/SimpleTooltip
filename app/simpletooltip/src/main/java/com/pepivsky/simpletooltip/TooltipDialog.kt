package com.pepivsky.simpletooltip

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.res.Resources
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.animation.addListener
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import com.pepivsky.simpletooltip.databinding.TooltipDialogBinding


/**
 * Class for creating and displaying tooltips in an Android application.
 * Tooltips are informative pop-up windows that provide additional information
 * or descriptions for UI elements.
 */
class TooltipDialog private constructor() {

    private var onTooltipClosed: () -> Unit = {}
    private var onContentLoaded: (View) -> Unit = { _ -> }

    private var displayDialogItemIndex: Int = 0

    @LayoutRes
    private var textContentLayout: Int = R.layout.tooltip_text_content


    private lateinit var resources: Resources
    private lateinit var rootContentArea: ViewGroup
    private lateinit var flTooltip: FrameLayout
    private lateinit var textTooltipDialog: View
    private lateinit var layoutInflater: LayoutInflater
    private lateinit var binding: TooltipDialogBinding

    /**
     * Builder class for configuring and creating instances of [TooltipDialog].
     *
     * @param activity The [Activity] instance where the tooltip will be displayed.
     */
    class Builder(private val activity: Activity) {

        @LayoutRes
        private var bTextContentLayout: Int = R.layout.tooltip_text_content

        private var bOnTooltipClosed: () -> Unit = {}
        private var bOnContentLoaded: (View) -> Unit = { _ -> }
        private var disableTapAnywhere = false

        /**
         * Sets an event listener for the tooltip closed event.
         *
         * @param onTooltipClosed The callback function to be executed when the tooltip is closed.
         * @return The [Builder] instance for method chaining.
         */
        fun onTooltipClosedListener(onTooltipClosed: () -> Unit): Builder {
            this.bOnTooltipClosed = onTooltipClosed
            return this
        }

        /**
         * Sets the layout resource for the tooltip's content.
         *
         * @param layout The layout resource ID for the tooltip's content.
         * @return The [Builder] instance for method chaining.
         */
        fun textContentLayout(@LayoutRes layout: Int): Builder {
            this.bTextContentLayout = layout
            return this
        }

        /**
         * Sets an event listener for the tooltip content loaded event.
         *
         * @param onContentLoaded The callback function to be executed when the tooltip content is loaded.
         * @return The [Builder] instance for method chaining.
         */
        fun onContentLoadedListener(onContentLoaded: (View) -> Unit): Builder {
            this.bOnContentLoaded = onContentLoaded
            return this
        }

        /**
         * Disables or enables the ability to close the tooltip by tapping anywhere.
         *
         * @param shouldBeDisabled `true` to disable tap anywhere, `false` to enable it.
         * @return The [Builder] instance for method chaining.
         */
        fun disableTapAnywhere(shouldBeDisabled: Boolean): Builder {
            this.disableTapAnywhere = shouldBeDisabled
            return this
        }

        /**
         * Builds an instance of [TooltipDialog] with the specified configuration.
         *
         * @return The configured [TooltipDialog] instance.
         */
        fun build(): TooltipDialog {
            return TooltipDialog().apply {
                layoutInflater = activity.layoutInflater
                resources = activity.resources
                onTooltipClosed = bOnTooltipClosed
                onContentLoaded = bOnContentLoaded
                textContentLayout = bTextContentLayout

                flTooltip = FrameLayout(activity).apply {
                    id = R.id.flTooltip
                    visibility = View.INVISIBLE
                    alpha = 0.0f
                    setOnClickListener {
                        if (!disableTapAnywhere) {
                            dismissTooltipDialog()
                        }
                    }
                }

                rootContentArea =
                    activity.window.decorView.findViewById(android.R.id.content) as ViewGroup
                rootContentArea.addView(
                    flTooltip, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
                binding = TooltipDialogBinding.inflate(layoutInflater)
            }
        }
    }

    /**
     * Dismisses the tooltip dialog.
     */
    fun dismissTooltipDialog() {
        ObjectAnimator.ofFloat(
            flTooltip,
            "alpha",
            1.0f, 0.0f
        ).apply {
            duration = ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { va ->
                val alpha = (va.animatedValue as Float)
                flTooltip.alpha = alpha
                flTooltip.requestLayout()
            }
            addListener(
                onEnd = {
                    resetRootContent()
                }
            )
            start()
        }
    }

    private fun resetRootContent() {
        displayDialogItemIndex = 0
        rootContentArea.removeView(flTooltip)
        onTooltipClosed()
    }

    /**
     * Shows the text-based tooltip dialog at a specified target view.
     *
     * @param targetView The view relative to which the tooltip should be displayed.
     */
    fun showTextTooltipDialog(targetView: View) {
        binding = TooltipDialogBinding.inflate(layoutInflater)
        binding.layoutDialogContent.layoutResource = textContentLayout
        val content = binding.layoutDialogContent.inflate()
        onContentLoaded(content)

        flTooltip.addView(
            binding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        binding.root.post {
            repositionDialogBasedOnTarget(targetView, content)
            animateTooltipAppearance()
        }
    }

    private fun animateTooltipAppearance() {
        flTooltip.visibility = View.VISIBLE
        flTooltip.animate()
            .alpha(1.0f)
            .setDuration(ANIMATION_DURATION)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun repositionDialogBasedOnTarget(targetView: View, content: View) {
        val (positionX, positionY) = getTargetViewPosition(targetView)

        val tooltipX = positionX + (targetView.width / 2) - (binding.root.width / 2)
        val tooltipY = positionY - CLIP_PADDING_OFFSET - binding.root.height

        val pointerX = positionX + (targetView.width / 2)
        var pointerWidth = (binding.layoutPointerBottom.root.width / 2) + content.marginStart

        when {
            (tooltipX < 0) or (tooltipX + binding.root.width > flTooltip.width) -> { // Check whether the dialog over shoots to left or right
                binding.root.x = 0.0f + content.marginStart
            }

            else -> {
                pointerWidth -= content.marginStart
                binding.root.x = tooltipX
            }
        }

        if (tooltipY < 0) {
            // migrate this
            binding.layoutPointerTop.root.visibility = View.VISIBLE
            binding.layoutPointerBottom.root.visibility = View.GONE
            binding.layoutPointerTop.root.x = pointerX - pointerWidth
        } else {
            binding.layoutPointerTop.root.visibility = View.GONE
            binding.layoutPointerBottom.root.visibility = View.VISIBLE
            binding.layoutPointerBottom.root.x = pointerX - pointerWidth
        }

        binding.root.y = tooltipY.takeIf {
            it > -1
        } ?: (positionY + CLIP_PADDING_OFFSET + targetView.height)

        // content
        repositionContentOfDialog(positionX, targetView, content)
    }

    private fun repositionContentOfDialog(positionX: Float, targetView: View, content: View) {
        val contentX = positionX - (content.width / 2) + (targetView.width / 2)
        when {
            (contentX < 0) -> {
                content.x = 0.0f
            }

            (contentX + content.width > flTooltip.width) -> {
                content.x = flTooltip.width.toFloat() - (content.width + (content.marginEnd * 2))
            }

            else -> {
                content.x = contentX
            }
        }
    }

    private fun getTargetViewPosition(targetView: View): TargetViewPosition {
        val offsetViewBounds = Rect()
        targetView.getDrawingRect(offsetViewBounds)
        rootContentArea.offsetDescendantRectToMyCoords(targetView, offsetViewBounds)
        return TargetViewPosition(
            offsetViewBounds.left.toFloat(),
            offsetViewBounds.top.toFloat()
        )
    }

    private data class TargetViewPosition(
        val x: Float,
        val y: Float
    )

    companion object {
        private const val CLIP_PADDING_OFFSET = 13
        private const val ANIMATION_DURATION = 250L

    }
}