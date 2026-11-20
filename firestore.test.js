const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write any user collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);

  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("foodEntries").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("foodEntries").doc("entry_1").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      name: "Koshary",
      calories: 650,
      category: "LUNCH",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("Authenticated owner: valid UserProfile create and partial update succeed; ghost field & immutable createdAt fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      bodyWeightKg: 87.6,
      estimatedBmrKcal: 1850,
      baseNeatKcal: 300,
      dailyWalkGoalMinutes: 45,
      dailyWalkGoalKcal: 300,
      initialized: true,
      sampleDataActive: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      bodyWeightKg: 86.9,
      dailyWalkGoalMinutes: 50,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject immutable createdAt overwrite
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Authenticated owner: foodEntries, walkEntries (with and without distanceKm), presets, contexts, and weightCheckIns succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  // Food entry (no @DocumentId `id` field in payload)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("foodEntries").doc("food_1").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      name: "Koshary (Medium bowl)",
      calories: 650,
      category: "LUNCH",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update on foodEntry
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("foodEntries").doc("food_1").update({
      calories: 680,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject unexpected 'id' field on create
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("foodEntries").doc("food_2").set({
      id: "food_2",
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      name: "Tea",
      calories: 95,
      category: "DRINK_SNACK",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Walk entry without optional distanceKm
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("walkEntries").doc("walk_1").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      durationMinutes: 30,
      pace: "BRISK_WALK",
      caloriesBurned: 166,
      manualCalories: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Walk entry with optional distanceKm
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("walkEntries").doc("walk_2").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      durationMinutes: 45,
      pace: "FAST_PACE",
      distanceKm: 4.8,
      caloriesBurned: 285,
      manualCalories: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Weight check-in & Daily context
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("weightCheckIns").doc("day_20412").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      weightKg: 87.6,
      tag: "BLOATED_WATER_RETENTION",
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("dailyContexts").doc("day_20412").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      metabolicState: "MEDICATION_DRAG",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Subcollection list queries succeed on empty & populated collections
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("foodEntries").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("foodPresets").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("walkEntries").get());
});

test("Cross-user isolation: Alice cannot read, list, update, or delete Bob's records", async () => {
  const created = new Date(Date.now() - 10000);
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      bodyWeightKg: 80.0,
      estimatedBmrKcal: 1800,
      baseNeatKcal: 300,
      dailyWalkGoalMinutes: 45,
      dailyWalkGoalKcal: 300,
      initialized: true,
      sampleDataActive: false,
      createdAt: created,
      updatedAt: created,
    });
    await db.collection("users").doc(BOB_UID).collection("foodEntries").doc("bob_food").set({
      userId: BOB_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      name: "Bob Meal",
      calories: 500,
      category: "LUNCH",
      createdAt: created,
      updatedAt: created,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("foodEntries").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("foodEntries").doc("bob_food").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("foodEntries").doc("bob_food").update({
      calories: 100,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("foodEntries").doc("bob_food").delete());

  // Spoofing userId inside own subcollection also rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("foodEntries").doc("spoof").set({
      userId: BOB_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      name: "Spoof",
      calories: 100,
      category: "LUNCH",
      createdAt: created,
      updatedAt: created,
    })
  );
});

test("Reject future client timestamps and invalid numeric bounds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const future = new Date(Date.now() + 60000);

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("foodEntries").doc("future_1").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      name: "Future Meal",
      calories: 400,
      category: "LUNCH",
      createdAt: future,
      updatedAt: future,
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("weightCheckIns").doc("day_bad").set({
      userId: ALICE_UID,
      dateEpochDay: 20412,
      timestampMillis: Date.now(),
      weightKg: 999.0, // > 250.0
      tag: "NORMAL",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
