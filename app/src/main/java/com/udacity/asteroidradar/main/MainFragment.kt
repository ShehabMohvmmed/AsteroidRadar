package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModel.Factory(requireActivity().application)).get(MainViewModel::class.java)
    }
    private val asteroidAdapter = AsteroidAdapter(AsteroidAdapter.AsteroidListener {
        viewModel.onAsteroidSelected(it)
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        binding.asteroidRecycler.adapter = asteroidAdapter

        viewModel.navigateToDetailAsteroid.observe(viewLifecycleOwner, Observer { asteroid ->
            if (asteroid != null) {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
                viewModel.onAsteroidNavigated()
            }
        })
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.asteroidList.observe(viewLifecycleOwner, Observer<List<Asteroid>> { asteroid ->
            asteroid.apply {
                asteroidAdapter.submitList(this)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.onFilterSelected(
            when (item.itemId) {
                R.id.show_buy_menu -> { MainViewModel.AsteroidFilter.ALL }
                R.id.show_all_menu -> { MainViewModel.AsteroidFilter.WEEK }
                else -> { MainViewModel.AsteroidFilter.TODAY }
            }
        )
        return true
    }
}
