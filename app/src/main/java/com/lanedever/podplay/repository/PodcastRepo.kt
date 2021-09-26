package com.lanedever.podplay.repository

import com.lanedever.podplay.model.Episode
import com.lanedever.podplay.model.Podcast
import com.lanedever.podplay.service.FeedService
import com.lanedever.podplay.service.RssFeedResponse
import com.lanedever.podplay.service.RssFeedService
import com.lanedever.podplay.util.DateUtils

// Class to to pass the podcast through to return the podcast to the ui
class PodcastRepo(private var feedService: RssFeedService) {

    suspend fun getPodcast(feedUrl: String): Podcast? {
        var podcast: Podcast? = null
        val feedResponse = feedService.getFeed(feedUrl)
        if (feedResponse != null) {
            podcast = rssResponseToPodcast(feedUrl, "", feedResponse)
        }
        return podcast
    }

    private fun rssItemsToEpisodes(episodeResponses: List<RssFeedResponse.EpisodeResponse>): List<Episode> {
        return episodeResponses.map {
            Episode(
                it.guid ?: "",
                it.title ?: "",
                it.description ?: "",
                it.url ?: "",
                it.type ?: "",
                DateUtils.xmlDateToDate(it.pubDate),
                it.duration ?: ""
            )
        }
    }

    private fun rssResponseToPodcast(
        feedUrl: String, imageUrl: String, rssResponse: RssFeedResponse
    ): Podcast? {
        // 1
        val items = rssResponse.episodes ?: return null
        // 2
        val description = if (rssResponse.description == "")
            rssResponse.summary else rssResponse.description
        // 3
        return Podcast(feedUrl, rssResponse.title, description, imageUrl,
            rssResponse.lastUpdated, episodes = rssItemsToEpisodes(items))
    }
}