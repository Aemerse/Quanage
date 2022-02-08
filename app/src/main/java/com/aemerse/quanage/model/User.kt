package com.aemerse.quanage.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

//No serialization of Timestamps and location params yet

@Keep
class User : Parcelable {
    var name: String? = null
    var emailId: String? = null
    var phoneNumber: String? = null
    var dob: String? = null
    var profilePics: HashMap<String,String>? = null
    var gender: String? = null
    var username: String? = null
    var bio: String? = null
    var userId: String? = null
    var work: String? = null
    var education: String? = null
    var token: String? = null
    var credits: Int? = null

    @ServerTimestamp var timestamp: Date? = null
    @ServerTimestamp var joinedSince: Date? = null
    var g: Double? = null
    var l: GeoPoint? = null

    constructor()
    constructor(name: String?, emailId: String?, phoneNumber: String?,
                dob: String?, gender: String?, username: String?, bio: String?,
                education: String?, work: String?, timestamp: Date?, joinedSince: Date?,
                userId: String?, profilePics: HashMap<String,String>?, token: String?, credits: Int?) {
        this.name = name
        this.emailId = emailId
        this.dob = dob
        this.phoneNumber = phoneNumber
        this.gender = gender
        this.profilePics = profilePics
        this.username = username
        this.bio = bio
        this.timestamp = timestamp
        this.joinedSince = joinedSince
        this.userId = userId
        this.work = work
        this.education = education
        this.token = token
        this.credits = credits
    }

    private constructor(`in`: Parcel) {
        name = `in`.readString()
        emailId = `in`.readString()
        phoneNumber = `in`.readString()
        dob = `in`.readString()
        val size = `in`.readInt()
        profilePics = HashMap(size)
        for (i in 0 until size) {
            val key = `in`.readString()
            val value = `in`.readString()
            profilePics!![key!!] = value!!
        }
        gender = `in`.readString()
        username = `in`.readString()
        bio = `in`.readString()
        userId = `in`.readString()
        work = `in`.readString()
        education = `in`.readString()
        token = `in`.readString()
        credits = `in`.readInt()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
        dest.writeString(emailId)
        dest.writeString(phoneNumber)
        dest.writeString(dob)
        dest.writeInt(profilePics!!.size)
        for ((key, value) in profilePics!!.entries) {
            dest.writeString(key)
            dest.writeString(value)
        }
        dest.writeString(gender)
        dest.writeString(username)
        dest.writeString(bio)
        dest.writeString(userId)
        dest.writeString(work)
        dest.writeString(education)
        dest.writeString(token)
        dest.writeInt(credits!!)
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}