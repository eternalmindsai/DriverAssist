package com.driverassist

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.driverassist.databinding.FragmentCallLogBinding

class CallLogFragment : Fragment() {

    private var _binding: FragmentCallLogBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CallLogAdapter

    private val callLogReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refreshLog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCallLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CallLogAdapter()
        binding.rvCallLog.adapter = adapter

        refreshLog()

        binding.btnClearLog.setOnClickListener {
            AppPrefs.clearCallLog(requireContext())
            refreshLog()
        }
    }

    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(
            callLogReceiver,
            IntentFilter("com.driverassist.CALL_LOG_UPDATED"),
            Context.RECEIVER_NOT_EXPORTED
        )
        refreshLog()
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(callLogReceiver)
    }

    private fun refreshLog() {
        val log = AppPrefs.getCallLog(requireContext())
        adapter.submitList(log.toList())

        if (log.isEmpty()) {
            binding.rvCallLog.visibility = View.GONE
            binding.tvEmpty.visibility = View.VISIBLE
            binding.btnClearLog.visibility = View.GONE
        } else {
            binding.rvCallLog.visibility = View.VISIBLE
            binding.tvEmpty.visibility = View.GONE
            binding.btnClearLog.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
