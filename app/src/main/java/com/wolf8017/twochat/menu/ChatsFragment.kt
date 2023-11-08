package com.wolf8017.twochat.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wolf8017.twochat.R
import com.wolf8017.twochat.adapter.ChatListAdapter
import com.wolf8017.twochat.model.ChatList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsFragment : Fragment() {
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

    private var lists: MutableList<ChatList> = ArrayList()
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_chats, container, false)

        recyclerView = view.findViewById(R.id.recycleView_chat)
        recyclerView.layoutManager = LinearLayoutManager(context)

        getChatList();
        return view
    }

    private fun getChatList() {
        lists.add(ChatList("4","wolf8017","Hello World","14/09/2023","https://tuyengiao.vn/Uploads/2023/2/14/25/cats.jpg"))
        lists.add(ChatList("4","wolf8017","Hello World","14/09/2023","dangcongsan.png"))
        lists.add(ChatList("4","wolf8017","Hello World","14/09/2023","dangcongsan.png"))
        lists.add(ChatList("4","wolf8017","Hello World","14/09/2023","dangcongsan.png"))

        recyclerView.adapter = ChatListAdapter(lists,requireContext())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
