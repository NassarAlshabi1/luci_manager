package com.example.lucimanager.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.lucimanager.R
import com.example.lucimanager.databinding.FragmentHomeBinding
import com.example.lucimanager.network.LuciApiClient
import com.example.lucimanager.repository.NetworkRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var toggle: ActionBarDrawerToggle
    private var childNavController: NavController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager
            .findFragmentById(R.id.home_nav_host_fragment) as? NavHostFragment
            ?: (binding.homeNavHostFragment.getFragment<NavHostFragment>())

        childNavController = navHostFragment.navController

        // Setup toolbar with drawer toggle
        toggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            binding.toolbar,
            R.string.nav_drawer_open,
            R.string.nav_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Setup NavigationView with NavController
        childNavController?.let { nav ->
            NavigationUI.setupWithNavController(binding.navView, nav)

            // Handle non-navigation items (language) ourselves, keep normal
            // destination handling identical to NavigationUI's default.
            binding.navView.setNavigationItemSelectedListener { item ->
                if (item.itemId == R.id.action_language) {
                    showLanguageDialog()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                } else {
                    NavigationUI.onNavDestinationSelected(item, nav)
                }
            }

            nav.addOnDestinationChangedListener { _, destination, _ ->
                binding.toolbar.title = destination.label
            }
        }

        // Setup header info
        val repository = NetworkRepository(requireContext())
        val savedCredentials = repository.getSavedCredentials()
        val headerView = binding.navView.getHeaderView(0)
        val subtitleView = headerView.findViewById<TextView>(R.id.nav_header_subtitle)
        subtitleView.text = savedCredentials?.ipAddress ?: ""

        // Add logout item handling
        binding.navView.menu.findItem(R.id.notificationSettingsFragment)?.let {
            // Already handled by NavigationUI
        }

        // Add logout to toolbar menu
        binding.toolbar.inflateMenu(R.menu.menu_home)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun showLanguageDialog() {
        val tags = arrayOf("", "en", "ar")
        val labels = arrayOf(
            getString(R.string.language_system),
            getString(R.string.language_english),
            getString(R.string.language_arabic)
        )
        val current = AppCompatDelegate.getApplicationLocales()
        val currentTag = if (current.isEmpty) "" else current.toLanguageTags()
        val checkedIndex = tags.indexOfFirst { it == currentTag }.takeIf { it >= 0 } ?: 0

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.language_dialog_title)
            .setSingleChoiceItems(labels, checkedIndex) { dialog, which ->
                val tag = tags[which]
                val locales = if (tag.isEmpty()) {
                    LocaleListCompat.getEmptyLocaleList()
                } else {
                    LocaleListCompat.forLanguageTags(tag)
                }
                AppCompatDelegate.setApplicationLocales(locales)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        CoroutineScope(Dispatchers.Main).launch {
            LuciApiClient.logout()
            val repository = NetworkRepository(requireContext())
            repository.logout()
            findNavController().navigate(R.id.action_home_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
