package com.example.starwars.presentation.ui.characterlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.starwars.presentation.R
import com.example.starwars.presentation.utils.AnimatedProgressBarManager
import com.example.starwars.presentation.utils.StarWarsPhrases
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CharacterListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var searchEditText: EditText
    private lateinit var clearSearch: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var loadingContainer: LinearLayout
    private lateinit var loadingPhraseText: TextView
    private lateinit var loadingInfoText: TextView
    private lateinit var emptyView: View
    private lateinit var errorView: View
    private lateinit var errorText: TextView

    private val viewModel: CharacterListViewModel by viewModels()
    private lateinit var adapter: CharacterAdapter
    private lateinit var animatedProgressManager: AnimatedProgressBarManager

        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_character_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupAnimatedProgressBar()
        setupRecyclerView()
        setupSearchView()
        setupSwipeRefresh()
        observeViewModel()

        viewModel.loadCharacters()

        swipeRefresh.setColorSchemeColors(
            resources.getColor(R.color.star_wars_yellow, null),
            resources.getColor(R.color.star_wars_white, null)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        animatedProgressManager.cleanup()
        searchEditText.removeTextChangedListener(null)
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        searchEditText = view.findViewById(R.id.searchView)
        clearSearch = view.findViewById(R.id.clearSearch)
        progressBar = view.findViewById(R.id.progressBar)
        loadingContainer = view.findViewById(R.id.loadingContainer)
        loadingPhraseText = view.findViewById(R.id.loadingPhraseText)
        loadingInfoText = view.findViewById(R.id.loadingInfoText)
        emptyView = view.findViewById(R.id.emptyView)
        errorView = view.findViewById(R.id.errorView)
        errorText = view.findViewById(R.id.errorText)
    }

    private fun setupAnimatedProgressBar() {
        animatedProgressManager = AnimatedProgressBarManager(
            loadingContainer = loadingContainer,
            loadingPhraseText = loadingPhraseText,
            phrases = StarWarsPhrases.loadingPhrases
        )
    }

    private fun setupRecyclerView() {
        adapter = CharacterAdapter { character ->
            val bundle = Bundle().apply {
                putInt("characterId", character.id)
            }
            findNavController().navigate(R.id.characterDetailFragment, bundle)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@CharacterListFragment.adapter
        }
    }

    private fun setupSearchView() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterCharacters(s.toString())
                clearSearch.isVisible = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        clearSearch.setOnClickListener {
            searchEditText.text.clear()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.refreshCharacters()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { uiState ->
                        updateUI(uiState)
                    }
                }
            }
        }
    }

    private fun updateUI(uiState: CharacterListUiState) {
        adapter.submitList(uiState.filteredCharacters)

        when {
            uiState.isLoading && adapter.currentList.isEmpty() -> showFullScreenLoading()
            uiState.isLoading -> swipeRefresh.isRefreshing = true
            else -> {
                hideFullScreenLoading()
                swipeRefresh.isRefreshing = false
                if (adapter.currentList.isNotEmpty()) {
                    recyclerView.isVisible = true
                }
            }
        }

        if (uiState.filteredCharacters.isEmpty() && !uiState.isLoading) {
            emptyView.isVisible = true
            recyclerView.isVisible = false
        } else if (uiState.filteredCharacters.isNotEmpty()) {
            emptyView.isVisible = false
            recyclerView.isVisible = true
        }

        if (uiState.errorMessage != null && adapter.currentList.isEmpty()) {
            showError(uiState.errorMessage)
        } else {
            hideError()
        }
    }

    private fun showFullScreenLoading() {
        progressBar.isVisible = false
        recyclerView.isVisible = false
        emptyView.isVisible = false
        errorView.isVisible = false
        animatedProgressManager.start()
    }

    private fun hideFullScreenLoading() {
        animatedProgressManager.stop()
        progressBar.isVisible = false
    }

    private fun showError(error: String) {
        animatedProgressManager.stop()
        progressBar.isVisible = false
        recyclerView.isVisible = false
        emptyView.isVisible = false
        errorView.isVisible = true
        errorText.text = error
    }

    private fun hideError() {
        errorView.isVisible = false
    }
}