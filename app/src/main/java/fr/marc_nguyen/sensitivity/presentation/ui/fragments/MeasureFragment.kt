package fr.marc_nguyen.sensitivity.presentation.ui.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.ar.core.ArCoreApk
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

    private var _gameAdapter: ArrayAdapter<String>? = null
    private val gameAdapter: ArrayAdapter<String>
        get() = _gameAdapter!!

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

        /* AR */
        maybeEnableArButton()

        /* Adapters */
        _gameAdapter =
            ArrayAdapterNoFilter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                mutableListOf()
            )
        binding.editTextSourceGame.setAdapter(gameAdapter)
        refreshGameList()

        val unitAdapter = ArrayAdapterNoFilter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            MeasureUnit.symbols
        )
        binding.editTextUnit.setAdapter(unitAdapter)
        binding.editTextTargetUnit.setAdapter(unitAdapter)

        /* Navigation */
        viewModel.goToDataTableFragment.observe(viewLifecycleOwner) {
            it?.let {
                this.findNavController()
                    .navigate(MeasureFragmentDirections.actionMeasureFragmentToDataTableFragment(it.game))
                viewModel.goToDataTableFragmentDone()
            }
        }

        viewModel.goToArActivity.observe(viewLifecycleOwner) {
            it?.let {
                this.findNavController()
                    .navigate(MeasureFragmentDirections.actionMeasureFragmentToArActivity())
                viewModel.goToArActivityDone()
            }
        }

        /* Results */
        viewModel.addResult.observe(viewLifecycleOwner) {
            it?.fold(
                {
                    viewModel.updateResult()
                    refreshGameList()
                },
                { e ->
                    Timber.e(e)
                    Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                }
            )
        }

        viewModel.computeQuadraticResult.observe(viewLifecycleOwner) {
            it?.doOnFailure { e ->
                when (e) {
                    is NullPointerException -> Timber.i(e.toString())
                    else -> {
                        Timber.e(e)
                        Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        /* Inputs */
        viewModel.sourceGameInput.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateResult()
                binding.buttonAdd.text = if (it.isNotBlank()) "Add to $it" else "Add to \"\""
            }
        }

        viewModel.targetUnitInput.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateResult()
            }
        }

        viewModel.targetDistancePer360Input.observe(viewLifecycleOwner) {
            it?.let {
                viewModel.updateResult()
            }
        }

        return binding.root
    }

    private fun maybeEnableArButton() {
        val availability =
            ArCoreApk.getInstance().checkAvailability(requireContext().applicationContext)
        if (availability.isTransient) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    maybeEnableArButton()
                },
                200
            )
        }
        viewModel.updateArSupport(availability.isSupported)
    }

    private fun refreshGameList() = lifecycleScope.launch {
        val games = repository.findGames()
        gameAdapter.clear()
        gameAdapter.addAll(games)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _gameAdapter = null
    }
}
