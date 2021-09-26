package com.lanedever.podplay.repository

import com.lanedever.podplay.service.ItunesService

// Finding the podcast on iTunes
class ItunesRepo(private val itunesService: ItunesService) {

    suspend fun searchByTerm(term: String) = itunesService.searchPodcastByTerm(term)

}