package fr.marc_nguyen.sensitivity.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import fr.marc_nguyen.sensitivity.core.state.doOnFailure
import fr.marc_nguyen.sensitivity.core.state.fold
import fr.marc_nguyen.sensitivity.databinding.FragmentMeasureBinding
import fr.marc_nguyen.sensitivity.domain.entities.MeasureUnit
import fr.marc_nguyen.sensitivity.domain.repositories.MeasureRepository
import fr.marc_nguyen.sensitivity.presentation.ui.adapters.ArrayAdapterNoFilter
import fr.marc_nguyen.sensitivity.presentation.viewmodels.MeasureViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MeasureFragment : Fragment() {
    private val viewModel: MeasureViewModel by viewModels()
    private var _binding: FragmentMeasureBinding? = null
    private val binding: FragmentMeasureBinding
        get() = _binding!!
    private lateinit var gameAdapter: ArrayAdapter<String>

    @Inject
    lateinit var repository: MeasureRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeasureBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        gameAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line)
        binding.editTextGame.setAdapter(gameAdapter)

        refreshGameList()

        val unitAdapter = ArrayAdapterNoFilter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            MeasureUnit.symbols
        )
        binding.editTextUnit.setAdapter(unitAdapter)
        binding.editTextTargetUnit.setAdapter(unitAdapter)

        viewModel.addResult.observe(viewLifecycleOwner) {
            it?.fold(
                {
                    refreshGameList()
                },
                { e ->
                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                    Timber.e(e)
                }
            )
        }

        viewModel.computeResult.observe(viewLifecycleOwner) {
            it?.doOnFailure { e ->
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                Timber.e(e)
            }
        }

        viewModel.goToDataTableFragment.observe(viewLifecycleOwner) {
            it?.let {
                this.findNavController()
                    .navigate(MeasureFragmentDirections.actionMeasureFragmentToDataTableFragment(it.game))
                viewModel.goToDataTableFragmentDone()
            }
        }

        viewModel.gameInput.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateResult()
            }
        }

        viewModel.targetPer360Input.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateResult()
            }
        }

        viewModel.targetUnitInput.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateResult()
            }
        }

        return binding.root
    }

    private fun refreshGameList() = lifecycleScope.launch {
        val games = repository.findGames()
        gameAdapter.clear()
        gameAdapter.addAll(games)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
