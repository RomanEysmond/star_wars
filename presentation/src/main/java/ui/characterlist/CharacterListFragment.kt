package com.example.starwars.presentation.ui.characterlist

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
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

    private val starWarsPhrases = listOf(
        "СКАНИРОВАНИЕ ОКРУЖАЮЩЕГО ПРОСТРАНСТВА...",
        "ЗАТОЧКА СВЕТОВОГО МЕЧА...",
        "НАСТРОЙКА СИНХРОНИЗАТОРА...",
        "ПОИСК ИСТОЧНИКОВ СИЛЫ...",
        "СБОР РАЗВЕДДАННЫХ...",
        "ДЕШИФРОВКА СООБЩЕНИЯ ПОВСТАНЦЕВ...",
        "СКАНИРОВАНИЕ ОКРУЖАЮЩЕГО ПРОСТРАНСТВА...",
        "ЗАГРУЗКА БОЕВЫХ ПРОТОКОЛОВ..."
    )

    private var phraseIndex = 0
    private var handler = Handler(Looper.getMainLooper())
    private var phraseRunnable: Runnable? = null

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
        stopPhraseAnimation()
        handler.removeCallbacksAndMessages(null)
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

    private fun startPhraseAnimation() {
        phraseIndex = 0
        loadingPhraseText.text = starWarsPhrases[phraseIndex]

        phraseRunnable = object : Runnable {
            override fun run() {
                val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 500 }
                val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 500 }

                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {
                        phraseIndex = (phraseIndex + 1) % starWarsPhrases.size
                        loadingPhraseText.text = starWarsPhrases[phraseIndex]
                        loadingPhraseText.startAnimation(fadeIn)
                    }
                })

                loadingPhraseText.startAnimation(fadeOut)
                handler.postDelayed(this, 3500)
            }
        }

        handler.post(phraseRunnable!!)
    }

    private fun stopPhraseAnimation() {
        phraseRunnable?.let {
            handler.removeCallbacks(it)
        }
        phraseRunnable = null
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
        loadingContainer.isVisible = true
        progressBar.isVisible = false
        recyclerView.isVisible = false
        emptyView.isVisible = false
        errorView.isVisible = false
        startPhraseAnimation()
    }

    private fun hideFullScreenLoading() {
        loadingContainer.isVisible = false
        progressBar.isVisible = false
        stopPhraseAnimation()
    }

    private fun showError(error: String) {
        loadingContainer.isVisible = false
        progressBar.isVisible = false
        recyclerView.isVisible = false
        emptyView.isVisible = false
        errorView.isVisible = true
        errorText.text = error
        stopPhraseAnimation()
    }

    private fun hideError() {
        errorView.isVisible = false
    }
}