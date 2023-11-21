package com.springkotlindemo.theater.domain

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Performance(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) val id: Long,
    val title: String
) {
    // it's a bad practice to put this fields that have mappings inside the default constructor for a data class
    // because when the code from the data class is generated like hashCode it doesn't make sense for these mapping
    // fields
    @OneToMany(mappedBy = "performance")
    lateinit var bookings: List<Booking>
}
