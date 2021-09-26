package com.lanedever.podplay.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lanedever.podplay.model.Episode
import com.lanedever.podplay.model.Podcast
import com.lanedever.podplay.repository.PodcastRepo
import kotlinx.coroutines.launch
import java.util.*

class PodcastViewModel(application: Application) : AndroidViewModel(application) {

    var podcastRepo: PodcastRepo? = null
    private val _podcastLiveData = MutableLiveData<PodcastViewData?>()
    val podcastLiveData: LiveData<PodcastViewData?> = _podcastLiveData

    fun getPodcast(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        podcastSummaryViewData.feedUrl?.let { url ->
            viewModelScope.launch {
                podcastRepo?.getPodcast(url)?.let {
                    it.feedTitle = podcastSummaryViewData.name ?: ""
                    it.imageUrl = podcastSummaryViewData.imageUrl ?: ""
                    _podcastLiveData.value = podcastToPodcastView(it)
                } ?: run {
                    _podcastLiveData.value = null
                }
            }
        } ?: run {
            _podcastLiveData.value = null
        }
    }

    private fun podcastToPodcastView(podcast: Podcast): PodcastViewData {
        return PodcastViewData(false, podcast.feedTitle, podcast.feedUrl, podcast.feedDesc,
            podcast.imageUrl, episodesToEpisodesView(podcast.episodes))
    }

    private fun episodesToEpisodesView(episodes: List<Episode>): List<EpisodeViewData> {
        return episodes.map {
            EpisodeViewData(it.guid, it.title, it.description, it.mediaUrl, it.releaseDate, it.duration)
        }
    }

    data class PodcastViewData(var subscribed: Boolean = false, var feedTitle: String? = "",
                               var feedUrl: String? = "", var feedDesc: String? = "",
                               var imageUrl: String? = "", var episodes: List<EpisodeViewData>)

    data class EpisodeViewData(var guid: String? = "", var title: String? = "",
                               var description: String? = "", var mediaUrl: String? = "",
                               var releaseDate: Date? = null, var duration: String? = "")
}