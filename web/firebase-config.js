// Shared Firebase configuration matching the PawCare Android app backend.
// Register a Web App in Firebase Console for project "gen-lang-client-0678804709"
// and paste your Web App ID into `appId` below before deploying.

export const firebaseConfig = {
  apiKey: "AIzaSyBOMmxCj0KmzKW0msP5wv8qsAxt0JUnZUg",
  authDomain: "gen-lang-client-0678804709.firebaseapp.com",
  projectId: "gen-lang-client-0678804709",
  storageBucket: "gen-lang-client-0678804709.firebasestorage.app",
  messagingSenderId: "245238076976",
  appId: "1:245238076976:web:48e0e8aefe55e3483afff5"
};

export const FIREBASE_CONFIG = firebaseConfig;

// Shared named Cloud Firestore database ID (identical to Android R.string.firestore_database_id)
export const FIRESTORE_DATABASE_ID =
  "ai-studio-android-pawcare-2a8411f6-a6f1-489d-a6a9-6d6eaf19b5d9";

export function isWebConfigReady() {
  return (
    typeof firebaseConfig.apiKey === "string" &&
    firebaseConfig.apiKey.trim() !== "" &&
    !firebaseConfig.apiKey.includes("<copy from the box>") &&
    typeof firebaseConfig.appId === "string" &&
    firebaseConfig.appId.trim() !== "" &&
    !firebaseConfig.appId.includes("<copy from the box>") &&
    !firebaseConfig.appId.includes("YOUR_FIREBASE_WEB_APP_ID")
  );
}
