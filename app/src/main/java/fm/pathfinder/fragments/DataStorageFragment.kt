package fm.pathfinder.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import fm.pathfinder.R
import fm.pathfinder.data.FileManager

class DataStorageFragment : Fragment() {
    private lateinit var dataFiles: List<String>

    private lateinit var listView: ListView
    private lateinit var fileManager : FileManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contentView = inflater.inflate(R.layout.fragment_data_storage, container, false)
        listView = contentView.findViewById(R.id.list_files)

        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fileManager = FileManager(activity as Context)

            dataFiles = fileManager.loadAllDataFiles()
            if (this::dataFiles.isInitialized) {
                val itemsAdapter =
                    ArrayAdapter(activity as Context, R.layout.simplest_list_el, dataFiles)
                listView.setOnItemClickListener { parent, view, position, id ->
                    val clicked = parent.getItemAtPosition(position) as String
                    openNavigationForFile(clicked)
                }
                listView.adapter = itemsAdapter
            } else {
                Toast.makeText(this.context, "Data is not available", Toast.LENGTH_LONG).show()
            }


    }

    private fun openNavigationForFile(filename: String) {
        val rooms = fileManager.loadDataFromFile(filename)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DataStorageFragment().apply {

            }
    }


}