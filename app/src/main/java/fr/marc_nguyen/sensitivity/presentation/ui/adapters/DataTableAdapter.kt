package fr.marc_nguyen.sensitivity.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.marc_nguyen.sensitivity.databinding.ItemMeasureBinding
import fr.marc_nguyen.sensitivity.domain.entities.Measure

class DataTableAdapter(private val onDelete: (Measure) -> Unit) :
    ListAdapter<Measure, DataTableAdapter.ViewHolder>(Comparator) {
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
        onDelete,
    )

    class ViewHolder(
        private val binding: ItemMeasureBinding,
        private val onDelete: (Measure) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(measure: Measure) {
            binding.measure = measure
            binding.executePendingBindings()
        }

        fun delete() {
            onDelete(binding.measure!!)
        }

        companion object {
            fun create(parent: ViewGroup, onDelete: (Measure) -> Unit) =
                ViewHolder(
                    ItemMeasureBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    onDelete,
                )
        }
    }
}
