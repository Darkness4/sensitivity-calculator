package fr.marc_nguyen.sensitivity.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import fr.marc_nguyen.sensitivity.core.state.doOnFailure
import fr.marc_nguyen.sensitivity.databinding.FragmentDataTableBinding
import fr.marc_nguyen.sensitivity.presentation.ui.adapters.DataTableAdapter
import fr.marc_nguyen.sensitivity.presentation.ui.adapters.SwipeToDeleteCallback
import fr.marc_nguyen.sensitivity.presentation.viewmodels.DataTableViewModel
import fr.marc_nguyen.sensitivity.presentation.viewmodels.DataTableViewModel.Companion.provideFactory
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DataTableFragment : Fragment() {
    private val args by navArgs<DataTableFragmentArgs>()
    private var _binding: FragmentDataTableBinding? = null
    private val binding: FragmentDataTableBinding
        get() = _binding!!

    @Inject
    lateinit var assisted: DataTableViewModel.AssistedFactory
    private val viewModel: DataTableViewModel by viewModels {
        assisted.provideFactory(args.game)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDataTableBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.recyclerViewDataTable.adapter = DataTableAdapter(onDelete = viewModel::delete)
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewHolder as DataTableAdapter.ViewHolder
                viewHolder.delete()
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewDataTable)

        viewModel.measures.observe(viewLifecycleOwner) {
            it?.doOnFailure { e ->
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                Timber.e(e)
            }
        }

        viewModel.meanStd.observe(viewLifecycleOwner) {
            it?.doOnFailure { e ->
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                Timber.e(e)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
