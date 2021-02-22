package fr.marc_nguyen.sensitivy.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import fr.marc_nguyen.sensitivy.R
import fr.marc_nguyen.sensitivy.databinding.FragmentMeasureBinding
import fr.marc_nguyen.sensitivy.presentation.viewmodels.MeasureViewModel

class MeasureFragment : Fragment() {
    private val viewModel: MeasureViewModel by viewModels()
    private lateinit var binding: FragmentMeasureBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMeasureBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}
