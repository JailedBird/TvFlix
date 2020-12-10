package com.android.tvmaze.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.android.tvmaze.R
import com.android.tvmaze.databinding.ActivityHomeBinding
import com.android.tvmaze.favorite.FavoriteShowsActivity
import com.android.tvmaze.shows.AllShowsActivity
import com.android.tvmaze.utils.GridItemDecoration
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity(), ShowsAdapter.Callback {
    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var showsAdapter: ShowsAdapter
    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setToolbar()
        homeViewModel.onScreenCreated()
        homeViewModel.getHomeViewState().observe(this, { setViewState(it) })
        binding.popularShowHeader.text = String.format(
            getString(R.string.popular_shows_airing_today),
            homeViewModel.country
        )
    }

    private fun setViewState(homeViewState: HomeViewState) {
        when (homeViewState) {
            is Loading -> setProgress(true)
            is NetworkError -> {
                setProgress(false)
                showError(homeViewState.message!!)
            }
            is Success -> {
                setProgress(false)
                showPopularShows(homeViewState.homeViewData)
            }
        }
    }

    private fun setToolbar() {
        val toolbar = binding.toolbar.toolbar
        setSupportActionBar(toolbar)
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setSubtitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        setTitle(R.string.app_name)
    }

    private fun setProgress(isLoading: Boolean) {
        if (isLoading) {
            showProgress()
        } else {
            hideProgress()
        }
    }

    private fun showPopularShows(homeViewData: HomeViewData) {
        val gridLayoutManager = GridLayoutManager(this, NO_OF_COLUMNS)
        showsAdapter = ShowsAdapter(this)
        showsAdapter.updateList(homeViewData.episodes.toMutableList())
        binding.popularShows.apply {
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
            adapter = showsAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.show_grid_spacing)
            addItemDecoration(GridItemDecoration(spacing, NO_OF_COLUMNS))
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showProgress() {
        binding.progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        binding.progress.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_shows -> {
                AllShowsActivity.start(this)
                true
            }
            R.id.action_favorites -> {
                FavoriteShowsActivity.start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onFavoriteClicked(showViewData: HomeViewData.ShowViewData) {
        if (!showViewData.isFavoriteShow) {
            homeViewModel.addToFavorite(showViewData.show)
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
        } else {
            homeViewModel.removeFromFavorite(showViewData.show)
            Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val NO_OF_COLUMNS = 2
    }
}
