package com.arifahmadalfian.todolist.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.arifahmadalfian.todolist.R
import com.arifahmadalfian.todolist.data.room.ToDoData
import com.arifahmadalfian.todolist.databinding.FragmentUpdateBinding
import com.arifahmadalfian.todolist.utils.SharedViewModel
import com.arifahmadalfian.todolist.viewmodel.ToDoViewModel

class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()

    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()

    private var _binding: FragmentUpdateBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateBinding.inflate(inflater, container, false)
        binding?.args = args

        setHasOptionsMenu(true)

        binding?.spCurrentDescription?.onItemSelectedListener = mSharedViewModel.listener
        return binding?.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> updateItem()
            R.id.action_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmItemRemoval() {
        AlertDialog.Builder(requireContext())
                .setPositiveButton("Yes"){ _, _ ->
                    mToDoViewModel.deleteItem(args.currentItem)
                    Toast.makeText(
                            requireContext(),
                            "Succesfully Removed: ${args.currentItem.title}",
                            Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_updateFragment_to_listFragment)
                }
                .setNegativeButton("No"){_, _ ->}
                .setTitle("Delete '${args.currentItem.title}'?")
                .setMessage("Are you sure want to remove '${args.currentItem.title}'?")
                .create().show()

    }

    private fun updateItem() {
        val title = binding?.etCurrentTitle?.text.toString()
        val description = binding?.etCurrentDescription?.text.toString()
        val priority = binding?.spCurrentDescription?.selectedItem.toString()

        val validation = mSharedViewModel.verifyDataFromUser(title, description)
        if (validation) {
            val updateItem = ToDoData(
                    args.currentItem.id,
                    title,
                    mSharedViewModel.parsePriority(priority),
                    description
            )
            mToDoViewModel.updateData(updateItem)
            Toast.makeText(requireContext(), "Succesfully Update", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

