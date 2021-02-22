package fr.marc_nguyen.sensitivy.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.marc_nguyen.sensitivy.databinding.ItemMeasureBinding
import fr.marc_nguyen.sensitivy.domain.entities.Measure

class DataTableAdapter : ListAdapter<Measure, DataTableAdapter.ViewHolder>(Comparator) {
    object Comparator : DiffUtil.ItemCallback<Measure>() {
        override fun areItemsTheSame(oldItem: Measure, newItem: Measure) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Measure, newItem: Measure) = oldItem == newItem
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val station = getItem(position)
        station.let(holder::bind)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.create(
        parent,
    )

    class ViewHolder(
        private val binding: ItemMeasureBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(measure: Measure) {
            binding.measure = measure
            binding.executePendingBindings()
        }

        companion object {
            fun create(parent: ViewGroup) =
                ViewHolder(
                    ItemMeasureBinding.inflate( // station_item.xml
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                )
        }
    }
}
