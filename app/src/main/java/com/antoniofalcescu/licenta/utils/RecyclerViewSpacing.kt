package com.antoniofalcescu.licenta.utils

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.R

enum class Orientation {
    VERTICAL,
    HORIZONTAL
}

enum class Spacing {
    EXTRA_SMALL,
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE,
}

fun Context.getSpacing(dimen: Spacing): Int {
    return when(dimen) {
        Spacing.EXTRA_SMALL -> this.resources.getDimensionPixelSize(R.dimen.extra_small_margin)
        Spacing.SMALL -> this.resources.getDimensionPixelSize(R.dimen.small_margin)
        Spacing.MEDIUM -> this.resources.getDimensionPixelSize(R.dimen.medium_margin)
        Spacing.LARGE -> this.resources.getDimensionPixelSize(R.dimen.large_margin)
        Spacing.EXTRA_LARGE -> this.resources.getDimensionPixelSize(R.dimen.extra_large_margin)
    }
}

class RecyclerViewSpacing(private val space: Int, private val orientation: Orientation, private val includeLast: Boolean = false) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        if (parent.getChildAdapterPosition(view) != parent.adapter?.itemCount?.minus(1)) {
            if (orientation == Orientation.HORIZONTAL) {
                outRect.right = space
            } else if (orientation == Orientation.VERTICAL) {
                outRect.bottom = space
            }
        } else if (includeLast) {
            if (orientation == Orientation.HORIZONTAL) {
                outRect.right = space
            } else if (orientation == Orientation.VERTICAL) {
                outRect.bottom = space
            }
        }
    }
}