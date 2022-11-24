package fm.pathfinder.fragments

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import fm.pathfinder.R

class FragmentChanger {
    companion object {
        /**
         * @param fragmentManager - fragment manager that changes fragment
         * @param id - number of fragment to put in fragment_container
         * 0 - Main Menu Fragment
         * 1 - Maps Fragment
         * 2 - Data Loader Fragment
         * 3 - Navigation Fragment, #buildingName must be included!
         */
        fun changeFragment(fragmentManager: FragmentManager, id: Int, buildingName: String = "") {
            fragmentManager.beginTransaction().apply {
                when (id) {
                    0 -> {
                        replace(
                            R.id.fragment_container,
                            MainMenuFragment.newInstance()
                        )
                    }
                    1 -> {
                        replace(R.id.fragment_container, MapsFragment.newInstance())
                    }
                    2 -> {
                        replace(R.id.fragment_container, DataStorageFragment.newInstance())
                    }
                    3->{
                        if (buildingName != "")
                        replace(R.id.fragment_container, NavigationFragment.newInstance(buildingName))
                        else throw java.lang.Exception("Building name is empty")
                    }
                }
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                commit()
            }

        }
    }
}