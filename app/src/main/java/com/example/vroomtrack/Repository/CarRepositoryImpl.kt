package com.example.vroomtrack.Repository

import com.example.vroomtrack.model.CarModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CarRepositoryImpl : CarRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("cars")

    override fun getAllCars(): Flow<List<CarModel>> = callbackFlow {
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val carsList = mutableListOf<CarModel>()
                for (childSnapshot in snapshot.children) {
                    val car = childSnapshot.getValue(CarModel::class.java)
                    if (car != null) carsList.add(car)
                }
                trySend(carsList).isSuccess
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                close(error.toException())
            }
        }
        dbRef.addValueEventListener(listener)
        awaitClose {
            dbRef.removeEventListener(listener)
        }
    }

    override suspend fun getCarById(id: String): CarModel? {
        val snapshot = dbRef.child(id).get().await()
        return snapshot.getValue(CarModel::class.java)
    }

    override suspend fun addCar(car: CarModel): Boolean {
        return try {
            val key = dbRef.push().key ?: return false
            val carWithId = car.copy(id = key)
            dbRef.child(key).setValue(carWithId).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateCar(car: CarModel): Boolean {
        return try {
            dbRef.child(car.id).setValue(car).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteCar(id: String): Boolean {
        return try {
            dbRef.child(id).removeValue().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
