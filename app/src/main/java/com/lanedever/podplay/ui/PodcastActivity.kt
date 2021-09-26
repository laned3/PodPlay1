package com.lanedever.podplay.ui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.lanedever.podplay.R
import com.lanedever.podplay.adapter.PodcastListAdapter
import com.lanedever.podplay.adapter.PodcastListAdapter.PodcastListAdapterListener
import com.lanedever.podplay.databinding.ActivityPodcastBinding
import com.lanedever.podplay.repository.ItunesRepo
import com.lanedever.podplay.repository.PodcastRepo
import com.lanedever.podplay.service.FeedService
import com.lanedever.podplay.service.ItunesService
import com.lanedever.podplay.service.RssFeedService
import com.lanedever.podplay.viewmodel.PodcastViewModel
import com.lanedever.podplay.viewmodel.SearchViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastActivity : AppCompatActivity(), PodcastListAdapter.PodcastListAdapterListener {

    private val searchViewModel by viewModels<SearchViewModel>()
    private val podcastViewModel by viewModels<PodcastViewModel>()
    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem
    private lateinit var databinding: ActivityPodcastBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(databinding.root)
        setupToolbar()
        setupViewModels()
        updateControls()
        createSubscription()
        handleIntent(intent)
        addBackStackListener()
    }

    // Search icon added to app bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem.actionView as SearchView

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        if (supportFragmentManager.backStackEntryCount > 0) {
            databinding.podcastRecyclerView.visibility = View.INVISIBLE
        }

        if (databinding.podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }

        return true
    }

    // Makes the toolbar act as the ActionBar
    private fun setupToolbar() {
        setSupportActionBar(databinding.toolbar)
    }

    // Called when the user taps on a podcast
    override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        podcastSummaryViewData.feedUrl?.let {
            showProgressBar()
            podcastViewModel.getPodcast(podcastSummaryViewData)
        }
    }

    // Searches ItunesRepo for podcast and prints the results to the Logcat window
    private fun performSearch(term: String) {
        showProgressBar()
        GlobalScope.launch {
            val results = searchViewModel.searchPodcasts(term)
            withContext(Dispatchers.Main) {
                hideProgressBar()
                databinding.toolbar.title = term
                podcastListAdapter.setSearchData(results)
            }
        }
    }

    // Takes Intent and checks against ACTION_SEARCH - will return the string to the performSearch method
    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?: return
            performSearch(query)
        }
    }

    // Receive the updated Intent when a new search is performed
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun createSubscription() {
        podcastViewModel.podcastLiveData.observe(this, {
            hideProgressBar()
            if (it != null) {
                showDetailsFragment()
            } else {
                showError("Error loading feed")
            }
        })
    }

    // Creates an instance of the ItunesService and then uses ViewModelProviders to get an instance of the SearchViewModel
    private fun setupViewModels() {
        val service = ItunesService.instance
        searchViewModel.iTunesRepo = ItunesRepo(service)
        podcastViewModel.podcastRepo = PodcastRepo(RssFeedService.instance)
    }

    // Method to set up the RecyclerView
    private fun updateControls() {
        databinding.podcastRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        databinding.podcastRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(databinding.podcastRecyclerView.context,
            layoutManager.orientation)
        databinding.podcastRecyclerView.addItemDecoration(dividerItemDecoration)

        podcastListAdapter = PodcastListAdapter(null, this, this)
        databinding.podcastRecyclerView.adapter = podcastListAdapter
    }

    // Creates details fragment or if exists
    private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
        var podcastDetailsFragment = supportFragmentManager.findFragmentByTag(TAG_DETAILS_FRAGMENT) as PodcastDetailsFragment?

        if (podcastDetailsFragment == null) {
            podcastDetailsFragment = PodcastDetailsFragment.newInstance()
        }

        return podcastDetailsFragment
    }

    // Displays the fragment details
    private fun showDetailsFragment() {
        val podcastDetailsFragment = createPodcastDetailsFragment()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.podcastDetailsContainer, podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
            .addToBackStack("DetailsFragment")
            .commit()
        databinding.podcastRecyclerView.visibility = View.INVISIBLE
        searchMenuItem.isVisible = false
    }

    // Method called when items are added or removed from the stack
    private fun addBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                databinding.podcastRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this).setMessage(message).setPositiveButton(getString(R.string.ok_button),
            null).create().show()
    }

    private fun showProgressBar() {
        databinding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        databinding.progressBar.visibility = View.INVISIBLE
    }

    companion object {
        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
    }
}