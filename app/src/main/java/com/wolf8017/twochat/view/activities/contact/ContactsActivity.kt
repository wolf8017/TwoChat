package com.wolf8017.twochat.view.activities.contact

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.wolf8017.twochat.R
import com.wolf8017.twochat.adapter.ContactsAdapter
import com.wolf8017.twochat.databinding.ActivityContactsBinding
import com.wolf8017.twochat.model.user.User

class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private var list: MutableList<User> = mutableListOf()
    private lateinit var contactsAdapter: ContactsAdapter
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts)

        binding.recycleViewContacts.layoutManager = LinearLayoutManager(this)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        firestore = FirebaseFirestore.getInstance()

        if (firebaseUser != null) {
            getContactList()
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getContactList() {
        firestore.collection("User")
            .get()
            .addOnSuccessListener {
                for (snapshots: QueryDocumentSnapshot in it) {
                    val userID: String = snapshots["userID"].toString()
                    val userName: String = snapshots["userName"].toString()
                    val imageUrl: String = snapshots["imageProfile"].toString()
                    val desc: String = snapshots["bio"].toString()

                    val defaultImageResource = R.drawable.profile_avatar // Replace with your default image resource ID

                    val finalImageUrl = if (imageUrl.isNullOrEmpty()) {
                        defaultImageResource.toString()
                    } else {
                        imageUrl
                    }

                    if (userID != firebaseUser?.uid)
                    list.add(
                        User(
                            userID,
                            userName,
                            "",
                            finalImageUrl,
                            "",
                            "",
                            "",
                            "",
                            "",
                            desc ?: ""
                        )
                    )
                }
                contactsAdapter = ContactsAdapter(list, this@ContactsActivity)
                binding.recycleViewContacts.adapter = contactsAdapter
            }
    }
}
