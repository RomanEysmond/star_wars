package com.example.starwars.presentation.ui.characterlist

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.starwars.presentation.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CharacterListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var searchEditText: EditText
    private lateinit var clearSearch: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: View
    private lateinit var errorView: View
    private lateinit var errorText: TextView

    private val viewModel: CharacterListViewModel by viewModels()
    private lateinit var adapter: CharacterAdapter

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

        // Загружаем данные только если они еще не загружены
        viewModel.loadCharacters()

        // Настройка цветов SwipeRefreshLayout
        swipeRefresh.setColorSchemeColors(
            resources.getColor(R.color.star_wars_yellow, null),
            resources.getColor(R.color.star_wars_white, null)
        )
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        searchEditText = view.findViewById(R.id.searchView)
        clearSearch = view.findViewById(R.id.clearSearch)
        progressBar = view.findViewById(R.id.progressBar)
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

    private fun observeViewModel() {
        viewModel.filteredCharacters.observe(viewLifecycleOwner) { characters ->
            adapter.submitList(characters)

            if (characters.isEmpty() && viewModel.isLoading.value != true) {
                emptyView.isVisible = true
                recyclerView.isVisible = false
            } else {
                emptyView.isVisible = false
                recyclerView.isVisible = true
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Показываем прогресс только если данные не загружены и нет списка
            if (isLoading == true) {
                // Проверяем, есть ли уже данные
                if (adapter.currentList.isEmpty()) {
                    progressBar.isVisible = true
                    recyclerView.isVisible = false
                } else {
                    // Если данные уже есть, скрываем прогресс
                    progressBar.isVisible = false
                }
                swipeRefresh.isRefreshing = false
            } else {
                progressBar.isVisible = false
                swipeRefresh.isRefreshing = false
                // Если данные загружены, показываем список
                if (adapter.currentList.isNotEmpty()) {
                    recyclerView.isVisible = true
                }
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null && adapter.currentList.isEmpty()) {
                errorView.isVisible = true
                errorText.text = error
            } else {
                errorView.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchEditText.removeTextChangedListener(null)
    }
}