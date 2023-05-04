package com.antoniofalcescu.licenta.utils

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antoniofalcescu.licenta.R
import com.antoniofalcescu.licenta.profile.recentlyPlayedTracks.RecentlyPlayedTrackItem
import com.antoniofalcescu.licenta.profile.artists.ArtistItem
import com.antoniofalcescu.licenta.profile.artists.ArtistsAdapter
import com.antoniofalcescu.licenta.profile.recentlyPlayedTracks.RecentlyPlayedAdapter
import com.antoniofalcescu.licenta.profile.tracks.TrackItem
import com.antoniofalcescu.licenta.profile.tracks.TracksAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.murgupluoglu.flagkit.FlagKit

@BindingAdapter("circleImageUrl")
fun bindCircleImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .transform(CircleCrop())
            .apply(RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}

@BindingAdapter("countryFlag")
fun bindCountry(imgView: ImageView, countryCode: String?) {
    countryCode?.let {
        val countryFlagDrawable = FlagKit.getDrawable(imgView.context, countryCode)

        Glide.with(imgView.context)
            .load(countryFlagDrawable)
            .apply(RequestOptions()
                .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}

@BindingAdapter("roundedImageUrl")
fun bindRoundedImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        val imgUri = imgUrl.toUri().buildUpon().scheme("https").build()
        Glide.with(imgView.context)
            .load(imgUri)
            .transform(RoundedCorners(28))
            .apply(RequestOptions()
                .placeholder(R.drawable.loading_animation)
                .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}

@BindingAdapter("tracksList")
fun bindTracksRecyclerView(recyclerView: RecyclerView, data: List<TrackItem>?) {
    val adapter = recyclerView.adapter as TracksAdapter
    adapter.submitList(data)
}

@BindingAdapter("artistsList")
fun bindArtistsRecyclerView(recyclerView: RecyclerView, data: List<ArtistItem>?) {
    val adapter = recyclerView.adapter as ArtistsAdapter
    adapter.submitList(data)
}

@BindingAdapter("recentlyPlayedList")
fun bindRecentlyPlayedRecyclerView(recyclerView: RecyclerView, data: List<RecentlyPlayedTrackItem>?) {
    val adapter = recyclerView.adapter as RecentlyPlayedAdapter
    adapter.submitList(data)
}