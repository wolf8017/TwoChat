package com.wolf8017.twochat.view.activities.contact

import android.Manifest

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.wolf8017.twochat.R
import com.wolf8017.twochat.adapter.ContactsAdapter
import com.wolf8017.twochat.databinding.ActivityContactsBinding
import com.wolf8017.twochat.model.user.User


class ContactsActivity : AppCompatActivity() {

    private val TAG: String = "ContactsActivity"

    private lateinit var binding: ActivityContactsBinding
    private var list: MutableList<User> = mutableListOf()
    private lateinit var contactsAdapter: ContactsAdapter
    private var firebaseUser: FirebaseUser? = null
    private lateinit var firestore: FirebaseFirestore

    //get list contacts
    val REQUEST_READ_CONTACTS: Int = 79
    var contactlist: ListView? = null
    var mobileArray: ArrayList<*>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contacts)

        binding.recycleViewContacts.layoutManager = LinearLayoutManager(this)

        binding.btnBack.setOnClickListener {
            finish()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        firestore = FirebaseFirestore.getInstance()

        if (firebaseUser != null) {
            getContactFromPhone()
        }

        if (mobileArray != null) {
            getContactList()
        }
    }

    private fun getContactFromPhone() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            mobileArray = getAllPhoneContacts()
        } else {
            requestPermission()
        }

    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            // show UI part if you want here to show some rationale !!!
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS
            )
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS
            )
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mobileArray = getAllPhoneContacts()
                } else {
                    finish()
                }
                return
            }
        }
    }

    @SuppressLint("Range")
    private fun getAllPhoneContacts(): ArrayList<String> {
        val phoneList = ArrayList<String>()
        val cr = contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )
                    while (pCur?.moveToNext() == true) {
                        var phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                        // Transform the phone number
                        phoneNo = trimPhoneNumber(phoneNo)
                        phoneNo = replaceLeadingZero(phoneNo)

                        phoneList.add(phoneNo)
                    }
                    pCur?.close()
                }
            }
        }
        cur?.close()
        return phoneList
    }


    @SuppressLint("SuspiciousIndentation")
    private fun getContactList() {
        firestore.collection("User")
            .get()
            .addOnSuccessListener {
                for (snapshots in it) {
                    val userID: String = snapshots["userID"].toString()
                    val userName: String = snapshots["userName"].toString()
                    val imageUrl: String = snapshots["imageProfile"].toString()
                    val desc: String = snapshots["bio"].toString()
                    val phone: String = snapshots["userPhone"].toString()

                    val defaultImageResource = R.drawable.profile_avatar // Replace with your default image resource ID

                    val finalImageUrl = if (imageUrl.isEmpty()) {
                        defaultImageResource.toString()
                    } else {
                        imageUrl
                    }
                    val user = User(
                        userID,
                        userName,
                        phone,
                        finalImageUrl,
                        "",
                        "",
                        "",
                        "",
                        "",
                        desc ?: "",
                        ""
                    )

                    if (userID != firebaseUser?.uid
                        && userID != firebaseUser!!.uid
                        && mobileArray!!.contains(user.userPhone)) {
                        list.add(user)
                    }
                }

                contactsAdapter = ContactsAdapter(list, this@ContactsActivity)
                binding.recycleViewContacts.adapter = contactsAdapter
            }
    }

    private fun trimPhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters
        return phoneNumber.replace("\\D+".toRegex(), "")
    }

    private fun replaceLeadingZero(phoneNumber: String): String {
        // Check if the phone number starts with a zero
        return if (phoneNumber.startsWith("0")) {
            // If it does, replace the leading zero with +84
            phoneNumber.replaceFirst("0", "+84")
        } else {
            // If it doesn't, return the original phone number
            phoneNumber
        }
    }
}
