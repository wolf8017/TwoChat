package com.wolf8017.twochat.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wolf8017.twochat.R
import com.wolf8017.twochat.adapter.CallListAdapter
import com.wolf8017.twochat.model.CallList
import com.wolf8017.twochat.model.ChatList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CallsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CallsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }


    private var lists: MutableList<CallList> = ArrayList()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_calls, container, false)

        recyclerView = view.findViewById(R.id.recycleView_call)
        recyclerView.layoutManager = LinearLayoutManager(context)

        getCallList()

        return view
    }

    private fun getCallList() {
        lists.add(
            CallList(
                "044",
                "wolf8017",
                "14/09/2023 \u00B7 20:00",
                "https://tuyengiao.vn/Uploads/2023/2/14/25/cats.jpg",
                "income"
            )
        )
        lists.add(
            CallList(
                "044",
                "wolf8017",
                "14/09/2023 \u00B7 19:58",
                "https://tuyengiao.vn/Uploads/2023/2/14/25/cats.jpg",
                "missed"
            )
        )
        lists.add(
            CallList(
                "044",
                "wolf8017",
                "14/09/2023 \u00B7 19:57",
                "https://tuyengiao.vn/Uploads/2023/2/14/25/cats.jpg",
                "missed"
            )
        )
        lists.add(
            CallList(
                "044",
                "wolf8017",
                "14/09/2023 \u00B7 19:50",
                "https://tuyengiao.vn/Uploads/2023/2/14/25/cats.jpg",
                "out"
            )
        )
        recyclerView.adapter = CallListAdapter(lists, requireContext())
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CallsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CallsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
