const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validProfilePayload(uid, timestamp = new Date(Date.now() - 2000)) {
  return {
    userId: uid,
    fullName: "Alice Johnson",
    email: "alice@example.com",
    phoneNumber: "+1 555-0142",
    address: "742 Evergreen Terrace",
    city: "Portland",
    state: "Oregon",
    petDetails: {
      petName: "Barnaby",
      petType: "Dog",
      petBreed: "Golden Retriever",
      petAge: "3 years",
      petGender: "Male",
    },
    createdAt: timestamp,
    updatedAt: timestamp,
  };
}

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

test("Unauthenticated visitor: cannot read, list, create, or update users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID))
  );
});

test("Authenticated owner: valid registration create & partial updates succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Create profile (matches UserRepository.registerUserProfile)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  // Read own profile
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  // Partial update: owner details only
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      fullName: "Alice M. Johnson",
      email: "alice.m@example.com",
      phoneNumber: "+1 555-9999",
      address: "100 Oak Lane",
      city: "Seattle",
      state: "Washington",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Partial update: pet details only
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      petDetails: {
        petName: "Barnaby Jr",
        petType: "Cat",
        petBreed: "Maine Coon",
        petAge: "2 years",
        petGender: "Female",
      },
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Authenticated user: cannot list users collection or access/modify another owner's profile", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .set(validProfilePayload(BOB_UID, new Date(Date.now() - 10000)));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set(validProfilePayload(BOB_UID))
  );
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).update({
      fullName: "Hijacked Name",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).delete());
});

test("Security bounds: reject spoofed userId, ghost fields, @DocumentId 'id', invalid enums, immutable createdAt, future timestamps", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 10000);

  // Spoofed userId inside body
  const spoofed = validProfilePayload(ALICE_UID, created);
  spoofed.userId = BOB_UID;
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(spoofed));

  // Ghost field / @DocumentId 'id' rejection on create
  const withId = validProfilePayload(ALICE_UID, created);
  withId.id = ALICE_UID;
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(withId));

  const withRole = validProfilePayload(ALICE_UID, created);
  withRole.isAdmin = true;
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(withRole));

  // Invalid petType enum
  const badPetType = validProfilePayload(ALICE_UID, created);
  badPetType.petDetails.petType = "Dragon";
  await assertFails(aliceDb.collection("users").doc(ALICE_UID).set(badPetType));

  // Tampered future timestamp
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .set(validProfilePayload(ALICE_UID, new Date(Date.now() + 60000)))
  );

  // Valid create followed by immutable field tampering on update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(validProfilePayload(ALICE_UID, created))
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});
