package com.driverassist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.driverassist.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Restore toggle state
        val isOn = AppPrefs.isDrivingMode(requireContext())
        binding.switchDrivingMode.isChecked = isOn
        updateUI(isOn)

        binding.switchDrivingMode.setOnCheckedChangeListener { _, isChecked ->
            AppPrefs.setDrivingMode(requireContext(), isChecked)
            updateUI(isChecked)
        }
    }

    private fun updateUI(isOn: Boolean) {
        if (isOn) {
            binding.tvStatus.text = "Driving Mode is ON"
            binding.tvStatusSub.text = "Incoming calls will be auto-rejected\nand the caller will receive an SMS."
            binding.ivCar.setImageResource(R.drawable.ic_car_active)
            binding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.status_on, null))
        } else {
            binding.tvStatus.text = "Driving Mode is OFF"
            binding.tvStatusSub.text = "Toggle the switch above to enable\nauto-reject while driving."
            binding.ivCar.setImageResource(R.drawable.ic_car_inactive)
            binding.cardStatus.setCardBackgroundColor(resources.getColor(R.color.status_off, null))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
