package com.example.data

import android.content.Context
import com.example.R
import com.example.data.backup.BackupDailyContext
import com.example.data.backup.BackupFoodEntry
import com.example.data.backup.BackupFoodPreset
import com.example.data.backup.BackupSettings
import com.example.data.backup.BackupWalkEntry
import com.example.data.backup.BackupWeightCheckIn
import com.example.data.backup.PulseLogBackup
import com.example.data.local.SampleDataSeeder
import com.example.data.model.DailyContextDoc
import com.example.data.model.DateUtils
import com.example.data.model.FoodCategory
import com.example.data.model.FoodEntryDoc
import com.example.data.model.FoodPresetDoc
import com.example.data.model.MetabolicState
import com.example.data.model.UserProfileDoc
import com.example.data.model.UserSettings
import com.example.data.model.WalkEntryDoc
import com.example.data.model.WalkPace
import com.example.data.model.WeightCheckInDoc
import com.example.data.model.WeightTag
import com.example.data.model.toUserSettings
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TrackerRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val backupAdapter = moshi.adapter(PulseLogBackup::class.java).indent("  ")

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    private fun userDoc(uid: String) = db.collection("users").document(uid)

    fun observeUserSettings(userId: String): Flow<UserSettings> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId"
        emitAll(
            userDoc(userId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObject(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.toUserSettings()
                        ?: UserSettings()
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeContexts(userId: String): Flow<List<DailyContextDoc>> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId/dailyContexts"
        emitAll(
            userDoc(userId).collection("dailyContexts")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(DailyContextDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> handleStreamError(error, userId, path) }
        )
    }

    fun observePresets(userId: String): Flow<List<FoodPresetDoc>> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId/foodPresets"
        emitAll(
            userDoc(userId).collection("foodPresets")
                .orderBy("sortOrder", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FoodPresetDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> handleStreamError(error, userId, path) }
        )
    }

    fun observeFoodEntries(userId: String): Flow<List<FoodEntryDoc>> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId/foodEntries"
        emitAll(
            userDoc(userId).collection("foodEntries")
                .orderBy("timestampMillis", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FoodEntryDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> handleStreamError(error, userId, path) }
        )
    }

    fun observeWalkEntries(userId: String): Flow<List<WalkEntryDoc>> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId/walkEntries"
        emitAll(
            userDoc(userId).collection("walkEntries")
                .orderBy("timestampMillis", Query.Direction.DESCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(WalkEntryDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> handleStreamError(error, userId, path) }
        )
    }

    fun observeWeightCheckIns(userId: String): Flow<List<WeightCheckInDoc>> = flow {
        require(userId.isNotBlank()) { "userId must not be blank" }
        val path = "users/$userId/weightCheckIns"
        emitAll(
            userDoc(userId).collection("weightCheckIns")
                .orderBy("dateEpochDay", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(WeightCheckInDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error -> handleStreamError(error, userId, path) }
        )
    }

    private fun handleStreamError(error: Throwable, userId: String, path: String): Nothing {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val exceptionToLog = firestoreError ?: (error as? Exception)
        if (exceptionToLog != null && auth.currentUser?.uid == userId) {
            handleFirestoreError(exceptionToLog, OperationType.LIST, path)
        }
        throw error
    }

    suspend fun ensureInitialized(userId: String): Result<Unit> {
        var path = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user mismatch" }
            val profileRef = userDoc(uid)
            val snapshot = profileRef.get().await()
            if (!snapshot.exists()) {
                val batch = db.batch()
                batch.set(
                    profileRef,
                    mapOf(
                        "userId" to uid,
                        "bodyWeightKg" to 87.6,
                        "estimatedBmrKcal" to 1850L,
                        "baseNeatKcal" to 300L,
                        "dailyWalkGoalMinutes" to 45L,
                        "dailyWalkGoalKcal" to 300L,
                        "initialized" to true,
                        "sampleDataActive" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )

                SampleDataSeeder.defaultPresets(uid).forEach { preset ->
                    val ref = profileRef.collection("foodPresets").document()
                    batch.set(ref, presetCreateMap(uid, preset.name, preset.caloriesInt, preset.categoryEnum, preset.sortOrder.toInt(), preset.defaultPreset))
                }

                val sample = SampleDataSeeder.buildSampleDataset(userId = uid)
                sample.contexts.forEach { ctx ->
                    val ref = profileRef.collection("dailyContexts").document("day_${ctx.dateEpochDay}")
                    batch.set(ref, contextCreateMap(uid, ctx.dateEpochDay, ctx.stateEnum))
                }
                sample.foodEntries.forEach { food ->
                    val ref = profileRef.collection("foodEntries").document()
                    batch.set(ref, foodCreateMap(uid, food.dateEpochDay, food.timestampMillis, food.name, food.caloriesInt, food.categoryEnum))
                }
                sample.walkEntries.forEach { walk ->
                    val ref = profileRef.collection("walkEntries").document()
                    batch.set(ref, walkCreateMap(uid, walk.dateEpochDay, walk.timestampMillis, walk.durationInt, walk.paceEnum, walk.distanceKm, walk.caloriesInt, walk.manualCalories))
                }
                sample.weightCheckIns.forEach { weight ->
                    val ref = profileRef.collection("weightCheckIns").document("day_${weight.dateEpochDay}")
                    batch.set(ref, weightCreateMap(uid, weight.dateEpochDay, weight.timestampMillis, weight.weightKg, weight.tagEnum))
                }

                batch.commit().await()
            } else {
                val presetsSnap = profileRef.collection("foodPresets").limit(1).get().await()
                if (presetsSnap.isEmpty) {
                    val batch = db.batch()
                    SampleDataSeeder.defaultPresets(uid).forEach { preset ->
                        val ref = profileRef.collection("foodPresets").document()
                        batch.set(ref, presetCreateMap(uid, preset.name, preset.caloriesInt, preset.categoryEnum, preset.sortOrder.toInt(), preset.defaultPreset))
                    }
                    batch.commit().await()
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun setMetabolicState(epochDay: Long, state: MetabolicState): Result<Unit> {
        var path = "users/unknown/dailyContexts/day_$epochDay"
        return try {
            val uid = requireUserId()
            val ref = userDoc(uid).collection("dailyContexts").document("day_$epochDay")
            path = ref.path
            val exists = ref.get().await().exists()
            if (exists) {
                ref.update(
                    mapOf(
                        "dateEpochDay" to epochDay,
                        "metabolicState" to state.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                ref.set(contextCreateMap(uid, epochDay, state)).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun logFood(
        name: String,
        calories: Int,
        category: FoodCategory,
        saveAsPreset: Boolean = false,
        nextPresetSortOrder: Int = 0,
        epochDay: Long = DateUtils.currentEpochDay(),
        timestampMillis: Long = System.currentTimeMillis()
    ): Result<Unit> {
        var path = "users/unknown/foodEntries"
        return try {
            val uid = requireUserId()
            val cleanName = name.trim().take(120)
            require(cleanName.isNotEmpty()) { "Food name cannot be blank" }
            val clampedCal = calories.coerceIn(0, 10000)

            val foodRef = userDoc(uid).collection("foodEntries").document()
            path = foodRef.path
            val batch = db.batch()
            batch.set(foodRef, foodCreateMap(uid, epochDay, timestampMillis, cleanName, clampedCal, category))

            if (saveAsPreset) {
                val presetRef = userDoc(uid).collection("foodPresets").document()
                batch.set(presetRef, presetCreateMap(uid, cleanName, clampedCal, category, nextPresetSortOrder.coerceIn(0, 10000), false))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateFoodEntry(entry: FoodEntryDoc): Result<Unit> {
        var path = "users/unknown/foodEntries/${entry.id}"
        return try {
            val uid = requireUserId()
            require(entry.id.isNotBlank()) { "Food entry ID must not be blank" }
            val cleanName = entry.name.trim().take(120)
            require(cleanName.isNotEmpty()) { "Food name cannot be blank" }
            val ref = userDoc(uid).collection("foodEntries").document(entry.id)
            path = ref.path
            ref.update(
                mapOf(
                    "dateEpochDay" to entry.dateEpochDay,
                    "timestampMillis" to entry.timestampMillis,
                    "name" to cleanName,
                    "calories" to entry.calories.coerceIn(0L, 10000L),
                    "category" to entry.categoryEnum.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteFoodEntry(id: String): Result<Unit> {
        var path = "users/unknown/foodEntries/$id"
        return try {
            val uid = requireUserId()
            require(id.isNotBlank()) { "Food entry ID must not be blank" }
            val ref = userDoc(uid).collection("foodEntries").document(id)
            path = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun savePreset(preset: FoodPresetDoc): Result<Unit> {
        var path = "users/unknown/foodPresets/${preset.id}"
        return try {
            val uid = requireUserId()
            val cleanName = preset.name.trim().take(120)
            require(cleanName.isNotEmpty()) { "Preset name cannot be blank" }
            val clampedCal = preset.calories.coerceIn(0L, 10000L).toInt()
            if (preset.id.isBlank()) {
                val ref = userDoc(uid).collection("foodPresets").document()
                path = ref.path
                ref.set(presetCreateMap(uid, cleanName, clampedCal, preset.categoryEnum, preset.sortOrder.toInt().coerceIn(0, 10000), preset.defaultPreset)).await()
            } else {
                val ref = userDoc(uid).collection("foodPresets").document(preset.id)
                path = ref.path
                ref.update(
                    mapOf(
                        "name" to cleanName,
                        "calories" to clampedCal.toLong(),
                        "category" to preset.categoryEnum.name,
                        "sortOrder" to preset.sortOrder.coerceIn(0L, 10000L),
                        "defaultPreset" to preset.defaultPreset,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun deletePreset(id: String): Result<Unit> {
        var path = "users/unknown/foodPresets/$id"
        return try {
            val uid = requireUserId()
            require(id.isNotBlank()) { "Preset ID must not be blank" }
            val ref = userDoc(uid).collection("foodPresets").document(id)
            path = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun logWalk(
        durationMinutes: Int,
        pace: WalkPace,
        distanceKm: Double?,
        caloriesBurned: Int,
        manualCalories: Boolean,
        epochDay: Long = DateUtils.currentEpochDay(),
        timestampMillis: Long = System.currentTimeMillis()
    ): Result<Unit> {
        var path = "users/unknown/walkEntries"
        return try {
            val uid = requireUserId()
            val ref = userDoc(uid).collection("walkEntries").document()
            path = ref.path
            val clampedDur = durationMinutes.coerceIn(1, 1440)
            val clampedCal = caloriesBurned.coerceIn(0, 15000)
            val clampedDist = distanceKm?.coerceIn(0.0, 500.0)
            ref.set(walkCreateMap(uid, epochDay, timestampMillis, clampedDur, pace, clampedDist, clampedCal, manualCalories)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun updateWalkEntry(entry: WalkEntryDoc): Result<Unit> {
        var path = "users/unknown/walkEntries/${entry.id}"
        return try {
            val uid = requireUserId()
            require(entry.id.isNotBlank()) { "Walk entry ID must not be blank" }
            val ref = userDoc(uid).collection("walkEntries").document(entry.id)
            path = ref.path
            val updateMap = mutableMapOf<String, Any?>(
                "dateEpochDay" to entry.dateEpochDay,
                "timestampMillis" to entry.timestampMillis,
                "durationMinutes" to entry.durationMinutes.coerceIn(1L, 1440L),
                "pace" to entry.paceEnum.name,
                "caloriesBurned" to entry.caloriesBurned.coerceIn(0L, 15000L),
                "manualCalories" to entry.manualCalories,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (entry.distanceKm != null) {
                updateMap["distanceKm"] = entry.distanceKm.coerceIn(0.0, 500.0)
            }
            @Suppress("UNCHECKED_CAST")
            ref.update(updateMap.filterValues { it != null } as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun deleteWalkEntry(id: String): Result<Unit> {
        var path = "users/unknown/walkEntries/$id"
        return try {
            val uid = requireUserId()
            require(id.isNotBlank()) { "Walk entry ID must not be blank" }
            val ref = userDoc(uid).collection("walkEntries").document(id)
            path = ref.path
            ref.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun saveWeightCheckIn(
        weightKg: Double,
        tag: WeightTag,
        epochDay: Long = DateUtils.currentEpochDay()
    ): Result<Unit> {
        var path = "users/unknown/weightCheckIns/day_$epochDay"
        return try {
            val uid = requireUserId()
            val clamped = ((weightKg * 10.0).toInt() / 10.0).coerceIn(30.0, 250.0)
            val now = System.currentTimeMillis()
            val checkInRef = userDoc(uid).collection("weightCheckIns").document("day_$epochDay")
            path = checkInRef.path
            val exists = checkInRef.get().await().exists()
            if (exists) {
                checkInRef.update(
                    mapOf(
                        "dateEpochDay" to epochDay,
                        "timestampMillis" to now,
                        "weightKg" to clamped,
                        "tag" to tag.name,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                checkInRef.set(weightCreateMap(uid, epochDay, now, clamped, tag)).await()
            }

            if (epochDay == DateUtils.currentEpochDay()) {
                userDoc(uid).update(
                    mapOf(
                        "bodyWeightKg" to clamped,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateSettings(
        bodyWeightKg: Double,
        estimatedBmrKcal: Int,
        baseNeatKcal: Int,
        dailyWalkGoalMinutes: Int,
        dailyWalkGoalKcal: Int
    ): Result<Unit> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            val ref = userDoc(uid)
            path = ref.path
            ref.update(
                mapOf(
                    "bodyWeightKg" to bodyWeightKg.coerceIn(30.0, 250.0),
                    "estimatedBmrKcal" to estimatedBmrKcal.coerceIn(1000, 4000).toLong(),
                    "baseNeatKcal" to baseNeatKcal.coerceIn(0, 1500).toLong(),
                    "dailyWalkGoalMinutes" to dailyWalkGoalMinutes.coerceIn(10, 240).toLong(),
                    "dailyWalkGoalKcal" to dailyWalkGoalKcal.coerceIn(50, 2000).toLong(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun clearSampleDataAndStartFresh(): Result<Unit> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            val profileRef = userDoc(uid)
            path = profileRef.path
            val batch = db.batch()
            listOf("dailyContexts", "foodEntries", "walkEntries", "weightCheckIns").forEach { col ->
                val docs = profileRef.collection(col).get().await()
                docs.documents.forEach { batch.delete(it.reference) }
            }
            batch.update(
                profileRef,
                mapOf(
                    "sampleDataActive" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            Result.failure(e)
        }
    }

    suspend fun restoreSampleData(): Result<Unit> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            val profileRef = userDoc(uid)
            path = profileRef.path
            val batch = db.batch()
            listOf("dailyContexts", "foodEntries", "walkEntries", "weightCheckIns").forEach { col ->
                val docs = profileRef.collection(col).get().await()
                docs.documents.forEach { batch.delete(it.reference) }
            }
            val sample = SampleDataSeeder.buildSampleDataset(userId = uid)
            sample.contexts.forEach { ctx ->
                val ref = profileRef.collection("dailyContexts").document("day_${ctx.dateEpochDay}")
                batch.set(ref, contextCreateMap(uid, ctx.dateEpochDay, ctx.stateEnum))
            }
            sample.foodEntries.forEach { food ->
                val ref = profileRef.collection("foodEntries").document()
                batch.set(ref, foodCreateMap(uid, food.dateEpochDay, food.timestampMillis, food.name, food.caloriesInt, food.categoryEnum))
            }
            sample.walkEntries.forEach { walk ->
                val ref = profileRef.collection("walkEntries").document()
                batch.set(ref, walkCreateMap(uid, walk.dateEpochDay, walk.timestampMillis, walk.durationInt, walk.paceEnum, walk.distanceKm, walk.caloriesInt, walk.manualCalories))
            }
            sample.weightCheckIns.forEach { weight ->
                val ref = profileRef.collection("weightCheckIns").document("day_${weight.dateEpochDay}")
                batch.set(ref, weightCreateMap(uid, weight.dateEpochDay, weight.timestampMillis, weight.weightKg, weight.tagEnum))
            }
            batch.update(
                profileRef,
                mapOf(
                    "sampleDataActive" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun exportBackupJson(): Result<String> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            val profileRef = userDoc(uid)
            path = profileRef.path
            val profile = profileRef.get().await()
                .toObject(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                ?.toUserSettings()
                ?: UserSettings()

            val contexts = profileRef.collection("dailyContexts").get().await()
                .toObjects(DailyContextDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val presets = profileRef.collection("foodPresets").get().await()
                .toObjects(FoodPresetDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val foods = profileRef.collection("foodEntries").get().await()
                .toObjects(FoodEntryDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val walks = profileRef.collection("walkEntries").get().await()
                .toObjects(WalkEntryDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            val weights = profileRef.collection("weightCheckIns").get().await()
                .toObjects(WeightCheckInDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)

            val backup = PulseLogBackup(
                version = 1,
                exportedAtMillis = System.currentTimeMillis(),
                settings = BackupSettings(
                    bodyWeightKg = profile.bodyWeightKg,
                    estimatedBmrKcal = profile.estimatedBmrKcal,
                    baseNeatKcal = profile.baseNeatKcal,
                    dailyWalkGoalMinutes = profile.dailyWalkGoalMinutes,
                    dailyWalkGoalKcal = profile.dailyWalkGoalKcal
                ),
                dailyContexts = contexts.map { BackupDailyContext(it.dateEpochDay, it.metabolicState) },
                foodPresets = presets.map { BackupFoodPreset(it.name, it.caloriesInt, it.category, it.sortOrder.toInt(), it.defaultPreset) },
                foodEntries = foods.map { BackupFoodEntry(it.dateEpochDay, it.timestampMillis, it.name, it.caloriesInt, it.category) },
                walkEntries = walks.map { BackupWalkEntry(it.dateEpochDay, it.timestampMillis, it.durationInt, it.pace, it.distanceKm, it.caloriesInt, it.manualCalories) },
                weightCheckIns = weights.map { BackupWeightCheckIn(it.dateEpochDay, it.timestampMillis, it.weightKg, it.tag) }
            )
            Result.success(backupAdapter.toJson(backup))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            Result.failure(e)
        }
    }

    suspend fun importBackupJson(json: String): Result<Int> {
        var path = "users/unknown"
        return try {
            val uid = requireUserId()
            val profileRef = userDoc(uid)
            path = profileRef.path
            val parsed = backupAdapter.fromJson(json)
                ?: throw IllegalArgumentException("Invalid PulseLog backup JSON format.")

            val batch = db.batch()
            listOf("dailyContexts", "foodPresets", "foodEntries", "walkEntries", "weightCheckIns").forEach { col ->
                val docs = profileRef.collection(col).get().await()
                docs.documents.forEach { batch.delete(it.reference) }
            }

            batch.update(
                profileRef,
                mapOf(
                    "bodyWeightKg" to parsed.settings.bodyWeightKg.coerceIn(30.0, 250.0),
                    "estimatedBmrKcal" to parsed.settings.estimatedBmrKcal.coerceIn(1000, 4000).toLong(),
                    "baseNeatKcal" to parsed.settings.baseNeatKcal.coerceIn(0, 1500).toLong(),
                    "dailyWalkGoalMinutes" to parsed.settings.dailyWalkGoalMinutes.coerceIn(10, 240).toLong(),
                    "dailyWalkGoalKcal" to parsed.settings.dailyWalkGoalKcal.coerceIn(50, 2000).toLong(),
                    "initialized" to true,
                    "sampleDataActive" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            parsed.dailyContexts.forEach { ctx ->
                val ref = profileRef.collection("dailyContexts").document("day_${ctx.dateEpochDay}")
                batch.set(ref, contextCreateMap(uid, ctx.dateEpochDay, MetabolicState.fromRaw(ctx.metabolicState)))
            }

            val presetsToWrite = parsed.foodPresets.ifEmpty {
                SampleDataSeeder.defaultPresets(uid).map {
                    BackupFoodPreset(it.name, it.caloriesInt, it.category, it.sortOrder.toInt(), it.defaultPreset)
                }
            }
            presetsToWrite.forEachIndexed { idx, p ->
                val ref = profileRef.collection("foodPresets").document()
                batch.set(
                    ref,
                    presetCreateMap(
                        uid = uid,
                        name = p.name.take(120).ifBlank { "Meal Preset" },
                        calories = p.calories.coerceIn(0, 10000),
                        category = FoodCategory.fromRaw(p.category),
                        sortOrder = (if (p.sortOrder >= 0) p.sortOrder else idx).coerceIn(0, 10000),
                        defaultPreset = p.isDefault
                    )
                )
            }

            parsed.foodEntries.forEach { f ->
                val ref = profileRef.collection("foodEntries").document()
                batch.set(
                    ref,
                    foodCreateMap(
                        uid = uid,
                        epochDay = f.dateEpochDay,
                        timestampMillis = f.timestampMillis,
                        name = f.name.take(120).ifBlank { "Meal" },
                        calories = f.calories.coerceIn(0, 10000),
                        category = FoodCategory.fromRaw(f.category)
                    )
                )
            }

            parsed.walkEntries.forEach { w ->
                val ref = profileRef.collection("walkEntries").document()
                batch.set(
                    ref,
                    walkCreateMap(
                        uid = uid,
                        epochDay = w.dateEpochDay,
                        timestampMillis = w.timestampMillis,
                        durationMinutes = w.durationMinutes.coerceIn(1, 1440),
                        pace = WalkPace.fromRaw(w.pace),
                        distanceKm = w.distanceKm?.coerceIn(0.0, 500.0),
                        caloriesBurned = w.caloriesBurned.coerceIn(0, 15000),
                        manualCalories = w.isManualCalories
                    )
                )
            }

            parsed.weightCheckIns.forEach { wt ->
                val ref = profileRef.collection("weightCheckIns").document("day_${wt.dateEpochDay}")
                batch.set(
                    ref,
                    weightCreateMap(
                        uid = uid,
                        epochDay = wt.dateEpochDay,
                        timestampMillis = wt.timestampMillis,
                        weightKg = wt.weightKg.coerceIn(30.0, 250.0),
                        tag = WeightTag.fromRaw(wt.tag)
                    )
                )
            }

            batch.commit().await()
            Result.success(parsed.foodEntries.size + parsed.walkEntries.size + parsed.weightCheckIns.size)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    private fun contextCreateMap(uid: String, epochDay: Long, state: MetabolicState): Map<String, Any> = mapOf(
        "userId" to uid,
        "dateEpochDay" to epochDay,
        "metabolicState" to state.name,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    private fun presetCreateMap(
        uid: String,
        name: String,
        calories: Int,
        category: FoodCategory,
        sortOrder: Int,
        defaultPreset: Boolean
    ): Map<String, Any> = mapOf(
        "userId" to uid,
        "name" to name,
        "calories" to calories.toLong(),
        "category" to category.name,
        "sortOrder" to sortOrder.toLong(),
        "defaultPreset" to defaultPreset,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    private fun foodCreateMap(
        uid: String,
        epochDay: Long,
        timestampMillis: Long,
        name: String,
        calories: Int,
        category: FoodCategory
    ): Map<String, Any> = mapOf(
        "userId" to uid,
        "dateEpochDay" to epochDay,
        "timestampMillis" to timestampMillis,
        "name" to name,
        "calories" to calories.toLong(),
        "category" to category.name,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    @Suppress("UNCHECKED_CAST")
    private fun walkCreateMap(
        uid: String,
        epochDay: Long,
        timestampMillis: Long,
        durationMinutes: Int,
        pace: WalkPace,
        distanceKm: Double?,
        caloriesBurned: Int,
        manualCalories: Boolean
    ): Map<String, Any> = mapOf(
        "userId" to uid,
        "dateEpochDay" to epochDay,
        "timestampMillis" to timestampMillis,
        "durationMinutes" to durationMinutes.toLong(),
        "pace" to pace.name,
        "distanceKm" to distanceKm,
        "caloriesBurned" to caloriesBurned.toLong(),
        "manualCalories" to manualCalories,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    ).filterValues { it != null } as Map<String, Any>

    private fun weightCreateMap(
        uid: String,
        epochDay: Long,
        timestampMillis: Long,
        weightKg: Double,
        tag: WeightTag
    ): Map<String, Any> = mapOf(
        "userId" to uid,
        "dateEpochDay" to epochDay,
        "timestampMillis" to timestampMillis,
        "weightKg" to weightKg,
        "tag" to tag.name,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )
}
