package com.driverassist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.driverassist.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    // Steps: 0 = Welcome, 1 = Permissions, 2 = Battery, 3 = Done
    private var currentStep = 0

    private val requiredPermissions = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ANSWER_PHONE_CALLS,
        Manifest.permission.SEND_SMS
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        refreshStep()
    }

    private val batteryOptLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshStep()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If the user has already completed onboarding, go straight to the main screen
        if (AppPrefs.isOnboardingDone(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showStep(0)

        binding.btnAction.setOnClickListener {
            when (currentStep) {
                0 -> showStep(1)
                1 -> requestAllPermissions()
                2 -> requestBatteryOptimization()
                3 -> finishSetup()
            }
        }

        binding.btnSkip.setOnClickListener {
            // Allow skip only on battery step
            finishSetup()
        }
    }

    private fun showStep(step: Int) {
        currentStep = step
        when (step) {
            0 -> setupWelcomeStep()
            1 -> setupPermissionsStep()
            2 -> setupBatteryStep()
            3 -> setupDoneStep()
        }
        updateProgressDots(step)
    }

    private fun setupWelcomeStep() {
        binding.ivStepIcon.text = "🚗"
        binding.tvStepTitle.text = "Welcome to\nDriver Assist"
        binding.tvStepDesc.text = "This app will automatically reject incoming calls and send an SMS reply while you're driving — keeping you and others safe."
        binding.tvStepSub.text = "We need to set up a few things before you start.\nIt only takes 30 seconds."
        binding.tvStepSub.visibility = View.VISIBLE
        binding.btnAction.text = "Let's Get Started →"
        binding.btnSkip.visibility = View.GONE
        binding.layoutChecklist.visibility = View.GONE
    }

    private fun setupPermissionsStep() {
        val allGranted = allPermissionsGranted()
        binding.ivStepIcon.text = if (allGranted) "✅" else "🔐"
        binding.tvStepTitle.text = "App Permissions"
        binding.tvStepDesc.text = "Driver Assist needs 3 permissions to work. Tap below to grant them all at once."
        binding.tvStepSub.visibility = View.GONE
        binding.layoutChecklist.visibility = View.VISIBLE

        binding.tvCheck1.text = permissionStatus(Manifest.permission.READ_PHONE_STATE) + "  Detect incoming calls"
        binding.tvCheck2.text = permissionStatus(Manifest.permission.ANSWER_PHONE_CALLS) + "  Reject/end incoming calls"
        binding.tvCheck3.text = permissionStatus(Manifest.permission.SEND_SMS) + "  Send auto-reply SMS"

        if (allGranted) {
            binding.btnAction.text = "Next →"
            binding.btnSkip.visibility = View.GONE
        } else {
            binding.btnAction.text = "Grant Permissions"
            binding.btnSkip.visibility = View.GONE
        }
    }

    private fun setupBatteryStep() {
        val isIgnoring = isBatteryOptimizationIgnored()
        binding.ivStepIcon.text = if (isIgnoring) "✅" else "🔋"
        binding.tvStepTitle.text = "Disable Battery\nOptimization"
        binding.tvStepDesc.text = "Android may kill background apps to save battery. Without this, your calls might NOT be auto-rejected when the screen is off."
        binding.tvStepSub.text = "In the next screen, tap \"Allow\" to let Driver Assist run in background."
        binding.tvStepSub.visibility = View.VISIBLE
        binding.layoutChecklist.visibility = View.GONE

        if (isIgnoring) {
            binding.btnAction.text = "Next →"
            binding.btnSkip.visibility = View.GONE
        } else {
            binding.btnAction.text = "Disable Battery Optimization"
            binding.btnSkip.text = "Skip for now"
            binding.btnSkip.visibility = View.VISIBLE
        }
    }

    private fun setupDoneStep() {
        binding.ivStepIcon.text = "🎉"
        binding.tvStepTitle.text = "You're All Set!"
        binding.tvStepDesc.text = "Driver Assist is ready. Just toggle Driving Mode ON before you start driving — everything else is automatic."
        binding.tvStepSub.text = "Stay safe. We'll handle the calls. 🚗"
        binding.tvStepSub.visibility = View.VISIBLE
        binding.layoutChecklist.visibility = View.GONE
        binding.btnAction.text = "Open Driver Assist"
        binding.btnSkip.visibility = View.GONE
    }

    private fun requestAllPermissions() {
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            permissionLauncher.launch(missing.toTypedArray())
        } else {
            // All granted, move to next step
            showStep(2)
        }
    }

    private fun refreshStep() {
        when (currentStep) {
            1 -> {
                setupPermissionsStep()
                if (allPermissionsGranted()) {
                    // Auto-advance after short delay
                    binding.root.postDelayed({ showStep(2) }, 800)
                }
            }
            2 -> {
                setupBatteryStep()
                if (isBatteryOptimizationIgnored()) {
                    binding.root.postDelayed({ showStep(3) }, 800)
                }
            }
        }
    }

    private fun requestBatteryOptimization() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            batteryOptLauncher.launch(intent)
        } catch (e: Exception) {
            // Fallback: open general battery settings
            try {
                batteryOptLauncher.launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (_: Exception) {
                showStep(3)
            }
        }
    }

    private fun finishSetup() {
        AppPrefs.setOnboardingDone(this, true)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun allPermissionsGranted() = requiredPermissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun isBatteryOptimizationIgnored(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun permissionStatus(permission: String): String {
        return if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED)
            "✅" else "⬜"
    }

    private fun updateProgressDots(step: Int) {
        val dots = listOf(binding.dot0, binding.dot1, binding.dot2, binding.dot3)
        dots.forEachIndexed { i, dot ->
            dot.setBackgroundResource(
                if (i == step) R.drawable.dot_active else R.drawable.dot_inactive
            )
        }
    }
}
