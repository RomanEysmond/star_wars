package com.example.starwars.presentation.ui.characterdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.starwars.domain.models.Film
import com.example.starwars.presentation.databinding.ItemFilmBinding

class FilmAdapter : ListAdapter<Film, FilmAdapter.FilmViewHolder>(FilmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        val binding = ItemFilmBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FilmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FilmViewHolder(private val binding: ItemFilmBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(film: Film) {
            binding.apply {
                episodeNumber.text = getEpisodeText(film.episodeId)
                filmTitle.text = film.title
                filmDirector.text = "Director: ${film.director}"
                filmReleaseDate.text = film.releaseDate.substring(0, 4)
            }
        }

        private fun getEpisodeText(episodeId: Int): String {
            return when (episodeId) {
                1 -> "EPISODE I: THE PHANTOM MENACE"
                2 -> "EPISODE II: ATTACK OF THE CLONES"
                3 -> "EPISODE III: REVENGE OF THE SITH"
                4 -> "EPISODE IV: A NEW HOPE"
                5 -> "EPISODE V: THE EMPIRE STRIKES BACK"
                6 -> "EPISODE VI: RETURN OF THE JEDI"
                7 -> "EPISODE VII: THE FORCE AWAKENS"
                8 -> "EPISODE VIII: THE LAST JEDI"
                9 -> "EPISODE IX: THE RISE OF SKYWALKER"
                else -> "STAR WARS"
            }
        }
    }

    class FilmDiffCallback : DiffUtil.ItemCallback<Film>() {
        override fun areItemsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Film, newItem: Film): Boolean {
            return oldItem == newItem
        }
    }
}