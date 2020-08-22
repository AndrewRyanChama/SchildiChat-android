/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package im.vector.app.features.home.room.detail.timeline.item

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.core.view.children
import androidx.core.view.updateLayoutParams
import com.airbnb.epoxy.EpoxyAttribute
import im.vector.app.R
import im.vector.app.core.epoxy.VectorEpoxyHolder
import im.vector.app.core.epoxy.VectorEpoxyModel
import im.vector.app.core.platform.CheckableView
import im.vector.app.core.ui.views.ReadReceiptsView
import im.vector.app.core.utils.DimensionConverter
import im.vector.app.features.themes.BubbleThemeUtils

/**
 * Children must override getViewType()
 */
abstract class BaseEventItem<H : BaseEventItem.BaseHolder> : VectorEpoxyModel<H>() {

    // To use for instance when opening a permalink with an eventId
    @EpoxyAttribute
    var highlighted: Boolean = false
    @EpoxyAttribute
    open var leftGuideline: Int = 0

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    lateinit var dimensionConverter: DimensionConverter

    @CallSuper
    override fun bind(holder: H) {
        super.bind(holder)
        holder.leftGuideline.updateLayoutParams<RelativeLayout.LayoutParams> {
            if (ignoreMessageGuideline(holder.leftGuideline.context)) {
                this.marginStart = 0
            } else {
                this.marginStart = leftGuideline
            }
        }
        holder.checkableBackground.isChecked = highlighted

        updateMessageBubble(holder)
    }

    /**
     * Returns the eventIds associated with the EventItem.
     * Will generally get only one, but it handles the merging items.
     */
    abstract fun getEventIds(): List<String>

    abstract class BaseHolder(@IdRes val stubId: Int) : VectorEpoxyHolder() {
        val leftGuideline by bind<View>(R.id.messageStartGuideline)
        val checkableBackground by bind<CheckableView>(R.id.messageSelectedBackground)
        val readReceiptsView by bind<ReadReceiptsView>(R.id.readReceiptsView)

        override fun bindView(itemView: View) {
            super.bindView(itemView)
            inflateStub()
        }

        private fun inflateStub() {
            view.findViewById<ViewStub>(stubId).inflate()
        }
    }

    open fun ignoreMessageGuideline(context: Context): Boolean {
        return false
    }

    protected fun setFlatRtl(layout: ViewGroup, direction: Int, childDirection: Int, depth: Int = 1) {
        layout.layoutDirection = direction
        for (child in layout.children) {
            if (depth > 1 && child is ViewGroup) {
                setFlatRtl(child, direction, childDirection, depth-1)
            } else {
                child.layoutDirection = childDirection
            }
        }
    }

    fun updateMessageBubble(holder: H) {
        val bubbleStyleSetting = BubbleThemeUtils.getBubbleStyle(holder.checkableBackground.context)
        val bubbleStyle = if (messageBubbleAllowed(holder.checkableBackground.context)) bubbleStyleSetting else BubbleThemeUtils.BUBBLE_STYLE_NONE
        val reverseBubble = shouldReverseBubble() && bubbleStyle == BubbleThemeUtils.BUBBLE_STYLE_BOTH

        setBubbleLayout(holder, bubbleStyle, bubbleStyleSetting, reverseBubble)
    }

    open fun messageBubbleAllowed(context: Context): Boolean {
        return false
    }

    open fun shouldReverseBubble(): Boolean {
        return false
    }

    @CallSuper
    open fun setBubbleLayout(holder: H, bubbleStyle: String, bubbleStyleSetting: String, reverseBubble: Boolean) {
        val defaultDirection = holder.readReceiptsView.resources.configuration.layoutDirection;
        val defaultRtl = defaultDirection == View.LAYOUT_DIRECTION_RTL
        val reverseDirection = if (defaultRtl) View.LAYOUT_DIRECTION_LTR else View.LAYOUT_DIRECTION_RTL

        // Always keep read receipts of others on other side for dual side bubbles
        val dualBubbles = bubbleStyleSetting == BubbleThemeUtils.BUBBLE_STYLE_BOTH

        val receiptParent = holder.readReceiptsView.parent
        if (receiptParent is LinearLayout) {
            (holder.readReceiptsView.layoutParams as LinearLayout.LayoutParams).gravity = if (dualBubbles) Gravity.START else Gravity.END

            (receiptParent.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.END_OF)
            (receiptParent.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_START)
            if (dualBubbles) {
                (receiptParent.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE)
            } else {
                (receiptParent.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.END_OF, R.id.messageStartGuideline)
            }
        } else {
            if (dualBubbles) {
                (holder.readReceiptsView.layoutParams as RelativeLayout.LayoutParams).removeRule(RelativeLayout.ALIGN_PARENT_END)
            } else {
                (holder.readReceiptsView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.ALIGN_PARENT_END)
            }
        }

        // Also set rtl to have members fill from the natural side
        setFlatRtl(holder.readReceiptsView, if (dualBubbles) reverseDirection else defaultDirection, defaultDirection)
    }

}
