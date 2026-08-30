package com.nanh.quanlybanhang.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.nanh.quanlybanhang.R

class ChoiceGroupsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_choice_groups_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            childFragmentManager.commit {
                replace(R.id.containerChoiceGroupsFlow, ChoiceGroupsListFragment.newInstance())
            }
        }
    }

    companion object {
        fun newInstance(): ChoiceGroupsFragment = ChoiceGroupsFragment()
    }
}
