import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-app.js";
import {
  getAuth,
  GoogleAuthProvider,
  onAuthStateChanged,
  signInWithPopup,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  sendPasswordResetEmail,
  signOut
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-auth.js";
import {
  initializeFirestore,
  doc,
  onSnapshot,
  setDoc,
  updateDoc,
  serverTimestamp
} from "https://www.gstatic.com/firebasejs/11.6.0/firebase-firestore.js";
import {
  FIREBASE_CONFIG,
  FIRESTORE_DATABASE_ID,
  isWebConfigReady
} from "./firebase-config.js";

const CARE_GUIDES = {
  Dog: {
    title: "Canine Daily Wellness & Exercise",
    summary: "Dogs thrive on consistent movement, balanced protein, and routine checkups.",
    bullets: [
      "Provide 30–60 minutes of daily brisk walks and mental enrichment.",
      "Keep fresh water accessible and measure portioned meals twice daily.",
      "Schedule annual booster vaccinations and year-round flea/tick prevention."
    ]
  },
  Cat: {
    title: "Feline Comfort, Hydration & Play",
    summary: "Indoor cats stay healthy with hydration support, vertical perches, and interactive play.",
    bullets: [
      "Encourage hydration with wide water bowls or circulating fountains.",
      "Rotate interactive feather/wand play for 15 minutes before evening meals.",
      "Scoop litter boxes daily and book yearly dental & wellness exams."
    ]
  },
  Bird: {
    title: "Avian Nutrition & Safe Habitats",
    summary: "Companion birds need varied foraging diets, clean air, and daily social interaction.",
    bullets: [
      "Offer formulated pellets alongside dark leafy greens and safe fruits.",
      "Place cages away from kitchen fumes, drafts, and direct harsh heat.",
      "Provide natural wood perches of varying widths for healthy feet."
    ]
  },
  Other: {
    title: "Small Mammals & Exotic Companions",
    summary: "Rabbits, guinea pigs, and reptiles depend on tailored habitats and exotic vet care.",
    bullets: [
      "Maintain species-specific temperature, bedding, and fresh hay or greens.",
      "Monitor appetite and activity daily — small pets hide discomfort early.",
      "Keep an exotic-experienced veterinarian's contact on your PawCare card."
    ]
  }
};

// DOM refs
const configBanner = document.getElementById("config-banner");
const views = {
  homepage: document.getElementById("view-homepage"),
  login: document.getElementById("view-login"),
  register: document.getElementById("view-register"),
  dashboard: document.getElementById("view-dashboard")
};

let app = null;
let auth = null;
let db = null;
let currentUser = null;
let currentProfile = null;
let profileUnsubscribe = null;
let pendingProfileSave = null;
let activeView = "homepage"; // ALWAYS start on Public Homepage

if (!isWebConfigReady()) {
  configBanner.classList.remove("hidden");
} else {
  app = initializeApp(FIREBASE_CONFIG);
  auth = getAuth(app);
  // Connect to the exact same named Firestore database used by Android
  db = initializeFirestore(app, {}, FIRESTORE_DATABASE_ID);
}

// View Routing (Never force unauthenticated users to login)
function showView(viewName) {
  if (viewName === "dashboard" && !currentUser) {
    viewName = "login";
  }
  activeView = viewName;
  Object.entries(views).forEach(([key, el]) => {
    el.classList.toggle("hidden", key !== viewName);
  });
  document.getElementById("mobile-drawer").classList.add("hidden");
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function updateNavbarForAuth(user) {
  const signedIn = Boolean(user);
  document.getElementById("nav-actions-guest").classList.toggle("hidden", signedIn);
  document.getElementById("nav-actions-auth").classList.toggle("hidden", !signedIn);
  document.getElementById("mobile-actions-guest").classList.toggle("hidden", signedIn);
  document.getElementById("mobile-actions-auth").classList.toggle("hidden", !signedIn);
  document.getElementById("hero-guest-cta").classList.toggle("hidden", signedIn);
  document.getElementById("hero-auth-cta").classList.toggle("hidden", !signedIn);
}

// Validation helpers matching firestore.rules 1:1
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function validateOwnerFields(owner) {
  const errs = {};
  if (owner.fullName.length < 2 || owner.fullName.length > 80) {
    errs.fullName = "Enter your full name (2–80 characters).";
  }
  if (!EMAIL_REGEX.test(owner.email) || owner.email.length > 120) {
    errs.email = "Enter a valid email address.";
  }
  const digits = owner.phoneNumber.replace(/\D/g, "");
  if (owner.phoneNumber.length < 7 || digits.length < 7 || owner.phoneNumber.length > 25) {
    errs.phoneNumber = "Enter a valid phone number (at least 7 digits).";
  }
  if (owner.address.length < 3 || owner.address.length > 160) {
    errs.address = "Enter your residential address (3–160 characters).";
  }
  if (owner.city.length < 2 || owner.city.length > 80) {
    errs.city = "Enter your city (2–80 characters).";
  }
  if (owner.state.length < 2 || owner.state.length > 80) {
    errs.state = "Enter your state (2–80 characters).";
  }
  return errs;
}

function validatePetFields(pet) {
  const errs = {};
  if (pet.petName.length < 1 || pet.petName.length > 60) {
    errs.petName = "Pet name is required (1–60 characters).";
  }
  if (pet.petBreed.length < 1 || pet.petBreed.length > 80) {
    errs.petBreed = "Pet breed is required (1–80 characters).";
  }
  if (pet.petAge.length < 1 || pet.petAge.length > 30) {
    errs.petAge = "Pet age is required (e.g., 3 years).";
  }
  return errs;
}

function showAlert(el, msg, isError = true) {
  el.textContent = msg;
  el.classList.remove("hidden", "alert-error", "alert-success");
  el.classList.add(isError ? "alert-error" : "alert-success");
}

function hideAlert(el) {
  el.classList.add("hidden");
}

// Pet Care Tabs
function renderCareGuide(species) {
  const guide = CARE_GUIDES[species] || CARE_GUIDES.Dog;
  document.getElementById("care-title").textContent = guide.title;
  document.getElementById("care-summary").textContent = guide.summary;
  const list = document.getElementById("care-bullets");
  list.innerHTML = "";
  guide.bullets.forEach((b) => {
    const li = document.createElement("li");
    li.textContent = b;
    list.appendChild(li);
  });
}

document.querySelectorAll(".tab-btn").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll(".tab-btn").forEach((b) => b.classList.remove("active"));
    btn.classList.add("active");
    renderCareGuide(btn.dataset.species);
  });
});
renderCareGuide("Dog");

// Mobile Menu
document.getElementById("mobile-menu-toggle").addEventListener("click", () => {
  document.getElementById("mobile-drawer").classList.toggle("hidden");
});

// Section Links
document.querySelectorAll("[data-section]").forEach((link) => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    const sectionId = link.dataset.section;
    showView("homepage");
    requestAnimationFrame(() => {
      document.getElementById(sectionId)?.scrollIntoView({ behavior: "smooth" });
    });
  });
});

document.querySelectorAll("[data-back-home]").forEach((btn) => {
  btn.addEventListener("click", () => showView("homepage"));
});

// Navigation Buttons
["nav-login-btn", "mobile-login-btn", "hero-login-btn", "register-to-login-btn"].forEach((id) => {
  document.getElementById(id).addEventListener("click", () => showView("login"));
});

["nav-register-btn", "mobile-register-btn", "hero-register-btn", "login-to-register-btn"].forEach((id) => {
  document.getElementById(id).addEventListener("click", () => {
    prepareRegisterFormForUser(currentUser);
    showView("register");
  });
});

["nav-dashboard-btn", "mobile-dashboard-btn", "hero-dashboard-btn"].forEach((id) => {
  document.getElementById(id).addEventListener("click", () => showView("dashboard"));
});

["nav-logout-btn", "mobile-logout-btn"].forEach((id) => {
  document.getElementById(id).addEventListener("click", async () => {
    if (!auth) return;
    if (profileUnsubscribe) {
      profileUnsubscribe();
      profileUnsubscribe = null;
    }
    pendingProfileSave = null;
    await signOut(auth);
    showView("homepage");
  });
});

// Contact Us Inquiry Form
document.getElementById("contact-form").addEventListener("submit", (e) => {
  e.preventDefault();
  const name = document.getElementById("contact-name").value.trim();
  const email = document.getElementById("contact-email").value.trim();
  const message = document.getElementById("contact-message").value.trim();
  document.getElementById("err-contact-name").textContent = name.length < 2 ? "Please enter your name." : "";
  document.getElementById("err-contact-email").textContent = !EMAIL_REGEX.test(email) ? "Enter a valid email address." : "";
  document.getElementById("err-contact-message").textContent = message.length < 5 ? "Enter a brief question or message." : "";

  if (name.length >= 2 && EMAIL_REGEX.test(email) && message.length >= 5) {
    document.getElementById("contact-success").classList.remove("hidden");
    e.target.reset();
  }
});

// Populate Dashboard DOM
function renderDashboard(profile) {
  currentProfile = profile;
  const pet = profile.petDetails || {};
  document.getElementById("dash-welcome-title").textContent = `Welcome back, ${profile.fullName}!`;
  document.getElementById("dash-welcome-subtitle").textContent =
    `Caring for ${pet.petName || "your pet"} (${pet.petType || "Pet"})`;

  document.getElementById("dash-full-name").textContent = profile.fullName || "—";
  document.getElementById("dash-email").textContent = profile.email || "—";
  document.getElementById("dash-phone").textContent = profile.phoneNumber || "—";
  document.getElementById("dash-address").textContent = profile.address || "—";
  document.getElementById("dash-city-state").textContent = `${profile.city || "—"}, ${profile.state || "—"}`;

  document.getElementById("dash-pet-subtitle").textContent = `${pet.petType || "Pet"} Companion Card`;
  document.getElementById("dash-pet-name").textContent = pet.petName || "—";
  document.getElementById("dash-pet-type").textContent = pet.petType || "—";
  document.getElementById("dash-pet-breed").textContent = pet.petBreed || "—";
  document.getElementById("dash-pet-age").textContent = pet.petAge || "—";
  document.getElementById("dash-pet-gender").textContent = pet.petGender || "—";

  const createdDate = profile.createdAt?.toDate ? profile.createdAt.toDate() : new Date();
  document.getElementById("dash-created-at").textContent = createdDate.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric"
  });
}

function prepareRegisterFormForUser(user) {
  const signedIn = Boolean(user);
  document.getElementById("reg-password-row").classList.toggle("hidden", signedIn);
  document.getElementById("register-email-btn").classList.toggle("hidden", signedIn);
  document.getElementById("register-google-btn").textContent = signedIn
    ? "Save Profile & Open Dashboard"
    : "Register & Connect with Google";
  document.getElementById("register-badge").textContent = signedIn
    ? "Step 2: Complete Profile"
    : "New Member Registration";
  document.getElementById("register-heading").textContent = signedIn
    ? "Finish Your Owner & Pet Profile"
    : "Register as a PawCare Pet Owner";
  if (signedIn) {
    if (!document.getElementById("reg-full-name").value && user.displayName) {
      document.getElementById("reg-full-name").value = user.displayName;
    }
    if (!document.getElementById("reg-email").value && user.email) {
      document.getElementById("reg-email").value = user.email;
    }
  }
}

async function saveNewUserProfile(uid, owner, pet) {
  const docRef = doc(db, "users", uid);
  await setDoc(docRef, {
    userId: uid,
    fullName: owner.fullName,
    email: owner.email,
    phoneNumber: owner.phoneNumber,
    address: owner.address,
    city: owner.city,
    state: owner.state,
    petDetails: {
      petName: pet.petName,
      petType: pet.petType,
      petBreed: pet.petBreed,
      petAge: pet.petAge,
      petGender: pet.petGender
    },
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  });
}

function readRegisterInputs() {
  const owner = {
    fullName: document.getElementById("reg-full-name").value.trim(),
    email: document.getElementById("reg-email").value.trim(),
    phoneNumber: document.getElementById("reg-phone").value.trim(),
    address: document.getElementById("reg-address").value.trim(),
    city: document.getElementById("reg-city").value.trim(),
    state: document.getElementById("reg-state").value.trim()
  };
  const pet = {
    petName: document.getElementById("reg-pet-name").value.trim(),
    petType: document.getElementById("reg-pet-type").value,
    petBreed: document.getElementById("reg-pet-breed").value.trim(),
    petAge: document.getElementById("reg-pet-age").value.trim(),
    petGender: document.getElementById("reg-pet-gender").value
  };
  return { owner, pet };
}

function applyRegisterFieldErrors(errs) {
  document.getElementById("err-reg-full-name").textContent = errs.fullName || "";
  document.getElementById("err-reg-email").textContent = errs.email || "";
  document.getElementById("err-reg-phone").textContent = errs.phoneNumber || "";
  document.getElementById("err-reg-address").textContent = errs.address || "";
  document.getElementById("err-reg-city").textContent = errs.city || "";
  document.getElementById("err-reg-state").textContent = errs.state || "";
  document.getElementById("err-reg-pet-name").textContent = errs.petName || "";
  document.getElementById("err-reg-pet-breed").textContent = errs.petBreed || "";
  document.getElementById("err-reg-pet-age").textContent = errs.petAge || "";
}

function ensureConfiguredOrWarn(alertEl) {
  if (!isWebConfigReady() || !auth || !db) {
    showAlert(
      alertEl,
      "Please paste your Firebase Web appId into web/firebase-config.js (from Firebase Console project gen-lang-client-0678804709) first."
    );
    return false;
  }
  return true;
}

// Auth state listener (Real-time sync with Android & Firestore)
if (auth && db) {
  onAuthStateChanged(auth, async (user) => {
    currentUser = user;
    updateNavbarForAuth(user);

    if (profileUnsubscribe) {
      profileUnsubscribe();
      profileUnsubscribe = null;
    }

    if (!user) {
      currentProfile = null;
      if (activeView === "dashboard") showView("homepage");
      return;
    }

    if (pendingProfileSave) {
      const { owner, pet } = pendingProfileSave;
      pendingProfileSave = null;
      try {
        await saveNewUserProfile(user.uid, owner, pet);
      } catch (err) {
        console.error("Failed saving registration profile:", err);
      }
    }

    const userDocRef = doc(db, "users", user.uid);
    profileUnsubscribe = onSnapshot(
      userDocRef,
      (snapshot) => {
        if (snapshot.exists()) {
          renderDashboard(snapshot.data());
          if (activeView === "login" || activeView === "register") {
            showView("dashboard");
          }
        } else if (activeView === "login" || activeView === "dashboard") {
          prepareRegisterFormForUser(user);
          showView("register");
        }
      },
      (err) => {
        console.error("Firestore snapshot error:", err);
        showAlert(
          document.getElementById("dashboard-feedback"),
          err.message || "Unable to sync profile from Firestore."
        );
      }
    );
  });
}

// Login with Google
document.getElementById("login-google-btn").addEventListener("click", async () => {
  const alertEl = document.getElementById("login-alert");
  hideAlert(alertEl);
  if (!ensureConfiguredOrWarn(alertEl)) return;
  try {
    await signInWithPopup(auth, new GoogleAuthProvider());
    showView("dashboard");
  } catch (err) {
    showAlert(alertEl, err.message || "Google Sign-In failed.");
  }
});

// Login with Email & Password
document.getElementById("login-email-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const alertEl = document.getElementById("login-alert");
  hideAlert(alertEl);
  if (!ensureConfiguredOrWarn(alertEl)) return;

  const email = document.getElementById("login-email").value.trim();
  const password = document.getElementById("login-password").value;
  document.getElementById("err-login-email").textContent = !EMAIL_REGEX.test(email) ? "Enter a valid email." : "";
  document.getElementById("err-login-password").textContent = password.length < 6 ? "Enter your password (min 6 chars)." : "";
  if (!EMAIL_REGEX.test(email) || password.length < 6) return;

  try {
    await signInWithEmailAndPassword(auth, email, password);
    showView("dashboard");
  } catch (err) {
    showAlert(alertEl, err.message || "Login failed.");
  }
});

// Forgot Password
document.getElementById("forgot-password-btn").addEventListener("click", async () => {
  const alertEl = document.getElementById("login-alert");
  const infoEl = document.getElementById("login-info");
  hideAlert(alertEl);
  hideAlert(infoEl);
  if (!ensureConfiguredOrWarn(alertEl)) return;

  const email = document.getElementById("login-email").value.trim();
  if (!EMAIL_REGEX.test(email)) {
    showAlert(
      infoEl,
      "Enter your email above and click Forgot password to send a reset email — or use Google Sign-In directly.",
      false
    );
    return;
  }
  try {
    await sendPasswordResetEmail(auth, email);
    showAlert(infoEl, `Password reset instructions sent to ${email}.`, false);
  } catch (err) {
    showAlert(alertEl, err.message || "Could not send password reset email.");
  }
});

// Register with Google (works both for new users and signed-in profile completion)
document.getElementById("register-google-btn").addEventListener("click", async () => {
  const alertEl = document.getElementById("register-alert");
  hideAlert(alertEl);
  if (!ensureConfiguredOrWarn(alertEl)) return;

  const { owner, pet } = readRegisterInputs();
  const errs = { ...validateOwnerFields(owner), ...validatePetFields(pet) };
  applyRegisterFieldErrors(errs);
  if (Object.keys(errs).length > 0) {
    showAlert(alertEl, "Please fix the highlighted registration fields.");
    return;
  }

  try {
    if (currentUser) {
      await saveNewUserProfile(currentUser.uid, owner, pet);
      showView("dashboard");
    } else {
      pendingProfileSave = { owner, pet };
      await signInWithPopup(auth, new GoogleAuthProvider());
    }
  } catch (err) {
    pendingProfileSave = null;
    showAlert(alertEl, err.message || "Registration failed.");
  }
});

// Register with Email & Password
document.getElementById("register-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  const alertEl = document.getElementById("register-alert");
  hideAlert(alertEl);
  if (!ensureConfiguredOrWarn(alertEl)) return;

  const { owner, pet } = readRegisterInputs();
  const password = document.getElementById("reg-password").value;
  const confirm = document.getElementById("reg-confirm-password").value;

  const errs = { ...validateOwnerFields(owner), ...validatePetFields(pet) };
  if (password.length < 6) errs.password = "Password must be at least 6 characters.";
  if (confirm !== password) errs.confirmPassword = "Passwords do not match.";

  applyRegisterFieldErrors(errs);
  document.getElementById("err-reg-password").textContent = errs.password || "";
  document.getElementById("err-reg-confirm-password").textContent = errs.confirmPassword || "";

  if (Object.keys(errs).length > 0) {
    showAlert(alertEl, "Please fix the highlighted registration fields.");
    return;
  }

  try {
    pendingProfileSave = { owner, pet };
    await createUserWithEmailAndPassword(auth, owner.email, password);
  } catch (err) {
    pendingProfileSave = null;
    showAlert(alertEl, err.message || "Email registration failed.");
  }
});

// Edit Owner Modal
const editOwnerModal = document.getElementById("edit-owner-modal");
document.getElementById("open-edit-owner-btn").addEventListener("click", () => {
  if (!currentProfile) return;
  document.getElementById("edit-full-name").value = currentProfile.fullName || "";
  document.getElementById("edit-email").value = currentProfile.email || "";
  document.getElementById("edit-phone").value = currentProfile.phoneNumber || "";
  document.getElementById("edit-address").value = currentProfile.address || "";
  document.getElementById("edit-city").value = currentProfile.city || "";
  document.getElementById("edit-state").value = currentProfile.state || "";
  editOwnerModal.classList.remove("hidden");
});

document.getElementById("cancel-edit-owner-btn").addEventListener("click", () => {
  editOwnerModal.classList.add("hidden");
});

document.getElementById("edit-owner-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!currentUser || !db) return;

  const owner = {
    fullName: document.getElementById("edit-full-name").value.trim(),
    email: document.getElementById("edit-email").value.trim(),
    phoneNumber: document.getElementById("edit-phone").value.trim(),
    address: document.getElementById("edit-address").value.trim(),
    city: document.getElementById("edit-city").value.trim(),
    state: document.getElementById("edit-state").value.trim()
  };

  const errs = validateOwnerFields(owner);
  document.getElementById("err-edit-full-name").textContent = errs.fullName || "";
  document.getElementById("err-edit-email").textContent = errs.email || "";
  document.getElementById("err-edit-phone").textContent = errs.phoneNumber || "";
  document.getElementById("err-edit-address").textContent = errs.address || "";
  document.getElementById("err-edit-city").textContent = errs.city || "";
  document.getElementById("err-edit-state").textContent = errs.state || "";

  if (Object.keys(errs).length > 0) return;

  try {
    await updateDoc(doc(db, "users", currentUser.uid), {
      ...owner,
      updatedAt: serverTimestamp()
    });
    editOwnerModal.classList.add("hidden");
    showAlert(document.getElementById("dashboard-feedback"), "Owner profile updated.", false);
  } catch (err) {
    showAlert(document.getElementById("dashboard-feedback"), err.message || "Update failed.");
  }
});

// Edit Pet Modal
const editPetModal = document.getElementById("edit-pet-modal");
document.getElementById("open-edit-pet-btn").addEventListener("click", () => {
  if (!currentProfile) return;
  const pet = currentProfile.petDetails || {};
  document.getElementById("edit-pet-name").value = pet.petName || "";
  document.getElementById("edit-pet-type").value = pet.petType || "Dog";
  document.getElementById("edit-pet-breed").value = pet.petBreed || "";
  document.getElementById("edit-pet-age").value = pet.petAge || "";
  document.getElementById("edit-pet-gender").value = pet.petGender || "Male";
  editPetModal.classList.remove("hidden");
});

document.getElementById("cancel-edit-pet-btn").addEventListener("click", () => {
  editPetModal.classList.add("hidden");
});

document.getElementById("edit-pet-form").addEventListener("submit", async (e) => {
  e.preventDefault();
  if (!currentUser || !db) return;

  const pet = {
    petName: document.getElementById("edit-pet-name").value.trim(),
    petType: document.getElementById("edit-pet-type").value,
    petBreed: document.getElementById("edit-pet-breed").value.trim(),
    petAge: document.getElementById("edit-pet-age").value.trim(),
    petGender: document.getElementById("edit-pet-gender").value
  };

  const errs = validatePetFields(pet);
  document.getElementById("err-edit-pet-name").textContent = errs.petName || "";
  document.getElementById("err-edit-pet-breed").textContent = errs.petBreed || "";
  document.getElementById("err-edit-pet-age").textContent = errs.petAge || "";

  if (Object.keys(errs).length > 0) return;

  try {
    await updateDoc(doc(db, "users", currentUser.uid), {
      petDetails: pet,
      updatedAt: serverTimestamp()
    });
    editPetModal.classList.add("hidden");
    showAlert(document.getElementById("dashboard-feedback"), "Pet details updated.", false);
  } catch (err) {
    showAlert(document.getElementById("dashboard-feedback"), err.message || "Update failed.");
  }
});
