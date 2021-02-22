package fr.marc_nguyen.sensitivity.presentation.ui.adapters

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class ArrayAdapterNoFilter<T>(
    context: Context,
    textViewResourceId: Int,
    private val objects: List<T>
) : ArrayAdapter<T>(context, textViewResourceId, objects) {
    private val filter: Filter = NoFilter()

    override fun getFilter(): Filter {
        return filter
    }

    private inner class NoFilter : Filter() {
        override fun performFiltering(arg0: CharSequence?): FilterResults {
            return FilterResults().apply {
                values = objects
                count = objects.size
            }
        }

        override fun publishResults(arg0: CharSequence?, arg1: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}
