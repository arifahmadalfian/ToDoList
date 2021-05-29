package com.arifahmadalfian.todolist.fragments.list

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.arifahmadalfian.todolist.R
import com.arifahmadalfian.todolist.data.room.ToDoData
import com.arifahmadalfian.todolist.databinding.FragmentListBinding
import com.arifahmadalfian.todolist.utils.SharedViewModel
import com.arifahmadalfian.todolist.utils.SwipeToDelete
import com.arifahmadalfian.todolist.utils.hideKeyboard
import com.arifahmadalfian.todolist.viewmodel.ToDoViewModel
import com.google.android.material.snackbar.Snackbar
import com.arifahmadalfian.todolist.fragments.list.adapter.ListAdapter
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val msharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding

    private val adapter: ListAdapter by lazy { ListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding?.lifecycleOwner = this
        binding?.mSharedViewModel = msharedViewModel

        setUpRecyclerView()

        // Observer LiveData
        mToDoViewModel.getAllData.observe(viewLifecycleOwner) { data ->
            msharedViewModel.checkDatabaseEmpty(data)
            adapter.setData(data)
        }
        msharedViewModel.emptyDatabase.observe(viewLifecycleOwner) {
            showEmptyDatabaseViews(it)
        }

        setHasOptionsMenu(true)

        hideKeyboard(requireActivity())

        return binding?.root
    }

    private fun setUpRecyclerView() {
        val recyclerView = binding?.recyclerView
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView?.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 200
        }

        if (recyclerView != null) {
            swipeToDelete(recyclerView)
        }
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object: SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deleteItem = adapter.dataList[viewHolder.adapterPosition]
                //delete item
                mToDoViewModel.deleteItem(deleteItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                // restore delete item
                restoreDeleteData(viewHolder.itemView, deleteItem)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeleteData(view: View, deleteItem: ToDoData) {
        val snackBar = Snackbar.make(
            view, "Deleted '${deleteItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deleteItem)
        }
        snackBar.show()
    }

    private fun showEmptyDatabaseViews(emptyDatabase: Boolean) {
        if (emptyDatabase) {
            binding?.viewEmpty?.root?.visibility = View.VISIBLE
        } else {
            binding?.viewEmpty?.root?.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.action_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_all -> confirmRemoval()
            R.id.action_priority_high -> mToDoViewModel.sortByHighPriority.observe(this,
                    { adapter.setData(it) })
            R.id.action_priority_low -> mToDoViewModel.sortByLowPriority.observe(this,
                    { adapter.setData(it) })
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") {_, _ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(
                    requireContext(),
                    "Succesfully Removed Everything",
                    Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No"){_, _ -> }
        builder.setTitle("Delete Everything?")
        builder.setMessage("Are you sure want to remove Everything")
        builder.create().show()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list?.let {
                adapter.setData(it)
            }
        })
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null) {
            searchThroughDatabase(newText)
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}