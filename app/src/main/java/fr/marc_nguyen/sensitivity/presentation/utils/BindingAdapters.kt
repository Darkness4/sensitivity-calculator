package fr.marc_nguyen.sensitivity.presentation.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.marc_nguyen.sensitivity.core.state.State
import fr.marc_nguyen.sensitivity.core.state.doOnSuccess
import fr.marc_nguyen.sensitivity.domain.entities.Measure
import fr.marc_nguyen.sensitivity.presentation.ui.adapters.DataTableAdapter

@BindingAdapter("showOnSuccess")
fun showOnSuccess(view: TextView, state: State<String>?) {
    view.text = state?.getOrNull() ?: ""
}

@BindingAdapter("bind")
fun bindDataTableAdapter(
    view: RecyclerView,
    state: State<List<Measure>>?
) {
    val adapter = view.adapter as DataTableAdapter
    state?.doOnSuccess { adapter.submitList(it) }
}
