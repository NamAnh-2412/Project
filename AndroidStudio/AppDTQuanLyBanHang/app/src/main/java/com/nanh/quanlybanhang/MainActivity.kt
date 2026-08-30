package com.nanh.quanlybanhang

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.navigation.NavigationBarView
import com.nanh.quanlybanhang.data.repo.AuthDestination
import com.nanh.quanlybanhang.databinding.ActivityMainBinding
import com.nanh.quanlybanhang.ui.account.AccountFragment
import com.nanh.quanlybanhang.ui.auth.LoginFragment
import com.nanh.quanlybanhang.ui.auth.RegisterFragment
import com.nanh.quanlybanhang.ui.customers.CustomersFragment
import com.nanh.quanlybanhang.ui.dashboard.xml.DashboardFragment
import com.nanh.quanlybanhang.ui.navigation.DashboardTopTabNavigator
import com.nanh.quanlybanhang.ui.orders.OrdersFragment
import com.nanh.quanlybanhang.ui.orders.PlaceOrderFragment
import com.nanh.quanlybanhang.ui.products.ProductsFragment
import com.nanh.quanlybanhang.ui.reports.ReportsFragment
import com.nanh.quanlybanhang.ui.suppliers.SuppliersFragment
import com.nanh.quanlybanhang.ui.warehouse.WarehouseFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), DashboardTopTabNavigator {

    private lateinit var binding: ActivityMainBinding
    private lateinit var app: QuanLyBanHangApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as QuanLyBanHangApp
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener(
            NavigationBarView.OnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> clearBackStackAndShow(DashboardFragment.newInstance())
                    R.id.nav_orders -> clearBackStackAndShow(PlaceOrderFragment.newInstance())
                    R.id.nav_reports -> clearBackStackAndShow(ReportsFragment.newInstance())
                    R.id.nav_account -> clearBackStackAndShow(AccountFragment.newInstance())
                    else -> return@OnItemSelectedListener false
                }
                true
            },
        )

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                app.authRepository.validateSession()
                app.authRepository.destination.collect { applyAuthDestination(it) }
            }
        }
    }

    private fun applyAuthDestination(dest: AuthDestination) {
        when (dest) {
            AuthDestination.REGISTER -> {
                binding.bottomNavigation.isVisible = false
                val cur = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (cur !is RegisterFragment) {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, RegisterFragment.newInstance())
                    }
                }
            }
            AuthDestination.LOGIN -> {
                binding.bottomNavigation.isVisible = false
                val cur = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (cur !is LoginFragment) {
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragmentManager.commit {
                        replace(R.id.fragmentContainer, LoginFragment.newInstance())
                    }
                }
            }
            AuthDestination.MAIN -> {
                binding.bottomNavigation.isVisible = true
                val cur = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (cur is LoginFragment || cur is RegisterFragment || cur == null) {
                    clearBackStackAndShow(DashboardFragment.newInstance())
                    binding.bottomNavigation.selectedItemId = R.id.nav_home
                }
            }
        }
    }

    override fun openOrderHistoryFromDashboardTab() {
        clearBackStackAndShow(OrdersFragment.newInstance(fromDashboard = true))
    }

    override fun pushOrderHistoryFromPlaceOrder() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, OrdersFragment.newInstance(fromDashboard = false))
            addToBackStack(null)
        }
    }

    override fun returnToDashboardFromOrderHistory() {
        clearBackStackAndShow(DashboardFragment.newInstance())
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    override fun openProductsFromDashboardTab() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, ProductsFragment.newInstance())
            addToBackStack(null)
        }
    }

    override fun openCustomersFromDashboardTab() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, CustomersFragment.newInstance())
            addToBackStack(null)
        }
    }

    override fun openSuppliersFromDashboardTab() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, SuppliersFragment.newInstance())
            addToBackStack(null)
        }
    }

    override fun openWarehouseFromDashboardTab() {
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, WarehouseFragment.newInstance())
            addToBackStack(null)
        }
    }

    override fun openReportsFromDashboardTab() {
        clearBackStackAndShow(ReportsFragment.newInstance())
        binding.bottomNavigation.selectedItemId = R.id.nav_reports
    }

    private fun clearBackStackAndShow(fragment: Fragment) {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
    }
}
